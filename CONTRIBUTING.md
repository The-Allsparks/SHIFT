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

## Dependabot

Do not merge a Dependabot PR just because it opened. SHIFT must keep Java 8 tests, Palantir Spotless 6.25.x, and `compileAgainstFtcSdk` against RobotCore 12.0.0 green.

- JUnit BOM stays on 5.10.2. Do not merge JUnit 6.x until a compatibility note exists and Java 8 SHIFT tests prove green. Dependabot PR #5 (5.10.2 to 6.1.3) failed CI.
- Do not upgrade TeamCode's FTC Gradle wrapper because SHIFT's wrapper moved. Robot projects `includeBuild` SHIFT into the FTC SDK wrapper; keep SHIFT Gradle scripts compatible with that wrapper.
- GitHub Actions stay SHA-pinned with version comments (see above).

Details: [docs/release.md](docs/release.md).

## License

Contributions are accepted under the MIT License ([LICENSE](LICENSE)). No CLA is required.
