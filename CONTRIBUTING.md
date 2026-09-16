# Contributing to SHIFT

SHIFT is maintained by [The Allsparks](https://github.com/The-Allsparks) (FTC Team 36117).

```powershell
git clone https://github.com/The-Allsparks/SHIFT.git
cd SHIFT
.\gradlew.bat check
.\gradlew.bat compileAgainstFtcSdk
```

Coding agents: read [AGENTS.md](AGENTS.md) before changing module boundaries or `Shift.update()`.

## Commands

| Command | What it prevents |
|---------|------------------|
| `.\gradlew.bat check` | Broken tests, architecture regressions, example compile failures, stubs on robot artifacts, format drift (`spotlessCheck`) |
| `.\gradlew.bat spotlessApply` | Rewrite Java (except `shift-ftc-stubs`) to Palantir Java Format |
| `.\gradlew.bat compileAgainstFtcSdk` | Adapters drifting from FTC SDK 12.0.0 |

## Rules

1. `shift-core` must remain free of FTC and Android types.
2. Do not add a command scheduler, drivetrain, or mechanism control.
3. Profile validation happens at load, never in `update()`.
4. Do not commit secrets or student PII.
5. Match Palantir Java Format (`.\gradlew.bat spotlessApply`).

## Pull requests

- Prefer small, reviewable PRs.
- Run `.\gradlew.bat check` before requesting review.
- Adapter PRs that touch FTC types should also run `.\gradlew.bat compileAgainstFtcSdk`.
- Architecture tests (`PackageBoundaryTest`, `HotPathGuardTest`, `FtcArchitectureTest`) must stay green.
- Do not add `Thread.sleep`, file I/O, networking, or extra threads to `Shift.update()`.
- Update docs when behavior or the configuration schema changes.

## Branch protection (`main`)

`main` already requires those checks:

| Rule | Policy |
|------|--------|
| Force push / delete | Disallowed |
| `enforce_admins` | On |
| Required status checks | `test (ubuntu-latest)`, `test (windows-latest)`, `docs-structure`, `sdk-compile`, `Analyze Java` |
| Pull request reviews | Required approving review count is **0** (solo-maintainer workflow) |
| Conversation resolution | Required before merge |

Details: [docs/release.md](docs/release.md).

## GitHub Actions pins

Workflows pin third-party actions to full commit SHAs with a version comment. Do not switch back to floating major tags.

## License

Contributions are accepted under the MIT License ([LICENSE](LICENSE)). No CLA is required.
