# Initial repository audit (2026-09-04)

## Finding

There was **no existing SHIFT repository** in The Allsparks workspace. Sibling libraries (AMPER, HELM, TRACE, MIMIC, ECHO, BEACON, ViDAR, FORGE) do not implement a semantic input framework.

FORGE's architecture map lists TRACE, AMPER, MIMIC, ViDAR, BEACON, ECHO, and HELM. SHIFT is specified as the human-interface layer feeding HELM, with events consumable by TRACE/ECHO, and no compile-time coupling.

## What was retained

Nothing to retain: this is a greenfield library. Build conventions were copied from AMPER (multi-module core/ftc/stubs/examples, Java 8, Spotless Palantir, RobotCore 11.2 `compileAgainstFtcSdk`, MIT license, CI matrix).

## Architecture decisions already implied by siblings

* Core must not import FTC types (AMPER `amper-core`, HELM desktop tests).
* Adapters live in `*-ftc` with compile-only stubs for CI.
* Observability is a sink interface, not a TRACE dependency (HELM/TRACE ADRs).
* Replay wants immutable snapshots (TRACE ADR 0004 input/output/event; HELM replay isolation).

## Gaps filled by this V1

Input snapshots, role mapping, analog transforms, digital edges, versioned JSON profiles, validation, layers, composite triggers, specificity, typed intents, feedback, event sink, simulated/replay devices, example profile, JSON Schema, CI.
