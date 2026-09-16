package org.allsparks.shift.clock;

/**
 * Test/replay clock whose time is advanced explicitly.
 */
public final class ManualClock implements ShiftClock {
    private long now;

    public ManualClock() {
        this(0L);
    }

    public ManualClock(long initialMillis) {
        this.now = initialMillis;
    }

    public void set(long millis) {
        this.now = millis;
    }

    public long advance(long deltaMillis) {
        this.now += deltaMillis;
        return now;
    }

    @Override
    public long nowMillis() {
        return now;
    }
}
