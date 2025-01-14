SELECT t.*, a.name AS counterPartyAccountName, a.number AS counterPartyAccountNumber, a.code AS counterPartyAccountCode,
s.number AS statementNumber, s.period AS statementPeriod,
tt.type AS transactionTypeStr, tt.code AS transactionTypeCode
FROM [transaction] t
JOIN [account] a ON t.counterPartyAccount = a.accountId
JOIN [statement] s ON t.statement = s.statementId
JOIN [transactionType] tt ON t.transactionType = tt.trxTypeId
WHERE t.ownAccountNumber = ?