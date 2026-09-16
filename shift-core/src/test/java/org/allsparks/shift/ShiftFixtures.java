package org.allsparks.shift;

import org.allsparks.shift.clock.ManualClock;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.input.SimulatedInputDevice;
import org.allsparks.shift.intent.IntentRegistry;
import org.allsparks.shift.intent.IntentType;
import org.allsparks.shift.observe.RecordingEventSink;
import org.allsparks.shift.observe.ShiftEventLevel;

public final class ShiftFixtures {
    private ShiftFixtures() {}

    public static IntentRegistry exampleIntents() {
        return new IntentRegistry()
                .register("drive.translation", IntentType.VECTOR2, "DRIVE_TRANSLATION")
                .register("drive.rotation", IntentType.ANALOG, "DRIVE_ROTATION")
                .register("drive.cor.longitudinal", IntentType.ANALOG)
                .register("drive.slow", IntentType.BOOLEAN)
                .register("helm.acquire", IntentType.BOOLEAN)
                .register("helm.score", IntentType.EVENT)
                .register("intake.collect", IntentType.BOOLEAN, "INTAKE")
                .register("elevator.score.high", IntentType.EVENT, "ELEVATOR")
                .register("elevator.score.low", IntentType.EVENT, "ELEVATOR")
                .register("system.safeRetract", IntentType.EVENT)
                .register("vision.nextTarget", IntentType.EVENT)
                .register("vision.lockTarget", IntentType.EVENT)
                .register("calibration.home", IntentType.EVENT);
    }

    public static String profile(String body) {
        return "{ \"schemaVersion\": 1, \"id\": \"test\", \"controllers\": {"
                + "\"driver\": { \"slot\": 1, \"defaultLayer\": \"drive\" },"
                + "\"codriver\": { \"slot\": 2, \"defaultLayer\": \"mechanism\" }"
                + "}, "
                + body
                + " }";
    }

    public static Shift.Builder base(SimulatedInputDevice driver, SimulatedInputDevice codriver) {
        RecordingEventSink sink = new RecordingEventSink();
        return Shift.builder()
                .addDevice(ControllerRole.DRIVER, driver)
                .addDevice(ControllerRole.CODRIVER, codriver)
                .registerIntents(exampleIntents())
                .eventSink(sink)
                .minimumEventLevel(ShiftEventLevel.TRACE)
                .clock(new ManualClock(1_000L));
    }

    public static SimulatedInputDevice driver() {
        return new SimulatedInputDevice(ControllerRole.DRIVER, new ManualClock(1_000L));
    }

    public static SimulatedInputDevice driver(ManualClock clock) {
        return new SimulatedInputDevice(ControllerRole.DRIVER, clock);
    }

    public static SimulatedInputDevice codriver(ManualClock clock) {
        return new SimulatedInputDevice(ControllerRole.CODRIVER, clock);
    }
}
