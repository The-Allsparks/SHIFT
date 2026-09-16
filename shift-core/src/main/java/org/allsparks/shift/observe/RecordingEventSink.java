package org.allsparks.shift.observe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test/debug sink that retains events in memory. Not for competition loops.
 */
public final class RecordingEventSink implements ShiftEventSink {
    private final List<ShiftEvent> events = new ArrayList<ShiftEvent>();

    @Override
    public void onEvent(ShiftEvent event) {
        events.add(event);
    }

    public List<ShiftEvent> events() {
        return Collections.unmodifiableList(events);
    }

    public void clear() {
        events.clear();
    }

    public boolean containsType(ShiftEventType type) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).type() == type) {
                return true;
            }
        }
        return false;
    }

    public ShiftEvent last(ShiftEventType type) {
        for (int i = events.size() - 1; i >= 0; i--) {
            if (events.get(i).type() == type) {
                return events.get(i);
            }
        }
        return null;
    }
}
