package org.allsparks.shift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allsparks.shift.clock.ManualClock;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.SimulatedInputDevice;
import org.allsparks.shift.intent.IntentFrame;
import org.allsparks.shift.intent.Vector2;
import org.junit.jupiter.api.Test;

class MultiControllerTest {
    @Test
    void rolesStayIndependent() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = ShiftFixtures.base(driver, codriver)
                .loadProfile(ShiftFixtures.profile("\"layers\": {"
                        + "\"drive\": { \"controller\": \"driver\", \"bindings\": ["
                        + "  { \"source\": \"Y\", \"event\": \"ON_PRESS\", \"intent\": \"helm.score\" },"
                        + "  { \"x\": { \"source\": \"LEFT_STICK_X\" }, \"y\": { \"source\": \"LEFT_STICK_Y\", \"transform\": { \"invert\": true } }, \"intent\": \"drive.translation\" }"
                        + "] },"
                        + "\"mechanism\": { \"controller\": \"codriver\", \"bindings\": ["
                        + "  { \"source\": \"Y\", \"event\": \"ON_PRESS\", \"intent\": \"elevator.score.high\" }"
                        + "] } }"))
                .build();

        driver.setStick(0.5, -1.0, 0.0, 0.0);
        driver.press(Control.Y);
        codriver.press(Control.Y);
        IntentFrame frame = shift.update();
        assertTrue(frame.emitted("helm.score"));
        assertTrue(frame.emitted("elevator.score.high"));
        Vector2 translation = frame.vector2("drive.translation");
        assertEquals(0.5, translation.x(), 1e-9);
        assertEquals(1.0, translation.y(), 1e-9);
        assertFalse(frame.emitted("intake.collect"));
    }
}
