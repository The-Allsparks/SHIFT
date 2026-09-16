package org.allsparks.shift.clock;

/**
 * Millisecond clock used for event timestamps when a snapshot does not already
 * carry a timestamp. Injected so tests and replay stay deterministic.
 *
 * <p>SHIFT does not start threads and does not call this from a background
 * worker. {@link #nowMillis()} must be cheap and non-blocking.
 */
public interface ShiftClock {
    long nowMillis();
}
