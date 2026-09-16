package org.allsparks.shift.transform;

/**
 * Analog transform applied to a single axis or trigger.
 *
 * <p>Application order, which is stable and unit-tested:
 *
 * <ol>
 *   <li>invert
 *   <li>deadband with range rescale
 *   <li>exponent (sign-preserving response curve)
 *   <li>scale
 *   <li>min/max clamp
 * </ol>
 *
 * <p>Extension points for slew-rate limiting, calibration, or asymmetric curves
 * should wrap this class rather than mutating it. Those transforms are
 * intentionally not in V1.
 */
public final class AnalogTransform {
    public static final AnalogTransform IDENTITY = new AnalogTransform(0.0, false, 1.0, 1.0, -1.0, 1.0, 0.5);

    private final double deadband;
    private final boolean invert;
    private final double scale;
    private final double exponent;
    private final double min;
    private final double max;
    private final double threshold;

    AnalogTransform(
            double deadband, boolean invert, double scale, double exponent, double min, double max, double threshold) {
        this.deadband = deadband;
        this.invert = invert;
        this.scale = scale;
        this.exponent = exponent;
        this.min = min;
        this.max = max;
        this.threshold = threshold;
    }

    public static Builder builder() {
        return new Builder();
    }

    public double deadband() {
        return deadband;
    }

    public boolean invert() {
        return invert;
    }

    public double scale() {
        return scale;
    }

    public double exponent() {
        return exponent;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public double threshold() {
        return threshold;
    }

    /**
     * Applies the analog curve. The result is clamped to {@code [min, max]}.
     */
    public double apply(double raw) {
        double value = invert ? -raw : raw;
        double magnitude = Math.abs(value);
        if (magnitude <= deadband || deadband >= 1.0) {
            return clamp(0.0);
        }
        double normalized = (magnitude - deadband) / (1.0 - deadband);
        if (normalized < 0.0) {
            normalized = 0.0;
        } else if (normalized > 1.0) {
            normalized = 1.0;
        }
        if (exponent != 1.0) {
            normalized = Math.pow(normalized, exponent);
        }
        double signed = Math.copySign(normalized, value) * scale;
        return clamp(signed);
    }

    /**
     * True when the transformed analog magnitude meets the digital threshold.
     * Used when an analog control appears in a composite trigger.
     */
    public boolean asDigital(double raw) {
        return Math.abs(apply(raw)) >= threshold;
    }

    public boolean asDigitalRaw(double raw) {
        double value = invert ? -raw : raw;
        return Math.abs(value) >= threshold;
    }

    private double clamp(double value) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static final class Builder {
        private double deadband;
        private boolean invert;
        private double scale = 1.0;
        private double exponent = 1.0;
        private double min = -1.0;
        private double max = 1.0;
        private double threshold = 0.5;

        public Builder deadband(double deadband) {
            this.deadband = deadband;
            return this;
        }

        public Builder invert(boolean invert) {
            this.invert = invert;
            return this;
        }

        public Builder scale(double scale) {
            this.scale = scale;
            return this;
        }

        public Builder exponent(double exponent) {
            this.exponent = exponent;
            return this;
        }

        public Builder min(double min) {
            this.min = min;
            return this;
        }

        public Builder max(double max) {
            this.max = max;
            return this;
        }

        public Builder threshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        public AnalogTransform build() {
            if (deadband < 0.0 || deadband >= 1.0) {
                throw new IllegalArgumentException("deadband must be in [0, 1): " + deadband);
            }
            if (exponent <= 0.0) {
                throw new IllegalArgumentException("exponent must be > 0: " + exponent);
            }
            if (min > max) {
                throw new IllegalArgumentException("min " + min + " > max " + max);
            }
            if (threshold < 0.0) {
                throw new IllegalArgumentException("threshold must be >= 0: " + threshold);
            }
            return new AnalogTransform(deadband, invert, scale, exponent, min, max, threshold);
        }
    }
}
