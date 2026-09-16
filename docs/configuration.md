# JSON configuration reference

Every profile MUST declare `"schemaVersion": 1`. Other versions are rejected at load.

JSON Schema (editor validation and autocomplete): [`schema/shift-profile-v1.json`](../schema/shift-profile-v1.json).

## Root object

| Field | Required | Description |
| ----- | -------- | ----------- |
| `schemaVersion` | yes | Integer `1` |
| `id` | no | Stable name such as `competition` or `garrett/offense` |
| `name` | no | Display name |
| `description` | no | Human notes |
| `person` | no | Operator folder such as `garrett`. Combined with `set` into canonical id `person/set` |
| `set` | no | Named variant inside a person folder: `general`, `offense`, `defense`, `red`, `blue`, or any other token |
| `controllers` | yes | Role → slot / default layer / optional hardware `model` |
| `layers` | no | Named layers with bindings |
| `bindings` | no | Top-level bindings (empty layer = all layers) |
| `feedback` | no | Semantic feedback → rumble/LED |
| `settings` | no | Opaque string map; SHIFT does not interpret it |

## Controllers

```json
"controllers": {
  "driver": { "slot": 1, "defaultLayer": "drive", "model": "iwgame-wired-ps5" },
  "codriver": { "slot": 2, "defaultLayer": "mechanism" }
}
```

Role names are application-defined. `slot` is informational for humans and Driver Station numbering. SHIFT binds devices by the role string passed to `addDevice`.

`model` is optional. It names the physical pad (Logitech F310, Xbox 360, DualShock 4 / Etpark, official DualSense, or the IWGAME wired PS-5/PC DualSense-style clone). It does **not** remap HID; the Driver Station already did that. Unknown model tokens fail at load.

PlayStation / DualSense-style aliases (`CIRCLE`, `CREATE`, `EAST`, `SOUTH`, `TOUCHPAD_FINGER_1`, …) resolve to the same physical control as the canonical FTC field names. They do not duplicate state.

Digital trigger clicks (`LEFT_TRIGGER_PRESSED` / `RIGHT_TRIGGER_PRESSED`, aliases `LTPRESSED`, `LTCLICK`, …) are SDK boolean fields, distinct from analog `LEFT_TRIGGER` / `RIGHT_TRIGGER`. Use analog `gt`/`gte` when you want a travel threshold; use the pressed control when you want the pad's click.

IWGAME rear paddles are firmware-programmable. RobotCore 11.2 has no paddle fields, so program those two back buttons onto existing controls (bumpers, face buttons) and bind those names in the profile.

## Profile banks

`ProfileBank` parses JSON once at construction. There is no coded cap on how many profiles a bank may hold.

```java
Profile session = bank.compose("garrett/offense", "sam/general");
shift.activate(session); // compiled Profile only; no JSON on the loop
```

`compose` takes driver bindings from the first profile and operator (codriver) bindings from the second. Person-role files for gamepad 1 must declare `controllers.driver`; operator files must declare `controllers.codriver`. Examples live in `examples/profiles/garrett-*.json` and `examples/profiles/sam-general.json`.

Call `activate` from `init_loop` or between matches, not from `update()`.

## Bindings

Simple digital:

```json
{
  "source": "Y",
  "event": "ON_PRESS",
  "intent": "elevator.score.high"
}
```

PlayStation / semantic aliases (`CIRCLE`, `CREATE`, `EAST`, `SOUTH`, …) resolve to the same physical control as `B` / `A` / `BACK`. They do not duplicate state.

Analog:

```json
{
  "source": "LEFT_STICK_Y",
  "intent": "drive.rotation",
  "transform": {
    "deadband": 0.08,
    "invert": true,
    "scale": 1.0,
    "exponent": 1.8
  }
}
```

Transform order: invert → deadband (with range rescale) → exponent → scale → clamp.

VECTOR2:

```json
{
  "intent": "drive.translation",
  "x": { "source": "LEFT_STICK_X", "transform": { "deadband": 0.08 } },
  "y": { "source": "LEFT_STICK_Y", "transform": { "deadband": 0.08, "invert": true } }
}
```

### Events

| Event | Meaning |
| ----- | ------- |
| `ON_PRESS` | Rising edge. One discrete intent. |
| `ON_RELEASE` | Falling edge. One discrete intent. |
| `WHILE_HELD` | Continuous boolean (`frame.held(id)`). Requires a `BOOLEAN` intent. |
| `ANALOG` | Continuous numeric/vector. |

`TOGGLE`, `LONG_PRESS`, and `DOUBLE_TAP` are reserved and rejected in schemaVersion 1.

If `event` is omitted, SHIFT infers `ON_PRESS` for `EVENT`, `WHILE_HELD` for `BOOLEAN`, and `ANALOG` for `ANALOG`/`VECTOR2`.

### Composite triggers

```json
{
  "when": { "all": ["LEFT_BUMPER", "Y"] },
  "event": "ON_PRESS",
  "intent": "system.safeRetract"
}
```

```json
{
  "when": { "any": ["A", "B"] },
  "event": "ON_PRESS",
  "intent": "elevator.score.low"
}
```

```json
{
  "when": { "not": "LEFT_BUMPER" }
}
```

```json
{
  "when": { "gt": { "source": "RIGHT_TRIGGER", "value": 0.7 } }
}
```

Comparisons: `gt`, `gte`, `lt`, `lte`. There is no expression language or script engine.

### Layer actions

```json
{
  "role": "codriver",
  "source": "DPAD_RIGHT",
  "event": "ON_PRESS",
  "setLayer": "vision"
}
```

`cycleLayers` rotates through a list. Layer changes apply at the end of the update cycle.

### Other binding fields

| Field | Description |
| ----- | ----------- |
| `id` | Stable id for logs (defaults to `binding-N`) |
| `role` / `controller` | Role that owns the binding |
| `layer` | Restrict to a layer |
| `priority` | Metadata for downstream arbitration |
| `allowOverlap` | Disable specificity suppression and equal-trigger conflict rejection for this binding |

## Specificity and conflicts

Rules are deterministic and **not** based on JSON order:

1. A firing digital binding is suppressed when another firing binding on the same role and layer has a proper superset of required (positive) controls.
2. Example: `Y` and `LEFT_BUMPER + Y` — the chord wins while the bumper is held.
3. Two bindings with the same role, layer, event, required control set, and different intents fail validation.
4. Duplicate identical bindings fail validation.
5. `allowOverlap: true` opts a binding out of (1) and (3).

`NOT` does not add required controls. Analog predicates add specificity beyond the control set.

## Feedback

```json
"feedback": {
  "TARGET_ACQUIRED": {
    "role": "driver",
    "rumble": "short",
    "led": { "r": 0, "g": 1, "b": 0, "durationMs": 250 }
  }
}
```

Rumble patterns: `short`, `long`, `double`, `pulse`, `stop`. LED RGB is 0–1. Xbox 360 and Logitech F310 have no RGB light; the SDK no-ops. DualSense-style pads (official DualSense and IWGAME wired PS-5/PC) use the same `setLedColor` path as PS4 once the Driver Station maps them as a PlayStation gamepad.

## Validation errors

Errors include a JSON path and, where cheap, a suggestion:

```text
layers.drive.bindings[7].intent:
Unknown intent 'elevtor.score.high'.
Did you mean 'elevator.score.high'?
```

Validated at load: schema version, unknown controls, malformed triggers, invalid transforms, unknown layers, duplicate/conflicting bindings, unreachable layers, unknown intents, intent/event type mismatches, invalid feedback references (when a feedback registry is provided).
