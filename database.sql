IF DB_ID(N'jakartaJPA') IS NULL
BEGIN
    CREATE DATABASE jakartaJPA;
END
GO

USE jakartaJPA;
GO

CREATE TABLE categories
(
    CategoryId INT IDENTITY(1,1) NOT NULL
        CONSTRAINT PK_categories PRIMARY KEY,
    CategoryName NVARCHAR(100) NOT NULL,
    Images NVARCHAR(500) NULL,
    Status INT NULL
);
GO

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
    CONSTRAINT UQ_users_Username UNIQUE (Username),
    CONSTRAINT UQ_users_Email UNIQUE (Email)
);
GO

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
GO

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
GO

INSERT INTO categories (CategoryName, Images, Status)
VALUES (N'Electronics', N'', 1);
GO

INSERT INTO products
    (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
VALUES
    (N'iPhone 15 Pro', N'Premium smartphone with a titanium design.', 28990000, N'iphone15.jpg', 1, 1, '2026-09-05T10:00:00'),
    (N'Samsung Galaxy S25', N'Flagship Android smartphone with a bright display.', 23990000, N'samsung-s25.jpg', 1, 1, '2026-09-05T09:00:00'),
    (N'MacBook Air M3', N'Lightweight laptop powered by Apple silicon.', 27990000, N'macbook-air-m3.jpg', 1, 1, '2026-09-05T08:00:00'),
    (N'Dell XPS 13', N'Compact premium Windows ultrabook.', 24990000, N'dell-xps13.jpg', 1, 1, '2026-09-05T07:00:00'),
    (N'AirPods Pro 2', N'Wireless earbuds with active noise cancellation.', 5990000, N'airpods-pro.jpg', 1, 1, '2026-09-05T06:00:00'),
    (N'iPad Air M2', N'Versatile tablet for work, study, and entertainment.', 16990000, N'ipad-air-m2.jpg', 1, 1, '2026-09-05T05:00:00'),
    (N'ASUS ROG Strix', N'Performance gaming laptop with a dedicated GPU.', 32990000, N'rog-strix.jpg', 1, 1, '2026-09-05T04:00:00'),
    (N'Apple Watch Series 10', N'Smartwatch with fitness and health tracking.', 10990000, N'apple-watch.jpg', 1, 1, '2026-09-05T03:00:00'),
    (N'Sony WH-1000XM5', N'Over-ear wireless headphones with noise cancellation.', 8490000, N'sony-xm5.jpg', 1, 1, '2026-09-05T02:00:00'),
    (N'Logitech MX Master 3', N'Ergonomic wireless mouse for productivity.', 2490000, N'logitech-mx.jpg', 1, 1, '2026-09-05T01:00:00');
GO
