package org.allsparks.shift.observe;

/**
 * Observer of SHIFT lifecycle events. TRACE, ECHO, or application telemetry
 * implement this. Implementations must be non-blocking; SHIFT calls the sink
 * on the robot loop thread.
 */
public interface ShiftEventSink {
    void onEvent(ShiftEvent event);

    ShiftEventSink NOOP = new ShiftEventSink() {
        @Override
        public void onEvent(ShiftEvent event) {
            // intentionally empty
        }
    };
}
