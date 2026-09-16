# SHIFT priority ledger

Living tracker. GitHub issues are authoritative. Update this file after each issue or pull request.

**Updated:** 2026-09-16
**Main head:** `400d4b4` (PR #17 merged, closes #9)
**Automatic merge:** false except when a human says proceed on a ready PR. Do not merge Dependabot without analysis.
**Max active implementation PRs:** 1

Combined stack: [FORGE#4](https://github.com/The-Allsparks/FORGE/issues/4)

## Current selection

| Field | Value |
|-------|--------|
| Selected | [#12](https://github.com/The-Allsparks/SHIFT/issues/12) Add phase template and priority ledger |
| Why highest priority | Students need a P0-first ledger and a phase template before more library work |
| Why ready | Docs and template only. No hardware. |
| Hardware required | No |

## Ledger

P0 epic first, then children in recommended order.

| Issue | Priority | Status | Notes |
|-------|----------|--------|-------|
| [#8](https://github.com/The-Allsparks/SHIFT/issues/8) Make SHIFT a tracked FTC input library | **P0 parent** | Open | Parent epic. Do not close until children land or are explicitly deferred. |
| [#9](https://github.com/The-Allsparks/SHIFT/issues/9) Pin CI to FTC SDK 12.0.0 | P0 child | **DONE** | [PR #17](https://github.com/The-Allsparks/SHIFT/pull/17) |
| [#12](https://github.com/The-Allsparks/SHIFT/issues/12) Phase template and priority ledger | P0 child | This issue | Template, ledger, and present-tense branch protection |
| [#13](https://github.com/The-Allsparks/SHIFT/issues/13) Dependabot merge rules | P0 child | Open | Do not merge Dependabot until rules exist |
| [#10](https://github.com/The-Allsparks/SHIFT/issues/10) Student install and teaching path | P0 child | Open | Docs in this repo |
| [#14](https://github.com/The-Allsparks/SHIFT/issues/14) TRACE and ECHO adapter contracts | P0 child | Open | Observe-only. SHIFT does not command motors. |
| [#11](https://github.com/The-Allsparks/SHIFT/issues/11) Control Hub evidence | P0 child | Open | Use existing 8 Sep logs. Do not require a new shop run. |
| [FORGE#35](https://github.com/The-Allsparks/FORGE/issues/35) catalog | P0 child | Open | FORGE curriculum catalog. Separate repo. |
| [#15](https://github.com/The-Allsparks/SHIFT/issues/15) Tag 0.1.x | P0 child | Open | After the software V1 bar |
| [#16](https://github.com/The-Allsparks/SHIFT/issues/16) Deferred V1 capabilities | tracker | Open | Tracker only. Not season Drive work. |

## Stop conditions

- Do not implement other #8 children from this issue's PR.
- Do not merge Dependabot without analysis.
- Do not force-push `main`.
- #11 uses existing 8 Sep logs. Do not block on a new shop run.
- #16 tracks deferred capabilities. It is not a request to implement season Drive work.
