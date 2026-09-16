# Upstream research and design notes

SHIFT is an input/intent framework. It is not a copy of WPILib command-based programming or of FTCLib.

Sources reviewed while designing V1 (2026-09):

* FTC SDK `Gamepad` (RobotCore 12.0.0 Javadoc and OpenRC-Turbo `Gamepad.java`, which tracks the public SDK fields)
* [Game Manual 0 — Gamepad Usage](https://gm0.org/en/latest/docs/software/tutorials/gamepad.html)
* [FTCLib GamepadEx / ButtonReader](https://docs.ftclib.org/ftclib/features/gamepad-extensions)
* [WPILib 2025 binding commands to triggers](https://docs.wpilib.org/en/2025/docs/software/commandbased/binding-commands-to-triggers.html)
* FtcRobotController v12.0 `build.common.gradle` (Java 8, minSdk 24)

## FTC SDK — adopted

| SDK capability | SHIFT decision |
| -------------- | -------------- |
| Public boolean/float fields | Snapshot copies them once per cycle |
| Xbox/PS aliases (`circle`=`b`, `cross`=`a`, …) | Canonical controls are `A/B/X/Y`; `CIRCLE`/`SOUTH` are name aliases only, matching SDK storage |
| `copy()` via robocol bytes | **Not used.** Field reads avoid `RobotCoreException` and keep core FTC-free |
| `timestamp`, `id`, `type()` | Stored as `DeviceMetadata` + snapshot timestamp |
| Rumble (`rumble`, `rumbleBlips`, `stopRumble`) | `FtcFeedbackActuator` |
| LED (`setLedColor`, `LedEffect`) | LED command abstraction; simple color in V1, `LedEffect` sequences deferred |
| Touchpad | `TOUCHPAD` plus finger contacts and XY (`TOUCHPAD_FINGER_1`, `TOUCHPAD_FINGER_1_X`, …) |
| Digital trigger click | `LEFT_TRIGGER_PRESSED` / `RIGHT_TRIGGER_PRESSED` from `left_trigger_pressed` / `right_trigger_pressed`, distinct from analog trigger axes |
| DualSense / DualSense-style clones | Same PlayStation field map as PS4. Official DualSense is a Driver Station 11.2+ device; RobotCore 12.0 `Gamepad.Type` stays PS4. IWGAME wired PS-5/PC is catalogued as `iwgame-wired-ps5`. Rear paddles are firmware-remapped, not SDK fields. Gyro is not in `Gamepad`. |
| `atRest()` | Not wrapped; applications can inspect analog zeros |
| TestGamepad | Driver Station diagnostic utility, not an OpMode API. SHIFT tests use `SimulatedInputDevice` |

SHIFT does not reimplement rumble effect builders or LED step machines. It calls the SDK.

## FTCLib — learned from, not depended on

Useful patterns: named `GamepadKeys`, `wasJustPressed` / `isDown` edge state, trigger threshold as digital (~0.5), wrapping SDK `Gamepad` rather than replacing it.

Not adopted as a dependency: command scheduler, subsystem requirements as exclusive locks, `ToggleButtonReader` (toggle is post-V1), forcing teams onto FTCLib's OpMode base.

SHIFT bindings are data (JSON + compiled triggers), not `Command` objects.

## WPILib — adopted ideas, not the scheduler

| WPILib | SHIFT V1 |
| ------ | -------- |
| `onTrue` | `ON_PRESS` |
| `onFalse` | `ON_RELEASE` |
| `whileTrue` | `WHILE_HELD` as continuous boolean intent, not a scheduled command |
| `and` / `or` / `negate` | `all` / `any` / `not` in JSON |
| Debounce | Deferred |
| Command requirements | Optional `LogicalResource` metadata only |
| Default commands | Out of scope (robot/HELM) |
| Scheduler arbitration | Out of scope. Intent priority is metadata for HELM |
| Trigger composition on `BooleanSupplier` | Precompiled `TriggerCondition` evaluated on snapshots |

SHIFT must not become “WPILib Commands for FTC.” HELM already exists as the team's coordinator. SHIFT stops at semantic intent.

## Java / Android constraint

FtcRobotController v12.0 still compiles TeamCode as Java 8 because OnBotJava cannot consume newer class files. SHIFT matches that, even though CI runs JDK 17.
