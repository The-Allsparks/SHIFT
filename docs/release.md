# Release process

SHIFT follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html) and [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Maven Central publication is deferred. Until then, teams consume sibling source or the two robot-facing jars (`shift-core`, `shift-ftc`).

## Version meaning

| Label | Meaning |
|-------|---------|
| `0.1.0` | First software V1: snapshots, FTC adapters, JSON profiles, layers, intents, feedback |
| `0.1.x` | Compatible fixes on schemaVersion 1 |
| `1.x` | Compatibility commitments after teams have used the library in season |

Do not silently reinterpret schemaVersion 1 files. Breaking binding JSON requires schemaVersion 2.

## Checklist before tagging

- [ ] `./gradlew check javadocAll assembleReleaseArtifacts`
- [ ] `./gradlew compileAgainstFtcSdk` (official RobotCore 12.0.0, not stubs)
- [ ] Example OpMode compiles (`:shift-examples:compileJava`)
- [ ] Architecture tests green (`PackageBoundaryTest`, `HotPathGuardTest`, `FtcArchitectureTest`)
- [ ] README / changelog match implementation
- [ ] No motor writes in `shift-core`, `shift-ftc`, or `shift-examples`

## Branch protection (`main`)

`main` uses classic branch protection with `enforce_admins`:

- Force-push and branch deletion are disallowed.
- Required checks (strict): `test (ubuntu-latest)`, `test (windows-latest)`, `docs-structure`, `sdk-compile`, `Analyze Java`.
- Required approving review count: **0**.
- Conversation resolution is required before merge.

Do not add required checks that do not exist (no hardware CI). SHIFT does not command motors; desktop tests plus `compileAgainstFtcSdk` are the V1 bar.

## Dependabot policy

Dependabot may open PRs. Merging is a maintainer decision after `./gradlew check` and `./gradlew compileAgainstFtcSdk` stay green on Java 8. Do not merge a bump because it is monthly or because Dependabot opened it.

These gates must stay green:

| Gate | Current bar |
|------|-------------|
| Unit tests | Java 8 (`./gradlew check`) |
| Format | Palantir Spotless 6.25.x (`spotlessCheck`) |
| SDK compile | `compileAgainstFtcSdk` vs official RobotCore 12.0.0 |

Do not merge until a written compatibility note exists and the gates above are green:

- JUnit BOM 6.x. Current pin is JUnit 5.10.2. Dependabot [PR #5](https://github.com/The-Allsparks/SHIFT/pull/5) (5.10.2 to 6.1.3) failed CI.
- Spotless 8.x while Palantir Spotless 6.25.x is the format bar.
- TeamCode FTC Gradle wrapper upgrades just because SHIFT's wrapper moved. Robot projects `includeBuild` SHIFT into the FTC SDK wrapper. Keep SHIFT Gradle scripts compatible with the robot project's wrapper.
- Floating GitHub Actions major tags. Keep SHA pins with version comments ([CONTRIBUTING.md](../CONTRIBUTING.md)).

Leave unmatched Dependabot PRs open and unmerged. That is the safe default. Re-run `./gradlew check` before any bump merge.

## Tagging

```bash
git tag v0.1.0
git push origin v0.1.0
```

Publishing to GitHub Packages or Maven Central is a maintainer step after secrets exist.
