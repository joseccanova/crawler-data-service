SELECT
  NULL AS TABLE_CAT,
  PRIMARY_KEYS.OWNER AS TABLE_SCHEM,
  PRIMARY_KEYS.TABLE_NAME,
  PRIMARY_KEYS.CONSTRAINT_NAME AS PK_NAME,
  PK_COLUMNS.COLUMN_POSITION AS KEY_SEQ,
  PK_COLUMNS.COLUMN_NAME
FROM
  ${catalogscope}_CONSTRAINTS PRIMARY_KEYS
  INNER JOIN ${catalogscope}_IND_COLUMNS PK_COLUMNS
  ON
    PRIMARY_KEYS.CONSTRAINT_NAME = PK_COLUMNS.INDEX_NAME
    AND PRIMARY_KEYS.OWNER = PK_COLUMNS.TABLE_OWNER
    AND PRIMARY_KEYS.TABLE_NAME = PK_COLUMNS.TABLE_NAME
    AND PRIMARY_KEYS.OWNER = PK_COLUMNS.INDEX_OWNER
  INNER JOIN ${catalogscope}_USERS USERS
    ON PRIMARY_KEYS.OWNER = USERS.USERNAME
      AND USERS.ORACLE_MAINTAINED = 'N'
      AND NOT REGEXP_LIKE(USERS.USERNAME, '^APEX_[0-9]{6}$')
      AND NOT REGEXP_LIKE(USERS.USERNAME, '^FLOWS_[0-9]{5}$')
WHERE
  REGEXP_LIKE(PRIMARY_KEYS.OWNER, '${schemas}')
  AND PRIMARY_KEYS.TABLE_NAME NOT LIKE 'BIN$%'
  AND NOT REGEXP_LIKE(PRIMARY_KEYS.TABLE_NAME, '^(SYS_IOT|MDOS|MDRS|MDRT|MDOT|MDXT)_.*$')
  AND PRIMARY_KEYS.CONSTRAINT_TYPE = 'P'
ORDER BY
  TABLE_SCHEM,
  TABLE_NAME,
  PK_NAME,
  KEY_SEQ
