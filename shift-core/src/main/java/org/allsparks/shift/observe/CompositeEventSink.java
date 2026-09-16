package org.allsparks.shift.observe;

/**
 * Fan-out sink so TRACE, ECHO, and telemetry can listen without SHIFT depending
 * on those libraries. Listeners run in registration order on the robot loop
 * thread. Implementations must not block.
 */
public final class CompositeEventSink implements ShiftEventSink {
    private final ShiftEventSink[] sinks;

    public CompositeEventSink(ShiftEventSink[] sinks) {
        this.sinks = copy(sinks);
    }

    public static CompositeEventSink of(ShiftEventSink first, ShiftEventSink second) {
        return new CompositeEventSink(new ShiftEventSink[] {first, second});
    }

    public static CompositeEventSink of(ShiftEventSink first, ShiftEventSink second, ShiftEventSink third) {
        return new CompositeEventSink(new ShiftEventSink[] {first, second, third});
    }

    @Override
    public void onEvent(ShiftEvent event) {
        for (int i = 0; i < sinks.length; i++) {
            sinks[i].onEvent(event);
        }
    }

    private static ShiftEventSink[] copy(ShiftEventSink[] sinks) {
        if (sinks == null || sinks.length == 0) {
            return new ShiftEventSink[] {ShiftEventSink.NOOP};
        }
        ShiftEventSink[] copy = new ShiftEventSink[sinks.length];
        int count = 0;
        for (int i = 0; i < sinks.length; i++) {
            if (sinks[i] != null) {
                copy[count] = sinks[i];
                count++;
            }
        }
        if (count == 0) {
            return new ShiftEventSink[] {ShiftEventSink.NOOP};
        }
        if (count == copy.length) {
            return copy;
        }
        ShiftEventSink[] trimmed = new ShiftEventSink[count];
        System.arraycopy(copy, 0, trimmed, 0, count);
        return trimmed;
    }
}
