# Compatibility

## FTC SDK

| Item | Support |
| ---- | ------- |
| Minimum FTC SDK | **11.2.0** (DECODE 2025–2026 / 2026 offseason) |
| Verified compile | `org.firstinspires.ftc:RobotCore:11.2.1` via `./gradlew compileAgainstFtcSdk` |
| Older SDKs | Not a project goal. Gamepad rumble/LED/touchpad have existed since SDK 6.x; SHIFT still targets current RobotCore APIs. |

`FtcGamepadDevice` reads public `Gamepad` fields and `type()`, `getGamepadId()`, `timestamp`. Touchpad click, both finger contacts, finger XY, and digital trigger clicks (`left_trigger_pressed` / `right_trigger_pressed`) are sampled. `FtcFeedbackActuator` calls `rumble`, `rumbleBlips`, `stopRumble`, and `setLedColor`.

## Gamepads

SHIFT does not talk USB. The FTC Driver Station maps HID onto `Gamepad` fields; SHIFT snapshots those fields. RobotCore 11.2 `Gamepad.Type` is still `UNKNOWN`, `LOGITECH_F310`, `XBOX_360`, `SONY_PS4`, and `SONY_PS4_SUPPORTED_BY_KERNEL`. Official DualSense support added in Driver Station 11.2 still reports a PS4 type, not a `SONY_PS5` enum.

| Model token | Hardware | Notes |
| ----------- | -------- | ----- |
| `logitech-f310` | Logitech F310 | No rumble/RGB in the SDK path SHIFT uses |
| `xbox-360` | Xbox 360 | Rumble yes; `setLedColor` no-ops |
| `sony-ps4` | DualShock 4 / Etpark | Rumble, RGB, touchpad |
| `sony-dualsense` | Sony DualSense | Driver Station 11.2; DualSense Edge is **not** supported by FIRST |
| `iwgame-wired-ps5` | IWGAME wired PS-5/PC DualSense-style clone | Same SHIFT layout as DualSense: Hall sticks/triggers, RGB, rumble, touchpad. Wired USB only. If the Driver Station shows the pad as unknown VID/PID, assign **Sony PS4** in DS gamepad settings so LED/rumble/touchpad map. Program the two rear buttons onto existing controls; they are not RobotCore fields. Gyro and adaptive-trigger features are not in the FTC `Gamepad` API. |

Declare `"model": "iwgame-wired-ps5"` on a profile controller when that pad is in the slot. The token is documentation and validation; it does not change sampling.

Competition legality is Game Manual 1, not SHIFT. Official DualSense is listed for SDK 11.2-era events. A third-party IWGAME clone may be practice-only.

Always use a wired connection. Bluetooth is illegal in FTC.

## Java

Source and bytecode are **Java 8**. That matches `FtcRobotController` v11.2 `compileOptions` (`VERSION_1_8`) and OnBotJava, which still does not support later language levels.

CI formats and compiles with Temurin **17**. Do not use `var`, records, sealed classes, text blocks, or `List.of` in SHIFT sources.

## Android

minSdk 24, as required by the FTC SDK. SHIFT core is pure Java. The FTC adapter uses only `Gamepad`, which is available on the Robot Controller.

Do not add Android-only APIs to `shift-core`.

## Gradle

This repository uses Gradle 9.7 (wrapper copied from the Allsparks AMPER project) and the `java-library` plugin. Robot projects continue to use the FTC Android Gradle Plugin; they consume SHIFT as jars or `implementation project` modules, not by applying this build.

Robot-facing artifacts: `shift-core` and `shift-ftc` only.

## Dependencies

| Dependency | Where | Why |
| ---------- | ----- | --- |
| `org.json:json` | `shift-core` api | Profile load and snapshot JSON. Standard on Android. |
| JUnit 5 | tests | |
| Palantir Java Format via Spotless | CI | Same as AMPER |
| FTC RobotCore | `compileAgainstFtcSdk` only | Adapter drift check |

FTCLib is **not** a dependency. WPILib is **not** a dependency.
