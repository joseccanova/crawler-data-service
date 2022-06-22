SELECT
  NULL AS PKTABLE_CAT,
  P.OWNER AS PKTABLE_SCHEM,
  P.TABLE_NAME AS PKTABLE_NAME,
  PC.COLUMN_NAME AS PKCOLUMN_NAME,
  NULL AS FKTABLE_CAT,
  F.OWNER AS FKTABLE_SCHEM,
  F.TABLE_NAME AS FKTABLE_NAME,
  FC.COLUMN_NAME AS FKCOLUMN_NAME,
  FC.POSITION AS KEY_SEQ,
  NULL AS UPDATE_RULE,
  DECODE (F.DELETE_RULE, 'CASCADE', 0, 'SET NULL', 2, 1) AS DELETE_RULE,
  F.CONSTRAINT_NAME AS FK_NAME,
  P.CONSTRAINT_NAME AS PK_NAME,
  DECODE(F.DEFERRABLE, 'DEFERRABLE', 5,'NOT DEFERRABLE', 7, 'DEFERRED', 6) AS DEFERRABILITY
FROM
  ${catalogscope}_CONS_COLUMNS PC,
  ${catalogscope}_CONSTRAINTS P,
  ${catalogscope}_CONS_COLUMNS FC,
  ${catalogscope}_CONSTRAINTS F,
  ${catalogscope}_USERS USERS
WHERE 1 = 1
  AND NOT REGEXP_LIKE(USERS.USERNAME, '^APEX_[0-9]{6}$')
  AND NOT REGEXP_LIKE(USERS.USERNAME, '^FLOWS_[0-9]{5}$')
  AND P.OWNER = USERS.USERNAME
  AND REGEXP_LIKE(P.OWNER, '${schemas}')
  AND P.OWNER = F.R_OWNER
  AND F.CONSTRAINT_TYPE = 'R'
  AND P.CONSTRAINT_NAME = F.R_CONSTRAINT_NAME
  AND P.CONSTRAINT_TYPE IN ('P', 'U')
  AND PC.OWNER = P.OWNER
  AND PC.CONSTRAINT_NAME = P.CONSTRAINT_NAME
  AND PC.TABLE_NAME = P.TABLE_NAME
  AND FC.OWNER = F.OWNER
  AND FC.CONSTRAINT_NAME = F.CONSTRAINT_NAME
  AND FC.TABLE_NAME = F.TABLE_NAME
  AND FC.POSITION = PC.POSITION
ORDER BY
  PKTABLE_SCHEM,
  PKTABLE_NAME,
  KEY_SEQ
