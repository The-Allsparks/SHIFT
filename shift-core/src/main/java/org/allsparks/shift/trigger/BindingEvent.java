package org.allsparks.shift.trigger;

/**
 * Digital event semantics inspired by WPILib {@code Trigger}:
 *
 * <ul>
 *   <li>{@link #ON_PRESS} — rising edge ({@code onTrue})
 *   <li>{@link #ON_RELEASE} — falling edge ({@code onFalse})
 *   <li>{@link #WHILE_HELD} — true while the condition holds ({@code whileTrue} state)
 *   <li>{@link #ANALOG} — continuous numeric output, not an edge
 * </ul>
 *
 * <p>TOGGLE, LONG_PRESS, and DOUBLE_TAP are reserved for a later release and
 * rejected by the V1 schema.
 */
public enum BindingEvent {
    ON_PRESS,
    ON_RELEASE,
    WHILE_HELD,
    ANALOG;

    public static BindingEvent parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("event is required");
        }
        String key = raw.trim().toUpperCase();
        if ("ONPRESS".equals(key.replace("_", "")) || "RISING".equals(key) || "PRESSED".equals(key)) {
            return ON_PRESS;
        }
        if ("ONRELEASE".equals(key.replace("_", "")) || "FALLING".equals(key) || "RELEASED".equals(key)) {
            return ON_RELEASE;
        }
        if ("WHILEHELD".equals(key.replace("_", "")) || "HELD".equals(key) || "WHILETRUE".equals(key)) {
            return WHILE_HELD;
        }
        if ("ANALOG".equals(key) || "CONTINUOUS".equals(key) || "AXIS".equals(key)) {
            return ANALOG;
        }
        if ("TOGGLE".equals(key) || "LONG_PRESS".equals(key) || "DOUBLE_TAP".equals(key)) {
            throw new IllegalArgumentException("event '" + raw
                    + "' is reserved for a later SHIFT release and is not available in schemaVersion 1");
        }
        throw new IllegalArgumentException("Unknown event '" + raw + "'");
    }

    public boolean discrete() {
        return this == ON_PRESS || this == ON_RELEASE;
    }

    public boolean continuous() {
        return this == WHILE_HELD || this == ANALOG;
    }
}
