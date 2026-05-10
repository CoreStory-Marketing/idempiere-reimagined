# Verification report — iDempiere parity claims

> Run on 2026-05-09 against `/Users/johnives/Downloads/Claude Context/idempiere/` (read-only legacy clone).
> Verifies `[unverified]` claims from `Build-Scope-of-Work.md` §11.3 and the seven-category dry-run on conversation 5276.

This report is a candid finding, not a contradiction of the dry-run. It demonstrates a key skill behavior: **even with a grounded intel store, claims that cite specific symbol names must be verified against source.**

## Summary

| Claim | Verified | Notes |
|---|---|---|
| `MClient.RequestEMail` exists | ✅ Yes | `MClient.java:570, 587, 597, 603, 605` |
| `MClient.SMTPHost` exists | ✅ Yes | `MClient.java:1226+` (getter), called at 603 |
| `AlertProcessor` class exists | ✅ Yes | `org.adempiere.server/src/main/server/org/compiere/server/AlertProcessor.java` |
| `MUser.IsNoEMail` opt-in | ❌ **Not found anywhere** in iDempiere | The dry-run said "likely on the X_AD_User parent class". Not present. **Demo opportunity:** the gap analysis correctly flagged `[unverified]`; subsequent verification confirmed it's hallucinated, **so the parity table omits it**. This is a credibility win for the skill — flags > silent fabrications. |
| `MMailText.parseVariables()` | ❌ Wrong name | The actual method is named `parse()` (`MMailText.java:128, 139, 158`). The substitution behavior exists; only the method name differs. |
| `MMailText.sendEMailAttachments()` | ❌ Not in MMailText.java | Likely on a different class (e.g., `EMail` or `MClient`). Out of scope to verify deeper. |

## Updated parity claims for `docs/design-spec.md` §10

The DESIGN_SPEC parity table has been authored to reflect these corrections:

- **Removed** the `MUser.IsNoEMail ↔ notification_subscriptions.is_subscribed` row. There is no such legacy column. The target's `notification_subscriptions` pattern is a clean modernization, not a parity mirror.
- **Renamed** `parseVariables()` → `parse()` in any reference to MMailText's variable-substitution method.
- **Added** explicit verification dates next to claims that survived (`MClient.RequestEMail` ✅ 2026-05-09).

## Implications for the recorded demo

When `dual-store-gap-analysis` queries CoreStory project 457 during the recording, expect:

1. The seven-category response will likely repeat the original (cached) language about `IsNoEMail` and `parseVariables`.
2. The agent's response should be flagged `[unverified]` for those specific cells (per the conservative `[unverified]` policy).
3. **The gap analysis is correct anyway:** `notification_subscriptions.is_subscribed` is the right target design even if the legacy parity reference is wrong.

## Why this matters

Modernization is a multi-source intelligence problem. CoreStory's intel store is excellent for navigation and pattern discovery; source-of-truth verification belongs to `grep`. The skill leverages both:

- CoreStory: "what does iDempiere do for shipping notifications" → 90s answer with citations
- `grep`: "does the cited symbol actually exist?" → instant local check

Either alone is incomplete. Together they're solid.
