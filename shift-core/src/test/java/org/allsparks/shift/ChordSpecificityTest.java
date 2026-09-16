package org.allsparks.shift;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allsparks.shift.clock.ManualClock;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.SimulatedInputDevice;
import org.allsparks.shift.intent.IntentFrame;
import org.junit.jupiter.api.Test;

class ChordSpecificityTest {
    private Shift chordShift(SimulatedInputDevice driver, SimulatedInputDevice codriver) {
        return ShiftFixtures.base(driver, codriver)
                .loadProfile(ShiftFixtures.profile(
                        "\"layers\": { \"mechanism\": { \"controller\": \"codriver\", \"bindings\": ["
                                + "{ \"id\": \"y-high\", \"source\": \"Y\", \"event\": \"ON_PRESS\", \"intent\": \"elevator.score.high\" },"
                                + "{ \"id\": \"chord\", \"when\": { \"all\": [\"LEFT_BUMPER\", \"Y\"] }, \"event\": \"ON_PRESS\", \"intent\": \"system.safeRetract\" },"
                                + "{ \"id\": \"or-ab\", \"when\": { \"any\": [\"A\", \"B\"] }, \"event\": \"ON_PRESS\", \"intent\": \"elevator.score.low\" },"
                                + "{ \"id\": \"not-lb\", \"when\": { \"all\": [\"X\", { \"not\": \"LEFT_BUMPER\" }] }, \"event\": \"ON_PRESS\", \"intent\": \"helm.score\" },"
                                + "{ \"id\": \"trigger\", \"when\": { \"gt\": { \"source\": \"RIGHT_TRIGGER\", \"value\": 0.7 } }, \"event\": \"ON_PRESS\", \"intent\": \"vision.lockTarget\" }"
                                + "] } }"))
                .build();
    }

    @Test
    void moreSpecificChordSuppressesSimpleBinding() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = chordShift(driver, codriver);

        codriver.hold(Control.LEFT_BUMPER);
        shift.update();
        codriver.press(Control.Y);
        IntentFrame frame = shift.update();
        assertTrue(frame.emitted("system.safeRetract"));
        assertFalse(frame.emitted("elevator.score.high"));
    }

    @Test
    void simpleBindingFiresWithoutTheModifier() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = chordShift(driver, codriver);

        codriver.press(Control.Y);
        IntentFrame frame = shift.update();
        assertTrue(frame.emitted("elevator.score.high"));
        assertFalse(frame.emitted("system.safeRetract"));
    }

    @Test
    void orTriggerFiresForEitherButton() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = chordShift(driver, codriver);

        codriver.press(Control.A);
        assertTrue(shift.update().emitted("elevator.score.low"));
        codriver.release(Control.A);
        shift.update();
        codriver.press(Control.B);
        assertTrue(shift.update().emitted("elevator.score.low"));
    }

    @Test
    void notTriggerBlocksWhenModifierHeld() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = chordShift(driver, codriver);

        codriver.press(Control.X);
        assertTrue(shift.update().emitted("helm.score"));
        codriver.release(Control.X);
        shift.update();
        codriver.hold(Control.LEFT_BUMPER);
        shift.update();
        codriver.press(Control.X);
        assertFalse(shift.update().emitted("helm.score"));
    }

    @Test
    void analogPredicateUsesStructuredComparison() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = chordShift(driver, codriver);

        codriver.setAnalog(Control.RIGHT_TRIGGER, 0.69);
        assertFalse(shift.update().emitted("vision.lockTarget"));
        codriver.setAnalog(Control.RIGHT_TRIGGER, 0.71);
        assertTrue(shift.update().emitted("vision.lockTarget"));
    }
}
