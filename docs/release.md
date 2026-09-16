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

Classic branch protection with `enforce_admins` once the GitHub repo exists:

- Disallow force-push and branch deletion.
- Required checks (strict): `test (ubuntu-latest)`, `test (windows-latest)`, `docs-structure`, `sdk-compile`, `Analyze Java`.
- Required approving review count: **0** until a second maintainer is available.
- Require conversation resolution before merge.

Do not add required checks that do not exist (no hardware CI). SHIFT does not command motors; desktop tests plus `compileAgainstFtcSdk` are the V1 bar.

## Tagging

```bash
git tag v0.1.0
git push origin v0.1.0
```

Publishing to GitHub Packages or Maven Central is a maintainer step after secrets exist.
