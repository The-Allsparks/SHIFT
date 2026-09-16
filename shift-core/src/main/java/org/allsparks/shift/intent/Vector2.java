package org.allsparks.shift.intent;

/**
 * Immutable 2D value used by {@link IntentType#VECTOR2} consumers.
 */
public final class Vector2 {
    public static final Vector2 ZERO = new Vector2(0.0, 0.0);

    private final double x;
    private final double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double magnitude() {
        return Math.hypot(x, y);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Vector2)) {
            return false;
        }
        Vector2 that = (Vector2) other;
        return Double.doubleToLongBits(x) == Double.doubleToLongBits(that.x)
                && Double.doubleToLongBits(y) == Double.doubleToLongBits(that.y);
    }

    @Override
    public int hashCode() {
        long xBits = Double.doubleToLongBits(x);
        long yBits = Double.doubleToLongBits(y);
        return (int) (xBits ^ (xBits >>> 32)) * 31 + (int) (yBits ^ (yBits >>> 32));
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
