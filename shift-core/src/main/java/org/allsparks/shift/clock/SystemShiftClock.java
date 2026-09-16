package org.allsparks.shift.clock;

/**
 * Default clock using {@link System#currentTimeMillis()}.
 */
public final class SystemShiftClock implements ShiftClock {
    public static final SystemShiftClock INSTANCE = new SystemShiftClock();

    private SystemShiftClock() {}

    @Override
    public long nowMillis() {
        return System.currentTimeMillis();
    }
}
