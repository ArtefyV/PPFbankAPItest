IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'account') 
CREATE TABLE [account](
[accountId] BIGINT IDENTITY(1000,1) NOT NULL,
[name] NVARCHAR(50) NOT NULL,
[number] NVARCHAR(20) NOT NULL,
[code] NVARCHAR(4) NOT NULL,
CONSTRAINT PK_account_accountId PRIMARY KEY (accountId))