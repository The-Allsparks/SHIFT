package org.allsparks.shift.feedback;

import org.allsparks.shift.input.ControllerRole;

/**
 * Physical rumble command produced from a semantic feedback mapping.
 */
public final class RumbleCommand {
    private final ControllerRole role;
    private final RumblePattern pattern;
    private final double large;
    private final double small;
    private final int durationMs;

    public RumbleCommand(ControllerRole role, RumblePattern pattern, double large, double small, int durationMs) {
        this.role = role;
        this.pattern = pattern;
        this.large = large;
        this.small = small;
        this.durationMs = durationMs;
    }

    public static RumbleCommand of(ControllerRole role, RumblePattern pattern) {
        switch (pattern) {
            case LONG:
                return new RumbleCommand(role, pattern, 1.0, 1.0, 400);
            case DOUBLE:
                return new RumbleCommand(role, pattern, 1.0, 0.0, 120);
            case PULSE:
                return new RumbleCommand(role, pattern, 0.4, 0.4, 80);
            case STOP:
                return new RumbleCommand(role, pattern, 0.0, 0.0, 0);
            case SHORT:
            default:
                return new RumbleCommand(role, pattern, 1.0, 0.0, 150);
        }
    }

    public ControllerRole role() {
        return role;
    }

    public RumblePattern pattern() {
        return pattern;
    }

    public double large() {
        return large;
    }

    public double small() {
        return small;
    }

    public int durationMs() {
        return durationMs;
    }
}
