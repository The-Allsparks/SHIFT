# TRACE and ECHO adapter contracts

SHIFT exposes two observer interfaces so TRACE, ECHO, and telemetry can listen without a SHIFT compile-time dependency on those libraries. TeamCode owns the adapters. This repository must not add a TRACE or ECHO Gradle dependency.

The BumbleBee example lives in [The-Allsparks/FtcRobotController](https://github.com/The-Allsparks/FtcRobotController), not in this repo: `org.firstinspires.ftc.teamcode.TraceShiftAdapter`.

## ShiftEventSink vs ShiftInputListener

| Interface | When SHIFT calls it | What it is for |
| --------- | ------------------- | -------------- |
| `ShiftEventSink` | Structured lifecycle events (`PROFILE_LOADED`, `LAYER_CHANGED`, digital `INPUT` edges, and others) | Semantic events. TRACE may record them as TRACE events. ECHO may listen. |
| `ShiftInputListener` | Once per assigned controller after each `Shift.update()` sample | Stick and button samples every loop, including analog values. TRACE records them as `RecordCategory.INPUT`. |

Digital `INPUT` events on the sink are TRACE-level edges only. Analog sticks are not those events. Use `ShiftInputListener` for per-loop stick samples.

Do not retain the `InputSnapshot` across loops unless you copy it. The snapshot is only valid for this loop.

## Thread and no-block rule

SHIFT calls both interfaces on the OpMode loop thread (the same thread as `loop()` / `Shift.update()`).

Implementations must not block. Do not sleep, wait on I/O, flush a log to disk, start threads, or allocate unbounded structures inside `onEvent` or `onInput`. A blocking adapter stalls the whole robot loop.

## TeamCode owns adapters

```text
SHIFT (this repo)
  ShiftEventSink / ShiftInputListener
        |
        v
TeamCode adapter (FtcRobotController)
  TraceShiftAdapter, optional ECHO adapter, telemetry
        |
        +---> TRACE
        +---> ECHO
```

- SHIFT compiles against Java 8 and `org.json` only in `shift-core`. It does not import TRACE or ECHO.
- TRACE and ECHO remain compile-time independent of SHIFT.
- Glue lives in TeamCode. Do not move `TraceShiftAdapter` into this repository. Do not add an `implementation` of TRACE or ECHO to SHIFT Gradle files.

A built-in TRACE implementation inside SHIFT is deferred. See [deferred.md](deferred.md).

## TRACE prefix convention

Prefixes are **team-defined**. SHIFT does not require a specific TRACE name.

BumbleBee (`TraceShiftAdapter`) uses:

| Kind | TRACE name | Notes |
| ---- | ---------- | ----- |
| Stick samples | `SHIFT/<role>/LeftStickX` (also LeftStickY, RightStickX, RightBumper, LeftTrigger) | `role` is `driver` or `codriver` |
| Discrete events | `SHIFT/<type>` such as `SHIFT/LAYER_CHANGED` | Events below INFO are dropped |

Role prefixes:

```text
SHIFT/driver/...
SHIFT/codriver/...
```

BumbleBee drops sink events below `ShiftEventLevel.INFO` so analog and DEBUG chatter does not flood the ESSENTIAL log. Set SHIFT `minimumEventLevel` and/or filter in the adapter.

Example names a student can look up in AdvantageScope:

```text
SHIFT/driver/LeftStickX
SHIFT/driver/LeftStickY
SHIFT/driver/RightStickX
SHIFT/codriver/LeftStickX
```

## ECHO

ECHO may implement `ShiftEventSink` (and optionally `ShiftInputListener`) to hear `LAYER_CHANGED`, `FEEDBACK`, and similar events. SHIFT maps semantic feedback to rumble and LED only. SHIFT does not play audio.

## Composite fan-out

`CompositeEventSink` and `CompositeInputListener` deliver to each registered listener in registration order, still on the OpMode loop thread.

```java
Shift shift = FtcShift.builder(gamepad1, gamepad2)
    .registerIntents(MyRobotIntents.all())
    .loadProfile(profileJson)
    .eventSink(CompositeEventSink.of(traceShiftAdapter, echoAdapter, telemetrySink))
    .inputListener(traceShiftAdapter)
    .build();
```

BumbleBee's `TraceShiftAdapter` wraps each callback in TRACE `FailOpen` and peeks `Trace.wouldAccept` before analog rows so ESSENTIAL downsampling does not allocate stick samples. That TeamCode class is the cookbook other libraries copy.

Desktop proof: `ObservabilityFanoutTest` in `shift-core`.

## What not to do

- Do not import TRACE or ECHO from `shift-core` or `shift-ftc`.
- Do not log every analog sample as a TRACE-level `ShiftEvent`. That is what `ShiftInputListener` is for, and even then record only the controls you need.
- Do not block on the OpMode loop thread.
- Do not treat this repo as the home of `TraceShiftAdapter`.
