Please fix redundant SELECT_FIELD rows in relationship_detail.tsv.

New rule:
If a source field already participates in a stronger mapping relationship in the same SQL statement, suppress the redundant SELECT_FIELD row.

Stronger mapping relationships:
- CREATE_VIEW_MAP
- INSERT_SELECT_MAP
- UPDATE_SET_MAP
- MERGE_SET_MAP
- MERGE_INSERT_MAP

Examples:
1. CREATE VIEW AS SELECT:
keep CREATE_VIEW + SELECT_TABLE + CREATE_VIEW_MAP
remove redundant SELECT_FIELD for mapped columns

2. INSERT INTO ... SELECT ...:
keep INSERT_TABLE/INSERT_VIEW + INSERT_TARGET_COL + INSERT_SELECT_MAP + SELECT_TABLE + WHERE
remove redundant SELECT_FIELD for mapped source columns

3. Plain function SELECT:
keep SELECT_FIELD if there is no stronger mapping relationship

Important:
- dedup only within the same statement context
- do not suppress SELECT_FIELD globally
- do not suppress SELECT_EXPR, WHERE, SELECT_TABLE, CALL_FUNCTION, RETURN_VALUE

Make minimal code changes only.
Preserve TSV structure and sorting.
