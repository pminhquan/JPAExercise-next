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
