package org.allsparks.shift.feedback;

/**
 * Built-in rumble patterns. Profiles may also specify raw duration/power.
 */
public enum RumblePattern {
    SHORT,
    LONG,
    DOUBLE,
    PULSE,
    STOP;

    public static RumblePattern parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return SHORT;
        }
        String key = raw.trim().toUpperCase();
        for (RumblePattern pattern : values()) {
            if (pattern.name().equals(key)) {
                return pattern;
            }
        }
        throw new IllegalArgumentException("Unknown rumble pattern '" + raw + "'");
    }
}
