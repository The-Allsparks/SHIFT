# Deferred post-V1 capabilities

These are intentionally not in SHIFT V1. The runtime is designed so they can be added without replacing `InputDevice`, `InputSnapshot`, `Intent`, or profile schemaVersion 1's core fields.

| Capability | Why deferred | Hook already present |
| ---------- | ------------ | -------------------- |
| TOGGLE / LONG_PRESS / DOUBLE_TAP | Need solid V1 edges first | `BindingEvent.parse` rejects them with a reserved-feature message |
| Slew-rate limiting, calibration, asymmetric analog curves | Easy to get wrong on the hot path | Transform JSON reserves `slew` / `calibration` / `asymmetric` |
| Full input recording UI | Secondary to correctness | `ShiftInputListener` + `InputSnapshot` JSON + `ReplayInputDevice` |
| Keyboard / web / DS custom input | No current consumer | `InputDevice` |
| DualSense gyro / mute / adaptive triggers | Not present on FTC `Gamepad` | New `Control` values plus adapter fields if RobotCore grows them |
| Debounce windows (WPILib-style) | Extra state; not required for FTC digital buttons | Trigger wrapper can decorate later |
| JSON Schema validator on the robot | Too heavy; Android/FTC already parse via org.json | Schema file is for IDEs and CI |
| Maven Central publication | Org process | `maven-publish` is wired |
| Multi-SDK Gamepad adapters | 12.0 is the current ecosystem | Adapter module is separate from core |
| Built-in TRACE implementation | Would couple SHIFT to TRACE | `ShiftEventSink` + `ShiftInputListener` |
| Audio announcements | ECHO's job | Feedback / `LAYER_CHANGED` events |
| Command scheduler / subsystem requirements | Out of scope | Optional `LogicalResource` metadata only |

SchemaVersion 2 should be used if binding JSON shape must break. Do not silently reinterpret V1 files.
