# Intent design guide

SHIFT intents name **operator goals**, not hardware commands.

## Good

```text
elevator.score.high
intake.collect
drive.translation
vision.lockTarget
system.safeRetract
helm.acquire
```

These can stay stable when motors, gear ratios, or encoder ticks change.

## Bad

```text
elevator.motor.power.75
gamepad2.y
servo.wrist.0.42
encoder.1462
setPower(0.8)
```

Those belong in the mechanism layer (for example MIMIC), not in a human-input profile.

## Types

| Type | Use when | Consumer API |
| ---- | -------- | ------------ |
| `EVENT` | A one-shot request | `frame.emitted(id)` / `frame.events()` |
| `BOOLEAN` | A held mode or gated action | `frame.held(id)` |
| `ANALOG` | A single axis | `frame.analog(id)` |
| `VECTOR2` | A stick / planar command | `frame.vector2(id)` |

Do not pretend a stick is a button. Do not emit `ON_PRESS` for `drive.rotation`.

## Naming

Use `domain.action` or `domain.action.qualifier` with letters, digits, `_`, `.`, and `-`.

Prefer team vocabulary that drivers already use: score high, lock target, slow.

Register every intent before loading a profile:

```java
registry.register("drive.rotation", IntentType.ANALOG, "DRIVE_ROTATION");
registry.register("elevator.score.high", IntentType.EVENT, "ELEVATOR");
```

The optional resource string is metadata for downstream ownership reasoning. SHIFT does not implement a command scheduler.

## Priority

`SAFETY`, `MANUAL_OVERRIDE`, `AUTOMATION`, `ASSIST`, and `DEFAULT` are conventional ranks. SHIFT attaches them to emitted intents. HELM (or the OpMode) decides whether a drivetrain intent overrides autonomous. SHIFT only resolves **physical binding** conflicts.

## Application constants

JSON must stay stringly typed. Java may wrap ids:

```java
public final class MyRobotIntents {
    public static final IntentId ELEVATOR_HIGH = IntentId.of("elevator.score.high");

    public static IntentRegistry all() {
        return new IntentRegistry().register(ELEVATOR_HIGH, IntentType.EVENT);
    }
}
```
