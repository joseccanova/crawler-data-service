SELECT
  NULL AS SYNONYM_CATALOG,
  SYNONYMS.OWNER AS SYNONYM_SCHEMA,
  SYNONYMS.SYNONYM_NAME,
  NULL AS REFERENCED_OBJECT_CATALOG,
  SYNONYMS.TABLE_OWNER AS REFERENCED_OBJECT_SCHEMA,
  SYNONYMS.TABLE_NAME AS REFERENCED_OBJECT_NAME
FROM
  ${catalogscope}_SYNONYMS SYNONYMS
  INNER JOIN ${catalogscope}_USERS USERS
    ON SYNONYMS.OWNER = USERS.USERNAME
      AND NOT REGEXP_LIKE(USERS.USERNAME, '^APEX_[0-9]{6}$')
      AND NOT REGEXP_LIKE(USERS.USERNAME, '^FLOWS_[0-9]{5}$')
WHERE
  REGEXP_LIKE(SYNONYMS.OWNER, '${schemas}')
  AND SYNONYMS.TABLE_NAME NOT LIKE 'BIN$%'
  AND NOT REGEXP_LIKE(SYNONYMS.TABLE_NAME, '^(SYS_IOT|MDOS|MDRS|MDRT|MDOT|MDXT)_.*$')
ORDER BY
  SYNONYM_SCHEMA,
  SYNONYM_NAME
