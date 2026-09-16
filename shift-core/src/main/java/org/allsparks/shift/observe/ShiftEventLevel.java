package org.allsparks.shift.observe;

/**
 * Filtering level for {@link ShiftEvent}. Analog {@code INPUT} traces are
 * {@link #TRACE} so they can be dropped on the robot loop.
 */
public enum ShiftEventLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    public boolean atLeast(ShiftEventLevel minimum) {
        return ordinal() >= minimum.ordinal();
    }
}
