# Installing SHIFT into an FTC project

SHIFT is a Java library. It does not replace `FtcRobotController`. Clone it as a sibling of an FTC SDK **12.0** project, then consume `shift-core` and `shift-ftc` from TeamCode.

**Supported FTC SDK:** `12.0.0` (BIOBUZZ). See [compatibility.md](compatibility.md).

**Java:** source and bytecode are **Java 8**. That matches FTC SDK 12.0 `compileOptions` (`VERSION_1_8`) and OnBotJava.

**Maven Central** publication is deferred. Until a published coordinate exists, use `includeBuild` (recommended) or copy the two robot-facing jars.

## Layout

Clone [The-Allsparks/SHIFT](https://github.com/The-Allsparks/SHIFT) next to your FTC SDK project:

```text
Documents/
  FtcRobotController/     <- FTC SDK 12.0 (settings.gradle lives here)
  SHIFT/                  <- this repository
```

```text
git clone https://github.com/The-Allsparks/SHIFT.git
```

The robot project you deploy is `FtcRobotController/` (the folder with `settings.gradle`). Do not deploy the SHIFT folder by itself.

## Method A: includeBuild (recommended)

1. In the FTC project's `settings.gradle` add:

```gradle
includeBuild('../SHIFT')
```

2. In `TeamCode/build.gradle` depend on the robot-facing modules:

```gradle
dependencies {
    implementation 'org.allsparks:shift-core:0.1.0'
    implementation 'org.allsparks:shift-ftc:0.1.0'
}
```

`includeBuild('../SHIFT')` substitutes those coordinates from this checkout. No Maven login is required.

TeamCode in [The-Allsparks/FtcRobotController](https://github.com/The-Allsparks/FtcRobotController) already does this: `settings.gradle` has `includeBuild('../SHIFT')`, and `TeamCode/build.gradle` depends on `org.allsparks:shift-core` and `org.allsparks:shift-ftc`. Copy that wiring, do not invent a second composition root in this repo.

3. Sync Android Studio or FTC for VS Code, then compile TeamCode as usual.

Do **not** add `org.allsparks:shift-ftc-stubs` to TeamCode. Stubs exist so this repository can compile on a desktop JVM without the FTC SDK. **Do not ship `shift-ftc-stubs` to a Control Hub.**

## Method B: jars into TeamCode/libs

If composite build is impossible, assemble the two robot-facing jars from this checkout:

```powershell
.\gradlew.bat assembleReleaseArtifacts
```

Copy only:

```text
shift-core/build/libs/shift-core-0.1.0.jar
shift-ftc/build/libs/shift-ftc-0.1.0.jar
```

into `TeamCode/libs`. Then:

```gradle
dependencies {
    implementation files('libs/shift-core-0.1.0.jar')
    implementation files('libs/shift-ftc-0.1.0.jar')
}
```

Do **not** copy `shift-ftc-stubs` jars into `TeamCode/libs`. Stubs must not land on a Control Hub.

## After install

In TeamCode `init()`: register intents, load a JSON profile, then `build()`. In `loop()`: `shift.update()` and read named intents. SHIFT does not call `setPower`. See [TEACHING.md](TEACHING.md).

## Related

- [TEACHING.md](TEACHING.md) - stick to named intent to team mix; Motor Test omit path
- [compatibility.md](compatibility.md) - SDK, Java 8, Android minSdk 24
- [architecture.md](architecture.md) - module boundaries
- [release.md](release.md) - Maven Central is deferred
