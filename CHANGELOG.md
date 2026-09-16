# Changelog

## Unreleased

- Compile-check against FTC SDK **11.2.1** (Gamepad fields unchanged from 11.2).
- Sample digital trigger clicks as `LEFT_TRIGGER_PRESSED` / `RIGHT_TRIGGER_PRESSED`, distinct from analog `LEFT_TRIGGER` / `RIGHT_TRIGGER`.
- `CompositeEventSink`, `ShiftInputListener`, and `CompositeInputListener` so TRACE, ECHO, and telemetry can listen without a SHIFT compile dependency.
- `ProfileBank` with `person`/`set` ids (`garrett/offense`) and driver+operator compose; `Shift.activate(Profile)` swaps compiled maps without parsing JSON on the loop.

## 0.1.0 — 2026-09-04

Initial SHIFT V1: hardware-neutral snapshots, FTC adapters, JSON profiles, layers, composite triggers, specificity, typed intents, semantic feedback, observability sink, simulated and replay devices, JSON Schema, and CI.

DualSense-style pads, including the IWGAME wired PS-5/PC clone: profile `model` catalog, `CREATE` alias, touchpad finger contacts and XY sampling, and Driver Station mapping notes. Rear paddles remain firmware-remap-only (RobotCore 11.2 has no paddle fields).
