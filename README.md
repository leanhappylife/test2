Here's the modified email text clearly formatted for easy copying directly into Word:

---

**Subject:** Action Required: Approval for AWS SFTP Security Policy Exemption (Exemption ID: 4FF1D855)

**To:** Will Collison
**Cc:** Jhanhavi Ravindra Prabhu, Security Compliance Team
**From:** Samson G Q Su

Dear Will,

I hope you're well.

I'm reaching out to seek your formal approval, as the DSEC control owner, for a temporary exemption from the AWS Transfer Family security policy (P2 control) concerning our PBCCL production environment. This exemption request is critical to maintaining uninterrupted and secure operations during a crucial migration phase.

Please see the summarized rationale below:

**1. Legacy System Constraints (PBCCL)**

* PBCCL handles the transfer of over 500 mission-critical reports daily, classified as Confidential (P2).
* PBCCL currently operates on RHEL 6 with OpenSSH 5.3.
* ITID confirmed upgrading OpenSSH to version 8.7 necessitates a full OS upgrade to RHEL 9, which is presently not viable.

**2. Strategic Migration to AWS Cloud and Planning**

* PBCCL is actively migrating to AWS cloud infrastructure, targeted for completion by year-end.
* The OS upgrade from RHEL 6 to RHEL 8/9 is currently deprioritized to ensure resources focus on the critical SSP migration.
* Post-migration, comprehensive strategic planning will address modernization, including OS upgrades.
* Upon successful migration, we will immediately revert to the mandated TransferSecurityPolicy-2022-03, terminating this temporary exemption.

**3. Immediate Business Deadlines**

* A reliable and secure file-transfer mechanism must be in place by mid-May 2025 to meet essential business timelines for transferring reports from PBCCL to Sparta.

**Exemption Specifics:**

* **Exemption ID:** 4FF1D855
* **Duration Requested:** 12 months
* **Reference Documentation:** [AWS Transfer Family Confluence Page](https://alm-confluence.systems.uk.hsbc/confluence/display/EFOS/2.3.+AWS+transfer+family+between+Sparta+and+PBCCL)

Your prompt approval of this exemption is greatly appreciated. Should you require further details or a discussion, I am available at your earliest convenience.


