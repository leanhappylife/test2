Please fix the existing relationship_detail.tsv generation logic in the current Java project.

Do NOT redesign the whole project.
Do NOT change the overall TSV structure.
Make the minimum targeted code changes needed.

====================================
GOAL
====================================

Fix the implementation for these two cases:

1. SELECT_EXPR line_content must preserve the exact original SQL source line
2. RETURN_VALUE must support multiple direct dependencies for the same return line

====================================
CASE 1: SELECT_EXPR line_content must preserve exact raw source line
====================================

Problem:
For function INTERFACE.FN_REL_DEMO, line 12 currently loses the trailing `||` or otherwise simplifies the SQL line.

Expected behavior:
line_content must always be the exact raw original source line from the SQL file, retrieved by line_no.

For this SQL:

    SET V_OUT = (
        SELECT TRIM(V.CUST_NUM) || '-' ||
               INTERFACE.FN_GET_CODE_MAP_VALUE('MTM','STATUS','DEFAULT','A','B')
        FROM INTERFACE.V_API_MTM_REVAL V
        WHERE V.CUST_NUM = P_CUST_NUM
        FETCH FIRST 1 ROW ONLY
    );

The SELECT_EXPR row for line 12 must use exactly:

        SELECT TRIM(V.CUST_NUM) || '-' ||

Rules:
- do not reconstruct line_content from AST
- do not trim leading spaces
- do not remove commas
- do not remove semicolons
- do not remove trailing operators like `||`
- always load line_content directly from the original source file by line_no

Please inspect where line_content is currently derived and replace that logic with exact source-line lookup.

====================================
CASE 2: RETURN_VALUE must support multiple direct dependencies
====================================

Problem:
For function INTERFACE.FN_REL_DEMO, the return line is:

    RETURN V_OUT;

But V_OUT is assigned from an expression that depends on two direct sources:
- INTERFACE.V_API_MTM_REVAL.CUST_NUM
- INTERFACE.FN_GET_CODE_MAP_VALUE(...)

Expected behavior:
Generate two RETURN_VALUE rows for the same line_no = 19.

Both rows should target the current function return value, for example:
- target_object_type = FUNCTION
- target_object = INTERFACE.FN_REL_DEMO
- target_field = RETURN_VALUE

And they should differ by dependency source:

Row 1:
- source_field = CUST_NUM
- target_object_type = FUNCTION
- target_object = INTERFACE.FN_REL_DEMO
- target_field = RETURN_VALUE
- relationship = RETURN_VALUE
- line_no = 19
- line_content = exact raw source line: "    RETURN V_OUT;"
- persistent_impact_objects = empty
- intermediate_objects = INTERFACE.V_API_MTM_REVAL.CUST_NUM

Row 2:
- source_field = empty
- target_object_type = FUNCTION
- target_object = INTERFACE.FN_REL_DEMO
- target_field = RETURN_VALUE
- relationship = RETURN_VALUE
- line_no = 19
- line_content = exact raw source line: "    RETURN V_OUT;"
- persistent_impact_objects = empty
- intermediate_objects = INTERFACE.FN_GET_CODE_MAP_VALUE

Meaning:
A single RETURN statement may emit multiple direct relationship rows if the returned variable/expression has multiple direct dependencies.

====================================
IMPORTANT RULES
====================================

1. relationship_detail.tsv remains direct single-hop only
2. one direct dependency = one row
3. same source_object and same line_no may validly produce multiple rows
4. for RETURN_VALUE, this is expected and correct
5. do not collapse multiple direct dependencies into one row
6. do not remove the current sorting rule:
   - same source_object should be ordered by line_no ascending

====================================
EXPECTED FN_REL_DEMO RESULT
====================================

For INTERFACE.FN_REL_DEMO, the expected rows should be:

1.
source_object_type = FUNCTION
source_object = INTERFACE.FN_REL_DEMO
source_field = CUST_NUM
target_object_type = VIEW
target_object = INTERFACE.V_API_MTM_REVAL
target_field = CUST_NUM
relationship = SELECT_EXPR
line_no = 12
line_content = "        SELECT TRIM(V.CUST_NUM) || '-' ||"
persistent_impact_objects = empty
intermediate_objects = empty
confidence = PARSER

2.
source_object_type = FUNCTION
source_object = INTERFACE.FN_REL_DEMO
source_field = empty
target_object_type = FUNCTION
target_object = INTERFACE.FN_GET_CODE_MAP_VALUE
target_field = empty
relationship = CALL_FUNCTION
line_no = 13
line_content = "               INTERFACE.FN_GET_CODE_MAP_VALUE('MTM','STATUS','DEFAULT','A','B')"
persistent_impact_objects = empty
intermediate_objects = empty
confidence = PARSER

3.
source_object_type = FUNCTION
source_object = INTERFACE.FN_REL_DEMO
source_field = empty
target_object_type = VIEW
target_object = INTERFACE.V_API_MTM_REVAL
target_field = empty
relationship = SELECT_VIEW
line_no = 14
line_content = "        FROM INTERFACE.V_API_MTM_REVAL V"
persistent_impact_objects = empty
intermediate_objects = empty
confidence = PARSER

4.
source_object_type = FUNCTION
source_object = INTERFACE.FN_REL_DEMO
source_field = CUST_NUM
target_object_type = VIEW
target_object = INTERFACE.V_API_MTM_REVAL
target_field = CUST_NUM
relationship = WHERE
line_no = 15
line_content = "        WHERE V.CUST_NUM = P_CUST_NUM"
persistent_impact_objects = empty
intermediate_objects = empty
confidence = PARSER

5.
source_object_type = FUNCTION
source_object = INTERFACE.FN_REL_DEMO
source_field = CUST_NUM
target_object_type = FUNCTION
target_object = INTERFACE.FN_REL_DEMO
target_field = RETURN_VALUE
relationship = RETURN_VALUE
line_no = 19
line_content = "    RETURN V_OUT;"
persistent_impact_objects = empty
intermediate_objects = INTERFACE.V_API_MTM_REVAL.CUST_NUM
confidence = PARSER

6.
source_object_type = FUNCTION
source_object = INTERFACE.FN_REL_DEMO
source_field = empty
target_object_type = FUNCTION
target_object = INTERFACE.FN_REL_DEMO
target_field = RETURN_VALUE
relationship = RETURN_VALUE
line_no = 19
line_content = "    RETURN V_OUT;"
persistent_impact_objects = empty
intermediate_objects = INTERFACE.FN_GET_CODE_MAP_VALUE
confidence = PARSER

====================================
IMPLEMENTATION INSTRUCTIONS
====================================

Please:
1. inspect the existing line_content generation logic
2. inspect the existing RETURN_VALUE extraction logic
3. make only the minimum code changes required
4. keep existing package structure
5. preserve current TSV column order
6. preserve sorting by source_object and line_no
7. add or update tests if practical

At the end, explain:
- which classes you changed
- how exact source-line retrieval now works
- how multiple RETURN_VALUE dependencies are emitted
