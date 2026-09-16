# SHIFT teaching path

SHIFT maps operator sticks to **named intent**. TeamCode mixes that intent and commands wheels. SHIFT does not call `setPower`.

The composition root is TeamCode in [The-Allsparks/FtcRobotController](https://github.com/The-Allsparks/FtcRobotController), not this repository. Do not copy BumbleBee motor names into SHIFT as if this library owned hardware.

## Story a student can retell

```text
gamepad
   |
   v
JSON profile (TeamCode assets)
   |
   v
named intents:
  drive.translation
  drive.rotation
  drive.slow
  drive.boost
   |
   v
MecanumDrive in TeamCode
   |
   v
Hub setPower (TeamCode only)
```

1. Gamepad sticks move on the Driver Station.
2. A JSON profile in TeamCode (not Java `left_stick_y`) names those sticks.
3. SHIFT emits `drive.translation`, `drive.rotation`, `drive.slow`, and `drive.boost`.
4. TeamCode `MecanumDrive` mixes those values and is the only place that writes motors.

Register intents and load the profile in `init()`. Call `shift.update()` once per `loop()`. Do not parse JSON on the robot loop.

## Where to read the working example

| Piece | Lives in |
| ----- | -------- |
| `includeBuild` + coordinates | FTC project `settings.gradle` and `TeamCode/build.gradle` ([INSTALL.md](INSTALL.md)) |
| Drive profile JSON | FtcRobotController TeamCode `assets/shift/` |
| Composition root OpMode | FtcRobotController TeamCode (`BumbleBeeTeleOp`) |
| Mix + `setPower` | TeamCode `MecanumDrive` |
| Chassis motor lookup | TeamCode hardware map (not SHIFT) |

SHIFT examples in this repo are compile-checked illustrations. `ShiftExampleOpMode` in `shift-examples` stays `@Disabled` and **must not write motors**. Copy the builder pattern into TeamCode; do not treat the example as competition teleop.

## If SHIFT is omitted

Use **BumbleBee Motor Test** in TeamCode (`bringup`). That OpMode uses conventional sticks (or d-pad wheel select) and does not go through a SHIFT profile. It lives in FtcRobotController, not here.

Motor Test is how you prove a wheel turns when SHIFT is out of the path.

## If JSON is bad

`FtcShift.builder()` enables the embedded idle fallback. After `build()`, check `usedFallback()`.

- `usedFallback()` is **true**: the preferred profile failed. SHIFT is idle. Sticks will not drive.
- Use **BumbleBee Motor Test** until the asset path or JSON is fixed.
- Put `shift.usedFallback()` on Driver Station telemetry at INIT so a rejected profile is visible before PLAY.

To fail `init()` instead of idling, use `Shift.builder()` without `fallbackToEmbedded()`. TeamCode Drive uses the fallback so a bad file does not crash INIT.

## What SHIFT does not own

SHIFT does not:

- call `setPower` or `setVelocity`
- look up motor names
- mix mecanum wheel powers
- decide PIDF or path follow

Those stay in TeamCode (and later Pedro). Hardware names stay in the robot project. Point students at FtcRobotController TeamCode when they ask "which motor is front left?"

## Lesson order

1. Install SHIFT next to FTC SDK 12.0 ([INSTALL.md](INSTALL.md)).
2. Read TeamCode `assets/shift/README.md` for the BumbleBee drive dictionary.
3. Retell: gamepad -> JSON -> `drive.translation` / `drive.rotation` / `drive.slow` / `drive.boost` -> `MecanumDrive`.
4. Omit SHIFT: run BumbleBee Motor Test.
5. Break the JSON on purpose in a shop copy, confirm `usedFallback()` true, then Motor Test.

FORGE session plans stay in FORGE. Link only: [FORGE#35](https://github.com/The-Allsparks/FORGE/issues/35).
