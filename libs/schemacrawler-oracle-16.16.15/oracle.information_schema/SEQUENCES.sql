SELECT
  NULL AS SEQUENCE_CATALOG,
  SEQUENCES.SEQUENCE_OWNER AS SEQUENCE_SCHEMA,
  SEQUENCES.SEQUENCE_NAME AS SEQUENCE_NAME,
  SEQUENCES.INCREMENT_BY AS "INCREMENT",
  NULL AS START_VALUE,
  SEQUENCES.MIN_VALUE AS MINIMUM_VALUE,
  SEQUENCES.MAX_VALUE AS MAXIMUM_VALUE,
  CASE WHEN SEQUENCES.CYCLE_FLAG = 'Y' THEN 'YES' ELSE 'NO' END AS CYCLE_OPTION,
  SEQUENCES.ORDER_FLAG,
  SEQUENCES.CACHE_SIZE,
  SEQUENCES.LAST_NUMBER
FROM
  ${catalogscope}_SEQUENCES SEQUENCES
  INNER JOIN ${catalogscope}_USERS USERS
    ON SEQUENCES.SEQUENCE_OWNER = USERS.USERNAME
      AND USERS.ORACLE_MAINTAINED = 'N'
      AND NOT REGEXP_LIKE(USERS.USERNAME, '^APEX_[0-9]{6}$')
      AND NOT REGEXP_LIKE(USERS.USERNAME, '^FLOWS_[0-9]{5}$')
WHERE
  REGEXP_LIKE(SEQUENCES.SEQUENCE_OWNER, '${schemas}')
ORDER BY
  SEQUENCE_OWNER,
  SEQUENCE_NAME
