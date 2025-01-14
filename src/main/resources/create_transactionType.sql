IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'transactionType') 
CREATE TABLE [transactionType](
[trxTypeId] BIGINT IDENTITY(1000,1) NOT NULL,
[type] NVARCHAR(20) NOT NULL,
[code] INT NOT NULL,
CONSTRAINT PK_transactionType_trxTypeId PRIMARY KEY (trxTypeId))