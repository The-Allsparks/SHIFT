# SHIFT

**Semantic Human Input Framework for Teleoperation**

SHIFT sits between physical operator controls and robot behavior.

It translates:

**physical operator input → normalized input → configurable bindings → semantic intent**

It answers:

> What does the operator intend the robot to do?

It does **not** answer how the robot performs that action.

```text
BAD                         GOOD
gamepad2.x                  gamepad2.x
    ↓                           ↓
elevatorMotor.setTarget     SHIFT
Position(1462)                  ↓
                            elevator.score.high
                                ↓
                            robot application / mechanism layer
```

Built by **[The Allsparks](https://github.com/The-Allsparks)** (FTC Team **36117**), a FIRST Tech Challenge team in Las Vegas, Nevada.

Repository: **[The-Allsparks/SHIFT](https://github.com/The-Allsparks/SHIFT)**

> **Disclaimer:** SHIFT is community-developed and unofficial. It is **not** affiliated with or endorsed by FIRST, WPILib, FTCLib, or other referenced vendors.

---

## What SHIFT is not

SHIFT is not a drivetrain library, motor/servo abstraction, OpMode framework, path planner, vision stack, mechanism framework, command scheduler, Driver Station replacement, autonomous framework, telemetry system, or safety/power manager.

Robot mechanisms, PID, autonomous behavior, path planning, vision, localization, and hardware safety remain outside SHIFT.

---

## Architecture

```text
FTC Gamepad 1        FTC Gamepad 2
      │                    │
      └─────────┬──────────┘
                ▼
         INPUT ADAPTER
                │
                ▼
         INPUT SNAPSHOT
                │
                ▼
        INPUT NORMALIZATION
                │
                ▼
             SHIFT
       ┌────────┼────────┐
       │        │        │
    Profile   Layers   Bindings
       │        │        │
       └────────┼────────┘
                ▼
         SEMANTIC INTENT
                ▼
        ROBOT APPLICATION
```

Feedback travels the other way: the application emits a semantic event such as `TARGET_ACQUIRED`; SHIFT maps it to rumble and LED commands. External systems may also consume those events for audio or telemetry.

Observability is an interface. TRACE can implement `ShiftEventSink` (semantic events) and `ShiftInputListener` (per-loop snapshots). `CompositeEventSink` / `CompositeInputListener` fan out to TRACE, ECHO, and telemetry. SHIFT does not depend on those libraries.

Details: [docs/architecture.md](docs/architecture.md).

---

## Installation

SHIFT is Java 8 source/target to match FTC SDK 11.2 / OnBotJava.

Robot-facing artifacts:

* `shift-core` — hardware-independent runtime (required)
* `shift-ftc` — `FtcGamepadDevice` and `FtcFeedbackActuator` (required on the robot)

`shift-ftc-stubs` is compile-only for this repository's desktop CI. Do not ship stubs to a Control Hub.

Until published to Maven, clone this repository as a sibling of your FTC SDK project and `includeBuild('../SHIFT')`, or copy the two jars into `TeamCode/libs`.

```gradle
dependencies {
    implementation files('libs/shift-core-0.1.0.jar')
    implementation files('libs/shift-ftc-0.1.0.jar')
}
```

`org.json` is the only third-party runtime dependency of `shift-core`. Android already provides `org.json`; desktop tests use `org.json:json` from Maven Central.

Compatibility: [docs/compatibility.md](docs/compatibility.md).

---

## Minimal example

```java
Shift shift = FtcShift.builder(gamepad1, gamepad2)
    .registerIntents(MyRobotIntents.all())
    .loadProfile(profileJson)
    .build();

if (shift.usedFallback()) {
    // preferred profile was invalid; robot is on the embedded idle profile
}

// once per robot loop
IntentFrame intents = shift.update();

Vector2 translation = intents.vector2("drive.translation");
double rotation = intents.analog("drive.rotation");
boolean collect = intents.held("intake.collect");

if (intents.emitted("elevator.score.high")) {
    // ask the mechanism layer to score high — SHIFT does not move the elevator
}
```

Register intents before loading a profile. Unknown intent names fail at initialization, not while driving. `FtcShift.builder()` already enables the embedded idle fallback. Check `usedFallback()` after `build()`. To fail `init()` on a bad profile, use `Shift.builder()` without `fallbackToEmbedded()`.

---

## Configuration

Profiles are versioned JSON:

```json
{
  "schemaVersion": 1,
  "id": "competition",
  "controllers": {
    "driver": { "slot": 1, "defaultLayer": "drive", "model": "iwgame-wired-ps5" },
    "codriver": { "slot": 2, "defaultLayer": "mechanism" }
  }
}
```

Roles are application-defined. `driver` / `codriver` / `test` are conventions, not a closed enum.

Named per-driver maps use optional `person` and `set` (canonical id `garrett/offense`). Load them into a `ProfileBank` at `init()`, compose driver + operator, then `activate` between matches — never from `update()`:

```java
ProfileBank bank = ProfileBank.builder(MyRobotIntents.all())
    .addJson(garrettGeneral)
    .addJson(garrettOffense)
    .addJson(samGeneral)
    .build();

Shift shift = FtcShift.builder(gamepad1, gamepad2)
    .registerIntents(MyRobotIntents.all())
    .loadProfile(bank.compose("garrett/offense", "sam/general"))
    .eventSink(CompositeEventSink.of(echoAdapter, telemetrySink))
    .inputListener(traceSnapshotAdapter)
    .build();

// init_loop or between matches, not inside loop():
shift.activate(bank.compose("garrett/defense", "sam/general"));
```

Person-role files for gamepad 1 declare `controllers.driver` only; operator files declare `controllers.codriver`. Examples: `examples/profiles/garrett-offense.json` and `examples/profiles/sam-general.json`.

JSON Schema for editor autocomplete: [schema/shift-profile-v1.json](schema/shift-profile-v1.json).

Full field reference: [docs/configuration.md](docs/configuration.md).

---

## Intents, layers, bindings, feedback

* **Intents** are semantic names with a declared type: `EVENT`, `BOOLEAN`, `ANALOG`, `VECTOR2`. See [docs/intent-design.md](docs/intent-design.md).
* **Layers** remap the same physical controls. Layer switches take effect at the end of the update cycle and emit `LAYER_CHANGED`.
* **Bindings** use `ON_PRESS`, `ON_RELEASE`, `WHILE_HELD`, and `ANALOG`. Composite `all` / `any` / `not` triggers and analog comparisons such as `RIGHT_TRIGGER > 0.7` are supported. More-specific chords suppress simpler overlapping bindings.
* **Feedback** maps `TARGET_ACQUIRED` onto rumble/LED. Call `shift.emitFeedback("TARGET_ACQUIRED")` from application code. Do not call `gamepad.rumble` from robot behavior.

---

## FTC integration

```java
Shift shift = FtcShift.builder(gamepad1, gamepad2)
    .registerIntents(MyRobotIntents.all())
    .loadProfile(json)
    .build();
```

`FtcShift.builder` maps `gamepad1` to `driver` and `gamepad2` to `codriver`, installs `FtcFeedbackActuator`, and enables the embedded fallback. After `build()`, check `shift.usedFallback()`.

`FtcGamepadDevice` snapshots public `Gamepad` fields once per `update()`. It does not use `Gamepad.copy()`. PlayStation aliases in the FTC SDK already mirror onto `a`/`b`/`x`/`y`; SHIFT aliases `CIRCLE`/`SOUTH`/`CREATE` to the same canonical control. DualSense-style pads (official DualSense and IWGAME wired PS-5/PC) also sample touchpad fingers. Digital trigger clicks (`left_trigger_pressed`) are `LEFT_TRIGGER_PRESSED`, distinct from analog `LEFT_TRIGGER`.

A compile-checked OpMode illustration lives in `shift-examples` and is `@Disabled`.

---

## Testing

SHIFT core has no FTC SDK dependency. Use `SimulatedInputDevice`:

```java
device.press(Control.parse("SOUTH"));
IntentFrame frame = shift.update();
assertTrue(frame.emitted("intake.collect"));
```

`ReplayInputDevice` plus `InputSnapshot.toJson()` / `fromJson()` are the replay hook. `ShiftInputListener` delivers every sampled snapshot for a TeamCode TRACE adapter (`RecordCategory.INPUT`). Full recording UI is deferred; the snapshot format is stable.

Run:

```powershell
.\gradlew.bat check
.\gradlew.bat compileAgainstFtcSdk
```

`check` includes architecture tests (`PackageBoundaryTest`, `HotPathGuardTest`, `FtcArchitectureTest`). CI also runs CodeQL job `Analyze Java`.

---

## Documentation

| Document | Contents |
| -------- | -------- |
| [Architecture](docs/architecture.md) | Pipeline, module boundaries, loop lifecycle |
| [Configuration](docs/configuration.md) | JSON fields, specificity rules, validation |
| [Intent design](docs/intent-design.md) | How to name intents |
| [Migration](docs/migration.md) | Traditional FTC gamepad code vs SHIFT |
| [Compatibility](docs/compatibility.md) | FTC SDK, Java, Android, Gradle |
| [Release](docs/release.md) | Versioning, tag checklist, required CI checks |
| [Upstream research](docs/research/upstream.md) | FTC SDK, FTCLib, WPILib notes |
| [Deferred](docs/deferred.md) | Post-V1 capabilities |

---

## License

MIT. See [LICENSE](LICENSE).
