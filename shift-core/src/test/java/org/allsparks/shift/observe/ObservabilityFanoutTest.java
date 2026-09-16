package org.allsparks.shift.observe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allsparks.shift.Shift;
import org.allsparks.shift.ShiftFixtures;
import org.allsparks.shift.clock.ManualClock;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.input.InputSnapshot;
import org.allsparks.shift.input.SimulatedInputDevice;
import org.junit.jupiter.api.Test;

class ObservabilityFanoutTest {
    @Test
    void compositeEventSinkDeliversToBothListeners() {
        RecordingEventSink first = new RecordingEventSink();
        RecordingEventSink second = new RecordingEventSink();
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        ShiftFixtures.base(driver, codriver)
                .eventSink(CompositeEventSink.of(first, second))
                .loadProfile(ShiftFixtures.profile(
                        "\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": [] } }"))
                .build();
        assertTrue(first.containsType(ShiftEventType.PROFILE_LOADED));
        assertTrue(second.containsType(ShiftEventType.PROFILE_LOADED));
        assertEquals("test", first.last(ShiftEventType.PROFILE_LOADED).field("profile"));
    }

    @Test
    void inputListenerSeesEverySampledSnapshot() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        RecordingInputListener listener = new RecordingInputListener();
        Shift shift = ShiftFixtures.base(driver, codriver)
                .inputListener(listener)
                .loadProfile(ShiftFixtures.profile(
                        "\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": [] } }"))
                .build();
        driver.setAnalog(Control.LEFT_STICK_X, 0.4);
        shift.update();
        assertEquals(2, listener.count);
        assertSame(ControllerRole.DRIVER, listener.driverRole);
        assertEquals(0.4, listener.driverSnapshot.analog(Control.LEFT_STICK_X), 1e-9);
    }

    @Test
    void compositeInputListenerFansOut() {
        RecordingInputListener first = new RecordingInputListener();
        RecordingInputListener second = new RecordingInputListener();
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = ShiftFixtures.base(driver, codriver)
                .inputListener(CompositeInputListener.of(first, second))
                .loadProfile(ShiftFixtures.profile(
                        "\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": [] } }"))
                .build();
        shift.update();
        assertEquals(2, first.count);
        assertEquals(2, second.count);
    }

    static final class RecordingInputListener implements ShiftInputListener {
        int count;
        ControllerRole driverRole;
        InputSnapshot driverSnapshot;

        @Override
        public void onInput(long sequence, ControllerRole role, InputSnapshot snapshot) {
            count++;
            if (ControllerRole.DRIVER.equals(role)) {
                driverRole = role;
                driverSnapshot = snapshot;
            }
        }
    }
}
