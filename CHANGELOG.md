# Changelog

## Unreleased

- Example person-role profiles and docs use `studenta` / `studentb` instead of personal names.
- TRACE cookbook: TeamCode `TraceShiftAdapter` is fail-open and peeks `wouldAccept` before analog rows. SHIFT still does not import TRACE.

## 0.1.1 - 2026-09-16

First GitHub tag. GitHub never published `v0.1.0`. The FTC SDK 12.0.0 pin is a compatible patch on the 0.1.0 software V1.

- Compile-check against FTC SDK **12.0.0** (BIOBUZZ). Gamepad fields SHIFT samples are unchanged from 11.2.
- Sample digital trigger clicks as `LEFT_TRIGGER_PRESSED` / `RIGHT_TRIGGER_PRESSED`, distinct from analog `LEFT_TRIGGER` / `RIGHT_TRIGGER`.
- `CompositeEventSink`, `ShiftInputListener`, and `CompositeInputListener` so TRACE, ECHO, and telemetry can listen without a SHIFT compile dependency.
- `ProfileBank` with `person`/`set` ids (`studenta/offense`) and driver+operator compose; `Shift.activate(Profile)` swaps compiled maps without parsing JSON on the loop.

## 0.1.0 — 2026-09-04

Initial SHIFT V1: hardware-neutral snapshots, FTC adapters, JSON profiles, layers, composite triggers, specificity, typed intents, semantic feedback, observability sink, simulated and replay devices, JSON Schema, and CI.

DualSense-style pads, including the IWGAME wired PS-5/PC clone: profile `model` catalog, `CREATE` alias, touchpad finger contacts and XY sampling, and Driver Station mapping notes. Rear paddles remain firmware-remap-only (RobotCore 11.2 has no paddle fields).
