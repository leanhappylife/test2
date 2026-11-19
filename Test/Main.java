
## 1. Background

Hi everyone. We are moving Generic Memo from eTOM to Sparta as part of the SWAP project.

The TLM logic has not changed.

T1 is a test case from eTOM. S1 is the matching case from Sparta. Both test regular fixing.

Their TLM results are the same except for MTM. This is because their MTM input values are different.

Today, I will use S1 to show you how to check the TLM data.

## 2. Test Cases

*Open Test Case -TRS-Cycle.*

This sheet lists the test cases.

We test regular fixing, full and partial unwind, and reversal.

The T cases are from eTOM. The S cases are from Sparta.

For Cycle 2, we check the data for 15 May 2026.

## 3. Compare eTOM and Sparta

These cases help us compare the two systems.

We compare T1 with S1, T2 with S2, and T3 with S3.

Each pair covers a similar event.

First, we check the input data, date, and event.

Then, we compare the data in the TLM file.

If the input data is different, the results may be different.

For the other S cases, we check the result for each event.

## 4. Find the Customer and Deal Numbers

*Open TEMP.GM_CASE.*

Next, open TEMP.GM_CASE. This is the mapping sheet.

This sheet links each case to its customer and deal numbers.

Find S1. Then find its customer and deal numbers.

One case may have more than one row. Check all its rows.

## 5. Raw Data

*Open the raw data sheets.*

Next, let’s look at the raw data. This is the data from the system.

These sheets include:

- FOS.NMEMO_DEAL
- FOS.NMEMO_PAYMENT
- GM_COMMON_FIXING_DEAL

Use the customer and deal numbers to find the data for the case.

Check the dates, currency, and amounts if they are shown.

We use this data to check the TLM file.

## 6. TLM Results

*Open TLM_INTERFACE_FILE.*

Now, open TLM_INTERFACE_FILE.

Find the rows for the case.

Check that all the rows we expect are there. Then check the values.

If a row is missing or a value is wrong, check why.

## 7. Example with S1

Let’s use S1 as an example.

First, find S1 in the test case sheet. It is a regular fixing case from Sparta. Check the business date.

Next, open TEMP.GM_CASE. Find the customer and deal numbers for S1.

Use these numbers to find the raw data.

Then, open TLM_INTERFACE_FILE. Find S1’s rows and check the values.

We can also compare S1 with T1.

We use the same steps for the other cases.

## 8. The MTM Difference

Comparing eTOM and Sparta helps us check the move.

The TLM logic is the same. If the inputs are the same, we expect the same results.

The TLM results for T1 and S1 are the same except for MTM.

Their MTM input values are different, so the MTM results are different.

This result is for T1 and S1. We also need to check each other case.

## 9. Help from the TLM Team

Please help us check the TLM data.

Tell us which cases and columns you need to check.

Check for missing rows and wrong values.

If you find a problem, share the case number, date, column, and value.

Tell us what you see and what you expect.

## 10. Finish

That’s all from me.

We start with the test case. We find its customer and deal numbers.

Then we check the raw data and TLM results.

We also compare the eTOM and Sparta results.

Do you have any questions?

---

## Questions and Answers

**Q. What is this file for?**

It helps us check the TLM data for the move to Sparta.

**Q. Why do we compare eTOM and Sparta?**

We moved the screen to Sparta. The TLM logic did not change. We compare the results to check the move.

**Q. Are the T1 and S1 results the same?**

The TLM results are the same except for MTM.

**Q. How do I find the data for one case?**

Find the case in TEMP.GM_CASE. Use its customer and deal numbers to find the data.

**Q. Which date do we check?**

We check C2, which is 15 May 2026.

**Q. Why is there no action for S13?**

The test sheet says we cannot reverse the deal because of mark off. So there is no action in Cycle 2.

**Q. Why is S15 a full unwind?**

S15 is in its last period. The sheet says Sparta uses a full maturity unwind for regular fixing in this period. S15 has a full unwind in Cycle 2.

**Q. Why is MTM different?**

The MTM input values are different, so the MTM results are different.

**Q. What should the TLM team do?**

Check the cases and columns for your team. Tell us about missing rows or wrong values.

---

## One Minute Version

Hi everyone. We are moving Generic Memo from eTOM to Sparta as part of the SWAP project.

The screen has moved. The TLM logic has not changed.

We compare the eTOM and Sparta data to check the move.

T1 is a test case from eTOM. S1 is the matching case from Sparta. Their TLM results are the same except for MTM. Their MTM input values are different.

This file has the test cases, mapping sheet, raw data, and TLM results.

Let’s open one case and check it together.

Please tell us if you find a problem.
