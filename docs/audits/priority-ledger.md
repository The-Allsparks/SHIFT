# SHIFT priority ledger

Living tracker. GitHub issues are authoritative. Update this file after each issue or pull request.

**Updated:** 2026-09-16
**Main head:** `4ac8680` (PR #21 merged, closes #14)
**Automatic merge:** false except when a human says proceed on a ready PR. Do not merge Dependabot without analysis.
**Max active implementation PRs:** 1

Combined stack: [FORGE#4](https://github.com/The-Allsparks/FORGE/issues/4)

Hub evidence card: [hub-evidence-2026-09-08.md](hub-evidence-2026-09-08.md) (existing 8 Sep Hub pull; not CI).

## Current selection

| Field | Value |
|-------|--------|
| Selected | [#11](https://github.com/The-Allsparks/SHIFT/issues/11) Control Hub evidence |
| Why highest priority | Last P0 child that needs a dated Hub record before tagging. Students must point at logs, not Actions. |
| Why ready | Existing 8 Sep Hub pull. No new shop run. |
| Hardware required | No new run. Use downloaded logs. |

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
| [#11](https://github.com/The-Allsparks/SHIFT/issues/11) Control Hub evidence | P0 child | This PR | [hub-evidence-2026-09-08.md](hub-evidence-2026-09-08.md). Existing 8 Sep logs. Do not require a new shop run. |
| [FORGE#35](https://github.com/The-Allsparks/FORGE/issues/35) catalog | P0 child | Open | FORGE curriculum catalog. Separate repo. |
| [#15](https://github.com/The-Allsparks/SHIFT/issues/15) Tag 0.1.x | P0 child | Open | After the software V1 bar |
| [#16](https://github.com/The-Allsparks/SHIFT/issues/16) Deferred V1 capabilities | tracker | Open | Tracker only. Not season Drive work. |

## Stop conditions

- Do not implement other #8 children from this issue's PR.
- Do not merge Dependabot without analysis.
- Do not force-push `main`.
- #11 uses existing 8 Sep logs. Do not block on a new shop run.
- #16 tracks deferred capabilities. It is not a request to implement season Drive work.
