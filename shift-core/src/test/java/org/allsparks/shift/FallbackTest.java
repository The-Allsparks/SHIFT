package org.allsparks.shift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allsparks.shift.clock.ManualClock;
import org.allsparks.shift.config.ConfigException;
import org.allsparks.shift.input.SimulatedInputDevice;
import org.allsparks.shift.observe.RecordingEventSink;
import org.allsparks.shift.observe.ShiftEventType;
import org.junit.jupiter.api.Test;

class FallbackTest {
    @Test
    void invalidPreferredProfileFallsBackAndIsObservable() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        RecordingEventSink sink = new RecordingEventSink();
        Shift shift = ShiftFixtures.base(driver, codriver)
                .eventSink(sink)
                .loadProfile("{ \"schemaVersion\": 1, \"controllers\": { \"driver\": { \"slot\": 1 } },"
                        + "\"bindings\": [ { \"source\": \"Y\", \"intent\": \"not.a.real.intent\" } ] }")
                .fallbackToEmbedded()
                .build();

        assertTrue(shift.usedFallback());
        assertEquals("shift-safe-idle", shift.profile().id());
        assertTrue(sink.containsType(ShiftEventType.PROFILE_REJECTED));
        assertTrue(sink.containsType(ShiftEventType.PROFILE_LOADED));
        assertEquals("true", sink.last(ShiftEventType.PROFILE_LOADED).field("fallback"));
    }

    @Test
    void missingFallbackRethrows() {
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        assertThrows(ConfigException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                ShiftFixtures.base(driver, codriver)
                        .loadProfile("{ \"schemaVersion\": 1, \"controllers\": { \"driver\": { \"slot\": 1 } },"
                                + "\"bindings\": [ { \"source\": \"Y\", \"intent\": \"nope\" } ] }")
                        .build();
            }
        });
    }
}
