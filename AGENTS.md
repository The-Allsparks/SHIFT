# Agent instructions for SHIFT

SHIFT is the Semantic Human Input Framework for Teleoperation. It translates operator controls into semantic intent. It does not command hardware.

## Commands

```powershell
.\gradlew.bat check
.\gradlew.bat compileAgainstFtcSdk
.\gradlew.bat spotlessApply
```

## Module boundaries

| Module | May depend on | Must not |
| ------ | ------------- | -------- |
| `shift-core` | Java 8, org.json | FTC SDK, Android, `shift-ftc` |
| `shift-ftc` | `shift-core` + FTC Gamepad | Stubs on the robot classpath |
| `shift-examples` | `shift-ftc` | Real motor writes |

`Shift.update()` must not parse JSON, hit the filesystem, start threads, or block.

Architecture tests enforce this: `PackageBoundaryTest`, `HotPathGuardTest`, `FtcArchitectureTest`. If you need a new dependency direction, change the test in the same change and explain why.

## Language

Java 8 source/target. No records, sealed types, `var`, or `List.of`.

Format with Palantir Java Format (4-space) via Spotless. `shift-ftc-stubs` is excluded.
