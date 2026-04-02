Please extend the existing Java project incrementally.

Goal:
Implement ONLY relationship_detail.tsv generation.
Do NOT implement lineage_path.tsv yet.

Add a new dedicated main class, for example:
SampleRelationshipDetailMain

CLI args:
--tableDir
--viewDir
--functionDir
--spDir
--outputDir
--extraDir   optional

Important:
- Do not change the behavior of the existing main
- Reuse current scanner / extractor / writer code as much as possible
- Prefer minimal invasive changes

Work in this order:
1. Inspect the current codebase
2. Inspect the sample package structure
3. Inspect docs/relationship_list_with_samples.md
4. Inspect expected/relationship_detail.tsv
5. Summarize the inferred direct-relationship rules
6. Then implement relationship_detail.tsv only

Sample package root contains:
- table/
- view/
- function/
- sp/
- extra/
- expected/
- docs/

Assume:
- each file under table/ defines exactly one table
- each file under view/ defines exactly one view
- each file under function/ defines exactly one function
- each file under sp/ defines exactly one procedure
- files under extra/ are pattern coverage files, not formal DB objects

Output file:
relationship_detail.tsv

Columns in exact order:
source_object_type
source_object
source_field
target_object_type
target_object
target_field
relationship
line_no
line_content
persistent_impact_objects
intermediate_objects
confidence

Core rules:
- relationship_detail.tsv stores direct single-hop relationships only
- one direct relationship = one row
- it is relationship-based, NOT line-based
- do not emit every non-comment SQL line
- only emit meaningful relationship rows
- do not derive propagated or multi-hop lineage here

Supported object types:
TABLE
VIEW
FUNCTION
PROCEDURE
SESSION_TABLE
CTE
UNKNOWN
VIEW_DDL

Supported relationship types:
CREATE_TABLE
CREATE_VIEW
CREATE_VIEW_MAP
SELECT_TABLE
SELECT_VIEW
SELECT_FIELD
SELECT_EXPR
INSERT_TABLE
INSERT_VIEW
INSERT_TARGET_COL
INSERT_SELECT_MAP
UPDATE_TABLE
UPDATE_VIEW
UPDATE_TARGET_COL
UPDATE_SET
UPDATE_SET_MAP
DELETE_TABLE
DELETE_VIEW
MERGE_TABLE
MERGE_VIEW
MERGE_TARGET_COL
MERGE_MATCH
MERGE_SET_MAP
MERGE_INSERT_MAP
JOIN_ON
WHERE
GROUP_BY
HAVING
ORDER_BY
CALL_PROCEDURE
CALL_FUNCTION
RETURN_VALUE
TRUNCATE_TABLE
UNION_INPUT
CTE_DEFINE
CTE_READ
UNKNOWN

Allowed confidence values:
PARSER
REGEX
DYNAMIC_LOW_CONFIDENCE

Very important line_content rule:
- line_content must be the raw original source line text from the SQL file
- retrieve it by line_no
- do not reconstruct it from AST
- do not simplify it
- do not trim leading spaces
- do not remove commas, semicolons, or trailing operators like ||

For metadata-inferred rows use:
INFERRED TARGET COLUMN FROM METADATA: <COLUMN>

INSERT_TARGET_COL rule:
Emit INSERT_TARGET_COL in both cases:
1. explicit target column list exists
2. target column list omitted but inferred from metadata

If target column list is omitted:
- infer target columns from target table metadata declared order
- emit one INSERT_TARGET_COL row per inferred target column
- line_no = the INSERT INTO line
- line_content = INFERRED TARGET COLUMN FROM METADATA: <COLUMN>

SELECT * expansion rule:
For INSERT INTO target SELECT * FROM source:
- use explicit target columns if present
- otherwise use target metadata declared order
- use source metadata declared order
- emit one INSERT_SELECT_MAP per matched pair
- do not guess if metadata is missing

persistent_impact_objects rule:
- store only the final persistent target side for the current direct row
- do not store both source and target endpoints together

intermediate_objects rule:
- store intermediate object/column path for the current direct row
- prefer OBJECT.COLUMN when possible

Session table rule:
Support DECLARE GLOBAL TEMPORARY TABLE SESSION.xxx
Emit:
- target_object_type = SESSION_TABLE
- relationship = CREATE_TABLE
Do not include session tables in persistent_impact_objects

CTE rule:
Support WITH base_data AS (...)
Emit:
- CTE_DEFINE
- CTE_READ
- UNION_INPUT when relevant

Function return rule:
RETURN_VALUE should capture direct return dependencies.
If return depends on:
- a selected column
- a called function
both may appear as separate RETURN_VALUE rows.

Dynamic SQL rule:
For EXECUTE IMMEDIATE or unresolved dynamic SQL:
- relationship = UNKNOWN
- confidence = DYNAMIC_LOW_CONFIDENCE

DB2 script terminator rule:
- top-level scripts use @ on its own line
- routine bodies still use ; internally

Support these sample patterns:
- CREATE VIEW ... AS SELECT ...
- function reading CCL.CODE_MAP with WHERE + RETURN
- function reading a view and calling another function
- procedure declaring SESSION temp tables
- WITH BASE_DATA AS (...)
- UNION ALL
- INSERT INTO SESSION table ... SELECT ...
- INSERT INTO ... SELECT * ...
- UPDATE table with scalar subquery
- UPDATE view
- DELETE table
- DELETE view
- MERGE table USING session table
- MERGE view USING session table
- CALL procedure
- TRUNCATE TABLE
- graph case:
  SESSION.T_GRAPH_STAGE.COL_A -> TABLE_A.COL_A
  SESSION.T_GRAPH_STAGE.COL_A -> TABLE_B.COL_B
  TABLE_A.COL_A -> TABLE_C.COL_C
  TABLE_C.COL_C -> TABLE_B.COL_B

If extraDir is provided, also support:
- JOIN / WHERE / GROUP_BY / HAVING / ORDER_BY
- EXECUTE IMMEDIATE
Use a stable source_object such as EXTRA_PATTERNS for extra files.

Expected behavior:
- inspect reusable existing classes first
- add a dedicated new main
- add minimal supporting changes only
- write relationship_detail.tsv to outputDir
- if easy, compare with expected/relationship_detail.tsv and print mismatches

Implement in phases:
1. inspect current structure and reusable components
2. build metadata loading for table/view
3. add new main and output flow
4. generate relationship_detail.tsv
5. compare with expected and refine

Do not implement lineage_path.tsv in this task.
