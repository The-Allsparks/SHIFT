package org.allsparks.shift.feedback;

import org.allsparks.shift.input.ControllerRole;

/**
 * Physical LED command. FTC PS4/Etpark, DualSense, and DualSense-style clones
 * such as IWGAME wired PS-5/PC expose an RGB light; Xbox 360 and Logitech F310
 * do not. Adapters no-op when the hardware cannot show color.
 */
public final class LedCommand {
    private final ControllerRole role;
    private final double red;
    private final double green;
    private final double blue;
    private final int durationMs;

    public LedCommand(ControllerRole role, double red, double green, double blue, int durationMs) {
        this.role = role;
        this.red = clamp01(red);
        this.green = clamp01(green);
        this.blue = clamp01(blue);
        this.durationMs = durationMs;
    }

    private static double clamp01(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }

    public ControllerRole role() {
        return role;
    }

    public double red() {
        return red;
    }

    public double green() {
        return green;
    }

    public double blue() {
        return blue;
    }

    public int durationMs() {
        return durationMs;
    }
}
