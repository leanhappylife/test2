1. Introduction
Hi everyone. Let me quickly walk you through this Excel file.

This file contains the validation results for TRS UAT Cycle 2.

The main purpose is to check that the change does not cause unexpected differences in the downstream results.

There are four main parts: test cases, test case mapping, raw data, and report results.

Let’s go through them one by one.
2. Test Cases – Test Case -TRS-Cycle
First, let’s look at the test case sheet.

This sheet shows the test scenarios for the TRS life cycle. It tells us what to test and when each event happens.

T1, T2, and T3 come from the existing eTOM system. The S cases come from Sparta.

We cover regular fixing, full unwind, partial unwind, and deal reversal.

For example, T1 and S1 are regular fixing cases. T2 and S2 are full unwind cases. T3 and S3 are deal reversal cases.

We also have other Sparta cases, including partial unwind and more complex cases with multiple clients or a broker.

Some cases have long positions, and others have short positions.

On the right, we can see the event dates, such as trade date, premium value date, fixing dates, settlement dates, and maturity date.

C0, C1, C2, and the other C labels are UAT business dates. The dates are shown at the bottom of the sheet. For example, C2 is 15 May 2026.

The highlighted cells help us see the important dates for each test case.

So this sheet tells us what happens in each case and which business date we need to check.
3. eTOM and Sparta Comparison
For some test cases, we also compare Sparta with eTOM.

eTOM is the existing system, so we use its results as a reference for similar business scenarios.

For example, we compare T1 with S1 for regular fixing, T2 with S2 for full unwind, and T3 with S3 for deal reversal.

The scenarios are similar, but the deal numbers and some input data may be different.

So we do not expect every field to match exactly.

Instead, we check whether Sparta gives the expected business results, such as the right dates, amounts, and report records.

This is a business scenario comparison. It is different from checking the results before and after the change.
4. Test Case Mapping – TEMP.GM_CASE
Next, let’s look at the mapping sheet, TEMP.GM_CASE.

This sheet links each test case to the related records.

It contains details such as case number, customer number, sub-account number, deal number, and issue code.

For example, if we want to check S1, we find S1 here and use the customer and deal details to find the records in the other sheets.

One test case can have more than one related record, so we need to check the correct records.
5. Raw Data
Now, let’s look at the raw data.

These sheets contain source records from different tables, such as INTERFACE.SYS, FOS.NMEMO_DEAL, FOS.NMEMO_BROKER_DEAL, FOS.NMEMO_PAYMENT, FOS.NMEMO_BANK_PAYMENT, INTERFACE.GM_COMMON_DEAL, GM_COMMON_FIXING_DEAL, and MEMO_PRODUCT_POSITION.

We use the customer number or deal number from the mapping sheet to find the related data.

Here, we can check deal information, dates, currencies, amounts, and other fields.

This helps us understand the records behind the report results.
6. Report Results
Finally, let’s look at the report sheets.

These show the downstream results, including RADAR and Lombard outputs.

For example, we have RADAR_TRADED_PRODUCT_FILE, RADAR_WHOLESALE_PRODUCT_FILE, RADAR_HKMA_RETAIL_PRODUCT_FILE, RADAR_HKMA_WHOLESALE_PRODUCT, and LOMBARD_NCCR_RESULT.

Let’s open RADAR_TRADED_PRODUCT_FILE.

Here, we can find the related records and check the dates, currency, amounts, and other key fields.

Other reports may contain different fields, so we check them based on their requirements.
7. Example – Checking S1
Let me show you how we check one test case. Let’s take S1 as an example.

First, we open the test case sheet. S1 is a regular fixing case from Sparta. We check the scenario and the relevant business dates.

Next, we go to TEMP.GM_CASE to find the customer number and deal number.

Then, we use these details to find the related records in the raw data sheets.

After that, we open the RADAR report and find the related output records.

We check the key values and make sure we are looking at the correct business date.

So the process is: test case, mapping, raw data, and report result.

We can use the same approach for the other test cases.
8. Two Different Types of Checks
We have two types of checks in this work.

First, eTOM versus Sparta: we compare similar business scenarios to see whether Sparta produces the expected results. The data does not have to be identical.

Second, before versus after the change: we compare the same test data, the same business date, and the relevant output fields to look for unexpected differences.

For the before-and-after check, we expect the downstream results to remain the same.

If there is a difference, we need to investigate it.

The screenshots show the test cases and outputs, but we should only say the comparison has passed when the full comparison evidence has been reviewed.
9. What We Need from FCDP and VS
For FCDP and VS, we would appreciate your help reviewing the reports used by your teams.

Please let us know which files and fields you need to validate.

You can use the test case and mapping sheets to find the related records.

If you find missing records or unexpected values, please share the test case, report name, and field so we can check together.
10. Closing
So that is the overview.

The test case sheet shows what we are testing. The mapping sheet helps us find the records. The raw data sheets show the source data, and the report sheets show the downstream output.

We use these sheets together to support the validation.

That’s all from my side. Do you have any questions?
Quick Questions and Answers
Q: Why compare T1 with S1?

A: They are similar regular fixing scenarios, but they come from different systems. We compare the key business results.

Q: Do all fields need to be the same?

A: Not for eTOM versus Sparta. Some input details may be different. For before-and-after testing, we compare the same records and fields.

Q: How do we find a deal?

A: Find the case in TEMP.GM_CASE, then use the customer or deal number to find the records.

Q: What should FCDP and VS check?

A: Please review the reports and key fields used by your team, and flag any unexpected values.

Q: What if we find a difference?

A: Please share the case, report, business date, and field. We can investigate it together.
