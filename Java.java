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

==================================
  You are working in an existing Java project that already uses JSqlParser and already has code for:
- scanning SQL files
- extracting routines
- extracting SQL usage
- writing TSV / Excel-like outputs

Please extend the existing project incrementally.

====================================
GOAL
====================================

Implement ONLY the generation of:

- relationship_detail.tsv

Do NOT implement lineage_path.tsv in this task.

Add a new main class that accepts these folder arguments:

- --tableDir
- --viewDir
- --functionDir
- --spDir
- --outputDir

And also support this optional argument:

- --extraDir

The implementation must reuse the existing project structure as much as possible.

Do NOT modify the behavior of the existing main entry point unless strictly required.
Add a new dedicated main for this task.

====================================
SCOPE DECISION
====================================

For this task, process:

- table/
- view/
- function/
- sp/

And if provided, also process:

- extra/

That means:
- CLI inputs must support tableDir, viewDir, functionDir, spDir, outputDir
- extraDir is optional
- if extraDir is provided, process extra SQL files and include their direct relationships in relationship_detail.tsv

expected/relationship_detail.tsv used in this task may include rows from:
- table/
- view/
- function/
- sp/
- extra/

Do not ignore extra/ if it exists and expected result includes extra-pattern rows.

====================================
WORK IN THIS ORDER
====================================

1. Inspect the current project structure first
2. Inspect the sample package folder structure
3. Inspect docs/relationship_list_with_samples.md
4. Inspect expected/relationship_detail.tsv
5. Compare sample SQL and expected result carefully
6. Summarize the inferred relationship rules in your own words before implementation
7. Then implement only relationship_detail.tsv generation
8. Do NOT implement lineage_path.tsv in this task

====================================
SAMPLE PACKAGE STRUCTURE
====================================

Assume the sample package root contains:

- table/
- view/
- function/
- sp/
- extra/
- expected/
- docs/

Do not depend on the exact root folder name.

Typical structure:

sample_case_verified/
  table/
    CCL.CODE_MAP.sql
    FOS.API_MTM_REVAL.sql
    INTERFACE.API_MTM_REVAL.sql
    INTERFACE.API_MTM_REVAL_STAR.sql
    RPT.MTM_REVAL_AUDIT.sql
    INTERFACE.MTM_REVAL_HIST.sql
    INTERFACE.MTM_REVAL_TMP_COPY.sql
    TABLE_A.sql
    TABLE_B.sql
    TABLE_C.sql
  view/
    INTERFACE.V_API_MTM_REVAL.sql
  function/
    INTERFACE.FN_GET_CODE_MAP_VALUE.sql
    INTERFACE.FN_REL_DEMO.sql
  sp/
    RPT.PR_AUDIT_MTM_REVAL.sql
    INTERFACE.PI_API_MTM_REVAL_DEMO.sql
    INTERFACE.PI_GRAPH_DEMO.sql
  extra/
    extra_patterns.sql
  expected/
    relationship_detail.tsv
    lineage_path.tsv
  docs/
    relationship_list_with_samples.md

Assume:
- each file under table/ defines exactly one table
- each file under view/ defines exactly one view
- each file under function/ defines exactly one function
- each file under sp/ defines exactly one procedure
- files under extra/ are pattern coverage files, not formal DB objects

====================================
NEW MAIN
====================================

Add a new main class, for example:

org.example.sqlusagechecker.SampleRelationshipDetailMain

This main must accept:

--tableDir <path>
--viewDir <path>
--functionDir <path>
--spDir <path>
--outputDir <path>
--extraDir <path>   optional

Behavior:
- validate required args
- log resolved paths
- scan SQL files from tableDir, viewDir, functionDir, spDir
- if extraDir is provided, scan SQL files from extraDir too
- build metadata from table and view files
- extract direct relationships from function and procedure files
- process view files for CREATE_VIEW / CREATE_VIEW_MAP
- process extra SQL files if provided
- write relationship_detail.tsv to outputDir

Optional:
- if expected/relationship_detail.tsv is available, print a simple diff summary

====================================
SCOPE LIMIT
====================================

Implement ONLY relationship_detail.tsv.

Do NOT implement lineage_path.tsv.
Do NOT derive multi-hop or propagated lineage here.
Do NOT redesign the whole project.
Do NOT replace the current architecture.
Prefer minimal invasive changes.

====================================
OUTPUT COLUMNS
====================================

relationship_detail.tsv must contain these columns in this exact order:

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

====================================
CORE RULES
====================================

1. relationship_detail.tsv stores direct single-hop relationships only
2. one direct relationship = one row
3. relationship_detail.tsv is relationship-based, NOT line-based
4. do NOT emit every non-comment SQL line
5. only emit rows that correspond to meaningful relationships

Do not emit rows for purely structural lines such as:
- BEGIN
- END
- LANGUAGE SQL
- MODIFIES SQL DATA
- RETURNS ...
- WITH REPLACE
- ON COMMIT PRESERVE ROWS
- (
- )

unless a supported relationship is explicitly defined for that line.

====================================
SOURCE_OBJECT RULE
====================================

Use object name whenever the object is known.

Rules:
- for FUNCTION rows, use fully qualified function name in source_object
- for PROCEDURE rows, use fully qualified procedure name in source_object
- for VIEW_DDL rows, use the view name consistently if known, otherwise use file name consistently
- for extra pattern files, use a stable synthetic source_object such as EXTRA_PATTERNS or the extra file base name consistently
- prefer object name over file name whenever a meaningful object name exists

Do not mix file-name style and object-name style arbitrarily.

====================================
SUPPORTED OBJECT TYPES
====================================

At minimum support:

- TABLE
- VIEW
- FUNCTION
- PROCEDURE
- SESSION_TABLE
- CTE
- UNKNOWN
- VIEW_DDL

====================================
SUPPORTED RELATIONSHIP TYPES
====================================

At minimum support:

- CREATE_TABLE
- CREATE_VIEW
- CREATE_VIEW_MAP

- SELECT_TABLE
- SELECT_VIEW
- SELECT_FIELD
- SELECT_EXPR

- INSERT_TABLE
- INSERT_VIEW
- INSERT_TARGET_COL
- INSERT_SELECT_MAP

- UPDATE_TABLE
- UPDATE_VIEW
- UPDATE_TARGET_COL
- UPDATE_SET
- UPDATE_SET_MAP

- DELETE_TABLE
- DELETE_VIEW

- MERGE_TABLE
- MERGE_VIEW
- MERGE_TARGET_COL
- MERGE_MATCH
- MERGE_SET_MAP
- MERGE_INSERT_MAP

- JOIN_ON
- WHERE
- GROUP_BY
- HAVING
- ORDER_BY

- CALL_PROCEDURE
- CALL_FUNCTION

- RETURN_VALUE

- TRUNCATE_TABLE

- UNION_INPUT
- CTE_DEFINE
- CTE_READ

- UNKNOWN

====================================
CONFIDENCE VALUES
====================================

Only allow:

- PARSER
- REGEX
- DYNAMIC_LOW_CONFIDENCE

====================================
IMPORTANT RULES ABOUT line_no AND line_content
====================================

line_no:
- line_no must be the line number inside the original SQL source file

line_content:
- line_content must be the raw original source line text from the SQL file
- retrieve it by line_no from the source file
- do NOT reconstruct it from AST nodes
- do NOT simplify it
- do NOT trim leading spaces
- do NOT remove commas
- do NOT remove semicolons
- do NOT remove trailing operators like ||
- do NOT rebuild SQL text manually

For metadata-inferred rows where there is no explicit source line, use:

INFERRED TARGET COLUMN FROM METADATA: <COLUMN>

Example:
INFERRED TARGET COLUMN FROM METADATA: CUST_NUM

====================================
IMPORTANT RULES ABOUT INSERT_TARGET_COL
====================================

Emit INSERT_TARGET_COL in BOTH cases:

A. explicit target column list exists
Example:
INSERT INTO T (A, B, C)

B. target column list is omitted but can be inferred from metadata
Example:
INSERT INTO T
SELECT *
FROM S

In case B:
- infer target columns from target table metadata declared order
- still emit one INSERT_TARGET_COL row per inferred target column
- line_no = the line number of the INSERT INTO statement
- line_content = INFERRED TARGET COLUMN FROM METADATA: <COLUMN>

====================================
IMPORTANT RULES ABOUT SELECT * EXPANSION
====================================

For:
INSERT INTO target
SELECT *
FROM source

expand using metadata.

Rules:
- if explicit target column list exists, use that order
- otherwise use target metadata declared order
- use source metadata declared order
- generate one INSERT_SELECT_MAP per matched pair
- do not guess if metadata is missing

====================================
IMPORTANT RULES ABOUT persistent_impact_objects
====================================

persistent_impact_objects stores only the final persistent target side for the current direct row.

Examples:
- INTERFACE.API_MTM_REVAL.CUST_NUM
- INTERFACE.V_API_MTM_REVAL.CUR_MTM_AMT
- TABLE_B.COL_B

Do NOT store both source and target endpoints together in this column.

====================================
IMPORTANT RULES ABOUT intermediate_objects
====================================

intermediate_objects should store intermediate object/column path for the current direct row.

Prefer OBJECT.COLUMN granularity whenever possible.

Examples:
- BASE_DATA.CUST_NUM
- BASE_DATA.CUST_NUM,SESSION.T_MTM_STAGE.CUST_NUM
- SESSION.T_GRAPH_STAGE.COL_A

For object-level relationships like CTE_DEFINE or CREATE_TABLE for session tables,
object-only is acceptable.

====================================
SESSION TABLE RULE
====================================

Support:
DECLARE GLOBAL TEMPORARY TABLE SESSION.xxx

Emit:
- target_object_type = SESSION_TABLE
- relationship = CREATE_TABLE

Do not include session tables in persistent_impact_objects.

====================================
CTE RULE
====================================

Support:
WITH base_data AS (...)

Emit:
- CTE_DEFINE
- CTE_READ
- UNION_INPUT when relevant

====================================
FUNCTION RETURN RULE
====================================

For functions, RETURN_VALUE should capture direct return dependencies.

If return depends on:
- a selected column
- a called function

both may appear as separate RETURN_VALUE rows.

====================================
DYNAMIC SQL RULE
====================================

For EXECUTE IMMEDIATE or similar unresolved dynamic SQL:
- emit relationship = UNKNOWN
- confidence = DYNAMIC_LOW_CONFIDENCE

Do not overclaim precision.

====================================
DB2 SCRIPT TERMINATOR RULE
====================================

The sample DB2 scripts use:
- @ on its own line as top-level terminator
- ; inside procedure/function bodies

Your scanning and line lookup logic must preserve exact original lines correctly.

====================================
EXPECTED SAMPLE COVERAGE
====================================

The implementation must support these patterns present in the sample:

From view/function/procedure files:
1. CREATE VIEW ... AS SELECT ...
2. function selecting from CCL.CODE_MAP with WHERE clauses and RETURN
3. function selecting from a view and calling another function
4. procedure declaring SESSION temp tables
5. WITH BASE_DATA AS (...)
6. UNION ALL
7. INSERT INTO SESSION table ... SELECT ...
8. INSERT INTO ... SELECT * ...
9. UPDATE table with scalar subquery
10. UPDATE view
11. DELETE table
12. DELETE view
13. MERGE table USING session table
14. MERGE view USING session table
15. CALL procedure
16. TRUNCATE TABLE
17. graph case:
    SESSION.T_GRAPH_STAGE.COL_A -> TABLE_A.COL_A
    SESSION.T_GRAPH_STAGE.COL_A -> TABLE_B.COL_B
    TABLE_A.COL_A -> TABLE_C.COL_C
    TABLE_C.COL_C -> TABLE_B.COL_B

If extraDir is provided, also support extra pattern coverage such as:
18. JOIN / WHERE / GROUP BY / HAVING / ORDER BY
19. EXECUTE IMMEDIATE ...

====================================
GRAPH CASE EXPECTATION
====================================

For PI_GRAPH_DEMO:
- relationship_detail.tsv must only store direct rows
- do NOT try to store full propagated paths here
- just emit the direct edges

====================================
EXTRA FILE EXPECTATION
====================================

If extraDir is provided and contains extra_patterns.sql, relationship_detail.tsv should include direct rows for:
- SELECT_TABLE
- SELECT_FIELD
- JOIN_ON
- WHERE
- GROUP_BY
- HAVING
- ORDER_BY
- UNKNOWN for EXECUTE IMMEDIATE

Use a consistent source_object such as:
- EXTRA_PATTERNS

====================================
EXPECTED IMPLEMENTATION BEHAVIOR
====================================

Before implementation:
- inspect current reusable scanner / extractor / writer classes
- inspect sample SQL and expected TSV
- inspect docs/relationship_list_with_samples.md
- summarize the inferred direct-relationship rules

During implementation:
- reuse existing code as much as possible
- add only minimal helper/service changes required
- add a dedicated new main for this task
- keep unsupported cases explicit

After implementation:
- write relationship_detail.tsv to outputDir
- optionally compare it with expected/relationship_detail.tsv
- report mismatches clearly

====================================
DELIVERABLES
====================================

Please implement:

1. a new main class for relationship_detail generation
2. minimal supporting changes required
3. reuse current scanner/extractor/writer code as much as possible
4. write relationship_detail.tsv to outputDir
5. add tests if practical
6. if expected file comparison is easy, add a lightweight comparison mode

====================================
IMPLEMENTATION STYLE
====================================

- inspect the current code first
- prefer incremental enhancement
- avoid broad refactors
- keep package structure consistent
- write readable code
- add comments only where useful
- keep unsupported cases explicit

====================================
PHASED APPROACH
====================================

Implement in phases:

Phase 1:
- inspect current project structure
- identify reusable scanner / extractor / writer components

Phase 2:
- build metadata loading for table/view folders
- add new main and output flow

Phase 3:
- generate relationship_detail.tsv for the sample case

Phase 4:
- compare with expected/relationship_detail.tsv and refine mismatches

Do not implement lineage_path.tsv in this task.

  ======
