package org.allsparks.shift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allsparks.shift.clock.ManualClock;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.SimulatedInputDevice;
import org.allsparks.shift.intent.IntentFrame;
import org.junit.jupiter.api.Test;

class DigitalBindingTest {
    @Test
    void onPressFiresOnceWhileHeld() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = ShiftFixtures.base(driver, codriver)
                .loadProfile(
                        ShiftFixtures.profile("\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": ["
                                + "{ \"source\": \"Y\", \"event\": \"ON_PRESS\", \"intent\": \"elevator.score.high\" }"
                                + "] } }"))
                .build();

        driver.press(Control.Y);
        IntentFrame first = shift.update();
        assertTrue(first.emitted("elevator.score.high"));

        IntentFrame held = shift.update();
        assertFalse(held.emitted("elevator.score.high"));

        driver.release(Control.Y);
        shift.update();
        driver.press(Control.Y);
        assertTrue(shift.update().emitted("elevator.score.high"));
    }

    @Test
    void triggerPressedIsDistinctFromAnalogTrigger() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = ShiftFixtures.base(driver, codriver)
                .loadProfile(
                        ShiftFixtures.profile("\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": ["
                                + "{ \"source\": \"LEFT_TRIGGER_PRESSED\", \"event\": \"WHILE_HELD\", \"intent\": \"drive.slow\" }"
                                + "] } }"))
                .build();

        driver.setAnalog(Control.LEFT_TRIGGER, 1.0);
        assertFalse(shift.update().held("drive.slow"));
        driver.hold(Control.LEFT_TRIGGER_PRESSED);
        assertTrue(shift.update().held("drive.slow"));
    }

    @Test
    void onReleaseFiresWhenButtonGoesUp() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = ShiftFixtures.base(driver, codriver)
                .loadProfile(
                        ShiftFixtures.profile("\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": ["
                                + "{ \"source\": \"A\", \"event\": \"ON_RELEASE\", \"intent\": \"helm.score\" }"
                                + "] } }"))
                .build();

        driver.press(Control.A);
        assertFalse(shift.update().emitted("helm.score"));
        driver.release(Control.A);
        assertTrue(shift.update().emitted("helm.score"));
    }

    @Test
    void whileHeldIsContinuousBoolean() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = ShiftFixtures.base(driver, codriver)
                .loadProfile(
                        ShiftFixtures.profile("\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": ["
                                + "{ \"source\": \"A\", \"event\": \"WHILE_HELD\", \"intent\": \"intake.collect\" }"
                                + "] } }"))
                .build();

        assertFalse(shift.update().held("intake.collect"));
        driver.hold(Control.A);
        assertTrue(shift.update().held("intake.collect"));
        assertTrue(shift.update().held("intake.collect"));
        driver.release(Control.A);
        assertFalse(shift.update().held("intake.collect"));
    }

    @Test
    void analogBindingAppliesTransform() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = ShiftFixtures.base(driver, codriver)
                .loadProfile(
                        ShiftFixtures.profile("\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": ["
                                + "{ \"source\": \"RIGHT_STICK_X\", \"event\": \"ANALOG\", \"intent\": \"drive.rotation\","
                                + "  \"transform\": { \"deadband\": 0.2, \"invert\": true, \"scale\": 0.5 } }"
                                + "] } }"))
                .build();

        driver.setAnalog(Control.RIGHT_STICK_X, 1.0);
        IntentFrame frame = shift.update();
        assertEquals(-0.5, frame.analog("drive.rotation"), 1e-9);
    }
}
