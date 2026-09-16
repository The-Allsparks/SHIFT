package org.allsparks.shift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allsparks.shift.clock.ManualClock;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.input.SimulatedInputDevice;
import org.allsparks.shift.intent.IntentFrame;
import org.allsparks.shift.observe.RecordingEventSink;
import org.allsparks.shift.observe.ShiftEventType;
import org.junit.jupiter.api.Test;

class LayerTest {
    @Test
    void sameButtonMapsDifferentlyAcrossLayers() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        RecordingEventSink sink = new RecordingEventSink();
        Shift shift = ShiftFixtures.base(driver, codriver)
                .eventSink(sink)
                .loadProfile(ShiftFixtures.profile("\"layers\": {"
                        + "\"mechanism\": { \"controller\": \"codriver\", \"bindings\": ["
                        + "  { \"source\": \"A\", \"event\": \"WHILE_HELD\", \"intent\": \"intake.collect\" },"
                        + "  { \"source\": \"Y\", \"event\": \"ON_PRESS\", \"intent\": \"elevator.score.high\" }"
                        + "] },"
                        + "\"vision\": { \"controller\": \"codriver\", \"bindings\": ["
                        + "  { \"source\": \"A\", \"event\": \"ON_PRESS\", \"intent\": \"vision.nextTarget\" },"
                        + "  { \"source\": \"Y\", \"event\": \"ON_PRESS\", \"intent\": \"vision.lockTarget\" }"
                        + "] }"
                        + "}, \"bindings\": ["
                        + "{ \"role\": \"codriver\", \"source\": \"DPAD_RIGHT\", \"event\": \"ON_PRESS\", \"setLayer\": \"vision\" },"
                        + "{ \"role\": \"codriver\", \"source\": \"DPAD_LEFT\", \"event\": \"ON_PRESS\", \"setLayer\": \"mechanism\" }"
                        + "]"))
                .build();

        assertEquals("mechanism", shift.activeLayer(ControllerRole.CODRIVER));
        codriver.hold(Control.A);
        assertTrue(shift.update().held("intake.collect"));
        codriver.release(Control.A);
        shift.update();

        codriver.press(Control.DPAD_RIGHT);
        IntentFrame switched = shift.update();
        assertEquals("vision", shift.activeLayer(ControllerRole.CODRIVER));
        assertTrue(sink.containsType(ShiftEventType.LAYER_CHANGED));
        assertEquals("vision", switched.layer(ControllerRole.CODRIVER));

        codriver.release(Control.DPAD_RIGHT);
        shift.update();
        codriver.press(Control.A);
        IntentFrame vision = shift.update();
        assertTrue(vision.emitted("vision.nextTarget"));
        assertFalse(vision.held("intake.collect"));
    }

    @Test
    void layerSwitchDoesNotFireDestinationBindingsSameCycle() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = ShiftFixtures.base(driver, codriver)
                .loadProfile(ShiftFixtures.profile("\"layers\": {"
                        + "\"mechanism\": { \"controller\": \"codriver\", \"bindings\": ["
                        + "  { \"source\": \"A\", \"event\": \"WHILE_HELD\", \"intent\": \"intake.collect\" }"
                        + "] },"
                        + "\"vision\": { \"controller\": \"codriver\", \"bindings\": ["
                        + "  { \"source\": \"A\", \"event\": \"ON_PRESS\", \"intent\": \"vision.nextTarget\" }"
                        + "] }"
                        + "}, \"bindings\": ["
                        + "{ \"role\": \"codriver\", \"source\": \"DPAD_RIGHT\", \"event\": \"ON_PRESS\", \"setLayer\": \"vision\" }"
                        + "]"))
                .build();

        assertEquals("mechanism", shift.activeLayer(ControllerRole.CODRIVER));
        codriver.press(Control.A);
        codriver.press(Control.DPAD_RIGHT);
        IntentFrame switched = shift.update();
        assertEquals("vision", shift.activeLayer(ControllerRole.CODRIVER));
        assertTrue(switched.held("intake.collect"));
        assertFalse(switched.emitted("vision.nextTarget"));

        codriver.release(Control.DPAD_RIGHT);
        shift.update();
        codriver.release(Control.A);
        shift.update();
        codriver.press(Control.A);
        IntentFrame vision = shift.update();
        assertTrue(vision.emitted("vision.nextTarget"));
        assertFalse(vision.held("intake.collect"));
    }
}
