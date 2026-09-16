# Architecture

SHIFT owns the human-interface abstraction. It converts operator input into semantic intent and maps semantic feedback onto physical controller effects.

```text
physical input
    → snapshot
    → normalization
    → trigger evaluation
    → binding resolution
    → semantic intent
```

```text
semantic feedback
    → SHIFT
    → rumble / LED
    → ShiftEventSink (TRACE, ECHO, telemetry, …)
```

## Subsystem boundary

| SHIFT does | SHIFT does not |
| ---------- | -------------- |
| Sample controllers once per loop | Command motors, servos, or PID |
| Normalize analog axes | Plan paths or run autonomous |
| Evaluate bindings and layers | Schedule subsystems or default commands |
| Emit typed intents with metadata | Decide whether HELM overrides teleop |
| Map semantic feedback to rumble/LED | Play audio (ECHO may listen to events) |
| Validate profiles at load | Parse JSON during `update()` |

Downstream systems such as HELM, MIMIC, AMPER, BEACON, ECHO, and TRACE remain compile-time independent. They consume intents and events through their own adapters.

## Modules

| Module | May depend on | Must not |
| ------ | ------------- | -------- |
| `shift-core` | Java 8, `org.json` | FTC SDK, Android, `shift-ftc` |
| `shift-ftc` | `shift-core` + FTC `Gamepad` | Stubs at runtime on the robot |
| `shift-ftc-stubs` | nothing | Publication, TeamCode |
| `shift-examples` | `shift-ftc` | Production robot behavior |

Architecture tests fail the build if `shift-core` imports `com.qualcomm`, `org.firstinspires.ftc`, `android.*`, `shift-ftc`, or `shift-examples`. Runtime packages must not import `profile`. `Shift.update()` must not parse JSON, hit the filesystem, start threads, or allocate scratch maps. `shift-ftc` and `shift-examples` must not call `setPower` / `setVelocity`.

## Snapshot contract

Every `update()` samples each assigned `InputDevice` exactly once. All bindings in that cycle observe those snapshots. Adapters must not let later bindings see a newer gamepad state.

`InputSnapshot` is immutable and JSON-round-trippable. `ReplayInputDevice` feeds recorded snapshots into the same pipeline. `ShiftInputListener` is called once per assigned controller after each sample so a TeamCode TRACE adapter can record `RecordCategory.INPUT` without SHIFT depending on TRACE. Full recording UI is deferred; the snapshot format is stable.

## Loop lifecycle

FTC OpModes are a single-threaded loop. SHIFT follows that:

1. Construct `Shift` during `init()` (JSON parse + validation, or `ProfileBank.compose`).
2. Call `shift.update()` once per `loop()`.
3. Read `IntentFrame` continuous values and discrete events.
4. Optionally `emitFeedback(...)`.
5. Call `shift.activate(compiledProfile)` from `init_loop` or between matches, never from `update()`.
6. Never block, allocate unbounded structures, or start threads inside `update()`.

The returned `IntentFrame` is reused on the next `update()`. Copy values that must survive across loops.

Layer switches are computed during the cycle that detects the binding and applied at the **end** of that cycle. The press that changes layer does not also fire the destination layer's bindings.

## Binding resolution

1. Ignore bindings whose role has no device or whose layer is inactive (empty layer matches all layers for that role).
2. Evaluate compiled triggers against current/previous snapshots.
3. For digital events, suppress a firing binding when another firing binding on the same role and layer has a **proper superset** of required controls, unless `allowOverlap` is set.
4. JSON declaration order is not a tie-break for safety-critical conflicts. Equal triggers with different intents fail validation.

Continuous analog/vector bindings are independent of digital specificity unless they share an intent id, in which case higher `IntentPriority.rank` wins and equal rank keeps the lexicographically earlier binding id.

## Observability

`ShiftEventSink` receives structured events. `CompositeEventSink` fans out to TRACE, ECHO, and telemetry adapters in registration order on the robot loop thread. Implementations must not block.

`ShiftInputListener` receives every sampled snapshot (including analog sticks) once per assigned controller per `update()`. Digital `INPUT` events remain TRACE-level edges only. TeamCode implements both interfaces; SHIFT does not depend on TRACE or ECHO.

| Type | Default level | Notes |
| ---- | ------------- | ----- |
| `INPUT` | TRACE | Digital edges only, not every analog sample |
| `BINDING_MATCH` | DEBUG | |
| `INTENT_EMITTED` | DEBUG (TRACE for analog) | |
| `LAYER_CHANGED` | INFO | role, previous layer, new layer |
| `PROFILE_LOADED` | INFO | includes `fallback=true` when used; `person`/`set` when grouped |
| `PROFILE_REJECTED` | ERROR | never silent |
| `FEEDBACK` | INFO | |
| `CONFLICT` | DEBUG | suppressed-by-specific, or continuous collision |
| `ERROR` | ERROR/WARN | unknown feedback at runtime |

Set `minimumEventLevel` to drop analog/debug noise on the robot.

## Fallback

```text
preferred external profile
        ↓
    validate
   ┌────┴────┐
 valid      invalid
   │           │
   ▼           ▼
activate    PROFILE_REJECTED
              │
              ▼
       fallback profile (embedded or provided)
              │
              ▼
       PROFILE_LOADED fallback=true
```

Invalid competition configuration is never hidden. `FtcShift.builder()` always enables the embedded idle fallback. Check `shift.usedFallback()` after `build()` and put it on Driver Station telemetry. To fail `init()` instead, use `Shift.builder()` and omit `fallbackToEmbedded()`. If both preferred and fallback fail, construction throws.

## Determinism

Given the same snapshots, compiled profile, and clock values, SHIFT produces the same intents. Time comes from snapshot timestamps when present, otherwise from the injected `ShiftClock`.
