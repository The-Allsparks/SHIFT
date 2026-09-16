package org.allsparks.shift.observe;

/**
 * Drops events below a configured level. Analog {@code INPUT} traces should
 * normally be filtered on the robot.
 */
public final class FilteringEventSink implements ShiftEventSink {
    private final ShiftEventSink delegate;
    private final ShiftEventLevel minimum;

    public FilteringEventSink(ShiftEventSink delegate, ShiftEventLevel minimum) {
        this.delegate = delegate == null ? ShiftEventSink.NOOP : delegate;
        this.minimum = minimum == null ? ShiftEventLevel.INFO : minimum;
    }

    @Override
    public void onEvent(ShiftEvent event) {
        if (event.level().atLeast(minimum)) {
            delegate.onEvent(event);
        }
    }
}
