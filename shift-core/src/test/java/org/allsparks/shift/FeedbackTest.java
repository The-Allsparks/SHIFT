package org.allsparks.shift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allsparks.shift.clock.ManualClock;
import org.allsparks.shift.feedback.RecordingFeedbackActuator;
import org.allsparks.shift.feedback.RumblePattern;
import org.allsparks.shift.input.SimulatedInputDevice;
import org.allsparks.shift.observe.RecordingEventSink;
import org.allsparks.shift.observe.ShiftEventType;
import org.junit.jupiter.api.Test;

class FeedbackTest {
    @Test
    void semanticFeedbackMapsToConfiguredRumble() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        RecordingFeedbackActuator actuator = new RecordingFeedbackActuator();
        RecordingEventSink sink = new RecordingEventSink();
        Shift shift = ShiftFixtures.base(driver, codriver)
                .eventSink(sink)
                .feedbackActuator(actuator)
                .registerFeedback("TARGET_ACQUIRED")
                .loadProfile(ShiftFixtures.profile(
                        "\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": [] } },"
                                + "\"feedback\": { \"TARGET_ACQUIRED\": { \"role\": \"driver\", \"rumble\": \"short\","
                                + " \"led\": { \"r\": 0, \"g\": 1, \"b\": 0, \"durationMs\": 200 } } }"))
                .build();

        shift.emitFeedback("TARGET_ACQUIRED");
        assertEquals(1, actuator.rumbles().size());
        assertEquals(RumblePattern.SHORT, actuator.rumbles().get(0).pattern());
        assertEquals(1, actuator.leds().size());
        assertEquals(1.0, actuator.leds().get(0).green(), 1e-9);
        assertTrue(sink.containsType(ShiftEventType.FEEDBACK));
    }
}
