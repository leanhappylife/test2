You are working in an existing Java project that already uses JSqlParser and already has code for:
- scanning SQL files
- extracting routines
- extracting SQL usage
- generating relationship_detail.tsv
- writing TSV / Excel-like outputs

Please extend the existing project incrementally.

====================================
GOAL
====================================

Implement ONLY the generation of:

- lineage_path.tsv

Assume relationship_detail.tsv generation already exists and is working.

Do NOT redesign relationship_detail.tsv in this task.

The new lineage_path.tsv must be derived from direct relationships, not parsed independently from scratch.

====================================
KEY DESIGN RULE
====================================

lineage_path.tsv must be built based on relationship_detail.tsv direct edges.

That means:

1. relationship_detail.tsv stores direct single-hop relationships
2. lineage_path.tsv stores propagated multi-hop end-to-end paths
3. Do NOT duplicate parsing logic unnecessarily if direct relationships already exist
4. Prefer building lineage from the direct relationship graph

====================================
WORK IN THIS ORDER
====================================

1. Inspect the current project structure first
2. Inspect the existing relationship_detail.tsv generation logic
3. Inspect the sample package structure
4. Inspect expected/lineage_path.tsv
5. Compare relationship_detail.tsv and expected lineage_path.tsv carefully
6. Summarize the inferred propagation rules in your own words before implementation
7. Then implement only lineage_path.tsv generation

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

====================================
NEW MAIN
====================================

Add a new main class, for example:

org.example.sqlusagechecker.SampleLineagePathMain

This main must accept:

--relationshipDetail <path>
--outputDir <path>

Optional:
--expectedLineagePath <path>

Optional convenience mode:
- if the project already has a main that generates relationship_detail.tsv first,
  you may also support passing directories and reusing the generated relationship_detail.tsv,
  but the core logic of lineage_path.tsv must still be based on direct relationships.

====================================
SCOPE LIMIT
====================================

Implement ONLY lineage_path.tsv in this task.

Do NOT redesign relationship_detail.tsv.
Do NOT re-parse everything from scratch if the direct relationships are already available.
Do NOT redesign the whole project.
Prefer minimal invasive changes.

====================================
OUTPUT COLUMNS
====================================

lineage_path.tsv must contain these columns in this exact order:

root_source_object
root_source_field
final_target_object
final_target_field
path
hop_count
path_relationship
confidence

====================================
CORE RULES
====================================

1. lineage_path.tsv stores propagated end-to-end lineage paths
2. one final path = one row
3. one root source may produce multiple lineage rows
4. if the same final target is reached through different paths, keep both rows
5. do NOT collapse distinct paths into one row
6. lineage_path.tsv is path-based, not direct-edge-based

====================================
path_relationship RULE
====================================

Use only:

- PROPAGATED_LINEAGE

====================================
PATH STRING RULE
====================================

The path column must store the full endpoint chain in this format:

OBJECT.FIELD -> OBJECT.FIELD -> OBJECT.FIELD

Examples:

FOS.API_MTM_REVAL.CUSTOMER_NUMBER -> INTERFACE.V_API_MTM_REVAL.CUST_NUM

INTERFACE.V_API_MTM_REVAL.CUST_NUM -> BASE_DATA.CUST_NUM -> SESSION.T_MTM_STAGE.CUST_NUM

SESSION.T_GRAPH_STAGE.COL_A -> TABLE_A.COL_A -> TABLE_C.COL_C -> TABLE_B.COL_B

Rules:
- use full object name + field name
- use ` -> ` exactly
- preserve the path order from root source to final target

====================================
hop_count RULE
====================================

hop_count = number of arrows in the path

Examples:

A.x -> B.y
hop_count = 1

A.x -> B.y -> C.z
hop_count = 2

A.x -> B.y -> C.z -> D.w
hop_count = 3

====================================
CONFIDENCE RULE
====================================

For now, confidence may be inherited conservatively from the direct relationships involved.

Recommended rule:
- if all edges in the path are PARSER, path confidence = PARSER
- otherwise if any edge is REGEX and none are DYNAMIC_LOW_CONFIDENCE, path confidence = REGEX
- if any edge is DYNAMIC_LOW_CONFIDENCE, path confidence = DYNAMIC_LOW_CONFIDENCE

Document the rule clearly in code comments.

====================================
IMPORTANT IMPLEMENTATION RULE
====================================

Not every relationship type in relationship_detail.tsv should be used as a propagation edge.

Only use field-to-field propagating relationships as lineage graph edges.

At minimum, these relationship types should be treated as propagation edges:

- CREATE_VIEW_MAP
- INSERT_SELECT_MAP
- UPDATE_SET_MAP
- MERGE_SET_MAP
- MERGE_INSERT_MAP

Optionally include:
- RETURN_VALUE
- parameter propagation
only if the current direct relationship model already represents them safely

Do NOT use these as propagation edges by default:
- SELECT_TABLE
- SELECT_VIEW
- INSERT_TABLE
- UPDATE_TABLE
- DELETE_TABLE
- DELETE_VIEW
- GROUP_BY
- HAVING
- ORDER_BY
- UNKNOWN
- CREATE_TABLE
- CTE_DEFINE
- UNION_INPUT

These may still exist in relationship_detail.tsv for usage/audit purposes, but should not automatically become field-lineage edges.

====================================
EDGE EXTRACTION RULE
====================================

Build the lineage graph from direct relationships.

Each graph edge should represent:

source endpoint -> target endpoint

Prefer using:
- source_object / source_field
- target_object / target_field

If a row does not clearly define both source and target field endpoints, do not force it into the lineage graph.

Do not invent missing source fields.

====================================
ROOT SOURCE RULE
====================================

A root source is any field endpoint that has outgoing propagating edges and no incoming propagating edge in the same graph,
or any endpoint that should be treated as a lineage start for the sample.

Examples from sample:
- FOS.API_MTM_REVAL.CUSTOMER_NUMBER
- FOS.API_MTM_REVAL.DEAL_NUMBER
- FOS.API_MTM_REVAL.DEAL_TYPE
- FOS.API_MTM_REVAL.MTM_AMOUNT
- FOS.API_MTM_REVAL.LAST_UPDATE_TS
- SESSION.T_GRAPH_STAGE.COL_A

====================================
FINAL TARGET RULE
====================================

A final target is any field endpoint that:
- is reachable in the propagation graph
- and has no outgoing propagating edge
or is otherwise considered a terminal persistent destination

Examples:
- INTERFACE.API_MTM_REVAL.CUST_NUM
- INTERFACE.API_MTM_REVAL_STAR.CUST_NUM
- INTERFACE.V_API_MTM_REVAL.CUST_NUM
- RPT.MTM_REVAL_AUDIT.CUST_NUM
- TABLE_B.COL_B

====================================
MULTI-PATH RULE
====================================

If the same final target is reachable through two distinct paths, both rows must be preserved.

Graph example:

SESSION.T_GRAPH_STAGE.COL_A -> TABLE_A.COL_A
SESSION.T_GRAPH_STAGE.COL_A -> TABLE_B.COL_B
TABLE_A.COL_A -> TABLE_C.COL_C
TABLE_C.COL_C -> TABLE_B.COL_B

Then lineage_path.tsv must include both:

SESSION.T_GRAPH_STAGE.COL_A -> TABLE_B.COL_B
SESSION.T_GRAPH_STAGE.COL_A -> TABLE_A.COL_A -> TABLE_C.COL_C -> TABLE_B.COL_B

This is required and is not duplication.

====================================
SAMPLE EXPECTATIONS
====================================

The implementation must support these path patterns from the sample:

1. View mapping:
FOS.API_MTM_REVAL.CUSTOMER_NUMBER -> INTERFACE.V_API_MTM_REVAL.CUST_NUM

2. View -> CTE -> Session path:
INTERFACE.V_API_MTM_REVAL.CUST_NUM -> BASE_DATA.CUST_NUM -> SESSION.T_MTM_STAGE.CUST_NUM

3. Session -> target table:
SESSION.T_MTM_STAGE.CUST_NUM -> INTERFACE.API_MTM_REVAL.CUST_NUM

4. Session -> target view:
SESSION.T_MTM_STAGE.CUST_NUM -> INTERFACE.V_API_MTM_REVAL.CUST_NUM

5. Session_all -> select star target:
SESSION.T_MTM_STAGE_ALL.CUST_NUM -> INTERFACE.API_MTM_REVAL_STAR.CUST_NUM

6. Graph multi-path case:
SESSION.T_GRAPH_STAGE.COL_A -> TABLE_B.COL_B
SESSION.T_GRAPH_STAGE.COL_A -> TABLE_A.COL_A -> TABLE_C.COL_C -> TABLE_B.COL_B

7. Audit path if represented safely in direct relationships:
SESSION.T_MTM_STAGE.CUST_NUM -> RPT.MTM_REVAL_AUDIT.CUST_NUM
SESSION.T_MTM_STAGE.DEAL_NUM -> RPT.MTM_REVAL_AUDIT.DEAL_NUM

8. Longer propagated paths:
FOS.API_MTM_REVAL.CUSTOMER_NUMBER -> INTERFACE.V_API_MTM_REVAL.CUST_NUM -> BASE_DATA.CUST_NUM -> SESSION.T_MTM_STAGE.CUST_NUM -> SESSION.T_MTM_STAGE_ALL.CUST_NUM -> INTERFACE.API_MTM_REVAL_STAR.CUST_NUM

====================================
CYCLE SAFETY RULE
====================================

Protect against cycles.

Requirements:
- do not allow infinite recursion
- keep a visited-path strategy, not just visited-node globally
- distinct paths should still be preserved when valid
- stop path expansion when the next endpoint would repeat a node already in the current path

====================================
EXPECTED IMPLEMENTATION BEHAVIOR
====================================

Before implementation:
- inspect reusable existing classes
- inspect current relationship_detail.tsv generation logic
- inspect docs/relationship_list_with_samples.md
- inspect expected/lineage_path.tsv
- summarize the inferred propagation rules

During implementation:
- reuse existing graph / chain / expander logic if already present
- add only minimal helper/service changes required
- add a dedicated new main for this task
- keep unsupported cases explicit

After implementation:
- write lineage_path.tsv to outputDir
- optionally compare it with expected/lineage_path.tsv
- report mismatches clearly

====================================
DELIVERABLES
====================================

Please implement:

1. a new main class for lineage_path generation
2. minimal supporting changes required
3. reuse current chain / graph / writer logic as much as possible
4. build lineage_path.tsv from relationship_detail.tsv direct relationships
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
- inspect current structure and reusable chain/graph components
- identify which direct relationship rows become propagation edges

Phase 2:
- load relationship_detail.tsv
- build field-level graph edges

Phase 3:
- identify roots and terminal targets
- expand paths with cycle protection

Phase 4:
- generate lineage_path.tsv
- compare with expected/lineage_path.tsv and refine mismatches

Do not redesign relationship_detail.tsv in this task.
