package org.allsparks.shift.feedback;

import java.util.Locale;

/**
 * Semantic operator-feedback identifier such as {@code TARGET_ACQUIRED}.
 * Applications register these the same way they register intents.
 */
public final class FeedbackId {
    private final String value;

    private FeedbackId(String value) {
        this.value = value;
    }

    public static FeedbackId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Feedback id must not be empty");
        }
        return new FeedbackId(value.trim().toUpperCase(Locale.ROOT).replace(' ', '_'));
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FeedbackId)) {
            return false;
        }
        return value.equals(((FeedbackId) other).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
