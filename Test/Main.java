# react-scripts → Next.js 16.2 Post-Migration QA Prompt: Preserve All Existing Functionality

How to use: Before QA, the user copies the migrated code into the working tree of the project's original branch, whose HEAD does not contain the migration, and leaves the changes uncommitted. Fill in the project name and path, then give this prompt to an AI that can access the repository. Compare the original code in HEAD with the final migrated working tree, including staged, unstaged, and untracked files. The original application does not need to be installed or started. Run only the migrated version. Keep all migration changes uncommitted throughout QA.

---

You are a QA engineer and code reviewer for React and Next.js applications.

Project `<PROJECT_NAME>` has already migrated from **CRA / react-scripts to Next.js 16.2.x**. This migration must preserve all existing functionality, business logic, data-processing rules, and user interactions. Code changes required for framework compatibility must preserve the original business outcomes.

Perform a **functional parity review and regression verification** of this project. Answer:

> Under the same permissions, configuration, input data, and user actions, does the migrated application preserve the original functionality and business results? Which rules or behaviors have changed?

## 1. Known State and Objective

- The CRA / react-scripts → Next.js 16.2.x migration is already complete. Review the migration results.
- **The user has prepared the workspace: HEAD contains the original code, the working tree contains the complete migrated code, and the migration changes have not been committed.** Verify this state before reviewing differences. Do not synchronize code or change branches on the user's behalf.
- **Known limitation: the original version can no longer install its dependencies and cannot be run for comparison.** Review original source/configuration, derive expected results from the original logic, and verify those expectations against the migrated version. Do not repair the original environment, install its dependencies, or start the original application.
- The user reports that dev pages generally look correct, with no obvious problems noticed. Dropdown options, list counts, and other detailed rules have not been checked exhaustively. Record this as user-provided evidence with unknown coverage, not proof of full parity.
- Verify exact Next.js, React, and React DOM versions from the current lockfile and installed dependencies. Do not downgrade or repeat the migration.
- **Mandatory requirement: the migration must not change existing functionality.** Preserve CRUD, searching/filtering, list and dropdown contents, pagination, defaults, validation, calculations, permissions, routing, API contracts, and state-retention rules.
- If hooks, Redux, Axios, forms, tables, or other implementations changed for compatibility, verify behavioral equivalence. New framework or dependency defaults do not automatically become new business requirements.
- Report differences with original behavior, migrated behavior, and impact. This task is QA only: do not automatically modify application code. Proposed fixes must restore original behavior.
- Deliver an **evidence-backed functional difference report**. Build, lint, and dependency checks support the review but do not replace business verification.
- Also verify completion of the migration's original technical objectives: remove active CRA infrastructure, preserve required configuration behavior, minimize business-code changes, and verify the existing dependency/security checks. Report these separately from functional parity so a successful build or dependency cleanup cannot hide a business regression.

Project inputs:

```text
Project name: <PROJECT_NAME>
Current project path: <MIGRATED_PROJECT_DIRECTORY>
Original branch name: <CURRENT_ORIGINAL_BRANCH; read and confirm from Git>
Original source baseline: current branch HEAD; record its full SHA
Migrated code: final working tree, including staged, unstaged, and untracked files; uncommitted
Available dev URL, test accounts, and test data: <REUSE IF ALREADY PROVIDED>
```

Start with available code, Git history, and test material. If accounts, source files, or browser capabilities are unavailable, complete the remaining feasible checks and identify exact verification gaps.

## 2. Establish the Comparison Baseline

The baseline is defined: **original code = current branch HEAD; migrated code = final working tree contents.** The user prepares this state. Do not require a migration commit or search for another comparison branch.

1. Read the branch name, full HEAD SHA, and working tree status. Check that dependencies/startup configuration in HEAD belong to the original application and the working tree contains Next.js 16.2.x. Record evidence; do not rely on the branch name alone.
2. Compare tracked files against HEAD. Also inspect staged changes, unstaged changes, and untracked files so new pages, configuration, shared components, and deleted files are included.
3. If a file has staged and unstaged changes, its final contents on disk are the migrated version. Use the index as supporting context; do not review only the staged version.
4. Identify moves and renames. If an original file is deleted and its replacement is untracked, map responsibilities and contents manually. Automatic rename detection does not cover every case.
5. Read original files with `git show HEAD:<file-path>` and migrated files from their current paths. Do not switch to the old version, overwrite working files, or install old dependencies. If a new path has no HEAD equivalent, determine whether it implements behavior from an original component.
6. Derive business expectations from HEAD source, configuration, lockfiles, and test assertions. Existing historical reports, screenshots, or API examples can supplement evidence, but obtaining them is not a prerequisite to starting.
7. Run verification only against the migrated working tree. Reading an original test assertion does not mean that test was executed or passed.
8. Distinguish migration regressions, display/interaction differences, and pre-existing issues. A framework upgrade or changed component default does not justify accepting changed functionality.

Use these read-only commands as appropriate:

```bash
git status --short --branch --untracked-files=all
git branch --show-current
git rev-parse HEAD
git diff --name-status --find-renames HEAD --
git diff --find-renames HEAD --
git diff --cached --name-status --
git diff --name-status --
git ls-files --others --exclude-standard
```

`git diff HEAD` shows final changes to tracked files relative to HEAD but does not show untracked file contents. Read the listed new source/configuration files separately. Plain `git diff` shows only unstaged differences and is not the complete migration. If required source/configuration is ignored by Git but referenced by the application, inspect it through relevant imports/configuration relationships. Do not indiscriminately collect dependency directories, build output, or sensitive files as source material.

During QA, do not run `git add`, `git commit`, `git reset`, `git restore`, `git clean`, `git stash`, branch switching, merges, synchronization, or other operations that change the working tree, index, or baseline. The uncommitted state is intentional and already allows the full migration to be reviewed.

If HEAD already contains migration code, changes are missing, or the baseline cannot be confirmed, report the mismatch and continue checks that remain valid. Do not silently substitute `HEAD~1` or another branch, and do not reset the workspace.

### Derive Verifiable Expectations from the Original Source

For each important rule, record the original file/function, input conditions, filtering/calculation steps, expected record IDs/fields/count/order/state, and migrated implementation. Establish expectations before observing migrated output. Do not infer expected original behavior from migrated results.

For example, with 10 fixed input records where 8 satisfy the original filter, derive exactly which 8 IDs should remain, then verify that the migrated implementation returns the same records and fields. This does not require running the original application or mean every dropdown must contain 8 or 10 options.

If behavior depends on an implicit original component default, inspect the old lockfile, available source/documentation for that exact dependency version, or historical evidence. If evidence is unavailable, mark the original behavior as unconfirmed. Do not substitute the new dependency's default or guess. State limitations for menu sizing, lifecycle behavior, asynchronous timing, and other runtime-dependent behavior.

## 3. Identify Changes That May Affect Business Behavior

Review modified, added, deleted, and moved business code and affected callers. Include:

- CRUD pages and shared business components.
- Custom hooks, effect dependencies, and cleanup.
- Redux Providers, store initialization, reducers, selectors, and actions.
- Axios instances, interceptors, API services, transformations, and error handling.
- Routes, navigation wrappers, login redirects, and permission checks.
- Formik, validation, defaults, and submission logic.
- Table columns, search conditions, sorting, pagination, and exports.
- Date, number, boolean, empty-value, and ID utilities.
- Shared-source resolution and changed defaults in upgraded UI/table/form libraries.

For each important difference, trace the complete behavior:

```text
User action → event handler / hook → state / service → API request → response processing → UI feedback
```

Explain the original behavior, migrated behavior, equivalence assessment, and supporting source or test evidence.

A code change does not automatically mean a behavior change. An unchanged business file does not prove parity: routing, Providers, configuration, dependencies, and mounting behavior can change its results.

### Check That the Migration Stayed Within Its Intended Scope

Classify significant changes separately as CRA-to-Next framework adaptation, React major-version compatibility (such as React 18 to 19), dependency compatibility, business behavior change, or unrelated refactoring. For changed business components, hooks, and packages, identify the specific compatibility reason and supporting evidence; mark an undocumented reason as unconfirmed rather than inventing one.

Inspect whether the migration replaced Axios with fetch, introduced Server Actions, moved existing business data fetching into Server Components, replaced Redux or Formik, redesigned authentication, broadly converted JavaScript to TypeScript, or unnecessarily rewrote/renamed CRUD components and hooks. These require a concrete compatibility justification. Report unjustified changes as migration-scope deviations, even if no functional regression has been demonstrated; do not automatically roll them back or label every implementation change a business defect.

Compare manifests and lockfiles for package replacements and unrelated bulk upgrades. Check compatibility and changed behavior of packages actually used, especially shared/internal UI packages, older styled-components, Formik, React Router, table/grid libraries, drag-and-drop libraries, and test packages. Verify resolved versions and peer-dependency issues where the installed tree is available. Do not upgrade packages during this QA.

### Inventory Business Rules Page by Page

List counts and dropdown options are examples, not the complete scope. Inventory actual features from original routes, pages, and shared components. For each page, identify:

1. Inputs: user, role, URL parameters, search conditions, form values, API data.
2. Rules: defaults, filtering, calculations, sorting, pagination, permissions, validation, state transitions.
3. Outputs: record membership/order, displayed fields, totals/aggregates, button states, submission parameters.
4. Subsequent actions: paging, filtering, resetting, saving, deleting, returning, reopening, retrying after failure.

Locate rules in actual components, hooks, selectors, services, and utilities, then find the migrated equivalents. Establish page/shared-logic coverage before investigating changed paths and paths affected by common dependencies. Do not mechanically enumerate every input combination.

Distinguish "no difference found" from "not reviewed." List uncovered pages/rules. Do not mark a feature passed because a keyword search found nothing or its business file was unchanged.

## 4. Functional Checks

Check features that actually exist. Do not invent requirements for missing features.

| Area | Compare |
| --- | --- |
| Lists and search | Defaults, initial loading, search/reset, filter combinations, empty conditions, trigger timing, missing/duplicate requests |
| Sorting and pagination | Default sort, field mapping, zero/one-based page numbers, page size, total, page reset after filter changes |
| Dropdowns and selection controls | Complete option count, label/value, order, default selection, disabled options, search, clear, dependencies, incremental loading; total options versus visible rows |
| Create | Defaults, required fields, disabled buttons, payload, submission count, success feedback, list refresh |
| Edit | Detail population, read-only/hidden fields, record ID, unchanged fields, state after saving |
| Delete | Target records, single/bulk deletion, confirmation/cancel, permissions, refresh, failure feedback |
| Forms and dialogs | Validation rules/timing, messages, cancel/close/reopen, dirty-value retention/reset, duplicate-submission prevention |
| Data display | Missing columns/fields, dates/time zones, amounts/precision, enum labels, display/submission of null, empty string, zero, false |
| Import, export, download | Entry points, permissions, file names/formats, field order, filter scope, contents, error feedback |
| Failure paths | Empty results, validation failure, 401/403, timeout, backend errors, repeated clicks, loading recovery, retry, state retention |

Watch for removed branches, changed defaults, removed validation, rewritten hooks, consolidated requests, changed catch behavior, response mapping changes, and component replacements.

### List Counts and Record Contents: High Priority

**Explicit acceptance requirement: under identical conditions, if the original API returned 100 records and the original page displayed those 100 records under its existing rules, the migrated version must preserve and display the same 100 records.** It must not drop to 80, duplicate to 200, truncate records through a new component default, or merely display a total label of 100 while records are missing.

Report this check separately for the relevant page, identifying which original behavior applies:

| Original behavior with the same input and conditions | Required migrated result |
| --- | --- |
| API returns 100 records; the original list displays all 100 on one page | Preserve those 100 records and original page size/display behavior; do not silently change to 10 or 20 per page |
| API returns 100 records; client-side pagination uses 10 per page, total 100, and 10 pages | Preserve 10 records per applicable page, total 100, 10 pages, and access to every original record across pages |
| Server returns 10 current-page records with total=100; the page displays those 10 and total 100 | Preserve current-page count, total, page count, and pagination requests; do not require all 100 records in one response |

These are acceptance definitions, not measured project results. Preserve the original "100 records displayed" behavior. Use HEAD source to determine whether 100 means one-page records or the pagination total. Preserve original business filtering and its results; do not remove filtering to force 100 or introduce new filtering.

Trace API request/response, transformed collection, table-bound data, table-internal filtering/pagination, displayed total, and every record reachable through paging/scrolling. Compare business IDs, occurrence counts, and relevant fields as well as totals. Different records or duplicates must not make up 100. With virtualization, inspect the complete logical list and reachability rather than simultaneously mounted DOM rows.

When execution is available, verify the migrated page using a fixed 100-record fixture matching the original field structure. Keep actual processing/table logic; fix only external input. Otherwise, complete the source trace and mark the item "runtime verification pending." A normal-looking dev page is not acceptance evidence.

For tables, ordinary lists, search results, dialog lists, and other actual display areas, trace data in its real processing order. Client-side and server-side pagination may have different pipelines.

| Stage | Compare between implementations |
| --- | --- |
| Query input | Defaults, date ranges/boundaries, role/region, hidden filters, meaning of empty string/0/false/null |
| Request | Endpoint, filters, page/pageSize, offset/limit or cursor, sorting and defaults |
| Response | Current records/items count, meaning of server total, pagination metadata, stable business IDs |
| Frontend processing | Mapping, filtering, deduplication, merging, grouping, sorting, slicing; sequence/conditions; counts and IDs before/after each stage |
| State | Replace/append, cache/refresh, stale responses, duplicate requests/appends, effects of in-place array changes |
| Display | Bound data, table-internal filtering/pagination, displayed total, page count, current range |

Define count semantics: server matching total, current response count, loaded count, filtered count, current-page count, viewport-visible rows. A one-page response with a larger total can be correct. Group headers, summary rows, and virtualized DOM rows are not business-record totals.

Check:

- Changes between client-side/server-side pagination, accidental second pagination of a server page, or filtering only loaded records while implying all records were filtered.
- Default/available page sizes, zero/one-based numbering, offsets, last-page boundaries, and total incorrectly replaced by current-page length.
- Requests and reset/retention after initial load, page/page-size changes, filtering, clearing, sorting, refreshing, and returning.
- Cursor, hasMore, termination, append/replace, and deduplication in incremental loading/infinite scrolling.
- Page, total, and refresh after deleting the last item on the last page, creating an item, or editing it out of the current filter. Preserve original rules instead of inventing a page transition.
- Row IDs, keys, and deduplication keys that merge different records, reuse wrong rows, or mismatch selected records.
- Sorting before/after filtering/pagination; numbers/dates accidentally sorted as strings. Check original order guarantees and tie behavior; do not treat unspecified backend order as a proven frontend regression.
- Stale responses after rapid searches/page changes, repeated refreshes, and original loading/data-retention behavior after failures.

Equal counts still require content comparison. Use stable IDs to identify missing/extra records, each ID's occurrence count, relevant fields, and required order. Comparing only length or sets misses substitutions and duplicates. If no unique ID exists, derive a business-appropriate composite key and explain it.

Choose small fixed datasets relevant to the rules: empty, exactly one page, more than one page, incomplete last page, and different counts after filtering. For page size P, select useful sizes such as 0, 1, P, P+1 according to risk. Include relevant statuses, date boundaries, or duplicate labels with different IDs. Do not run every combination. Derive expectations from original rules, not migrated output.

When possible, feed fixed inputs to the real migrated implementation and compare intermediate/final results with source-derived expectations:

- Verify actual request parameters against original code before using fixed responses. A fixture must not hide changed request parameters.
- With browser access, run only the migrated application and verify pagination, counts, filters, and operations against original rules.
- Without a browser but with code execution, invoke real transformation functions, selectors, or relevant component tests in the migrated test environment. Compare expected IDs/fields/count/order. Keep auxiliary scripts in a temporary directory. Do not install the old project or replace the implementation under test with rewritten logic.
- With source access only, report exact condition changes, triggering inputs, and derived outputs as static analysis.

Example only: 25 input records should become 20 under the original filter, but migrated code returns 18. Locate missing records at the filter step. If all expected 20 remain but original page size 10 became 20, inspect pagination configuration. Original counts here are derived expectations, not old runtime measurements or universal requirements.

### Apply the Same Method to Other Business Rules

Identify and compare actual rules for:

- Display: mappings, enum text, empty-value placeholders, precision, dates/time zones, truncation.
- Calculations: amount/quantity totals, counts, grouping, rounding, and current-page/selected/all-matching scope.
- Interaction: button visibility/enabled state, single/multiple selection, select-all scope, cross-page selection, bulk-operation targets.
- Forms: defaults, required fields, ranges, dependencies, clearing, validation timing, submitted empty values and types.
- State after operations: filters, page, selections, dialogs, drafts after success, failure, cancellation, return.
- Export: current page versus all filtered/selected records, fields/order, counts, contents.

For each rule, record original implementation, migrated implementation, equivalence assessment, and evidence. Opening a list or clicking Save does not replace comparing inputs, conditions, and results.

### Dropdown and Control Details: High Priority

The user is concerned that a dropdown which originally contained 10 options might now contain 8 or 20 even though the page looks normal. These numbers are examples. Obtain the actual expectation from the original baseline; do not set every control to 10 options.

Distinguish:

- Records returned by the current API response versus the response's pagination total.
- The complete option collection after mapping, filtering, and deduplication for the role/search conditions; separately count disabled and selectable options.
- Rows visible at once when the menu opens, affected by menu height, row height, scrolling, and virtualization.
- For a table's page-size dropdown: available page-size values, default page size, and actual requested/displayed counts after selection.

Eight visible rows do not prove that two of ten options were lost. Ten options in both implementations do not prove their contents are the same.

**Step 1: Compare the source logic.** Identify selection controls, shared wrappers, and data sources on business pages. Prioritize changed shared components and their callers. Inspect dependencies/configuration even when the business component is unchanged.

Compare:

- Request limit, page, pageSize, search terms, pagination loading, and merge logic.
- Filtering, mapping, slicing, sorting, deduplication, permissions, region, and status conditions.
- Added, removed, or duplicated options such as All, Please select, and empty options.
- Option field mappings, label/value/key, string/number types, selection equality rules.
- Explicit props, wrapper defaults, upgraded dependency defaults, and relevant menu styles.
- Async loading, search debouncing, stale response overwrites, and repeated effects that omit or duplicate options.

**Step 2: Derive expectations from the original source and verify only the migrated version.** Define role, region, feature flags, parent selection, search text, and fixed API data. Derive the expected full option collection, order, default selection, and submitted value from original code/configuration. Replay that fixed response in the migrated application while retaining its real filtering, mapping, and control logic. Identify missing evidence where the source cannot establish expectations. Distinguish real backend data changes from logic changes.

- Wait for migrated loading to finish, open the dropdown, and compare the complete collection with expected count, label, value, disabled state, duplicates, and order.
- Report specific missing, extra, duplicate, or changed options, not only counts.
- Compare both directions: expected but absent from migrated results, and present in migrated results but not expected. Separately check text/state for matching values.
- Verify initial selection, displayed selection, form state, and actual submitted value against original rules. Identical text can still submit a different value.
- Check initial opening, scrolling to the end, searching for expected last options, clearing search, closing/reopening, and clearing/reloading dependent controls after parent changes.
- A virtualized list may mount only some options in the DOM. Do not treat a single DOM count as the total. Inspect complete options data, scroll traversal, and incremental-loading results until loading ends, confirming reachability.
- Screenshots can document migrated menu height, row height, truncation, and obstruction. Compare visual differences only when reliable original screenshots or explicit original configuration exist. Otherwise mark original visual behavior unconfirmed. Screenshots alone cannot prove hidden options, values, or submission behavior match original rules.

Classify differences as collection changes, submitted-value changes, visible-row/style changes, or backend data changes. If 10 options are expected and only 8 are visible initially but all 10 can be reached and selected, do not report two missing options. Only assert a visible-row-count change when original evidence establishes that 10 were previously visible at once. If two expected options cannot be found or selected, investigate data/interaction behavior.

Apply this method to page size, default sorting, date formats, default values, and button conditions. Label source-only conclusions as static analysis. For executed checks, state whether the migrated result matches the expectation derived from original source. Do not claim a runtime comparison of both applications.

## 5. Review APIs, Permissions, Routing, and State Separately

### API Contracts

For changed calls and calls affected by configuration, compare:

- HTTP method, browser request URL, and final backend path after base URL/rewrite handling.
- Query names, types, encoding, defaults, and empty-parameter handling.
- Body fields, structure, types, date formats, nulls, and omitted fields.
- Required headers, token/cookie behavior, and credentials settings. Redact sensitive values in evidence.
- Response parsing, success/error interpretation, interceptors, token refresh, and retries.
- Request count/order per user action, duplicate submissions, and stale responses overwriting state.

Inventory all actual original proxy paths and rewrite rules. Compare migrated request targets individually and preserve paths, parameters, and backend contracts.

### Authentication and Permissions

- Login, logout, session restoration, expiry handling, and return navigation after login.
- Role-based menus, buttons, pages, and permitted business operations.
- Protection of directly opened restricted URLs.
- Do not use hidden menus alone as evidence that access control is preserved.
- Distinguish existing backend permission problems from migration-introduced issues.

### Routing and Page State

- Paths, path parameters, query strings, hashes, and non-ASCII/special-character parameters.
- In-app navigation, direct links, refresh, forward/back behavior.
- Equivalent handling of business data previously passed through navigation state, especially list-to-detail/edit flows.
- Base paths and relative links affecting destinations or API addresses.
- Filter, page, sort, and selection retention after opening a detail page and returning.
- Provider/layout/page-wrapper/key/mounting changes that unexpectedly reset Redux, forms, or page state, or retain state that should be cleared.
- Identify the actual routing approach before judging it. Retaining React Router is not itself a defect.

### Indirect Changes Caused by Framework Adaptation

- Client/Server boundaries, initial rendering, and hydration affecting browser storage, permissions, initial values, or usability.
- Effect timing, subscription cleanup, duplicate requests, and races. Distinguish development behavior, production behavior, and actual business impact.
- If caching/data-fetching changed, verify correct data after saves, deletes, and account changes.
- Environment variables, region, feature flags, shared-source resolution, and component defaults that indirectly change functionality.
- Report risks grounded in actual code or reproducible paths, not unrelated generic Next.js concerns.

### Preserve the Original Configuration and Shared-Code Behavior

Read `config-overrides.js` and any related CRA build/proxy/environment scripts from HEAD, even if the migrated working tree deletes them. Inventory every behavior, map it to its migrated implementation, and record verification evidence. Include aliases, loaders, transforms, and other discovered behavior beyond the examples below. Deleting an override file is not evidence that its responsibilities were preserved.

| Original responsibility, where present | Required QA check |
| --- | --- |
| Public/deployment path | Compare original public-path behavior with actual basePath, assetPrefix, deployment routing, and DEPLOYMENT_LOCATION handling. Check direct nested links, refresh, JS/CSS/chunks/images, navigation, and downloads under the configured deployment prefix. Do not treat basePath and assetPrefix as interchangeable. |
| Environment injection | Trace each used value from its source to its consumer, including defaults and environment selection. Examples include REGION, ROUTER_BASE_PATH, CLIENT_ID, HSBC_ENV, DEPLOYMENT_LOCATION, and build metadata. Verify the correct API target, region, permissions, and flags; compare values safely without exposing secrets. |
| Development proxy | Map every original route's target, path rewrite, and origin/host behavior to the migrated route/proxy implementation. For routes such as /api, /api-staff, or /none only when present, verify final upstream paths, methods, query/body, authentication forwarding, and error behavior. A matching browser URL alone does not prove an equivalent upstream request. |
| Shared source outside src | Inspect paths such as ../shared/src, workspace/local packages, symlinks, aliases, and transpilation rules. Confirm the migrated app resolves and includes the intended shared business code rather than a stale build, alternate package version, stub, or fallback. A successful import alone is insufficient. |
| Compression and asset delivery | If a production CompressionPlugin or precompressed artifacts existed, determine how the migrated build/runtime/gateway satisfies the actual delivery requirement. Check available packaging/server configuration and response evidence. Do not insist on retaining the old plugin when the requirement is already met elsewhere; mark unavailable platform evidence as unverified. |

Check when each environment value is resolved: build time, server runtime, or browser initialization. Public NEXT_PUBLIC_ values can be embedded during the build and remain fixed in that artifact; verify any build-once/promote-to-another-environment workflow and dynamic environment lookups against the project's intended behavior. Do not assume changing deployment variables updates an already-built client bundle. See the [environment-variable guide](https://nextjs.org/docs/app/guides/environment-variables).

Trace all uses of any real CLIENT_SECRET or other secrets. Check NEXT_PUBLIC_ variables, next.config env entries, custom replacements, and data sent to the browser; an unprefixed name alone does not establish secrecy. See the [next.config env reference](https://nextjs.org/docs/app/api-reference/config/next-config-js/env). Inspect existing built client assets/payloads when available, without printing secret values. Do not introduce NEXT_PUBLIC_CLIENT_SECRET or reproduce a historical client-secret exposure to claim parity. Record pre-existing exposure separately from any newly introduced exposure, and recommend the minimum required correction without redesigning authentication during QA.

### Preserve Client Boundaries, Providers, and Styling

- For App Router code, inspect whether client boundaries at suitable page/feature/Provider entry points preserve the existing component tree. Flag blanket use-client additions or hook rewrites without a demonstrated need as scope concerns, not automatic functional failures. Do not require the directive on every imported child or utility.
- A use-client boundary does not by itself exclude initial server prerendering. Check browser-only module imports and window/document/storage access along initial-load and refresh paths, in addition to client navigation. Verify initial values, permissions, Providers, and hydration behavior. See the [Server and Client Components guide](https://nextjs.org/docs/app/getting-started/server-and-client-components).
- Check CSS/SCSS, styled-components, themes, layout, company UI packages, and actual form/grid components. Compare style-loading/order, initial-load styles, field/control visibility, dialog/dropdown positioning, and interaction after navigation. Verify the project's actual styling integration rather than imposing a new styling architecture. Where old visual evidence is unavailable, separate explicit configuration differences from unconfirmed visual differences.

## 6. Regression Verification

### A. Reuse Existing Business Tests

Read original test inputs/assertions and check whether migrated tests preserve those expectations. Run relevant tests available in the migrated environment. Do not install the old project or execute its original test suite. An original test file does not prove that test historically passed.

- Do not delete/skip tests, weaken assertions, bulk-update snapshots, or change old expectations to match new behavior and hide regressions.
- Check for missing cases/assertions and original tests excluded by configuration.
- Compare original test discovery/configuration with the migrated runner, including setup files, aliases, asset/style mocks, and applicable test/report scripts. Report discovered, executed, failed, and skipped counts separately when available. A green command that discovers no business tests is not migration evidence.
- Classify failures: functional regression, test configuration, mock/runtime environment, historically existing failure, or insufficient evidence.
- Call a failure pre-existing only with historical reports or other concrete evidence. The old installation failure is not evidence of a business failure.
- A Jest/DOM/mock setup error does not by itself prove a business defect. A skipped test is not a pass.
- If test infrastructure needs adaptation, report specific recommendations. Do not rewrite project tests just to obtain passing results.

### B. Target Verification at Affected Features

Reuse migrated tests and available browser automation. Verify high-risk changes first, then representative CRUD flows. All runtime checks target the migrated version.

For each scenario, record rules and expectations supported by original source/configuration/assertions, then execute the migrated version with matching roles, configuration, and fixed data:

```text
Original source and assertions → expectations for fixed inputs → migrated actual results → differences and evidence
```

Verify request parameters, transformed data, UI feedback, and subsequent state separately. Use independent test records or resettable fixtures for CRUD so one scenario's writes do not contaminate the next.

You may mock migrated API responses to fix inputs or isolate unavailable external dependencies. Keep the business logic under review intact, and state that mocked tests do not prove live backend integration. Without a browser, continue source review and executable migrated logic tests, listing UI interaction checks as pending. Do not stop other QA because the old version cannot be installed.

Use the designated test environment and test records for CRUD verification. Do not write to production business data.

### C. Use Build Checks as Supporting Evidence

Read the migrated scripts before running relevant build, lint, type checks, or representative production-mode verification. Follow the actual deployment/startup model rather than assuming a universal command.

Inspect each environment script and its configuration, including build:sit, build:uat, build:prd, and build:prod only where the project defines them. Preserve each environment's meaning; do not merge environments or infer that dev success verifies another configuration. Compare relevant Docker/Jenkins/CI commands, runtime requirements, artifact paths, deployment prefixes, and the hosting model expected by the existing pipeline. Record configuration review separately from an executed build. Inspect chained commands first and run only local validation, not deployment or publishing.

Apply version-specific checks to the installed Next.js 16.2.x version:

- Determine the actual bundler in dev and build scripts. Next.js 16 defaults to Turbopack; retained webpack settings are not proof that the active bundler implements the old aliases, loaders, shared-source, or plugin behavior. Check the actual flags and configuration without changing bundlers during QA.
- Next.js 16 removed next lint, and next build does not perform linting. Check the project's actual linter command and report its result separately.
- For server request APIs or page/route props actually introduced by the migration, verify the applicable asynchronous access requirements for cookies, headers, params, and searchParams. Do not apply server-prop requirements indiscriminately to browser routing hooks. Check local/CI Node and, if relevant, TypeScript against the installed framework's requirements.

Verify these points with the [Next.js 16 upgrade guide](https://nextjs.org/docs/app/guides/upgrading/version-16) and version-matched installed documentation where available. Documentation upgrade commands are not authorization to upgrade or run codemods in this QA.

Use the existing dev feedback to avoid unnecessary repeated troubleshooting. Investigate built-mode behavior when a specific remaining risk requires it. Do not expand the task into unrelated platform remediation.

Record the initial worktree status before validation. If a command would rewrite tracked configuration or source, use an isolated copy containing the exact migrated working tree, including untracked files, or report the check as unavailable. A copy of HEAD alone is the old application and is not the correct test target. Distinguish QA-generated artifacts from the user's migration changes; do not reset or overwrite user work to clean them up.

Record commands actually executed, results, environment, and evidence. Never mark an unexecuted check PASS.

### D. Verify CRA Removal and Existing Security Objectives

Check that react-scripts and react-app-rewired are absent from current dependencies, devDependencies, resolved dependency records, active npm scripts, build commands, and CI/CD execution paths. Inspect the installed dependency tree when available. For an npm project, suitable read-only checks include:

```bash
npm ls react-scripts --all
npm ls react-app-rewired --all
```

Use the repository's package-manager equivalent when needed. Inspect output and errors to distinguish a confirmed absence from an unavailable/incomplete installation; do not decide from an exit code alone. Do not install the original version. Historical documentation or fixtures mentioning CRA are not the same as active dependencies or executable CRA infrastructure.

Verify that obsolete config-overrides/devServer/public-path machinery is no longer active and that every required behavior has a mapped replacement. Report any retained active CRA infrastructure and its reason; do not remove it automatically during QA.

Use existing dependency/security/Sonar reports or the project's available validation workflow to check remaining and newly introduced findings. Do not run automatic dependency fixes or unrelated upgrades. Report removed vulnerabilities only when comparable original and current evidence supports that conclusion; an old lockfile alone does not prove an old scan result. When a scan cannot run, record it as unverified. Migrating to Next.js or removing a package does not by itself prove the original security objective was met.

## 7. Scope and Working Rules

- Complete source review and feasible verification before reporting. Do not deliver only a generic checklist.
- Perform QA only. Do not automatically modify business code, dependencies, lockfiles, or project test configuration.
- Do not rewrite CRUD, hooks, Redux, Axios, authentication, or routing architecture.
- Recommendations must restore original functionality, without new business rules or unrelated refactoring.
- Preserve uncommitted work. Read old files through Git history or an isolated directory; do not switch/overwrite the current working tree to install the original project.
- Preserve the prepared HEAD, index, and working tree. Do not automatically stage/commit, reset, or resynchronize the migration changes.
- The original version is read-only evidence. Do not repair/install/start it. Temporary migrated verification cases are allowed, without modifying application code or existing test expectations.
- Continue other checks when one input is missing, and consolidate blockers in the report.

## 8. Report Format

Write the report in English. Start by answering: **Which functional changes were found, and which areas remain unconfirmed?**

### 8.1 Scope and Conclusion

Include:

- Current branch, full original HEAD SHA, and coverage of staged/unstaged/untracked files. Confirm that the migrated target was the final working tree.
- The old installation limitation, sources of original behavior evidence, and which results were measured only in the migrated version.
- Actual Next.js, React, and React DOM versions.
- Reviewed functionality, executed verification, and remaining gaps.
- One conclusion with its evidence scope:
  - Functional changes found.
  - No functional changes found within the verified scope.
  - Insufficient evidence to determine parity.

Do not claim complete functional equivalence merely because the build succeeds, dev looks normal, or some tests pass.

### 8.2 Functional Differences

| ID | Page / feature | Original behavior | Migrated behavior | User / business impact | Difference category | Evidence and verification status |
| --- | --- | --- | --- | --- | --- | --- |

Categories: functional regression, display/interaction difference, migration-scope deviation, migration-completion/security finding, pre-existing issue, or suspected risk. List behavior differences explicitly; do not independently declare them acceptable. Keep implementation-scope and migration-completion findings separate from proven business regressions.

Verification statuses: static difference confirmed, migrated runtime matches original rule, migrated runtime violates original rule, original behavior unconfirmed, migrated verification pending. Explain static inference and triggering conditions; never present them as measurements from the old application.

### 8.3 Issue Details

For each important issue, provide:

1. Priority and concrete impact rationale.
2. Original/migrated files, functions, and precise locations.
3. Triggering conditions or reproduction steps.
4. Expected versus actual result.
5. Root cause and source/test/request evidence; explicitly state if not reproduced.
6. The smallest proposed fix that restores original behavior.
7. Targeted verification for that fix.

### 8.4 Verified and Pending Checks

| Feature | Comparison basis | Verification method | Result | Uncovered area / next step |
| --- | --- | --- | --- | --- |

Separate static review, user-reported dev testing, and tests actually executed during this QA. Populate only real evidence; do not prefill passing statuses.

List a small set of high-value manual checks, prioritizing substantial changes, permissions/data writes, and gaps in automated evidence. Do not repeat generic tests where sufficient evidence already exists.

### 8.5 Control Detail Comparison

For important reviewed dropdowns and similar controls, report actual results. Use "not verified" for unavailable data.

| Page / control | Role and data conditions | Original expected / migrated total and selectable counts | Missing / extra / duplicate items | Label / value / default differences | Visible rows and scrolling reachability | Evidence and conclusion |
| --- | --- | --- | --- | --- | --- | --- |

Match options using stable business values/IDs. Separately check duplicate values, order, text, and disabled state. State whether fixed responses were used and whether search, async loading, selection, and submission were executed.

### 8.6 List Data Flow and Business Rules

For important lists, compare stages and state their order, pagination mode, and count definitions. Do not present unmeasured values as actual results.

Report the relevant "API returns 100 records; original page displays 100 records" scenario separately: original-rule evidence, migrated actual record count, displayed total, current-page count, all records reachable through paging/scrolling, missing/duplicate IDs, and verification method. Use "not measured" when not executed; do not copy expected 100 into an actual-results cell.

| Page / scenario | Stage and count definition | Original source-derived expectation and basis | Migrated result and verification method | Missing / extra / duplicate IDs or field differences | Source and evidence |
| --- | --- | --- | --- | --- | --- |

Also report other identified business rules:

| Page / feature | Rule or triggering condition | Original behavior | Migrated behavior | Equivalence assessment and method | Coverage gaps |
| --- | --- | --- | --- | --- | --- |

Identify the first stage where data diverges and its ultimate business impact. State whether inputs, roles, and configuration were equivalent. Clearly separate source-derived counts from runtime measurements.

### 8.7 Migration Requirement Coverage

Summarize the technical migration requirements separately from the functional findings. Cover minimal business-code changes, React/dependency compatibility, CRA removal, each original override behavior, client boundaries/Providers, styling, environment builds/CI, and the existing security objective.

| Requirement / original responsibility | Original HEAD evidence | Migrated implementation | Verification and result | Remaining gap / smallest recommendation |
| --- | --- | --- | --- | --- |

Use explicit outcomes such as verified, static review only, failed, unverified, or not applicable with a reason. Identify actual React and dependency changes separately from CRA-to-Next changes. Reference the relevant functional finding where a configuration or dependency difference affects business behavior; do not count a build or security pass as proof of functional parity.

## 9. Final Execution Instructions

Start reviewing source and migration differences, complete all feasible regression verification, and deliver the report.

Use **read-only inspection of the original version and runtime verification of the migrated version**. The original installation failure is a known constraint, not a reason to stop QA or repair the old project.

Compare the user-prepared **original code in HEAD against the current uncommitted migrated working tree**, and leave it uncommitted. Include explicit list-count parity verification: when original rules display the 100 records returned by the API, preserve those same records and display rules after migration.

Keep the central question in view: **For the same user, permissions, data, and actions, are the business results preserved after migration?**

**Acceptance principle: migrating from react-scripts to Next.js 16.2.x must preserve all existing functionality.** Any change to counts, contents, rules, permissions, outcomes, or state must have an explicit finding and evidence. Report every verification gap honestly.

For framework compatibility questions, consult the [Next.js 16 upgrade guide](https://nextjs.org/docs/app/guides/upgrading/version-16) and [Next.js 16.2 release notes](https://nextjs.org/blog/next-16-2), checking applicability to the locked version. Business expectations remain grounded in this project's original implementation, tests, and existing requirements.
