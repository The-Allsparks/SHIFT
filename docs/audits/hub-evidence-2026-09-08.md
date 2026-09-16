# Hub evidence: SHIFT on BumbleBee Drive (2026-09-08)

Dated Control Hub record for [#11](https://github.com/The-Allsparks/SHIFT/issues/11). Desktop `check` and `compileAgainstFtcSdk` are not this record.

This card uses an existing Hub pull. It is not a new shop run. It does not attach `.tlog` binaries (gitignored). It does not copy match logcat files into SHIFT.

This is not an FTC-ready or competition-approval claim.

## What a student can point at

| Claim | Point here, not at GitHub Actions |
| ----- | --------------------------------- |
| SHIFT on | BumbleBee Drive match log, 8 Sep 18:11 |
| SHIFT omitted | BumbleBee Motor Test match log, 7 Sep 15:07 (same Hub pull) |
| Loop time | FORGE readiness dashboard 8 Sep Hub row (Drive-only TRACE median) |

## Sources

| Source | Where it lives | What it is |
| ------ | -------------- | ---------- |
| Drive match log | Workspace (not in SHIFT git): `logs/hub-2026-09-08-1813/matchlogs/Match-0-BumbleBee_Drive.txt` | Robot Controller match logcat for OpMode **BumbleBee Drive** |
| Motor Test match log | Same pull: `logs/hub-2026-09-08-1813/matchlogs/Match-0-BumbleBee_Motor_Test.txt` | Robot Controller match logcat for OpMode **BumbleBee Motor Test** |
| Loop duration | [FORGE readiness dashboard](https://github.com/The-Allsparks/FORGE/blob/main/season/2026-2027-biobuzz/readiness-dashboard.md) 8 Sep Hub row | Drive-only TRACE median; not a number printed in the match logcat file |
| Drive composition root | FtcRobotController TeamCode `BumbleBeeTeleOp` | Always constructs `FtcShift` |
| Motor Test omit path | FtcRobotController TeamCode `bringup/MotorTestOpMode` | SHIFT is not in that file on purpose |
| Profile asset | FtcRobotController TeamCode `assets/shift/bumblebee-drive.json` | JSON `id` is `bumblebee-drive` (source, not logcat) |

## Drive (SHIFT on)

OpMode **BumbleBee Drive** on the Hub pull `hub-2026-09-08-1813`:

- INIT: `2026-09-08 18:11:58` (`CMD_INIT_OP_MODE` BumbleBee Drive)
- PLAY: `2026-09-08 18:11:59` (`CMD_RUN_OP_MODE` BumbleBee Drive)
- STOP: `2026-09-08 18:13:27` (`$Stop$Robot$`)
- Enabled window: about 90 seconds

The match logcat has no Exception, FATAL, or SHIFT/init crash between INIT and PLAY. INIT completed and PLAY ran.

TeamCode `BumbleBeeTeleOp` always constructs `FtcShift` in `init()` from asset `shift/bumblebee-drive.json` and calls `shift.update()` once per `loop()`. That OpMode is the SHIFT-on path. Match logcat does not print each `update()` call.

## Motor Test (SHIFT omitted)

Same Hub pull, file dated **7 Sep** (Drive session cited above is **8 Sep 18:11**; both are Hub pulls):

- INIT: `2026-09-07 15:07:41` (`CMD_INIT_OP_MODE` BumbleBee Motor Test)
- PLAY: `2026-09-07 15:07:42` (`CMD_RUN_OP_MODE`)
- STOP: `2026-09-07 15:07:50`

`MotorTestOpMode` comments state SHIFT is not in that file on purpose. Direct `gamepad1` and `setPower`. A bad SHIFT JSON cannot hide a swapped wire.

The match logcat shows INIT through STOP without a crash. It does not print wheel motion.

## Loop time

From the FORGE readiness dashboard, 8 Sep Hub row (not from the match logcat file):

- Drive-only TRACE: about **18 ms** median
- AMPER with motor current: about **63 ms** median (**do not repeat**)

`.tlog` binaries are gitignored. This card does not attach them. TRACE `/TRACE/Loop/Duration` is not quoted from a file in this repository.

Motor Test loop time vs Drive is **not measured** in these match logs.

## Honest gaps

- RC logcat does not print `shiftFallback` or profile id. Those are Driver Station telemetry keys in `BumbleBeeTeleOp` (`shiftProfile`, `shiftFallback`). This card does not invent those values.
- Stick vs `drive.*` telemetry is also DS-only. It is not in the cited match logcat.
- Loop duration number is from the FORGE dashboard, not the match logcat file.
- Motor Test log is 7 Sep; Drive session cited is 8 Sep 18:11. Both are Hub pulls under `hub-2026-09-08-1813`.
- Fallback false on good JSON is the TeamCode contract (`usedFallback()` after `FtcShift.builder()`), not a logcat line from this session.

## What this does not claim

- Combined optional-library loop budget (AMPER motor current on Drive is the 63 ms row; do not repeat).
- AMPER intervention.
- Match play.
- FTC-ready from desktop JVM tests.
