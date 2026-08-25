
# cmt-common-entitlement-ui: CRA / react-scripts → Next.js 15 + React 19 Minimal Migration Prompt

You are migrating the existing React application **`cmt-common-entitlement-ui`** (Project B) from Create React App / `react-scripts` / `react-app-rewired` to Next.js.

There are two proven references available:

1. **Project A — `cmt-oms-ui-equities-order-entry` — existing production/business Next.js application**
   - Next.js `15.2.8`
   - React `19`
   - React DOM `19`
   - Redux / React Redux
   - Axios
   - AG Grid
   - styled-components
   - company common UI libraries
   - SIT / UAT / PRD / PROD build scripts
   - existing company deployment conventions

2. **Company Next.js template — `cmt-ui-nextjs-template`**
   - Next.js `15.2.8`
   - React `19`
   - React DOM `19`
   - company-standard build scripts
   - ESLint / Jest / TypeScript / PostCSS configuration
   - company deployment/environment conventions

Use **`cmt-oms-ui-equities-order-entry` first as the real-world implementation reference**, and use **`cmt-ui-nextjs-template` as the framework/configuration reference**.

---

# Project Mapping

```text
Project B / migration target:
cmt-common-entitlement-ui

Primary reference / real business Next.js project:
cmt-oms-ui-equities-order-entry

Secondary reference / company Next.js template:
cmt-ui-nextjs-template
```

Reference priority:

```text
1. Preserve cmt-common-entitlement-ui business behavior
2. Follow cmt-oms-ui-equities-order-entry for proven runtime/business patterns
3. Follow cmt-ui-nextjs-template for framework/configuration conventions
4. Use standard Next.js behavior only where neither reference project provides an answer
```

---

# Current Project B — `cmt-common-entitlement-ui`

`cmt-common-entitlement-ui` currently uses approximately:

```text
React 18
React DOM 18
react-scripts 5
react-app-rewired
config-overrides.js
react-router-dom 6
Redux / React Redux
Axios
Formik
styled-components
custom hooks
CRUD pages
company common UI libraries
custom build scripts
custom environment configuration
custom proxy configuration
custom base/public path
shared source outside normal src folder
```

`cmt-common-entitlement-ui` also has a custom CRA override layer similar to:

```text
react-scripts
    ↓
react-app-rewired
    ↓
config-overrides.js
    ↓
custom webpack/devServer behavior
```

The goal is to replace this with Next.js while preserving the existing business application behavior.

---

# Main Objective

Migrate **`cmt-common-entitlement-ui`** from:

```text
React 18
+ react-scripts
+ react-app-rewired
+ config-overrides.js
```

to:

```text
Next.js 15.2.8
+ React 19
+ React DOM 19
```

with the **smallest possible business-code change**.

The primary reasons are:

1. completely remove `react-scripts`
2. remove `react-app-rewired`
3. remove vulnerable CRA transitive dependencies
4. align Project B with the company's proven Next.js platform
5. preserve the existing CRUD application behavior

This is a **platform migration**, not a redesign or React refactoring exercise.

---

# Critical Migration Principle

The desired migration is:

```text
CRA infrastructure
        ↓
Next.js infrastructure
```

while keeping:

```text
existing CRUD logic
existing hooks
existing Redux logic
existing Axios services
existing forms
existing tables
existing modals
existing validation
existing business components
```

as unchanged as reasonably possible.

Do NOT turn this into a "rewrite everything using modern Next.js patterns" project.

---

# 1. React 18 → React 19 Strategy

**`cmt-oms-ui-equities-order-entry`** and **`cmt-ui-nextjs-template`** both use:

```text
Next.js 15.2.8
React 19
React DOM 19
```

Therefore, Project B should target the same platform baseline unless an actual dependency compatibility blocker is found.

Upgrade:

```text
React 18      → React 19
React DOM 18  → React DOM 19
```

However:

**React 19 upgrade is compatibility-only.**

Do NOT refactor existing React code merely because React is moving from 18 to 19.

Existing code such as:

```js
const [data, setData] = useState([]);

useEffect(() => {
  loadData();
}, []);
```

should remain unchanged unless there is a real compile-time or runtime compatibility issue.

Do NOT unnecessarily rewrite:

- `useState`
- `useEffect`
- `useMemo`
- `useCallback`
- `useRef`
- custom hooks
- Redux hooks
- business components
- CRUD state handling

Clearly separate:

```text
A. changes required for CRA → Next.js
B. changes required for React 18 → React 19 compatibility
```

Do not mix the two categories in the final report.

---

# 2. Dependency Compatibility Check Before Editing

Before modifying Project B, compare Project B dependencies against:

1. Project A
2. company Next.js template

Classify Project B dependencies into:

```text
Already proven with React 19 / Next 15
Needs version alignment
Needs compatibility verification
Can be removed with CRA
Potential migration blocker
```

Pay special attention to older or Project-B-specific libraries such as:

- `react-beautiful-dnd`
- older `styled-components`
- Formik
- `react-router-dom`
- company internal UI packages
- custom shared libraries
- testing packages
- any package with React 18-only peer dependencies

Do not replace a package merely because it is old.

Only replace or upgrade it if:

1. it prevents Next.js 15 / React 19 from building or running, or
2. the target company projects already use a compatible replacement/version.

For each changed package, explain the exact compatibility reason.

---

# 3. Preserve Existing React Business Code

Do NOT unnecessarily rewrite:

- `useState`
- `useEffect`
- `useMemo`
- `useCallback`
- `useRef`
- custom hooks
- Redux reducers
- Redux selectors
- Redux actions
- Axios calls
- API service classes
- CRUD logic
- validation logic
- forms
- tables
- modals
- shared business components
- utility functions
- existing state management

Existing CRUD components should remain as close to their current implementation as possible.

Do NOT replace Axios with `fetch`.

Do NOT introduce Server Actions.

Do NOT redesign the data-fetching architecture.

Do NOT convert working Redux logic to another state-management solution.

---

# 4. `'use client'` Strategy

If Project A uses the Next.js **App Router (`app/`)**, preserve Project B's current client-side React behavior using appropriate client boundaries.

For existing CRUD features that use:

- `useState`
- `useEffect`
- `useMemo`
- `useCallback`
- browser events
- `window`
- `document`
- `localStorage`
- `sessionStorage`
- Redux Provider / Redux hooks
- client-side routing
- client-side form state

use:

```js
'use client';
```

at an appropriate **top-level page or feature boundary**.

Preferred structure:

```text
Next.js page / client boundary
        ↓
existing Project B CRUD component tree
        ↓
existing hooks
        ↓
existing Redux
        ↓
existing Axios services
```

Example:

```jsx
'use client';

import ExistingUserManagement from '@/components/ExistingUserManagement';

export default function Page() {
  return <ExistingUserManagement />;
}
```

Do NOT add `'use client'` blindly to every `.js`, `.jsx`, `.ts`, or `.tsx` file.

Do NOT add `'use client'` unnecessarily to:

- constants
- utility files
- pure helper functions
- API helper modules
- server-safe configuration
- every child component

Prefer the minimum number of client boundaries that allow the existing React tree to remain unchanged.

Do NOT rewrite hooks merely to avoid `'use client'`.

---

# 5. Use Project A as the Primary Target Architecture

Before changing Project B, inspect Project A and identify:

- `src/app` or `src/pages`
- `next.config.*`
- `package.json`
- `tsconfig.json`
- ESLint configuration
- Jest configuration
- PostCSS configuration
- environment variable handling
- base path handling
- deployment-location handling
- SIT/UAT/PRD/PROD scripts
- authentication approach
- Redux Provider setup
- Axios configuration
- common company libraries
- Docker/Jenkins/CI/CD integration

Prefer copying **configuration patterns**, not business code.

**`cmt-oms-ui-equities-order-entry` is the first reference for runtime behavior.**

**`cmt-ui-nextjs-template` is the second reference for baseline framework configuration.**

---

# 6. Remove CRA Infrastructure Completely

After migration, the following should be removed unless there is a proven unavoidable reason:

```text
react-scripts
react-app-rewired
config-overrides.js
CRA-specific webpack customizations
CRA-specific devServer configuration
CRA-specific public-path handling
```

Verify that `react-scripts` and `react-app-rewired` no longer exist in:

- dependencies
- devDependencies
- npm scripts
- build commands
- CI/CD scripts
- dependency tree

Run:

```bash
npm ls react-scripts
npm ls react-app-rewired
```

Expected result:

```text
not installed
```

Do not claim they are removed until the dependency tree confirms it.

---

# 7. Migrate `config-overrides.js` Behavior Carefully

Project B's existing `config-overrides.js` contains important behavior.

Do not simply delete it.

First inventory every behavior and map it to Next.js or Project A.

Known areas include:

## 7.1 Public/Base Path

Project B currently uses a custom deployment path similar to:

```text
/u1/workspace/cmt/entitlement/maintenance
```

and currently handles it using CRA public-path logic.

Map this to the same approach used by Project A.

Evaluate:

```text
basePath
assetPrefix
DEPLOYMENT_LOCATION
company deployment routing
```

Do not invent a new deployment convention if Project A already solves this.

---

## 7.2 Environment Injection

Project B currently injects environment/build values such as:

```text
REGION
ROUTER_BASE_PATH
CLIENT_ID
CLIENT_SECRET
COMMIT_ID
BRANCH_NAME
BUILD_DATE
COMMIT_TAG
HSBC_ENV
```

Map these to the Next.js/company-standard environment mechanism.

Do NOT automatically convert every variable to:

```text
NEXT_PUBLIC_*
```

Only variables intentionally needed in browser code may be public.

### Security requirement

If `CLIENT_SECRET` is currently used by client-side code, inspect all usages before migration.

Do NOT create:

```text
NEXT_PUBLIC_CLIENT_SECRET
```

Do NOT expose secrets into the browser bundle.

Search for:

```text
process.env.CLIENT_SECRET
```

and determine whether it is truly required on the client.

If it is a real secret, keep it server-side and flag the old client exposure as a security concern.

---

## 7.3 Dev Proxy

Project B currently has devServer proxy behavior for routes such as:

```text
/api
/api-staff
/none
```

and uses:

```text
target
changeOrigin
pathRewrite
```

Migrate this behavior using the same Next.js mechanism already used by Project A, typically:

```text
rewrites()
```

Preserve exact URL behavior.

Do NOT change backend API contracts.

Do NOT change Axios business calls unless required.

Validate every existing proxy route individually.

---

## 7.4 Shared Source Outside `src`

Project B modifies CRA compilation rules to include shared code similar to:

```text
../shared/src
```

This is a migration risk and must be inspected explicitly.

Determine whether the shared code is:

- workspace package
- local package
- source-folder import
- symlinked library
- company common module

Prefer Project A's proven solution if it has the same structure.

Possible Next.js mechanisms may include:

```text
transpilePackages
workspace package configuration
webpack configuration
monorepo configuration
```

Use the smallest change that preserves existing imports.

Do not move or rewrite shared code unless necessary.

---

## 7.5 Compression

Project B currently adds `CompressionPlugin` in production.

Check whether Project A or the deployment platform already handles compression.

If Next.js / nginx / platform already provides compression, remove the custom webpack compression setup.

Do not preserve old webpack plugins without a demonstrated need.

---

# 8. Routing Migration

Project B currently uses:

```text
react-router-dom 6
```

Preserve all existing URLs and user navigation behavior.

Do not unnecessarily change paths such as:

```text
/users
/users/:id
/orders
/orders/:id
/config
```

First inspect how Project A handles routes.

If Project A uses App Router, migrate routes using the lowest-risk approach.

Do not rewrite all CRUD components merely to migrate routing.

Keep business components separate from routing changes.

For navigation APIs, change only where required by Next.js.

Clearly report every route whose implementation changes.

---

# 9. Redux

**`cmt-oms-ui-equities-order-entry`** already proves that Redux works with:

```text
Next.js 15
React 19
```

Therefore preserve Project B's Redux architecture where possible.

Do not replace Redux.

Do not rewrite reducers/actions/selectors unless a real compatibility issue exists.

If using App Router, create the minimum required client-side Provider boundary based on Project A.

Preferred principle:

```text
Provider boundary
    ↓
existing Redux-connected application
```

rather than rewriting Redux usage throughout Project B.

---

# 10. Axios and Backend API Integration

Preserve existing Axios behavior.

Do not rewrite:

```js
axios.get(...)
axios.post(...)
axios.put(...)
axios.delete(...)
```

unless required for compatibility.

Keep:

- existing base URL logic
- existing interceptors
- existing authentication headers
- existing error handling
- existing API service classes

Only migrate infrastructure around proxy/base-path/environment configuration.

---

# 11. Authentication

Preserve existing:

- login
- logout
- tokens
- cookies
- localStorage/sessionStorage
- authorization
- protected routes
- role-based menus
- authentication headers
- refresh logic

Use Project A as the compatibility reference.

Do not redesign authentication during this migration unless existing behavior cannot run safely under Next.js.

---

# 12. Build Scripts and Environments

**`cmt-oms-ui-equities-order-entry`** already uses company-standard scripts similar to:

```text
dev
build
build:sit
build:uat
build:prd
build:prod
start
```

and environment variables such as:

```text
HSBC_ENV
DEPLOYMENT_LOCATION
```

Align Project B with Project A's convention.

Replace CRA commands such as:

```text
react-app-rewired start
react-app-rewired build
```

with Next.js equivalents.

Preserve the meaning of each environment.

Do not merge SIT/UAT/PRD/PROD behavior unless Project A already does so.

---

# 13. UI / Styling

Preserve existing:

- CSS
- SCSS
- styled-components
- Formik
- AG Grid or other table libraries
- company UI libraries
- themes
- layouts
- styles

Do not redesign the UI.

If a styling library needs a React 19-compatible version, upgrade only as required and document the reason.

---

# 14. Testing

Use Project A / company template as the Jest/testing reference.

Preserve existing business tests where possible.

Migrate CRA-specific Jest configuration only where required.

Do not delete tests merely because the build framework changed.

Verify:

```text
npm test
npm run test:report
```

or Project A's equivalent scripts.

---

# Required Migration Workflow

## Phase 1 — Analysis Only

Do NOT edit Project B immediately.

First compare:

```text
Project B
vs
Project A
vs
Company Next.js template
```

Produce a migration assessment containing:

1. current Project B architecture
2. target architecture
3. files that can remain unchanged
4. files requiring modification
5. files to add
6. files to delete
7. dependencies to remove
8. dependencies to add
9. dependencies requiring React 19 compatibility checks
10. routing changes
11. environment-variable changes
12. proxy changes
13. base-path/deployment changes
14. authentication implications
15. shared-source implications
16. test changes
17. CI/CD changes
18. security risks

Do not start implementation until this assessment is complete.

---

## Phase 2 — Dependency Alignment

Align:

```text
next
react
react-dom
redux
react-redux
axios
```

with Project A where appropriate.

For other dependencies:

```text
keep existing version
```

unless a real React 19 / Next 15 compatibility issue is found.

Do not bulk-upgrade unrelated packages.

---

## Phase 3 — Next.js Infrastructure

Create the minimum Next.js infrastructure required.

Use Project A as the baseline for:

```text
next.config.*
app/pages structure
providers
environment handling
build scripts
Jest
ESLint
PostCSS
deployment
```

Migrate `config-overrides.js` behavior one feature at a time.

---

## Phase 4 — Preserve Existing Application Tree

Mount existing Project B business components into the Next.js structure.

Prefer:

```text
Next page/client wrapper
        ↓
existing Project B application/feature component
```

rather than moving business logic into new Next.js files.

Add `'use client'` at the minimum required boundaries.

---

## Phase 5 — Routing Compatibility

Migrate routing with minimal changes.

Preserve route URLs.

Do not refactor business pages while changing routing.

---

## Phase 6 — Build

Run:

```bash
npm install
npm run build
```

Fix only actual migration/compatibility errors.

Classify every error as:

```text
Next.js migration issue
React 19 compatibility issue
dependency compatibility issue
existing unrelated issue
```

Do not perform unrelated cleanup.

---

## Phase 7 — Functional Verification

Verify at minimum:

```text
application starts
login works
logout works
navigation works
route refresh works
protected routes work
Redux state works
CRUD list works
search/filter works
create works
edit works
delete works
forms work
modals work
API calls work
proxy works
base path works
SIT build works
UAT build works
PRD/PROD build works
existing UI remains visually equivalent
```

---

## Phase 8 — Security Verification

After migration, verify:

```bash
npm ls react-scripts
npm ls react-app-rewired
```

Confirm they are gone.

Then rerun the existing security/dependency scan.

Identify:

```text
high-severity vulnerabilities removed
transitive dependencies removed
remaining vulnerabilities
new vulnerabilities introduced by migration
```

Do NOT claim a Sonar/security issue is resolved only because the application migrated to Next.js.

Confirm with the actual dependency tree/security scan.

---

# Explicit Out-of-Scope Items

Unless required for successful migration, do NOT:

- redesign the UI
- rewrite CRUD pages
- rewrite React hooks
- convert Axios to fetch
- introduce Server Actions
- move all data fetching to Server Components
- replace Redux
- replace Formik
- convert all JavaScript to TypeScript
- redesign authentication
- change backend APIs
- change business URLs
- rename business components
- perform generic cleanup
- optimize performance unrelated to migration
- upgrade every package to the latest version
- introduce a new architecture merely because it is considered more modern

---

# Decision Priority

For every decision, use this order:

```text
1. Preserve current business behavior
2. Minimize Project B source-code changes
3. Follow Project A's proven implementation
4. Follow company Next.js template conventions
5. Remove react-scripts completely
6. Remove react-app-rewired completely
7. Align with Next.js 15.2.8 + React 19
8. Preserve CRUD/hooks/Redux/Axios
9. Preserve URLs and backend contracts
10. Fix only actual compatibility problems
11. Optimize later
```

---

# Desired Final Architecture

## Before

```text
React 18
+ react-scripts
+ react-app-rewired
+ config-overrides.js
+ React Router
+ Redux
+ Axios
+ existing hooks
+ existing CRUD
+ company UI libraries
```

## After

```text
Next.js 15.2.8
+ React 19
+ React DOM 19
+ Redux
+ Axios
+ existing hooks
+ existing CRUD
+ company UI libraries
```

with CRA infrastructure removed.

The desired result is NOT:

```text
Next.js
+ rewritten CRUD
+ rewritten hooks
+ rewritten Redux
+ rewritten Axios
+ new application architecture
```

---

# Expected Final Report

At completion, provide:

```text
Migration Summary

Reference projects:
- Production/business Project A:
- Company Next.js template:

Target versions:
- Next.js:
- React:
- React DOM:

Files changed:
- ...

Files added:
- ...

Files deleted:
- ...

CRA infrastructure removed:
- react-scripts: YES / NO
- react-app-rewired: YES / NO
- config-overrides.js: YES / NO

Dependencies removed:
- ...

Dependencies added:
- ...

Dependencies upgraded for React 19 compatibility:
- ...
- reason for each:

Business components modified:
- ...
- reason for each:

Hooks modified:
- ...
- reason for each:

'use client' boundaries added:
- ...

Redux changes:
- ...

Routing changes:
- ...

Environment changes:
- ...

Proxy/rewrites changes:
- ...

Base path / deployment path changes:
- ...

Shared source handling:
- ...

Authentication changes:
- ...

Build/CI changes:
- ...

React 18 → React 19 compatibility issues found:
- ...

npm run build:
- PASS / FAIL

Functional verification:
- Login:
- Logout:
- Navigation:
- Route refresh:
- Redux:
- List:
- Search:
- Create:
- Edit:
- Delete:
- API calls:
- SIT:
- UAT:
- PRD/PROD:

Security verification:
- npm ls react-scripts:
- npm ls react-app-rewired:
- high-severity vulnerabilities removed:
- remaining vulnerabilities:

Remaining risks:
- ...
```

---

# Final Instruction

Make the migration **boring and small**.

The goal is not to demonstrate new Next.js features.

The goal is to safely replace the old CRA/react-scripts/react-app-rewired platform with the company's proven Next.js 15.2.8 + React 19 platform while keeping Project B's existing CRUD application, hooks, Redux, Axios, UI components, and business behavior as unchanged as possible.
