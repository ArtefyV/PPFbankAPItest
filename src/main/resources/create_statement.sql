IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'statement') 
CREATE TABLE [statement](
[statementId] BIGINT IDENTITY(1000,1) NOT NULL,
[number] NVARCHAR(20) NOT NULL,
[period] NVARCHAR(20) NOT NULL,
[description] NVARCHAR(1000) NULL,
CONSTRAINT PK_statement_statementId PRIMARY KEY (statementId))