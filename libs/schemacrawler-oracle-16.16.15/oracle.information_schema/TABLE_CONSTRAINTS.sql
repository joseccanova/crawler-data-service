SELECT
  NULL AS CONSTRAINT_CATALOG,
  CONSTRAINTS.OWNER AS CONSTRAINT_SCHEMA,
  CONSTRAINTS.CONSTRAINT_NAME,
  NULL AS TABLE_CATALOG,
  CONSTRAINTS.OWNER AS TABLE_SCHEMA,
  CONSTRAINTS.TABLE_NAME,
  CASE CONSTRAINTS.CONSTRAINT_TYPE WHEN 'C' THEN 'CHECK' WHEN 'U' THEN 'UNIQUE' WHEN 'P' THEN 'PRIMARY KEY' WHEN 'R' THEN 'FOREIGN KEY' END
    AS CONSTRAINT_TYPE,
  CASE WHEN CONSTRAINTS.DEFERRABLE = 'NOT DEFERRABLE' THEN 'N' ELSE 'Y' END
    AS IS_DEFERRABLE,
  CASE WHEN CONSTRAINTS.DEFERRED = 'IMMEDIATE' THEN 'N' ELSE 'Y' END
    AS INITIALLY_DEFERRED
FROM
  ${catalogscope}_CONSTRAINTS CONSTRAINTS
  INNER JOIN ${catalogscope}_USERS USERS
    ON CONSTRAINTS.OWNER = USERS.USERNAME
      AND USERS.ORACLE_MAINTAINED = 'N'
      AND NOT REGEXP_LIKE(USERS.USERNAME, '^APEX_[0-9]{6}$')
      AND NOT REGEXP_LIKE(USERS.USERNAME, '^FLOWS_[0-9]{5}$')
WHERE
  REGEXP_LIKE(CONSTRAINTS.OWNER, '${schemas}')
  AND CONSTRAINTS.TABLE_NAME NOT LIKE 'BIN$%'
  AND NOT REGEXP_LIKE(CONSTRAINTS.TABLE_NAME, '^(SYS_IOT|MDOS|MDRS|MDRT|MDOT|MDXT)_.*$')
  AND CONSTRAINT_TYPE IN ('C', 'U', 'P', 'R')
