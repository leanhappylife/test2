Test Strategy for TARA Finance Parallel Run

1. Introduction

1.1 Purpose
This test strategy ensures that updates, enhancements, or changes implemented in the TARA Finance system during the parallel run do not negatively impact critical financial operations. It emphasizes validating the stability, accuracy, performance, compliance, and integrity of financial processes, safeguarding business continuity and operational efficiency.

2. Test Objectives

Validate accuracy and integrity of financial data post-migration.

Confirm system stability and performance under parallel execution conditions.

Ensure seamless functionality and no disruption to ongoing Finance operations.

Verify compliance with regulatory and internal control requirements.

Identify and document discrepancies or issues promptly for resolution.

3. RAID (Risks, Assumptions, Issues & Dependencies)

3.1 Assumptions

Legacy financial data is fully reconciled, accurate, and reliable.

Parallel systems will operate without unforeseen infrastructure issues.

Adequate resources and expertise will be available throughout the test duration.

3.2 Risks

Potential discrepancies between legacy and new financial data sets.

System performance degradation due to simultaneous batch processing.

Delays in issue resolution impacting parallel run timelines.

3.3 Issues

None identified at the time of strategy creation; monitoring to continue.

3.4 Dependencies

Availability of necessary testing environments and datasets.

Effective backup and restoration protocols.

Timely and responsive collaboration from stakeholders and support teams.

4. Test Management

4.1 Test Environments

HR1CPR4-HK Daily CCL Database

HR1CPR5-HK Monthly CCL Database

SR1CPR4-SG Daily CCL Database

SPHCPR5-SG Monthly CCL Database

Backup and recovery test environments aligned with production standards.

4.2 Test Approach

Conduct initial backups of Finance result tables into BK.RPT_XXX.

Execute data migration by loading impacted TARA data into Interface tables.

Explicitly omit execution of specific stored procedures (e.g., interface_PI for BATCH_ARE).

Trigger relevant batch processes under test conditions, monitoring system behavior and performance closely.

Systematically compare financial outcomes between RPT.XXX and BK.RPT_XXX tables, emphasizing accurate reconciliation and highlighting discrepancies clearly for resolution.

Document detailed test scenarios, cases, and expected results to guide the testing process effectively.

Maintain a clear defect logging, tracking, and resolution system throughout the test cycle.

4.3 Test Schedule

31 July 2025: Month-end PBCCL Finance snapshot and comparative analysis.

29 Aug 2025: Month-end PBCCL Finance snapshot with incremental testing and analysis.

30 Sept 2025: Final Month-end PBCCL Finance snapshot, including comprehensive validation and final sign-off.

4.4 Roles and Responsibilities

Test Lead: Overall test coordination, resource allocation, and issue escalation.

Business Analysts: Preparation of test data, execution of financial reconciliations, and validation of outcomes.

IT Infrastructure: Ensuring optimal environment performance and resolving technical impediments.

Developers: Supporting defect resolution and providing technical expertise.

Stakeholders: Reviewing test outcomes, providing timely feedback, and approving test results.

5. Reference Documents

PBCCL Daily Batch Processing Diagram

TARA Finance Migration Plan

Finance Reconciliation Guidelines

Regulatory Compliance Checklist

This enriched strategy facilitates thorough validation, detailed documentation, and effective risk management during the TARA Finance parallel run.
