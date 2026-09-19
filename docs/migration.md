# Migration: traditional FTC gamepad code → SHIFT

## Before

```java
@Override
public void loop() {
    double y = -gamepad1.left_stick_y;
    double x = gamepad1.left_stick_x;
    double rx = gamepad1.right_stick_x;
    if (Math.abs(y) < 0.08) y = 0;
    drive.setWeightedDrivePower(new Pose(x, y, rx));

    if (gamepad2.y) {
        elevator.setTargetPosition(1462);
        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        elevator.setPower(0.8);
    }
}
```

Problems: hardware numbers live next to button names; changing drivers or controllers requires code edits; `if (gamepad2.y)` retriggers every loop; unit tests need a Driver Hub or a mocked SDK `Gamepad`.

## After

Profile:

```json
{
  "source": "LEFT_STICK_Y",
  "intent": "drive.translation",
  "transform": { "deadband": 0.08, "invert": true }
}
```

```json
{
  "source": "Y",
  "event": "ON_PRESS",
  "intent": "elevator.score.high"
}
```

OpMode:

```java
IntentFrame frame = shift.update();
Pose drive = toPose(frame.vector2("drive.translation"), frame.analog("drive.rotation"));
driveSubsystem.setWeightedDrivePower(drive);

if (frame.emitted("elevator.score.high")) {
    elevator.scoreHigh(); // mechanism layer owns ticks and power
}
```

The robot application still owns motors. SHIFT only says the operator asked for high score.

`FtcShift.builder()` enables the embedded idle fallback. After `build()`, check `shift.usedFallback()` and put it on telemetry so a rejected competition profile is visible on the Driver Station.

## Per-driver maps

Traditional: copy-paste OpModes or `if (driver.equals("studenta"))` in `loop()`. SHIFT: one JSON file per person/set (`studenta/offense`), `ProfileBank.compose(driverId, operatorId)` at `init()`, `shift.activate(compiled)` between matches. JSON is never parsed in `update()`.

## Edge detection

Traditional:

```java
boolean yWas;
if (gamepad2.y && !yWas) { /* press */ }
yWas = gamepad2.y;
```

SHIFT: `"event": "ON_PRESS"` with a compiled edge detector. Bindings in the same cycle cannot see different gamepad states.

## Chords

Traditional ad-hoc `if (gamepad2.left_bumper && gamepad2.y)` races a lone `if (gamepad2.y)`. SHIFT treats `LEFT_BUMPER + Y` as more specific and suppresses `Y` unless `allowOverlap` is set.

## Feedback

Traditional: `gamepad1.rumble(200)` inside vision code. SHIFT: `shift.emitFeedback("TARGET_ACQUIRED")` with the effect declared in the profile so driver preference can change without recompiling vision.
