package org.allsparks.shift.intent;

import java.util.Locale;

/**
 * Stable semantic identifier such as {@code elevator.score.high}.
 *
 * <p>JSON profiles use the string form. Application code may hold {@code IntentId}
 * constants for compile-time safety.
 */
public final class IntentId {
    private final String value;

    private IntentId(String value) {
        this.value = value;
    }

    public static IntentId of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Intent id must not be null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Intent id must not be empty");
        }
        if (!isLegal(trimmed)) {
            throw new IllegalArgumentException("Intent id '" + trimmed + "' must match [a-zA-Z][a-zA-Z0-9_.-]*");
        }
        return new IntentId(trimmed);
    }

    static boolean isLegal(String value) {
        if (value.isEmpty()) {
            return false;
        }
        char first = value.charAt(0);
        if (!Character.isLetter(first)) {
            return false;
        }
        for (int i = 1; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (!(Character.isLetterOrDigit(ch) || ch == '_' || ch == '.' || ch == '-')) {
                return false;
            }
        }
        return true;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IntentId)) {
            return false;
        }
        return value.equals(((IntentId) other).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    public String lower() {
        return value.toLowerCase(Locale.ROOT);
    }
}
