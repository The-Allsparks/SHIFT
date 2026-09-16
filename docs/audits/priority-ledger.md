# SHIFT priority ledger

Living tracker. GitHub issues are authoritative. Update this file after each issue or pull request.

**Updated:** 2026-09-16
**Main head:** `969281e` (PR #22 merged, closes #11)
**Automatic merge:** false except when a human says proceed on a ready PR. Do not merge Dependabot without analysis.
**Max active implementation PRs:** 1

Combined stack: [FORGE#4](https://github.com/The-Allsparks/FORGE/issues/4)

Hub evidence card: [hub-evidence-2026-09-08.md](hub-evidence-2026-09-08.md) (existing 8 Sep Hub pull; not CI).

## Current selection

| Field | Value |
|-------|--------|
| Selected | [#15](https://github.com/The-Allsparks/SHIFT/issues/15) Tag 0.1.1 |
| Why highest priority | Software V1 bar is on main. GitHub never had `v0.1.0`. Publish `v0.1.1` so teams can pin a clone. |
| Why ready | SDK 12 pin, student docs, adapter contracts, and Hub evidence are merged. Maven Central stays deferred. |
| Hardware required | None. Desktop `check` and `compileAgainstFtcSdk`. |

## Ledger

P0 epic first, then children in recommended order.

| Issue | Priority | Status | Notes |
|-------|----------|--------|-------|
| [#8](https://github.com/The-Allsparks/SHIFT/issues/8) Make SHIFT a tracked FTC input library | **P0 parent** | Open | Parent epic. Do not close until children land or are explicitly deferred. |
| [#9](https://github.com/The-Allsparks/SHIFT/issues/9) Pin CI to FTC SDK 12.0.0 | P0 child | **DONE** | [PR #17](https://github.com/The-Allsparks/SHIFT/pull/17) |
| [#12](https://github.com/The-Allsparks/SHIFT/issues/12) Phase template and priority ledger | P0 child | **DONE** | [PR #18](https://github.com/The-Allsparks/SHIFT/pull/18) |
| [#13](https://github.com/The-Allsparks/SHIFT/issues/13) Dependabot merge rules | P0 child | **DONE** | [PR #19](https://github.com/The-Allsparks/SHIFT/pull/19) |
| [#10](https://github.com/The-Allsparks/SHIFT/issues/10) Student install and teaching path | P0 child | **DONE** | [PR #20](https://github.com/The-Allsparks/SHIFT/pull/20) |
| [#14](https://github.com/The-Allsparks/SHIFT/issues/14) TRACE and ECHO adapter contracts | P0 child | **DONE** | [PR #21](https://github.com/The-Allsparks/SHIFT/pull/21) |
| [#11](https://github.com/The-Allsparks/SHIFT/issues/11) Control Hub evidence | P0 child | **DONE** | [PR #22](https://github.com/The-Allsparks/SHIFT/pull/22). [hub-evidence-2026-09-08.md](hub-evidence-2026-09-08.md). |
| [FORGE#35](https://github.com/The-Allsparks/FORGE/issues/35) catalog | P0 child | Open | FORGE curriculum catalog. Separate repo. |
| [#15](https://github.com/The-Allsparks/SHIFT/issues/15) Tag 0.1.x | P0 child | This PR | Publish `v0.1.1` (not `v0.1.0`). Maven Central deferred. |
| [#16](https://github.com/The-Allsparks/SHIFT/issues/16) Deferred V1 capabilities | tracker | Open | Tracker only. Not season Drive work. |

## Stop conditions

- Do not implement other #8 children from this issue's PR.
- Do not merge Dependabot without analysis.
- Do not force-push `main`.
- #11 uses existing 8 Sep logs. Do not block on a new shop run.
- #16 tracks deferred capabilities. It is not a request to implement season Drive work.
