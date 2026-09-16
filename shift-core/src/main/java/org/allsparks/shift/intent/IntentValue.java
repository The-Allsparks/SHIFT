package org.allsparks.shift.intent;

/**
 * Typed payload for an {@link Intent}. Java 8 has no sealed types, so this is a
 * tagged union with factory methods. There is no unbounded {@code Object} field.
 */
public final class IntentValue {
    public static final IntentValue EVENT = new IntentValue(IntentType.EVENT, false, 0.0, 0.0, 0.0);
    public static final IntentValue TRUE = bool(true);
    public static final IntentValue FALSE = bool(false);

    private final IntentType type;
    private final boolean booleanValue;
    private final double analogValue;
    private final double x;
    private final double y;

    private IntentValue(IntentType type, boolean booleanValue, double analogValue, double x, double y) {
        this.type = type;
        this.booleanValue = booleanValue;
        this.analogValue = analogValue;
        this.x = x;
        this.y = y;
    }

    public static IntentValue event() {
        return EVENT;
    }

    public static IntentValue bool(boolean value) {
        return new IntentValue(IntentType.BOOLEAN, value, value ? 1.0 : 0.0, 0.0, 0.0);
    }

    public static IntentValue analog(double value) {
        return new IntentValue(IntentType.ANALOG, false, value, value, 0.0);
    }

    public static IntentValue vector2(double x, double y) {
        return new IntentValue(IntentType.VECTOR2, false, 0.0, x, y);
    }

    public IntentType type() {
        return type;
    }

    public boolean asBoolean() {
        require(IntentType.BOOLEAN);
        return booleanValue;
    }

    public double asAnalog() {
        require(IntentType.ANALOG);
        return analogValue;
    }

    public double x() {
        require(IntentType.VECTOR2);
        return x;
    }

    public double y() {
        require(IntentType.VECTOR2);
        return y;
    }

    public double analogOrZero() {
        if (type == IntentType.ANALOG) {
            return analogValue;
        }
        if (type == IntentType.BOOLEAN) {
            return booleanValue ? 1.0 : 0.0;
        }
        if (type == IntentType.VECTOR2) {
            return x;
        }
        return 0.0;
    }

    private void require(IntentType expected) {
        if (type != expected) {
            throw new IllegalStateException("IntentValue is " + type + ", not " + expected);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IntentValue)) {
            return false;
        }
        IntentValue that = (IntentValue) other;
        return type == that.type
                && booleanValue == that.booleanValue
                && Double.doubleToLongBits(analogValue) == Double.doubleToLongBits(that.analogValue)
                && Double.doubleToLongBits(x) == Double.doubleToLongBits(that.x)
                && Double.doubleToLongBits(y) == Double.doubleToLongBits(that.y);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (booleanValue ? 1 : 0);
        long analogBits = Double.doubleToLongBits(analogValue);
        result = 31 * result + (int) (analogBits ^ (analogBits >>> 32));
        long xBits = Double.doubleToLongBits(x);
        result = 31 * result + (int) (xBits ^ (xBits >>> 32));
        long yBits = Double.doubleToLongBits(y);
        result = 31 * result + (int) (yBits ^ (yBits >>> 32));
        return result;
    }

    @Override
    public String toString() {
        switch (type) {
            case EVENT:
                return "event";
            case BOOLEAN:
                return booleanValue ? "true" : "false";
            case ANALOG:
                return Double.toString(analogValue);
            case VECTOR2:
                return "(" + x + "," + y + ")";
            default:
                return type.name();
        }
    }
}
