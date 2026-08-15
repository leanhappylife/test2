## TXN Issues – Client Deal

**1. Missing client deals**
Four client deals are missing from the output:
STEX_BOP209131730, STEX_BOP209131962, STEX_BOP209132079, STEX_BOP209131957.

**2. Incorrect NMEMO_PRICE**
NMEMO_PRICE is 0. It should be -4 based on TOM Client Receive.

**3. Other products included**
The output contains non-HK-TRS products, such as fixed income, precious metal and shares. Please confirm whether this is expected.

**4. Incorrect settlement notional**
SETTLEMENT_NOTIONAL is incorrect. For example, the expected value is 100,000 (1,000 × 100), but the output is -4,000.

## TXN Issues – Broker Deal

**1. Incorrect broker field mapping**
The broker fields are mapped incorrectly. Broker ID and type should be in AVQ_ORDER_ID and ORDER_TYPE. SUB_ACCOUNT_NUMBER should be blank.

**2. Missing broker deals**
Three broker deals are missing:
STEX_BOPC209131737, STEX_BOPC209131969, STEX_BOPC209132085.

**3. Incorrect PREMIUM_AMOUNT**
PREMIUM_AMOUNT is 0. It should match TOM CONSIDERATION_AMOUNT, such as -3,000 or -18,000.

**4. Missing NMEMO_ISSUE_CODE**
NMEMO_ISSUE_CODE is blank. It should contain the correct issue code, such as NMA0080–NMA0095.

**5. Incorrect settlement notional**
The broker settlement notional values do not match the TOM nominal values.

## POS Issues

**1. Incorrect SUB_ACCOUNT_NUMBER**
SUB_ACCOUNT_NUMBER is incorrect. It should contain only the last four digits, for example 0001.

**2. Incorrect MEMO_PRICE and RATE**
MEMO_PRICE should be -4, and RATE should be 100.

**3. Additional position records**
The output contains 55 records, but the test data contains 32 positions. Some additional records also have blank issue code or currency fields.

**4. Same quantity values**
OUTSTANDING_QUANTITY, CURRENT_HOLDING_NOMINAL and CURRENT_HOLDING_NOMINAL_LEVERAGE have the same values. Please confirm whether this is expected.
