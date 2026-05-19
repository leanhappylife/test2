# Total Return Swap (TRS) Field Mapping Spec v3

Revision date: 2026-05-19

Primary source: D:\check\MEMO_Study\ODS_SPs

This version is English-only and is aligned to the executable logic visible in the three fixed DB2 stored procedure files:

- INTERFACE.PR_TRANSFORM_GM_COMMON_DEAL.db2_fixed.sql
- INTERFACE.PR_TRANSFORM_GM_COMMON_DEAL_EXT.db2_fixed.sql
- INTERFACE.PR_TRANSFORM_GM_COMMON_FIXING_DEAL.db2_fixed.sql

The document describes the target field mapping after initial insert plus post-insert override and enrichment logic. The procedures are generic Generic Memo transforms, not TRS-only transforms. TRS rows are identified mainly by TOTAL_RETURN_SWAP_FLAG.

## 1. Stored Procedure Scope

| Procedure | Target table | Main role |
|---|---|---|
| INTERFACE.PR_TRANSFORM_GM_COMMON_DEAL | INTERFACE.GM_COMMON_DEAL | Main deal snapshot for client and broker side. It produces trade identity, direction, notional, outstanding notional, MTM, underlying, issuer and guarantor, open-close linkage, broker linkage, final fixing and knockout state. |
| INTERFACE.PR_TRANSFORM_GM_COMMON_DEAL_EXT | INTERFACE.GM_COMMON_DEAL_EXT | IR leg extension table. The SQL filters FOS.NMEMO_TEMPLATE.ASSET_TYPE = 'IR'. Equity TRS with template asset type 'MX' will not enter this procedure unless the source creates separate IR template records. |
| INTERFACE.PR_TRANSFORM_GM_COMMON_FIXING_DEAL | INTERFACE.GM_COMMON_FIXING_DEAL | Fixing, payment and cashflow event table for client and broker side. It produces settlement amount, FX buy/sell split, market settlement dates, linked settlement deal, and broker nostro fields. |

## 2. Batch Setup and Common Rules

| Area | SP logic | Mapping impact |
|---|---|---|
| Business date | DEAL and FIXING read INTERFACE.SYS where SYS_CDE = 'EFOS' into LAST_BUS_DT, BUS_DT and REGION_CDE. DEAL_EXT reads BUS_DT and REGION_CDE. | Business date drives new-trade previous outstanding notional, EXT schedule selection fallback, and regional branch/company derivation. |
| Local currency | All three procedures call INTERFACE.FN_GET_LOCAL_CURRENCY. | Used to derive broker branch code and fixing branch code. |
| Full refresh | Each procedure deletes its target table before insert when the target has data. | The output is rebuilt for the batch, then enriched by update and merge steps inside that procedure. |
| Company code by region | SG maps to 68. HK maps to 63. | Used for broker DEAL, broker EXT, and both sides of FIXING. Client DEAL and client EXT take company code directly from FOS.NMEMO_DEAL. |
| Asset class | FOS.NMEMO_TEMPLATE.ASSET_TYPE in FX, EQ, FI, IR, CT maps to itself. NX maps to EQ. CD maps to CT. Everything else maps to MX. | TRS normally remains MX when the template asset type is MX. |
| TRS flag | TOTAL_RETURN_SWAP_FLAG = 'Y' when FOS.NMEMO_TEMPLATE.NMEMO_CODE in ('TR4','TR5'), ASSET_TYPE = 'MX', and the selected primary underlying class is EQ or NX. Otherwise 'N'. | Current SP logic only identifies equity or equity-index TRS. Other TRS types require a future rule change. |
| Primary underlying class | FOS.NMEMO_LEG_UNDERLYING is grouped by NMEMO_ISSUE_CODE and UNDERLYING_CLASS_CODE. The highest total UNDERLYING_WEIGHT is selected, with priority EQ or NX, then WX, then EX, then IR when weights tie. | Used by TOTAL_RETURN_SWAP_FLAG and ADVANCE_PURPOSE_CODE. |
| Top FX underlying | FOS.NMEMO_LEG_UNDERLYING rows with UNDERLYING_CLASS_CODE = 'FX' are grouped by issue and underlying issue, ordered by total weight descending. | Used for FX/MX-FX underlying issue and bullion flag derivation. |
| Leverage factor | DECODE(NMEMO_LEVERAGE_LEVEL, 0, 1, COALESCE(NMEMO_LEVERAGE_LEVEL, 1)). | NOTIONAL_AMT, OUTSTANDING_NOTIONAL_AMT, and new-trade previous outstanding notional are multiplied after initial insert and selected enrichment. |
| Side flag | Client side maps to BANK_CUST_FLAG = 'C'. Broker side maps to BANK_CUST_FLAG = 'B'. | Many update and merge steps are side-specific. |
| Bank-view direction | Client open O maps to DEAL_TRAN_TYPE = 'S'. Client close/non-open maps to 'P'. Broker open O maps to 'P'. Broker close/non-open maps to 'S'. | DEAL_TRAN_TYPE is expressed in bank view, not customer view. |

## 3. Source Aliases and Grains

| Alias or source | Physical source | Role |
|---|---|---|
| ND | FOS.NMEMO_DEAL | Client main deal source. |
| BROKER_ND | FOS.NMEMO_BROKER_DEAL | Broker or hedge main deal source. |
| NT | FOS.NMEMO_TEMPLATE | Product and template definition. |
| NTAD | FOS.NMEMO_TEMPLATE_AGENT_DETAIL | Issuer and guarantor source. |
| ORDER_DETAIL | FOS.NMEMO_ORDER_STATEMENT_DETAIL | Strike or reference price source. |
| PAYMENT | FOS.NMEMO_PAYMENT | Client fixing or payment event source. |
| BANK_PAYMENT | FOS.NMEMO_BANK_PAYMENT | Broker fixing or payment event source. |
| MPP | INTERFACE.MEMO_PRODUCT_POSITION | Current client MTM source. |
| MPPLD | INTERFACE.MEMO_PRODUCT_POSITION_LASTDAY | Previous client MTM source. |
| GCD_LASTDAY | INTERFACE.GM_COMMON_DEAL_LASTDAY | Previous outstanding notional source. |
| SI | INTERFACE.SHARE_ISSUE | Reuters code and country enrichment for single equity or index underlying. |

| Target table | Target grain | Practical key used by the SQL |
|---|---|---|
| INTERFACE.GM_COMMON_DEAL | One row per side per active open or close GM deal. Client side keeps real customer, sub-account and deal sub number. Broker side uses broker number as CUST_NUM, SB_ACCT_NUM = '0001', and DEAL_SB_NUM = 0. | Most updates match by DEAL_NUM, CUST_NUM, SB_ACCT_NUM, DEAL_SB_NUM, PROD_TYPE, ASSET_CLASS plus BANK_CUST_FLAG. |
| INTERFACE.GM_COMMON_DEAL_EXT | One row per side per active IR deal with one pay leg and one receive leg. | Commented primary key is DEAL_NUM, CUST_NUM, SB_ACCT_NUM, DEAL_SB_NUM, PROD_TYPE, ASSET_CLASS. |
| INTERFACE.GM_COMMON_FIXING_DEAL | One row per selected fixing or payment event after batch-date deduplication. | Client side uses customer, sub-account, product, deal, sub-deal, fixing date and event type. Broker side uses broker, bank deal, bank sub-deal, fixing date and event type. |

## 4. INTERFACE.GM_COMMON_DEAL Field Mapping

The table below shows the final intended value after initial insert and later enrichment steps in INTERFACE.PR_TRANSFORM_GM_COMMON_DEAL.

| Target field | Client side mapping | Broker side mapping | Business description |
|---|---|---|---|
| ACTV_FLAG | FOS.NMEMO_DEAL.ACTIVE_FLAG | FOS.NMEMO_BROKER_DEAL.ACTIVE_FLAG | Indicates an active EFOS/FOS source row that is allowed into the target; the insert filters currently require ACTIVE_FLAG = 'A'. |
| DEAL_NUM | FOS.NMEMO_DEAL.DEAL_NUMBER | FOS.NMEMO_BROKER_DEAL.DEAL_NUMBER | Deal identifier from the source side. Avaloq or Sparta migration must not assume this is native Avaloq identity. |
| DEAL_SB_NUM | FOS.NMEMO_DEAL.DEAL_SUB_NUMBER | Constant 0 | Sub-deal component of the target key. Client side preserves source sub-deal granularity; broker main deal rows are normalized to sub number 0. |
| DEAL_COMP_CDE | FOS.NMEMO_DEAL.DEAL_COMPANY_CODE | Derived from INTERFACE.SYS.REGION_CDE: SG = 68, HK = 63 | Legal entity or company code consumed by GM downstream reporting. Client rows keep the source company; broker rows derive company from the ODS region. |
| DEAL_BRNCH_CDE | FOS.NMEMO_DEAL.DEAL_BRANCH_CODE | If region HK then 02. If region SG and FOS.NMEMO_TEMPLATE.ASSET_TYPE = 'FX' then 02 when local currency equals FOS.NMEMO_TEMPLATE.SETTLEMENT_CURRENCY or FOS.NMEMO_TEMPLATE.NMEMO_CURRENCY_CODE. If region SG and asset type is not FX then 02 when local currency equals FOS.NMEMO_TEMPLATE.SETTLEMENT_CURRENCY. Otherwise 03. | Branch or COBR code used for local-versus-overseas booking. Broker rows derive it from region and local-currency exposure. |
| DEAL_TRAN_TYPE | O maps to S, otherwise P | O maps to P, otherwise S | Bank-view open or close direction used downstream for exposure orientation; client and broker rows intentionally invert the O/C mapping. |
| CUST_NUM | FOS.NMEMO_DEAL.CUSTOMER_NUMBER | FOS.NMEMO_BROKER_DEAL.NMEMO_BROKER_NUMBER | Client customer or broker counterparty identifier. |
| SB_ACCT_NUM | FOS.NMEMO_DEAL.SUB_ACCOUNT_NUMBER | Constant '0001' | Account dimension for the target key. Broker rows use the fixed bank sub-account because broker deals do not carry client sub-account granularity. |
| PROD_TYPE | Constant MM | Constant MM | Target product bucket for Generic Memo / structured memo products. |
| DEAL_TYPE | Constant MM | Constant MM | Target deal category used with PROD_TYPE to route Generic Memo deals in downstream GM processing. |
| ASSET_CLASS | Common asset-class mapping from FOS.NMEMO_TEMPLATE.ASSET_TYPE | Common asset-class mapping from FOS.NMEMO_TEMPLATE.ASSET_TYPE on the broker template. | TRS normally outputs MX when FOS.NMEMO_TEMPLATE.ASSET_TYPE is MX. |
| DEPOSIT_LINKED_FLAG | Initial N. Later Y if FOS.NMEMO_TEMPLATE_DEPO exists for the template. | Initial N. Later Y if FOS.NMEMO_TEMPLATE_DEPO exists for the broker template. | Identifies structures with an embedded deposit or funding deposit component that requires later interest enrichment. |
| OPTION_SWAP_FLAG | Y when count of IR underlying legs for the template equals 2, otherwise N. | Y when count of IR underlying legs for the broker template equals 2, otherwise N. | Marks two-interest-rate-leg structures that should be treated as swap-style memo products. |
| BULLION_FLAG | If top FX underlying exists, call INTERFACE.FN_GET_FTP_BULLION_FLAG using the two currencies in UNDERLYING_ISSUE_CODE. Otherwise map FOS.NMEMO_TEMPLATE.NMEMO_CURRENCY_CODE XAU = 1, XAG = 2, XPT = 3, XPD = 4, else 0. | Uses the broker template's top FX underlying or FOS.NMEMO_TEMPLATE.NMEMO_CURRENCY_CODE to derive the precious-metal code through the identical function and currency-code rule. | Precious-metal exposure indicator for FX or bullion-linked structures; 0 means no supported bullion currency was detected. |
| TOTAL_RETURN_SWAP_FLAG | Y when FOS.NMEMO_TEMPLATE.NMEMO_CODE in ('TR4','TR5'), FOS.NMEMO_TEMPLATE.ASSET_TYPE = 'MX', and primary underlying class in EQ or NX. Otherwise N. | Y when the broker template has NMEMO_CODE in ('TR4','TR5'), ASSET_TYPE = 'MX', and primary underlying class in EQ or NX. Otherwise N. | Main TRS classifier in GM_COMMON_DEAL. It is driven by template code and underlying class, not by a standalone source TRS flag. |
| TOTAL_INT_AMT | Initial 0. Later calculated from TEMPLATE_DEPO: ANNUAL uses NOTIONAL_AMT * DEPO_LEVEL / 100 * days(FIXING_DT - VALUE_DT) / 365; PERIOD uses NOTIONAL_AMT * DEPO_LEVEL / 100. | Initial 0. Later calculated from the broker template's FOS.NMEMO_TEMPLATE_DEPO after notional enrichment. | Accrued or period deposit interest for deposit-linked memo products; calculated after the final notional is available. |
| BANK_CUST_FLAG | C | B | Side indicator that separates client exposure rows from broker or hedge rows and drives side-specific downstream aggregation. |
| OPT_POSN_TYPE | FOS.NMEMO_DEAL.DEAL_TRANSACTION_TYPE | FOS.NMEMO_BROKER_DEAL.DEAL_TRANSACTION_TYPE | Original lifecycle or position type, normally O or C at insert. |
| OPT_TYPE | FOS.NMEMO_TEMPLATE.OPTION_TYPE when it is C or P, else empty string | FOS.NMEMO_TEMPLATE.OPTION_TYPE on the broker template when it is C or P, else empty string. | Option call or put type when the memo product has an option payoff; blank for non-option TRS structures. |
| OPT_STYL_CDE | E when FOS.NMEMO_TEMPLATE.NMEMO_PAYOFF_STYLE = 'EURO', else A | E when the broker template payoff style is EURO, else A. | Option exercise style used by downstream option-style reporting; non-European or missing styles default to American-style code A. |
| TRADE_DT | FOS.NMEMO_DEAL.NMEMO_TRADE_DATE | FOS.NMEMO_BROKER_DEAL.NMEMO_TRADE_DATE | Trade execution date for the client or hedge-side source deal. |
| MTUR_DT | FOS.NMEMO_DEAL.NMEMO_DELIVERY_DATE | FOS.NMEMO_TEMPLATE.NMEMO_DELIVERY_DATE | Contract maturity or delivery date used for exposure aging and maturity reporting. |
| VALUE_DT | FOS.NMEMO_DEAL.NMEMO_VALUE_DATE | FOS.NMEMO_BROKER_DEAL.NMEMO_VALUE_DATE | Value date from which settlement, accrual and new-trade previous outstanding logic are anchored. |
| FIXING_DT | FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE | FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE | Final contractual fixing or expiry date from the product template. |
| KNOCKOUT_DT | Initially null. Later set from FOS.NMEMO_PAYMENT.FIXING_DATE where FIXING_EVENT_TYPE = 'ER' and STATUS_FLAG in ('CONFIRMED','PREV'). | Initially null. Later set from FOS.NMEMO_BANK_PAYMENT.FIXING_DATE where FIXING_EVENT_TYPE = 'ER' and STATUS_FLAG in ('CONFIRMED','PREV'). | Early redemption or knockout event date; populated only when confirmed or previous early-redemption payment records exist. |
| NOTIONAL_CCY | If FOS.NMEMO_DEAL.NMEMO_CURRENCY_CODE = 'ASSET' then FOS.NMEMO_DEAL.SETTLEMENT_CURRENCY_CODE, else FOS.NMEMO_DEAL.NMEMO_CURRENCY_CODE. | If FOS.NMEMO_TEMPLATE.NMEMO_CURRENCY_CODE = 'ASSET' then FOS.NMEMO_TEMPLATE.SETTLEMENT_CURRENCY, else FOS.NMEMO_TEMPLATE.NMEMO_CURRENCY_CODE. | Reported notional currency. |
| NOTIONAL_AMT | Initial FOS.NMEMO_DEAL.SETTLEMENT_NOTIONAL for asset currency, else FOS.NMEMO_DEAL.NMEMO_NOMINAL. Final value is multiplied by leverage factor. | Initial 0 for asset currency, else FOS.NMEMO_BROKER_DEAL.NMEMO_NOMINAL. If asset currency, later replaced by sum of linked client NOTIONAL_AMT before leverage is applied. Final value is multiplied by leverage factor. | Gross trade notional after leverage adjustment. For asset-currency broker rows, notional is reconstructed from linked client exposure before final leverage. |
| OUTSTANDING_NOTIONAL_AMT | Initial basis follows NOTIONAL_AMT before leverage. Later replaced by min remaining settlement notional for asset currency or min remaining nmemo notional for non-asset currency from FOS.NMEMO_PAYMENT. Final value is multiplied by leverage factor. Close deals may copy the matched open deal outstanding amount. | Initial basis follows broker NOTIONAL_AMT before leverage. Later replaced by min remaining settlement or nmemo notional from FOS.NMEMO_BANK_PAYMENT; broker asset-currency rows first use linked client notional before payment override and leverage. Close deals may copy the matched open deal outstanding amount. | Current remaining exposure amount after payment-event reduction, close-deal linkage and leverage adjustment. |
| PREVIOUS_OUTSTANDING_NOTIONAL_AMT | If current row exists in GM_COMMON_DEAL_LASTDAY, use lastday OUTSTANDING_NOTIONAL_AMT. Otherwise, for rows created on BUS_DT, use NOTIONAL_AMT * leverage factor. | Uses INTERFACE.GM_COMMON_DEAL_LASTDAY broker rows where BANK_CUST_FLAG = 'B'; for new broker rows created on BUS_DT, uses NOTIONAL_AMT * leverage factor. | Previous business day remaining exposure used for day-on-day movement and new-trade initialization. |
| PREM_CCY | FOS.NMEMO_DEAL.SETTLE_CURRENCY_CODE | FOS.NMEMO_TEMPLATE.SETTLEMENT_CURRENCY | Premium or consideration currency. |
| PREM_AMT | ABS(FOS.NMEMO_DEAL.CONSIDERATION_AMOUNT) | ABS(FOS.NMEMO_BROKER_DEAL.CONSIDERATION_AMOUNT) | Absolute premium or consideration amount. |
| PREM_SETL_DT | FOS.NMEMO_DEAL.NMEMO_VALUE_DATE | FOS.NMEMO_BROKER_DEAL.NMEMO_VALUE_DATE | Date on which the premium or consideration amount is expected to settle. |
| PREM_PAY_RECEIVE_FLAG | P when FOS.NMEMO_DEAL.CONSIDERATION_AMOUNT > 0, else R | R when FOS.NMEMO_BROKER_DEAL.CONSIDERATION_AMOUNT > 0, else P | Bank-view premium direction. |
| STRK_PRICE | FOS.NMEMO_ORDER_STATEMENT_DETAIL.FIELD_VALUE after comma removal and decimal cast; default 1. | FOS.NMEMO_ORDER_STATEMENT_DETAIL.FIELD_VALUE after joining FOS.NMEMO_BANK_ORDER to the latest eligible FOS.NMEMO_ORDER; comma removal and decimal cast are applied, default 1. | Strike or reference price captured from order statement details. For TRS, this can represent the initial reference price used for equity or index performance. |
| MTM_CCY | Initially empty. Later from INTERFACE.MEMO_PRODUCT_POSITION.DEAL_CURRENCY_CODE. If still blank, fallback to PREM_CCY. Close deals can copy from matched open deal. | Initially empty. Later derived from linked client rows grouped by broker link. If still blank, fallback to PREM_CCY. Close deals can copy from matched open deal. | Currency used to report mark-to-market. Client rows use position currency; broker rows inherit the aggregated client-side currency where a broker hedge covers client deals. |
| CUR_MTM_AMT | Initially 0. Later INTERFACE.MEMO_PRODUCT_POSITION.MARKET_VALUE * -1, unless FINAL_FIXING_FLAG = 'Y' then 0. | Initially 0. Later negative sum of linked client CUR_MTM_AMT. | Current bank-view mark-to-market amount. Client position market value is sign-flipped; broker rows offset the linked client MTM. |
| PREVIOUS_MTM_AMT | Initially 0. Later INTERFACE.MEMO_PRODUCT_POSITION_LASTDAY.MARKET_VALUE * -1, default 0. | Initially 0. Later negative sum of linked client PREVIOUS_MTM_AMT. | Previous-day MTM in bank view. |
| UNDL_ISSUE_CDE | Initial FX/MX-FX top underlying issue. Later, for single EQ or NX underlying templates only, replaced by FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_ISSUE_CODE. | Initial FX/MX-FX top underlying issue from the broker template. Later, for single EQ or NX underlying broker templates only, replaced by FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_ISSUE_CODE. | Primary underlying issue used for single-name reporting. Basket structures are intentionally not forced into one issue by the enrichment rule. |
| UNDL_ISSUE_CRNCY_CDE | Initially empty. Later single EQ or NX underlying currency from FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_CURRENCY_CODE. Null cleanup sets empty string. | Initially empty. Later single EQ or NX underlying currency from the broker template's FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_CURRENCY_CODE. Null cleanup sets empty string. | Currency of the single-name equity or index underlying after enrichment; blank for basket or unsupported structures. |
| UNDL_REUTERS_CDE | Initially empty. Later INTERFACE.SHARE_ISSUE.REUTERS_CDE for a single EQ or NX underlying. | Initially empty. Later INTERFACE.SHARE_ISSUE.REUTERS_CDE for the broker template's single EQ or NX underlying. | Reuters or market ticker used for listed-underlying reporting when a single issue can be resolved. |
| UNDL_ISIN_CDE | Initial FOS.NMEMO_TEMPLATE.NMEMO_ISIN_CODE on client side. Later single EQ or NX update also uses FOS.NMEMO_TEMPLATE.NMEMO_ISIN_CODE. | Initially empty. Later single EQ or NX update uses FOS.NMEMO_TEMPLATE.NMEMO_ISIN_CODE. | ISIN used for listed-underlying identification when the template or single-underlying enrichment can provide one. |
| UNDL_CTRY_CDE | Initially empty. Later INTERFACE.SHARE_ISSUE.SHARE_CTRY_CDE for a single EQ or NX underlying. | Initially empty. Later INTERFACE.SHARE_ISSUE.SHARE_CTRY_CDE for the broker template's single EQ or NX underlying. | Country of risk or listing country for the resolved single underlying. |
| GUARANTOR_NUM | FOS.NMEMO_TEMPLATE_AGENT_DETAIL.GUARANTOR_NUMBER | FOS.NMEMO_TEMPLATE_AGENT_DETAIL.GUARANTOR_NUMBER | Guarantor party identifier attached to the product template for credit support or issuer-related reporting. |
| ISSUER_NUM | FOS.NMEMO_TEMPLATE_AGENT_DETAIL.ISSUER_NUMBER | FOS.NMEMO_TEMPLATE_AGENT_DETAIL.ISSUER_NUMBER | Issuer party identifier attached to the product template for product issuer and counterparty reporting. |
| OPEN_DEAL_NUM | Initial 0. Later set by matching open and close rows in FOS.NMEMO_DEAL for the relevant customer, sub account and deal number with opposite OPT_POSN_TYPE. | Initial 0. Later set from bank order and client order linkage. The latest fixed SQL has two broker merges: one for open rows and one for close rows. | Original open deal number used to tie close rows back to the lifecycle-opening trade. |
| OPEN_DEAL_SB_NUM | Initial 0. Later set to the matched open sub-deal on client side. | Initial 0. Broker merge sets 0. | Sub-deal component of the original open trade linkage; broker-side lifecycle linkage is held at sub number 0. |
| LINK_DEAL_NUM | FOS.NMEMO_DEAL.BROKER_DEAL_NUMBER | Initial 0. Null cleanup also sets 0 for supported asset classes. | Client-to-broker hedge link. Broker side does not link back to all client rows because one broker deal may hedge multiple clients. |
| LINK_DEAL_SB_NUM | FOS.NMEMO_DEAL.BROKER_DEAL_SUB_NUMBER | Initial 0. Null cleanup also sets 0. | Broker hedge sub-deal link. |
| ADVANCE_PURPOSE_CODE | If FOS.NMEMO_TEMPLATE.ASSET_TYPE = 'MX', use primary underlying class, with NX normalized to EQ; otherwise empty string. Null cleanup sets empty string. | If the broker template ASSET_TYPE = 'MX', use primary underlying class, with NX normalized to EQ; otherwise empty string. Null cleanup sets empty string. | Exposure-purpose code for multi-asset products; equity-index TRS is normalized to EQ for downstream classification. |
| TEMPLATE_ISSUE_CODE | FOS.NMEMO_TEMPLATE.NMEMO_ISSUE_CODE | FOS.NMEMO_BROKER_DEAL.NMEMO_ISSUE_CODE | Template identity used by later joins and updates. |
| FINAL_FIXING_FLAG | Initial N. Later Y when latest eligible payment has remaining settlement notional 0 and event type ER or M, or fixing date is greater than or equal to FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE. | Initial N. Later Y when latest eligible FOS.NMEMO_BANK_PAYMENT has remaining settlement notional 0 and event type ER or M, or fixing date is greater than or equal to FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE. | Final maturity or early-redemption indicator used to stop MTM carry-forward and identify fully fixed deals. |
| CREAT_TS | FOS.NMEMO_DEAL.CREATE_TIMESTAMP | FOS.NMEMO_BROKER_DEAL.CREATE_TIMESTAMP | Source creation timestamp used for auditability and new-trade logic. |
| RVRSE_TS | FOS.NMEMO_DEAL.REVERSE_TIMESTAMP | FOS.NMEMO_BROKER_DEAL.REVERSE_TIMESTAMP | Source reversal timestamp used to exclude immediately reversed records and retain reversal audit state. |
| UPDT_TS | FOS.NMEMO_DEAL.UPDATE_TIMESTAMP | FOS.NMEMO_BROKER_DEAL.UPDATE_TIMESTAMP | Source last update timestamp carried forward for operational audit and reconciliation. |
| OUR_NOSTRO_NUMBER | Not inserted by client side SQL. | FOS.NMEMO_BROKER_DEAL.OUR_NOSTRO_NUMBER | Broker-side nostro account added by the broker insert. |

### 4.1 GM_COMMON_DEAL Source Filters

| Side | Main filters |
|---|---|
| Client | FOS.NMEMO_DEAL.ACTIVE_FLAG = 'A'; FOS.NMEMO_TEMPLATE.ACTIVE_FLAG = 'A'; DEAL_TRANSACTION_TYPE in ('O','C'); rows where CREATE_TIMESTAMP and REVERSE_TIMESTAMP are both non-null and equal are excluded; duplicate reversed sub-deals are excluded by NOT EXISTS reverse-deal logic. |
| Broker | FOS.NMEMO_BROKER_DEAL.ACTIVE_FLAG = 'A'; FOS.NMEMO_TEMPLATE.ACTIVE_FLAG = 'A'; DEAL_TRANSACTION_TYPE in ('O','C'); rows where CREATE_TIMESTAMP and REVERSE_TIMESTAMP are both non-null and equal are excluded. |

Post-insert update and merge logic is embedded directly in the affected field rows above. This includes FINAL_FIXING_FLAG, OPEN_DEAL_NUM, previous/current outstanding notional, leverage, MTM, KNOCKOUT_DT, deposit-linked fields, underlying enrichment, and cleanup fallback rules.

## 5. INTERFACE.GM_COMMON_DEAL_EXT Field Mapping

Important scope note: INTERFACE.PR_TRANSFORM_GM_COMMON_DEAL_EXT only inserts rows where FOS.NMEMO_TEMPLATE.ASSET_TYPE = 'IR'. It does not create extension rows for a normal equity TRS template where ASSET_TYPE = 'MX'. If TRS funding legs must appear in this table, the source model or procedure scope needs explicit confirmation.

| Target field | Client side mapping | Broker side mapping | Business description |
|---|---|---|---|
| ACTV_FLAG | FOS.NMEMO_DEAL.ACTIVE_FLAG | FOS.NMEMO_BROKER_DEAL.ACTIVE_FLAG | Indicates the active source deal used to create an IR extension row; insert filters require active source deals and active templates. |
| DEAL_NUM | FOS.NMEMO_DEAL.DEAL_NUMBER | FOS.NMEMO_BROKER_DEAL.DEAL_NUMBER | Deal identity carried into GM_COMMON_DEAL_EXT so the IR leg row can be joined back to GM_COMMON_DEAL. Avaloq feeds must provide it through the cross-reference layer, not as a native Avaloq contract number. |
| DEAL_SB_NUM | FOS.NMEMO_DEAL.DEAL_SUB_NUMBER | Constant 0 | Sub-deal key component for joining extension rows to the base deal. Broker extension rows are normalized to sub number 0. |
| DEAL_COMP_CDE | FOS.NMEMO_DEAL.DEAL_COMPANY_CODE | Region SG = 68, HK = 63 | Legal entity or company code for the IR extension; client uses source company while broker derives from ODS region. |
| DEAL_BRNCH_CDE | FOS.NMEMO_DEAL.DEAL_BRANCH_CODE | HK = 02. SG = 02 when local currency equals FOS.NMEMO_TEMPLATE.SETTLEMENT_CURRENCY, else 03. | Branch/COBR code for the extension row, aligned with the deal's local-versus-overseas booking treatment. |
| DEAL_TRAN_TYPE | FOS.NMEMO_DEAL.DEAL_TRANSACTION_TYPE O maps to S, otherwise P | FOS.NMEMO_BROKER_DEAL.DEAL_TRANSACTION_TYPE O maps to P, otherwise S | Bank-view direction for the IR leg extension; broker orientation is inverted versus client orientation. |
| CUST_NUM | FOS.NMEMO_DEAL.CUSTOMER_NUMBER | FOS.NMEMO_BROKER_DEAL.NMEMO_BROKER_NUMBER | Client customer or broker counterparty key used with deal number and sub-account to identify the extension row. |
| SB_ACCT_NUM | FOS.NMEMO_DEAL.SUB_ACCOUNT_NUMBER | Constant '0001' | Account dimension for the extension key. Broker rows use the fixed bank sub-account because broker deals do not carry client sub-account granularity. |
| DEAL_TYPE | Constant MM | Constant MM | Target deal category used with PROD_TYPE for Generic Memo IR extension processing. |
| PROD_TYPE | Constant MM | Constant MM | Target product bucket for Generic Memo / structured memo products in the extension table. |
| ASSET_CLASS | Common asset-class mapping from FOS.NMEMO_TEMPLATE.ASSET_TYPE | Common asset-class mapping from FOS.NMEMO_TEMPLATE.ASSET_TYPE on the broker template. | Because this procedure filters FOS.NMEMO_TEMPLATE.ASSET_TYPE = 'IR', output is normally IR. |
| GROS_SETL_FLAG | Constant '0' | Constant '0' | Gross settlement indicator required by the target table; current SQL always outputs 0, so net/gross settlement is not differentiated here. |
| PAY_INT_RATE_CDE | FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_ISSUE_CODE for the client pay leg. If length > 4, use first 2 characters plus last 2 characters. | FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_ISSUE_CODE for the broker pay leg. Broker pay leg is selected from FOS.NMEMO_LEG where LEG_WEIGHT < 0. If length > 4, use first 2 characters plus last 2 characters. | Reference-rate code for the pay leg, normalized to the target short code format when the source issue code is longer than four characters. |
| RECV_INT_RATE_CDE | FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_ISSUE_CODE for the client receive leg. If length > 4, use first 2 characters plus last 2 characters. | FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_ISSUE_CODE for the broker receive leg. Broker receive leg is selected from FOS.NMEMO_LEG where LEG_WEIGHT >= 0. If length > 4, use first 2 characters plus last 2 characters. | Reference-rate code for the receive leg, normalized to the target short code format when the source issue code is longer than four characters. |
| PAY_UNDL_CCY_CDE | FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_CURRENCY_CODE for the client pay leg. | FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_CURRENCY_CODE for the broker pay leg. | Pay leg underlying or reference-rate currency used to interpret the pay-side interest leg. |
| RECV_UNDL_CCY_CDE | FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_CURRENCY_CODE for the client receive leg. | FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_CURRENCY_CODE for the broker receive leg. | Receive leg underlying or reference-rate currency used to interpret the receive-side interest leg. |
| PAY_INT_CAL_METH | Constant '1' | Constant '1' | Pay-leg interest calculation method code required by GM_COMMON_DEAL_EXT; current SQL hard-codes method 1. |
| RECV_INT_CAL_METH | Constant '1' | Constant '1' | Receive-leg interest calculation method code required by GM_COMMON_DEAL_EXT; current SQL hard-codes method 1. |
| PAY_MRGN_INT_RATE | FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_INITIAL_SPOT when the pay leg UNDERLYING_ISSUE_CODE is FIX, else 0. | FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_INITIAL_SPOT when the broker pay leg UNDERLYING_ISSUE_CODE is FIX, else 0. | Fixed margin or fixed coupon rate on the pay leg; floating-reference legs output 0. |
| RECV_MRGN_INT_RATE | FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_INITIAL_SPOT when the receive leg UNDERLYING_ISSUE_CODE is FIX, else 0. | FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_INITIAL_SPOT when the broker receive leg UNDERLYING_ISSUE_CODE is FIX, else 0. | Fixed margin or fixed coupon rate on the receive leg; floating-reference legs output 0. |
| PAY_RATE_FIXING_FREQUENCY | From FOS.NMEMO_LEG_FIXING_SCHEDULE_EXT.FREQUENCY on the client pay leg: MONTHLY = '1', QUARTERLY = '3', SEMI-ANNUALLY = '6', ANNUALLY = 'A', else '0'. | From FOS.NMEMO_LEG_FIXING_SCHEDULE_EXT.FREQUENCY on the broker pay leg: MONTHLY = '1', QUARTERLY = '3', SEMI-ANNUALLY = '6', ANNUALLY = 'A', else '0'. Current visible fixed SQL still has an OCR placeholder around this broker expression and should be repaired before DB2 execution. | Pay leg rate-reset frequency code. It controls how often the floating or resettable pay leg should refix. |
| REC_RATE_FIXING_FREQUENCY | From FOS.NMEMO_LEG_FIXING_SCHEDULE_EXT.FREQUENCY on the client receive leg: MONTHLY = '1', QUARTERLY = '3', SEMI-ANNUALLY = '6', ANNUALLY = 'A', else '0'. | From FOS.NMEMO_LEG_FIXING_SCHEDULE_EXT.FREQUENCY on the broker receive leg: MONTHLY = '1', QUARTERLY = '3', SEMI-ANNUALLY = '6', ANNUALLY = 'A', else '0'. | Receive leg rate-reset frequency code. It controls how often the floating or resettable receive leg should refix. |
| PAY_NXT_RT_FIXING_DT | COALESCE(client pay-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.NEXT_FIXING_DATE, FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE). | COALESCE(broker pay-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.NEXT_FIXING_DATE, FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE). | Next scheduled rate fixing date for the pay leg, with expiry date as fallback. |
| RECV_NXT_RT_FIXING_DT | COALESCE(client receive-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.NEXT_FIXING_DATE, FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE). | COALESCE(broker receive-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.NEXT_FIXING_DATE, FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE). | Next scheduled rate fixing date for the receive leg, with expiry date as fallback. |
| PAY_SETL_FREQ | Payment schedule frequency on the client pay leg: MONTHLY = 1, QUARTERLY = 3, SEMI-ANNUALLY = 6, ANNUALLY = 12, else 0. | Payment schedule frequency on the broker pay leg: MONTHLY = 1, QUARTERLY = 3, SEMI-ANNUALLY = 6, ANNUALLY = 12, else 0. | Pay leg settlement frequency in months for cashflow scheduling. |
| RECV_SETL_FREQ | Payment schedule frequency on the client receive leg: MONTHLY = 1, QUARTERLY = 3, SEMI-ANNUALLY = 6, ANNUALLY = 12, else 0. | Payment schedule frequency on the broker receive leg: MONTHLY = 1, QUARTERLY = 3, SEMI-ANNUALLY = 6, ANNUALLY = 12, else 0. | Receive leg settlement frequency in months for cashflow scheduling. |
| PAY_NEXT_SETL_DT | COALESCE(client pay-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.NEXT_SETTLE_DATE, FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE). | COALESCE(broker pay-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.NEXT_SETTLE_DATE, FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE). | Next scheduled settlement date for the pay leg, with expiry date as fallback. |
| RECV_NEXT_SETL_DT | COALESCE(client receive-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.NEXT_SETTLE_DATE, FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE). | COALESCE(broker receive-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.NEXT_SETTLE_DATE, FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE). | Next scheduled settlement date for the receive leg, with expiry date as fallback. |
| SCH_START_DATE | COALESCE(client pay-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.LAST_FIXING_DATE, client receive-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.LAST_FIXING_DATE, FOS.NMEMO_DEAL.NMEMO_VALUE_DATE). | COALESCE(broker pay-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.LAST_FIXING_DATE, broker receive-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.LAST_FIXING_DATE, FOS.NMEMO_BROKER_DEAL.NMEMO_VALUE_DATE). | Current schedule period start or previous reset boundary for the two-leg IR extension. |
| SCH_END_DATE | COALESCE(client pay-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.NEXT_FIXING_DATE, client receive-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.NEXT_FIXING_DATE, FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE). | COALESCE(broker pay-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.NEXT_FIXING_DATE, broker receive-leg FOS.NMEMO_FIXING_DATE_DETAIL_EXT.NEXT_FIXING_DATE, FOS.NMEMO_TEMPLATE.NMEMO_EXPIRY_DATE). | Current schedule period end or next reset boundary for the two-leg IR extension. |
| PERIOD_NUMBER | Constant 0 | Constant 0 | No longer populated from period schedule. |

| Side | Leg sign convention in DEAL_EXT |
|---|---|
| Client | Pay leg uses FOS.NMEMO_LEG.LEG_WEIGHT >= 0. Receive leg uses LEG_WEIGHT < 0. |
| Broker | Pay leg uses FOS.NMEMO_LEG.LEG_WEIGHT < 0. Receive leg uses LEG_WEIGHT >= 0. |

## 6. INTERFACE.GM_COMMON_FIXING_DEAL Field Mapping

The fixing procedure has a client insert with 35 target columns and a broker insert with 39 target columns. The broker insert includes four additional nostro fields.

| Target field | Client side mapping | Broker side mapping | Business description |
|---|---|---|---|
| ACTV_FLAG | FOS.NMEMO_PAYMENT.ACTIVE_FLAG | FOS.NMEMO_BANK_PAYMENT.ACTIVE_FLAG | Indicates an active payment or bank-payment event eligible for the fixing target; insert filters require ACTIVE_FLAG = 'A'. |
| DEAL_NUM | FOS.NMEMO_PAYMENT.NMEMO_DEAL_NUMBER | FOS.NMEMO_BANK_PAYMENT.NMEMO_BANK_DEAL_NUMBER | ODS deal key for the fixing event. Avaloq feeds must obtain this through the TOM/Sparta/Avaloq cross-reference layer before GM_COMMON_FIXING_DEAL conversion. |
| DEAL_SB_NUM | FOS.NMEMO_PAYMENT.NMEMO_DEAL_SUB_NUMBER | FOS.NMEMO_BANK_PAYMENT.NMEMO_BANK_DEAL_SUB_NUMBER | ODS sub-deal key for the fixing event, used with DEAL_NUM and side dimensions to link the cashflow back to the base GM_COMMON_DEAL row. |
| DEAL_COMP_CDE | Region SG = 68, HK = 63 | Region SG = 68, HK = 63 | Legal entity or company code for fixing rows; both client and broker sides derive it from ODS region in the fixed SQL. |
| DEAL_BRNCH_CDE | HK = 02. SG = 02 when local currency equals FOS.NMEMO_PAYMENT.PAY_CURRENCY_CODE or FOS.NMEMO_PAYMENT.RECEIVE_CURRENCY_CODE, else 03. | HK = 02. SG = 02 when local currency equals FOS.NMEMO_BANK_PAYMENT.PAY_CURRENCY_CODE or FOS.NMEMO_BANK_PAYMENT.RECEIVE_CURRENCY_CODE, else 03. | Latest fixed SQL derives fixing branch from actual payment currencies, not template settlement currency. |
| DEAL_TRAN_TYPE | FOS.NMEMO_DEAL.DEAL_TRANSACTION_TYPE | FOS.NMEMO_BROKER_DEAL.DEAL_TRANSACTION_TYPE | Lifecycle transaction type of the underlying deal associated with the fixing event. The SQL comment says 'F', but the expression uses the source deal transaction type. |
| CUST_NUM | FOS.NMEMO_PAYMENT.CUSTOMER_NUMBER | FOS.NMEMO_BANK_PAYMENT.BROKER_NUMBER | Client customer or broker counterparty. |
| SB_ACCT_NUM | FOS.NMEMO_PAYMENT.SUB_ACCOUNT_NUMBER | Constant '0001' | Account dimension for the fixing key. Broker payment rows use the fixed bank sub-account because bank-payment events do not carry client sub-account granularity. |
| DEAL_TYPE | Constant MM | Constant MM | Target deal category used with PROD_TYPE to route Generic Memo fixing rows. |
| PROD_TYPE | Constant MM | Constant MM | Target product bucket for Generic Memo / structured memo fixing events. |
| ASSET_CLASS | Common asset-class mapping from FOS.NMEMO_TEMPLATE.ASSET_TYPE | Common asset-class mapping from FOS.NMEMO_TEMPLATE.ASSET_TYPE on the broker template. | Asset class of the original deal template used to classify the fixing event; TRS rows normally inherit MX. |
| BANK_CUST_FLAG | C | B | Side indicator separating client cashflows from broker or hedge cashflows in the target. |
| FIXING_EVENT_TYPE | FOS.NMEMO_PAYMENT.FIXING_EVENT_TYPE | FOS.NMEMO_BANK_PAYMENT.FIXING_EVENT_TYPE | Event type such as ER, M, PD or other source values. |
| START_DT | FOS.NMEMO_PAYMENT.PERIOD_START_DATE | FOS.NMEMO_BANK_PAYMENT.PERIOD_START_DATE | Cashflow period start date. |
| END_DT | FOS.NMEMO_PAYMENT.FIXING_DATE | FOS.NMEMO_BANK_PAYMENT.FIXING_DATE | Fixing date or cashflow period end. |
| CASH_SETL_FLAG | Y when FOS.NMEMO_TEMPLATE.NMEMO_SETTLEMENT_TYPE = 'CASH', else N | Y when the broker template FOS.NMEMO_TEMPLATE.NMEMO_SETTLEMENT_TYPE = 'CASH', else N. | Indicates whether the fixing event settles in cash rather than by physical delivery or linked non-cash settlement. |
| INT_RATE | COALESCE(FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_INITIAL_SPOT for IR/FIX underlying, 0) | COALESCE(FOS.NMEMO_LEG_UNDERLYING.UNDERLYING_INITIAL_SPOT for broker-template IR/FIX underlying, 0). | Interest or coupon rate resolved from a FIX IR underlying; non-FIX or missing IR legs default to 0. |
| PAY_RECEIVE_FLAG | If FOS.NMEMO_PAYMENT.PAY_AMOUNT > 0 and FOS.NMEMO_PAYMENT.RECEIVE_AMOUNT is null then R. If FOS.NMEMO_PAYMENT.RECEIVE_AMOUNT > 0 and FOS.NMEMO_PAYMENT.PAY_AMOUNT is null then P. Else empty. | If FOS.NMEMO_BANK_PAYMENT.PAY_AMOUNT > 0 and FOS.NMEMO_BANK_PAYMENT.RECEIVE_AMOUNT is null then P. If FOS.NMEMO_BANK_PAYMENT.RECEIVE_AMOUNT > 0 and FOS.NMEMO_BANK_PAYMENT.PAY_AMOUNT is null then R. Else empty. | Bank-view pay or receive flag. Client and broker rules intentionally differ. |
| SETL_DT | FOS.NMEMO_PAYMENT.SETTLE_DATE | FOS.NMEMO_BANK_PAYMENT.SETTLE_DATE | Cash settlement date for the fixing/payment event. |
| SETL_CCY | When FOS.NMEMO_PAYMENT.FX_DEAL_NUMBER is null, COALESCE(FOS.NMEMO_PAYMENT.PAY_CURRENCY_CODE, FOS.NMEMO_PAYMENT.RECEIVE_CURRENCY_CODE). | When FOS.NMEMO_BANK_PAYMENT.FX_DEAL_NUMBER is null, COALESCE(FOS.NMEMO_BANK_PAYMENT.PAY_CURRENCY_CODE, FOS.NMEMO_BANK_PAYMENT.RECEIVE_CURRENCY_CODE). | Direct settlement currency when no linked FX deal exists. |
| SETL_AMT | When FOS.NMEMO_PAYMENT.FX_DEAL_NUMBER is null, COALESCE(FOS.NMEMO_PAYMENT.PAY_AMOUNT, FOS.NMEMO_PAYMENT.RECEIVE_AMOUNT, 0); else 0. | When FOS.NMEMO_BANK_PAYMENT.FX_DEAL_NUMBER is null, COALESCE(FOS.NMEMO_BANK_PAYMENT.PAY_AMOUNT, FOS.NMEMO_BANK_PAYMENT.RECEIVE_AMOUNT, 0); else 0. | Direct settlement amount. FX settlement rows carry buy/sell fields instead. |
| BUY_CCY | When FOS.NMEMO_PAYMENT.FX_DEAL_NUMBER is not null, FOS.NMEMO_PAYMENT.RECEIVE_CURRENCY_CODE. | When FOS.NMEMO_BANK_PAYMENT.FX_DEAL_NUMBER is not null, FOS.NMEMO_BANK_PAYMENT.RECEIVE_CURRENCY_CODE. | Currency bought in the linked FX settlement leg; only populated when the fixing event references an FX deal. |
| BUY_AMT | When FOS.NMEMO_PAYMENT.FX_DEAL_NUMBER is not null, COALESCE(FOS.NMEMO_PAYMENT.RECEIVE_AMOUNT, 0). | When FOS.NMEMO_BANK_PAYMENT.FX_DEAL_NUMBER is not null, COALESCE(FOS.NMEMO_BANK_PAYMENT.RECEIVE_AMOUNT, 0). | Amount bought in the linked FX settlement leg; defaults to 0 when receive amount is missing. |
| MRK_RECV_SETL_DT | FOS.NMEMO_PAYMENT.RECEIVE_MARK_SETTLE_DATE | Linked client FOS.NMEMO_PAYMENT.RECEIVE_MARK_SETTLE_DATE joined by bank deal number and bank deal sub number. | Market settlement date for the receive side of the linked settlement leg; broker rows inherit it from the linked client payment. |
| SELL_CCY | When FOS.NMEMO_PAYMENT.FX_DEAL_NUMBER is not null, FOS.NMEMO_PAYMENT.PAY_CURRENCY_CODE. | When FOS.NMEMO_BANK_PAYMENT.FX_DEAL_NUMBER is not null, FOS.NMEMO_BANK_PAYMENT.PAY_CURRENCY_CODE. | Currency sold in the linked FX settlement leg; only populated when the fixing event references an FX deal. |
| SELL_AMT | When FOS.NMEMO_PAYMENT.FX_DEAL_NUMBER is not null, COALESCE(FOS.NMEMO_PAYMENT.PAY_AMOUNT, 0). | When FOS.NMEMO_BANK_PAYMENT.FX_DEAL_NUMBER is not null, COALESCE(FOS.NMEMO_BANK_PAYMENT.PAY_AMOUNT, 0). | Amount sold in the linked FX settlement leg; defaults to 0 when pay amount is missing. |
| MRK_PAY_SETL_DT | FOS.NMEMO_PAYMENT.PAY_MARK_SETTLE_DATE | Linked client FOS.NMEMO_PAYMENT.PAY_MARK_SETTLE_DATE joined by bank deal number and bank deal sub number. | Market settlement date for the pay side of the linked settlement leg; broker rows inherit it from the linked client payment. |
| CROS_DEAL_NUM | FOS.NMEMO_PAYMENT.DEAL_NUMBER | FOS.NMEMO_BANK_PAYMENT.BANK_DEAL_NUMBER | Original cross or settlement deal number referenced by the fixing payment record. |
| CROS_DEAL_SB_NUM | FOS.NMEMO_PAYMENT.DEAL_SUB_NUMBER | FOS.NMEMO_BANK_PAYMENT.BANK_DEAL_SUB_NUMBER | Sub-deal component of the original cross or settlement deal referenced by the fixing payment record. |
| LINK_DEAL_TYPE | 51 when FOS.NMEMO_PAYMENT.FX_DEAL_NUMBER exists. Else 76 when FOS.NMEMO_PAYMENT.SHARE_DEAL_NUMBER exists. Else 04 when FOS.NMEMO_PAYMENT.PAY_DEAL_NUMBER or FOS.NMEMO_PAYMENT.RECEIVE_DEAL_NUMBER exists. Else empty. | 51 when FOS.NMEMO_BANK_PAYMENT.FX_DEAL_NUMBER exists. Else 76 when linked client FOS.NMEMO_PAYMENT.SHARE_DEAL_NUMBER exists. Else 04 when linked client FOS.NMEMO_PAYMENT.PAY_DEAL_NUMBER or FOS.NMEMO_PAYMENT.RECEIVE_DEAL_NUMBER exists. Else empty. | Downstream linked settlement type: FX, share, or cash/pay-receive. |
| LINK_DEAL_NUMBER | COALESCE(FOS.NMEMO_PAYMENT.FX_DEAL_NUMBER, FOS.NMEMO_PAYMENT.SHARE_DEAL_NUMBER, FOS.NMEMO_PAYMENT.PAY_DEAL_NUMBER, FOS.NMEMO_PAYMENT.RECEIVE_DEAL_NUMBER). | COALESCE(FOS.NMEMO_BANK_PAYMENT.FX_DEAL_NUMBER, linked client FOS.NMEMO_PAYMENT.SHARE_DEAL_NUMBER). | Broker SQL does not coalesce pay/receive deal number even when LINK_DEAL_TYPE = 04. This is a confirmed mapping gap to review. |
| LINK_DEAL_SUB_NUMBER | COALESCE(FOS.NMEMO_PAYMENT.FX_DEAL_SUB_NUMBER, FOS.NMEMO_PAYMENT.SHARE_DEAL_SUB_NUMBER, FOS.NMEMO_PAYMENT.PAY_DEAL_SUB_NUMBER, FOS.NMEMO_PAYMENT.RECEIVE_DEAL_SUB_NUMBER). | COALESCE(FOS.NMEMO_BANK_PAYMENT.FX_DEAL_SUB_NUMBER, linked client FOS.NMEMO_PAYMENT.SHARE_DEAL_SUB_NUMBER). | Broker SQL has the linked sub-number gap already noted for LINK_DEAL_NUMBER: pay/receive sub-number is not coalesced even when LINK_DEAL_TYPE = 04. |
| CREAT_TS | FOS.NMEMO_PAYMENT.CREATE_TIMESTAMP | FOS.NMEMO_BANK_PAYMENT.CREATE_TIMESTAMP | Source payment creation timestamp used for event audit and reconciliation. |
| RVRSE_TS | FOS.NMEMO_PAYMENT.REVERSE_TIMESTAMP | FOS.NMEMO_BANK_PAYMENT.REVERSE_TIMESTAMP | Source payment reversal timestamp carried forward for reversal audit state. |
| UPDT_TS | FOS.NMEMO_PAYMENT.UPDATE_TIMESTAMP | FOS.NMEMO_BANK_PAYMENT.UPDATE_TIMESTAMP | Source payment last update timestamp carried forward for operational audit and reconciliation. |
| PAY_OUR_NOSTRO_NUMBER | Not inserted by client side SQL. | FOS.NMEMO_BANK_PAYMENT.PAY_OUR_NOSTRO_NUMBER | Broker-side pay nostro account. |
| RECEIVE_OUR_NOSTRO_NUMBER | Not inserted by client side SQL. | FOS.NMEMO_BANK_PAYMENT.RECEIVE_OUR_NOSTRO_NUMBER | Broker-side receive nostro account. |
| PAY_THEIR_NOSTRO_NUMBER | Not inserted by client side SQL. | FOS.NMEMO_BANK_PAYMENT.PAY_THEIR_NOSTRO_NUMBER | Counterparty pay nostro account. |
| RECEIVE_THEIR_NOSTRO_NUMBER | Not inserted by client side SQL. | FOS.NMEMO_BANK_PAYMENT.RECEIVE_THEIR_NOSTRO_NUMBER | Counterparty receive nostro account. |

### 6.1 GM_COMMON_FIXING_DEAL Source Filters and Deduplication

| Side | Source filter and dedup logic |
|---|---|
| Client | FOS.NMEMO_PAYMENT joins FOS.NMEMO_DEAL and FOS.NMEMO_TEMPLATE. Payment ACTIVE_FLAG must be A, template ACTIVE_FLAG must be A, and STATUS_FLAG must be CONFIRMED or PREV. Dedup uses ROW_NUMBER partitioned by CUSTOMER_NUMBER, SUB_ACCOUNT_NUMBER, PRODUCT_TYPE, DEAL_NUMBER, DEAL_SUB_NUMBER, CREATE_DATE and ordered by FIXING_DATE descending, COALESCE(REMAINING_SETTLEMENT_NOTIONAL,0) ascending, FIXING_EVENT_TYPE. Only ROW_NUM = 1 is inserted. |
| Broker | FOS.NMEMO_BANK_PAYMENT joins FOS.NMEMO_BROKER_DEAL, linked client FOS.NMEMO_PAYMENT, and FOS.NMEMO_TEMPLATE. Payment ACTIVE_FLAG must be A, template ACTIVE_FLAG must be A, and STATUS_FLAG must be CONFIRMED or PREV. Dedup uses ROW_NUMBER partitioned by BROKER_NUMBER, BANK_DEAL_NUMBER, BANK_DEAL_SUB_NUMBER, CREATE_DATE and ordered by FIXING_DATE descending, COALESCE(REMAINING_SETTLEMENT_NOTIONAL,0) ascending, FIXING_EVENT_TYPE. Only ROW_NUM = 1 is inserted. |

## 7. Code Dictionary

| Field | Value | Meaning |
|---|---|---|
| BANK_CUST_FLAG | C | Client side row. |
| BANK_CUST_FLAG | B | Broker or bank side row. |
| DEAL_TRAN_TYPE | S | Bank sells or pays exposure from the bank-view mapping. |
| DEAL_TRAN_TYPE | P | Bank purchases or receives exposure from the bank-view mapping. |
| ASSET_CLASS | FX | Foreign exchange. |
| ASSET_CLASS | EQ | Equity or equity index after NX normalization. |
| ASSET_CLASS | FI | Fixed income. |
| ASSET_CLASS | IR | Interest rate. |
| ASSET_CLASS | CT | Credit, including source CD normalized to CT. |
| ASSET_CLASS | MX | Multi-asset or structured product bucket. TRS normally falls here. |
| TOTAL_RETURN_SWAP_FLAG | Y | Template code TR4/TR5, template asset type MX, and selected underlying class EQ or NX. |
| TOTAL_RETURN_SWAP_FLAG | N | Not identified as TRS by current SP logic. |
| FIXING_EVENT_TYPE | ER | Early redemption or knockout event. |
| FIXING_EVENT_TYPE | M | Maturity event recognized by final-fixing update. |
| PAY_RECEIVE_FLAG | P | Bank pays under the fixing mapping. |
| PAY_RECEIVE_FLAG | R | Bank receives under the fixing mapping. |
| LINK_DEAL_TYPE | 51 | Linked FX deal. |
| LINK_DEAL_TYPE | 76 | Linked share deal. |
| LINK_DEAL_TYPE | 04 | Linked cash, pay or receive deal. |

## 8. Avaloq and Sparta Migration Notes

The SP logic still targets EFOS/FOS-style GM keys. If TOM order capture is replaced by Sparta and ODS starts receiving Avaloq feeds, the converter must still produce values equivalent to the three SP outputs above.

| Topic | Required design rule |
|---|---|
| Deal identity | Avaloq should not be treated as owning GM_COMMON_DEAL.DEAL_NUM or DEAL_SB_NUM. Use a cross-reference layer to map Avaloq contract/order identity, Sparta order identity, and legacy TOM/FOS deal keys to ODS deal keys. |
| Client and broker sides | The converter must preserve the C/B side model used by the SPs. Client rows and broker rows have different direction, premium, MTM, branch and linkage logic. |
| Fixing events | Avaloq fixing or lifecycle event codes must be normalized to the values expected by the SP logic. If the source uses MT for maturity, normalize to M or enhance the final-fixing logic, because current SP final-fixing logic explicitly checks ER and M plus expiry date. |
| Position and MTM | Existing SP logic gets client MTM from MEMO_PRODUCT_POSITION and derives broker MTM from linked client rows. An Avaloq position feed must either populate compatible position staging tables or the converter must directly reproduce the required bank-view MTM output. |
| Outstanding notional | Deal file gives contractual notional. Fixing or payment events drive remaining notional after partial delivery, maturity or early redemption. Do not derive remaining notional from row order. |
| Broker link | Client LINK_DEAL_NUM points to broker deal number. Broker side is normally 0 because one broker deal can hedge multiple client deals. |

## 9. Validation Against Current Fixed SQL

| Check area | Result | Detail | Required action |
|---|---|---|---|
| GM_COMMON_DEAL client insert | Pass | Target columns through UPDT_TS are represented in Section 4. | Keep column-count regression check when SQL changes. |
| GM_COMMON_DEAL broker insert | Pass with broker-only field | Broker insert has 55 target columns; OUR_NOSTRO_NUMBER is represented in Section 4 as the broker-only column after UPDT_TS. | Confirm target table DDL includes OUR_NOSTRO_NUMBER. |
| GM_COMMON_DEAL broker open-deal update | Pass | The latest fixed SQL includes two broker OPEN_DEAL_NUM merges: one for open rows and one for close rows. | Keep both lifecycle cases in QA. |
| GM_COMMON_DEAL_EXT client insert | Pass | All 31 target fields are represented in Section 5. | Confirm whether TRS funding legs are expected in EXT, because the SQL filters ASSET_TYPE = 'IR'. |
| GM_COMMON_DEAL_EXT broker insert | Needs SQL repair before release | The visible fixed SQL still contains an OCR placeholder around PAY_RATE_FIXING_FREQUENCY. | Restore the missing ELSE '0' END AS PAY_RATE_FIXING_FREQUENCY expression and compile the DB2 procedure. |
| GM_COMMON_FIXING_DEAL client insert | Pass | Client insert has 35 target columns and the mapping is represented in Section 6. | Keep branch and pay/receive direction QA cases. |
| GM_COMMON_FIXING_DEAL broker insert | Pass | Broker insert has 39 target columns including four nostro fields. DEAL_COMP_CDE is present before DEAL_BRNCH_CDE. | Keep column-count regression check when the fixing SP changes. |
| Fixing branch logic | Pass | Section 6 follows the latest payment-currency branch rule for both client and broker side. | No additional MD change required. |
| Fixing pay/receive flag | Pass | Section 6 follows client pay-to-R and broker pay-to-P bank-view rules. | Add side-specific QA cases. |
| Broker fixing LINK_DEAL_NUMBER for type 04 | Open issue | Broker LINK_DEAL_TYPE can be 04 based on linked client FOS.NMEMO_PAYMENT.PAY_DEAL_NUMBER or FOS.NMEMO_PAYMENT.RECEIVE_DEAL_NUMBER, but LINK_DEAL_NUMBER only coalesces FX and share deal number. | Confirm whether pay/receive deal numbers should also be coalesced on broker side. |
| Outstanding notional latest logic | Open issue | SQL selects MAX(FIXING_DATE) but uses MIN remaining notional in the grouped result. The amount is not strictly tied to the max fixing row. | Confirm whether this is intended for partial delivery and maturity events. |
| OCR repaired SQL | Open issue | The fixed SQL files include OCR repair comments and at least one visible OCR placeholder in DEAL_EXT. | Compile and compare with production source before release sign-off. |
