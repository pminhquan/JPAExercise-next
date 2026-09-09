IF DB_ID(N'jakartaJPA') IS NULL
BEGIN
    CREATE DATABASE jakartaJPA;
END
GO

USE jakartaJPA;
GO

IF OBJECT_ID(N'users', N'U') IS NOT NULL
BEGIN
    -- 1. Detect and drop existing CHECK constraints involving users.Role, before any attempt to add Role
    -- If Role is absent, guard catalog inspection so the check-discovery step safely finds nothing
    DECLARE @roleColId INT = COLUMNPROPERTY(OBJECT_ID(N'users'), N'Role', 'ColumnId');
    DECLARE @reusableChkName NVARCHAR(128);
    DECLARE @curChkName NVARCHAR(128);
    DECLARE @curChkDef NVARCHAR(MAX);
    DECLARE @curChkNorm NVARCHAR(MAX);

    IF @roleColId IS NOT NULL
    BEGIN
        DECLARE chk_cursor CURSOR LOCAL FAST_FORWARD FOR
            SELECT cc.name, cc.definition
            FROM sys.check_constraints cc
            WHERE cc.parent_object_id = OBJECT_ID(N'users')
              AND (
                  cc.parent_column_id = @roleColId
                  OR EXISTS (
                      SELECT 1 FROM sys.sql_expression_dependencies d
                      WHERE d.referencing_id = cc.object_id
                        AND d.referenced_id = OBJECT_ID(N'users')
                        AND d.referenced_minor_id = @roleColId
                  )
                  OR cc.definition LIKE N'%[[]Role]%'
              );

        OPEN chk_cursor;
        FETCH NEXT FROM chk_cursor INTO @curChkName, @curChkDef;

        WHILE @@FETCH_STATUS = 0
        BEGIN
            SET @curChkNorm = UPPER(ISNULL(@curChkDef, N''));
            SET @curChkNorm = REPLACE(REPLACE(@curChkNorm, '[', ''), ']', '');
            SET @curChkNorm = REPLACE(REPLACE(REPLACE(REPLACE(@curChkNorm, ' ', ''), CHAR(9), ''), CHAR(10), ''), CHAR(13), '');
            SET @curChkNorm = REPLACE(@curChkNorm, ';', '');

            -- Reuse an existing check only when its normalized definition is the exact IN ('ADMIN','CUSTOMER') form;
            -- never reuse Role='ADMIN' OR Role='CUSTOMER', broader/weaker checks, or name-only matches.
            IF @curChkNorm IN (
                N'(ROLEIN(''ADMIN'',''CUSTOMER''))',
                N'((ROLEIN(''ADMIN'',''CUSTOMER'')))',
                N'ROLEIN(''ADMIN'',''CUSTOMER'')',
                N'(ROLEIN(N''ADMIN'',N''CUSTOMER''))',
                N'((ROLEIN(N''ADMIN'',N''CUSTOMER'')))',
                N'ROLEIN(N''ADMIN'',N''CUSTOMER'')'
            )
            BEGIN
                IF @reusableChkName IS NULL
                BEGIN
                    SET @reusableChkName = @curChkName;
                    -- Safely neutralize exact check during cleanup/alteration
                    DECLARE @disableSql NVARCHAR(MAX) = N'ALTER TABLE users NOCHECK CONSTRAINT ' + QUOTENAME(@curChkName) + N';';
                    EXEC sp_executesql @disableSql;
                END
                ELSE
                BEGIN
                    -- Drop duplicate exact check
                    DECLARE @dropDupSql NVARCHAR(MAX) = N'ALTER TABLE users DROP CONSTRAINT ' + QUOTENAME(@curChkName) + N';';
                    EXEC sp_executesql @dropDupSql;
                END
            END
            ELSE
            BEGIN
                -- Drop existing CHECK constraints involving users.Role (e.g. CHECK (Role = 'ADMIN'), OR-form, broader, weaker)
                DECLARE @dropOldChkSql NVARCHAR(MAX) = N'ALTER TABLE users DROP CONSTRAINT ' + QUOTENAME(@curChkName) + N';';
                EXEC sp_executesql @dropOldChkSql;
            END

            FETCH NEXT FROM chk_cursor INTO @curChkName, @curChkDef;
        END

        CLOSE chk_cursor;
        DEALLOCATE chk_cursor;
    END

    -- 2. Add Role if the column is missing
    IF COL_LENGTH(N'users', N'Role') IS NULL
    BEGIN
        ALTER TABLE users ADD Role NVARCHAR(20) NULL;
    END

    -- Drop any existing default constraint on users.Role before altering column width
    DECLARE @existingDefName NVARCHAR(128);
    SELECT TOP 1
        @existingDefName = dc.name
    FROM sys.default_constraints dc
    WHERE dc.parent_object_id = OBJECT_ID(N'users')
      AND dc.parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'users'), N'Role', 'ColumnId');

    IF @existingDefName IS NOT NULL
    BEGIN
        DECLARE @dropExistingDef NVARCHAR(MAX) = N'ALTER TABLE users DROP CONSTRAINT ' + QUOTENAME(@existingDefName) + N';';
        EXEC sp_executesql @dropExistingDef;
    END

    -- Capture whether existing Role type is fixed-width (CHAR/NCHAR) before any conversion
    DECLARE @isFixedText BIT = 0;
    SELECT TOP 1
        @isFixedText = CASE WHEN t.name IN ('char', 'nchar') THEN 1 ELSE 0 END
    FROM sys.columns c
    JOIN sys.types t ON c.user_type_id = t.user_type_id
    WHERE c.object_id = OBJECT_ID(N'users')
      AND c.name = N'Role';

    -- If reusable exact check was found but type alteration is needed, temporarily remove it
    IF @reusableChkName IS NOT NULL AND (
        @isFixedText = 1 OR EXISTS (
            SELECT 1 FROM sys.columns c
            JOIN sys.types t ON c.user_type_id = t.user_type_id
            WHERE c.object_id = OBJECT_ID(N'users')
              AND c.name = N'Role'
              AND (
                  (t.name = 'varchar' AND c.max_length <> -1 AND c.max_length < 8)
                  OR (t.name = 'nvarchar' AND c.max_length <> -1 AND c.max_length < 16)
                  OR t.name IN ('char', 'nchar', 'varchar')
              )
        )
    )
    BEGIN
        DECLARE @tempDropExact NVARCHAR(MAX) = N'ALTER TABLE users DROP CONSTRAINT ' + QUOTENAME(@reusableChkName) + N';';
        EXEC sp_executesql @tempDropExact;
    END

    -- Stage fixed-width CHAR/NCHAR and narrow VARCHAR/NVARCHAR safely as variable-width text (NVARCHAR(MAX)) without truncating data
    IF @isFixedText = 1
    BEGIN
        ALTER TABLE users ALTER COLUMN Role NVARCHAR(MAX) NULL;
    END
    ELSE
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM sys.columns c
            JOIN sys.types t ON c.user_type_id = t.user_type_id
            WHERE c.object_id = OBJECT_ID(N'users')
              AND c.name = N'Role'
              AND (
                  (t.name = 'varchar' AND c.max_length <> -1 AND c.max_length < 8)
                  OR (t.name = 'nvarchar' AND c.max_length <> -1 AND c.max_length < 16)
              )
        )
        BEGIN
            ALTER TABLE users ALTER COLUMN Role NVARCHAR(MAX) NULL;
        END
    END

    -- 3. Update NULL to CUSTOMER
    EXEC sp_executesql N'UPDATE users SET Role = N''CUSTOMER'' WHERE Role IS NULL;';

    -- 4. Normalize values with TRIM/RTRIM
    IF @isFixedText = 1
    BEGIN
        -- Fixed-width (CHAR/NCHAR): remove only physical right-padding with RTRIM-aware logic
        -- Preserve canonical logical values from padded storage; map NULL/unknown to CUSTOMER
        EXEC sp_executesql N'UPDATE users
        SET Role = CASE
            WHEN RTRIM(Role) COLLATE Latin1_General_BIN2 = ''ADMIN''
                 AND LEN(Role) = 5
                 AND RTRIM(Role) + ''x'' = ''ADMINx''
                 THEN N''ADMIN''
            WHEN RTRIM(Role) COLLATE Latin1_General_BIN2 = ''CUSTOMER''
                 AND LEN(Role) = 8
                 AND RTRIM(Role) + ''x'' = ''CUSTOMERx''
                 THEN N''CUSTOMER''
            ELSE N''CUSTOMER''
        END;';
    END
    ELSE
    BEGIN
        -- Variable-width (VARCHAR/NVARCHAR): trim before canonical comparison so ADMIN<space> and CUSTOMER<space> become canonical ADMIN/CUSTOMER; map unknown to CUSTOMER
        EXEC sp_executesql N'UPDATE users
        SET Role = CASE
            WHEN LTRIM(RTRIM(Role)) COLLATE Latin1_General_BIN2 = ''ADMIN''
                 THEN N''ADMIN''
            WHEN LTRIM(RTRIM(Role)) COLLATE Latin1_General_BIN2 = ''CUSTOMER''
                 THEN N''CUSTOMER''
            ELSE N''CUSTOMER''
        END;';
    END

    -- 5. ALTER Role to NVARCHAR(20) NOT NULL
    ALTER TABLE users ALTER COLUMN Role NVARCHAR(20) NOT NULL;

    -- 6. Add DEFAULT CUSTOMER
    DECLARE @targetDefName NVARCHAR(128) = N'DF_users_Role';
    DECLARE @defIndex INT = 0;
    WHILE EXISTS (SELECT 1 FROM sys.objects WHERE name = @targetDefName)
    BEGIN
        SET @defIndex = @defIndex + 1;
        SET @targetDefName = N'DF_users_Role_' + CONVERT(NVARCHAR(10), OBJECT_ID(N'users'))
            + CASE WHEN @defIndex = 1 THEN N'' ELSE N'_' + CONVERT(NVARCHAR(10), @defIndex - 1) END;
    END

    DECLARE @addDefSql NVARCHAR(MAX) = N'ALTER TABLE users ADD CONSTRAINT ' + QUOTENAME(@targetDefName) + N' DEFAULT N''CUSTOMER'' FOR Role;';
    EXEC sp_executesql @addDefSql;

    -- 7. Add exactly CHECK (Role IN ('ADMIN','CUSTOMER'))
    IF @reusableChkName IS NOT NULL
    BEGIN
        IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = @reusableChkName AND parent_object_id = OBJECT_ID(N'users'))
        BEGIN
            -- Revalidate and re-enable exact existing strict check
            DECLARE @recheckSql NVARCHAR(MAX) = N'ALTER TABLE users WITH CHECK CHECK CONSTRAINT ' + QUOTENAME(@reusableChkName) + N';';
            EXEC sp_executesql @recheckSql;
        END
        ELSE
        BEGIN
            -- Re-add temporarily removed exact check under its original name
            DECLARE @readdExactSql NVARCHAR(MAX) = N'ALTER TABLE users WITH CHECK ADD CONSTRAINT ' + QUOTENAME(@reusableChkName)
                + N' CHECK (Role IN (''ADMIN'',''CUSTOMER''));';
            EXEC sp_executesql @readdExactSql;

            DECLARE @reenableExactSql NVARCHAR(MAX) = N'ALTER TABLE users CHECK CONSTRAINT ' + QUOTENAME(@reusableChkName) + N';';
            EXEC sp_executesql @reenableExactSql;
        END
    END
    ELSE
    BEGIN
        -- Add strict check under a collision-safe deterministic name
        DECLARE @migChkName NVARCHAR(128);
        DECLARE @chkCandidateIndex INT = 0;

        WHILE 1 = 1
        BEGIN
            IF @chkCandidateIndex = 0
                SET @migChkName = N'CK_users_Role';
            ELSE IF @chkCandidateIndex = 1
                SET @migChkName = N'CK_users_Role_Strict';
            ELSE IF @chkCandidateIndex = 2
                SET @migChkName = N'CK_users_Role_' + CONVERT(NVARCHAR(10), OBJECT_ID(N'users'));
            ELSE IF @chkCandidateIndex = 3
                SET @migChkName = N'CK_users_Role_Strict_' + CONVERT(NVARCHAR(10), OBJECT_ID(N'users'));
            ELSE
                SET @migChkName = N'CK_users_Role_Strict_' + CONVERT(NVARCHAR(10), OBJECT_ID(N'users')) + N'_' + CONVERT(NVARCHAR(10), @chkCandidateIndex - 3);

            IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE name = @migChkName)
            BEGIN
                BREAK;
            END

            SET @chkCandidateIndex = @chkCandidateIndex + 1;
        END

        DECLARE @addChkSql NVARCHAR(MAX) = N'ALTER TABLE users WITH CHECK ADD CONSTRAINT ' + QUOTENAME(@migChkName)
            + N' CHECK (Role IN (''ADMIN'',''CUSTOMER''));';
        EXEC sp_executesql @addChkSql;

        DECLARE @enableChkSql NVARCHAR(MAX) = N'ALTER TABLE users CHECK CONSTRAINT ' + QUOTENAME(@migChkName) + N';';
        EXEC sp_executesql @enableChkSql;
    END
END
GO

IF OBJECT_ID(N'categories', N'U') IS NULL
BEGIN
    CREATE TABLE categories
    (
        CategoryId INT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_categories PRIMARY KEY,
        CategoryName NVARCHAR(100) NOT NULL,
        Images NVARCHAR(500) NULL,
        Status INT NULL
    );
END
GO

IF OBJECT_ID(N'users', N'U') IS NULL
BEGIN
    CREATE TABLE users
    (
        Id INT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_users PRIMARY KEY,
        Username NVARCHAR(50) NOT NULL,
        Email NVARCHAR(100) NOT NULL,
        PasswordHash NVARCHAR(255) NOT NULL,
        Active BIT NOT NULL,
        CreatedAt DATETIME2(6) NOT NULL,
        Fullname NVARCHAR(100) NULL,
        Phone NVARCHAR(30) NULL,
        Images NVARCHAR(500) NULL,
        Role NVARCHAR(20) NOT NULL
            DEFAULT N'CUSTOMER'
            CHECK (Role IN ('ADMIN','CUSTOMER')),
        CONSTRAINT UQ_users_Username UNIQUE (Username),
        CONSTRAINT UQ_users_Email UNIQUE (Email)
    );
END
GO

IF OBJECT_ID(N'products', N'U') IS NULL
BEGIN
    CREATE TABLE products
    (
        ProductId INT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_products PRIMARY KEY,
        ProductName NVARCHAR(250) NOT NULL,
        Description NVARCHAR(500) NULL,
        Price FLOAT(53) NULL,
        Images NVARCHAR(500) NULL,
        Status INT NULL,
        CategoryId INT NOT NULL,
        CreatedAt DATETIME2(6) NULL,
        CONSTRAINT FK_products_categories
            FOREIGN KEY (CategoryId) REFERENCES categories (CategoryId)
    );
END
GO

IF OBJECT_ID(N'otp_tokens', N'U') IS NULL
BEGIN
    CREATE TABLE otp_tokens
    (
        Id INT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_otp_tokens PRIMARY KEY,
        UserId INT NOT NULL,
        Purpose NVARCHAR(50) NOT NULL,
        CodeHash NVARCHAR(255) NOT NULL,
        ExpiresAt DATETIME2(6) NOT NULL,
        Attempts INT NOT NULL,
        Used BIT NOT NULL,
        CreatedAt DATETIME2(6) NOT NULL,
        CONSTRAINT FK_otp_tokens_users
            FOREIGN KEY (UserId) REFERENCES users (Id)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM categories WHERE CategoryName = N'Electronics')
BEGIN
    INSERT INTO categories (CategoryName, Images, Status)
    VALUES (N'Electronics', N'electronics.jpg', 1);
END
ELSE
BEGIN
    UPDATE categories
    SET Images = N'electronics.jpg'
    WHERE CategoryName = N'Electronics' AND (Images IS NULL OR LTRIM(RTRIM(Images)) = N'');
END
GO

DECLARE @ElectronicsCatId INT;
SELECT TOP 1 @ElectronicsCatId = CategoryId FROM categories WHERE CategoryName = N'Electronics';

IF @ElectronicsCatId IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'iPhone 15 Pro')
        INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
        VALUES (N'iPhone 15 Pro', N'Premium smartphone with a titanium design.', 28990000, N'iphone15.jpg', 1, @ElectronicsCatId, '2026-09-05T10:00:00');

    IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'Samsung Galaxy S25')
        INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
        VALUES (N'Samsung Galaxy S25', N'Flagship Android smartphone with a bright display.', 23990000, N'samsung-s25.jpg', 1, @ElectronicsCatId, '2026-09-05T09:00:00');

    IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'MacBook Air M3')
        INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
        VALUES (N'MacBook Air M3', N'Lightweight laptop powered by Apple silicon.', 27990000, N'macbook-air-m3.jpg', 1, @ElectronicsCatId, '2026-09-05T08:00:00');

    IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'Dell XPS 13')
        INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
        VALUES (N'Dell XPS 13', N'Compact premium Windows ultrabook.', 24990000, N'dell-xps13.jpg', 1, @ElectronicsCatId, '2026-09-05T07:00:00');

    IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'AirPods Pro 2')
        INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
        VALUES (N'AirPods Pro 2', N'Wireless earbuds with active noise cancellation.', 5990000, N'airpods-pro.jpg', 1, @ElectronicsCatId, '2026-09-05T06:00:00');

    IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'iPad Air M2')
        INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
        VALUES (N'iPad Air M2', N'Versatile tablet for work, study, and entertainment.', 16990000, N'ipad-air-m2.jpg', 1, @ElectronicsCatId, '2026-09-05T05:00:00');

    IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'ASUS ROG Strix')
        INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
        VALUES (N'ASUS ROG Strix', N'Performance gaming laptop with a dedicated GPU.', 32990000, N'rog-strix.jpg', 1, @ElectronicsCatId, '2026-09-05T04:00:00');

    IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'Apple Watch Series 10')
        INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
        VALUES (N'Apple Watch Series 10', N'Smartwatch with fitness and health tracking.', 10990000, N'apple-watch.jpg', 1, @ElectronicsCatId, '2026-09-05T03:00:00');

    IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'Sony WH-1000XM5')
        INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
        VALUES (N'Sony WH-1000XM5', N'Over-ear wireless headphones with noise cancellation.', 8490000, N'sony-xm5.jpg', 1, @ElectronicsCatId, '2026-09-05T02:00:00');

    IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'Logitech MX Master 3')
        INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
        VALUES (N'Logitech MX Master 3', N'Ergonomic wireless mouse for productivity.', 2490000, N'logitech-mx.jpg', 1, @ElectronicsCatId, '2026-09-05T01:00:00');
END
GO
