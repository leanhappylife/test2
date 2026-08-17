Hi Team,

Thanks for the discussion today.

As discussed, please find below the issues identified during SIT testing.

**1. Transaction File – Client Deals**

* Some client deals in the SIT test cases are missing from the transaction file.
* `NMEMO_PRICE` is incorrect. The expected value is -4, but the transaction file shows 0.
* The transaction file should only contain structured products. Fixed income, precious metal, and shares should not be included.
* `SETTLEMENT_NOTIONAL` is incorrect. It should be calculated as Outstanding Quantity × Gross Price.
* `SETTLEMENT_NOTIONAL_LEVERAGE` is incorrect. It should be calculated as Outstanding Quantity × Gross Price × Leverage.
* `NMEMO_ISSUE_CODE` format is incorrect. It currently contains customer number + sub-account number + issue code, but it should only contain the issue code.

**2. Transaction File – Broker Deals**

* Broker Order ID should be mapped to `AVQ_ORDER_ID`, Broker Order Type should be mapped to `ORDER_TYPE`, and `SUB_ACCOUNT_NUMBER` should be blank.
* Some broker deals in the SIT test cases are missing from the transaction file.
* `PREMIUM_AMOUNT` is incorrect. The TOM `CONSIDERATION_AMOUNT` is correct, but `PREMIUM_AMOUNT` in the transaction file is 0.
* `NMEMO_ISSUE_CODE` is blank for the broker deals.
* `SETTLEMENT_NOTIONAL` and `SETTLEMENT_NOTIONAL_LEVERAGE` should follow the same logic as client deals. If one broker deal is linked to multiple client deals, the values should be the sum of all related client deals.

**3. Transaction File Timing**

* The transaction file is coming too late in UAT.
* In production, we expect to receive the file at around 8 p.m., similar to the position file.

**4. Position File**

* `SUB_ACCOUNT_NUMBER` is incorrect. It should contain only the last four digits, but currently it contains both the customer number and sub-account number.
* `MEMO_PRICE` and `RATE` are incorrect. `MEMO_PRICE` should be Client Receive %, and `RATE` should be Gross Price.
* Some `MEMO_ISSUE_CODE` and `MEMO_CURRENCY_CODE` values are blank.
* As discussed in the previous meeting, we only need `CURRENT_HOLDING_NOMINAL`, which represents Outstanding Quantity. `OUTSTANDING_QUANTITY` and `CURRENT_HOLDING_NOMINAL_LEVERAGE` should be removed.
* `SETTLEMENT_NOTIONAL_LEVERAGE` is blank and should be calculated as Outstanding Quantity × Gross Price × Leverage.
* Some header names are incorrect:

  * `MEMO_ISSUE_CODE` should be `NMEMO_ISSUE_CODE`
  * `MEMO_CURRENCY_CODE` should be `NMEMO_CURRENCY_CODE`
  * `MEMO_PRICE` should be `NMEMO_PRICE`
* The header `META_TYP` should be changed to `META_TYPE`.
* For `NMEMO_MATURITY_DATE`, TOM currently gets the value from `NMEMO_DELIVERY_DATE` in `FOS.NMEMO_TEMPLATE`. Please check whether this information can be provided by AVQ.

**5. File Delivery**

* This time we only received the transaction file and the position file. We have not received the fixing file yet.

Please fix the above issues and resend the updated transaction and position files. Please also send us the fixing file.

Thanks.
