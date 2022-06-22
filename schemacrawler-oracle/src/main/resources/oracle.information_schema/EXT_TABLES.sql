SELECT
  NULL AS TABLE_CATALOG,
  TABLES.OWNER AS TABLE_SCHEMA,
  TABLES.TABLE_NAME,
  DBMS_METADATA.GET_DDL('TABLE', TABLES.TABLE_NAME, TABLES.OWNER)
    AS TABLE_DEFINITION
FROM
  ${catalogscope}_TABLES TABLES
WHERE
  REGEXP_LIKE(TABLES.OWNER, 'ADMACI')
  AND TABLES.TABLE_NAME NOT LIKE 'BIN$%'
  AND NOT REGEXP_LIKE(TABLES.TABLE_NAME, '^(SYS_IOT|MDOS|MDRS|MDRT|MDOT|MDXT)_.*$')
  AND TABLES.NESTED = 'NO'
  AND (TABLES.IOT_TYPE IS NULL OR TABLES.IOT_TYPE = 'IOT')
ORDER BY
  TABLE_SCHEMA,
  TABLE_NAME
