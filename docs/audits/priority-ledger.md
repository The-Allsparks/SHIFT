# SHIFT priority ledger

Living tracker. GitHub issues are authoritative. Update this file after each issue or pull request.

**Updated:** 2026-09-16
**Main head:** `7f2366e` (PR #23 merged, tag `v0.1.1`)
**Automatic merge:** false except when a human says proceed on a ready PR. Do not merge Dependabot without analysis.
**Max active implementation PRs:** 1

Combined stack: [FORGE#4](https://github.com/The-Allsparks/FORGE/issues/4)

Hub evidence card: [hub-evidence-2026-09-08.md](hub-evidence-2026-09-08.md) (existing 8 Sep Hub pull; not CI).

## Current selection

| Field | Value |
|-------|--------|
| Selected | none |
| Why highest priority | P0 software bar landed at v0.1.1. |
| Why ready | No active P0 child. Next work needs a new accepted issue. |
| Hardware required | None. |

## Ledger

P0 epic first, then children in recommended order.

| Issue | Priority | Status | Notes |
|-------|----------|--------|-------|
| [#8](https://github.com/The-Allsparks/SHIFT/issues/8) Make SHIFT a tracked FTC input library | **P0 parent** | **DONE** | P0 software bar landed at v0.1.1. Children below. |
| [#9](https://github.com/The-Allsparks/SHIFT/issues/9) Pin CI to FTC SDK 12.0.0 | P0 child | **DONE** | [PR #17](https://github.com/The-Allsparks/SHIFT/pull/17) |
| [#12](https://github.com/The-Allsparks/SHIFT/issues/12) Phase template and priority ledger | P0 child | **DONE** | [PR #18](https://github.com/The-Allsparks/SHIFT/pull/18) |
| [#13](https://github.com/The-Allsparks/SHIFT/issues/13) Dependabot merge rules | P0 child | **DONE** | [PR #19](https://github.com/The-Allsparks/SHIFT/pull/19) |
| [#10](https://github.com/The-Allsparks/SHIFT/issues/10) Student install and teaching path | P0 child | **DONE** | [PR #20](https://github.com/The-Allsparks/SHIFT/pull/20) |
| [#14](https://github.com/The-Allsparks/SHIFT/issues/14) TRACE and ECHO adapter contracts | P0 child | **DONE** | [PR #21](https://github.com/The-Allsparks/SHIFT/pull/21) |
| [#11](https://github.com/The-Allsparks/SHIFT/issues/11) Control Hub evidence | P0 child | **DONE** | [PR #22](https://github.com/The-Allsparks/SHIFT/pull/22). [hub-evidence-2026-09-08.md](hub-evidence-2026-09-08.md). |
| [FORGE#35](https://github.com/The-Allsparks/FORGE/issues/35) catalog | P0 child | **DONE** | [FORGE PR #36](https://github.com/The-Allsparks/FORGE/pull/36) |
| [#15](https://github.com/The-Allsparks/SHIFT/issues/15) Tag 0.1.x | P0 child | **DONE** | [PR #23](https://github.com/The-Allsparks/SHIFT/pull/23). Tag `v0.1.1`. Maven Central stays deferred. |
| [#16](https://github.com/The-Allsparks/SHIFT/issues/16) Deferred V1 capabilities | tracker | Standing | Not Drive work. Closed as deferred backlog tracker. Do not implement until a new accepted child issue splits a row out. |

## Stop conditions

- Do not merge Dependabot without analysis.
- Do not force-push `main`.
- Do not implement #16 deferred rows until a new accepted child issue splits one out.
- #16 is a standing deferred tracker. It is not season Drive work.
