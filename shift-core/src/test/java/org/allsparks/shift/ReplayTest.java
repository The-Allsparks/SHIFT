package org.allsparks.shift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.allsparks.shift.clock.ManualClock;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.input.InputSnapshot;
import org.allsparks.shift.input.ReplayInputDevice;
import org.allsparks.shift.input.SimulatedInputDevice;
import org.allsparks.shift.intent.IntentFrame;
import org.junit.jupiter.api.Test;

class ReplayTest {
    @Test
    void snapshotJsonRoundTripPreservesState() {
        InputSnapshot original = InputSnapshot.builder()
                .role(ControllerRole.DRIVER)
                .timestampMillis(42)
                .sequence(7)
                .setDigital(Control.A, true)
                .setDigital(Control.LEFT_TRIGGER_PRESSED, true)
                .setAnalog(Control.LEFT_STICK_Y, -0.25)
                .build();
        InputSnapshot restored = InputSnapshot.fromJson(original.toJson());
        assertEquals(original, restored);
    }

    @Test
    void replayDeviceFeedsRecordedFrames() {
        InputSnapshot press = InputSnapshot.builder()
                .role(ControllerRole.DRIVER)
                .timestampMillis(10)
                .sequence(1)
                .setDigital(Control.Y, true)
                .build();
        InputSnapshot hold = InputSnapshot.builder()
                .role(ControllerRole.DRIVER)
                .timestampMillis(20)
                .sequence(2)
                .setDigital(Control.Y, true)
                .build();
        ReplayInputDevice replay = new ReplayInputDevice(Arrays.asList(press, hold));
        SimulatedInputDevice codriver = ShiftFixtures.codriver(new ManualClock(0));
        Shift shift = Shift.builder()
                .addDevice(ControllerRole.DRIVER, replay)
                .addDevice(ControllerRole.CODRIVER, codriver)
                .registerIntents(ShiftFixtures.exampleIntents())
                .loadProfile(
                        ShiftFixtures.profile("\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": ["
                                + "{ \"source\": \"Y\", \"event\": \"ON_PRESS\", \"intent\": \"helm.score\" }] } }"))
                .build();

        IntentFrame first = shift.update();
        assertTrue(first.emitted("helm.score"));
        IntentFrame second = shift.update();
        assertTrue(!second.emitted("helm.score"));
    }
}
