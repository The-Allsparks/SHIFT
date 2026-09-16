package org.allsparks.shift.intent;

import java.util.Locale;

/**
 * Optional logical resource an intent is associated with, such as
 * {@code ELEVATOR} or {@code DRIVE_TRANSLATION}.
 *
 * <p>This is metadata for downstream ownership reasoning. SHIFT is not a command
 * scheduler and does not enforce exclusive resource ownership.
 */
public final class LogicalResource {
    private final String id;

    private LogicalResource(String id) {
        this.id = id;
    }

    public static LogicalResource of(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return new LogicalResource(id.trim().toUpperCase(Locale.ROOT));
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LogicalResource)) {
            return false;
        }
        return id.equals(((LogicalResource) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
