Please fix how persistent_impact_objects and intermediate_objects are populated in relationship_detail.tsv.

New rules:

persistent_impact_objects:
Only populate for true persistent write/materialization mappings:
- CREATE_VIEW_MAP
- INSERT_SELECT_MAP
- UPDATE_SET_MAP
- MERGE_SET_MAP
- MERGE_INSERT_MAP
Optional only if already clean:
- INSERT_TARGET_COL
- UPDATE_TARGET_COL
- MERGE_TARGET_COL

intermediate_objects:
Populate for intermediate dependency/path style rows:
- RETURN_VALUE
- CTE_DEFINE
- CTE_READ
- UNION_INPUT
- SELECT_EXPR
Optional:
- CALL_FUNCTION / CALL_PROCEDURE only if safe

Leave BOTH fields empty for:
- SELECT_TABLE
- SELECT_VIEW
- SELECT_FIELD
- WHERE
- JOIN_ON
- GROUP_BY
- HAVING
- ORDER_BY
- CALL_FUNCTION (default)
- CALL_PROCEDURE (default)
- DELETE_TABLE
- DELETE_VIEW
- TRUNCATE_TABLE
- UNKNOWN

Important fixes:
- FN_GET_CODE_MAP_VALUE line 18-22 WHERE rows: persistent_impact_objects must be empty
- FN_GET_CODE_MAP_VALUE RETURN_VALUE: persistent_impact_objects empty, intermediate_objects = CCL.CODE_MAP.CODE_VALUE
- FN_REL_DEMO RETURN_VALUE rows: persistent_impact_objects empty, intermediate_objects = dependency source

Make minimal code changes only. Preserve TSV structure and sorting.
