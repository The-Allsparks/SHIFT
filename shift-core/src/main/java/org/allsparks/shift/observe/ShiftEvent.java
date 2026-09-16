package org.allsparks.shift.observe;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable structured event. Field keys are stable so an external logger can
 * format lines such as {@code INTENT intent=elevator.score.high source=codriver}.
 */
public final class ShiftEvent {
    private final ShiftEventType type;
    private final ShiftEventLevel level;
    private final long timestampMillis;
    private final long sequence;
    private final String message;
    private final Map<String, String> fields;

    public ShiftEvent(
            ShiftEventType type,
            ShiftEventLevel level,
            long timestampMillis,
            long sequence,
            String message,
            Map<String, String> fields) {
        this.type = type;
        this.level = level;
        this.timestampMillis = timestampMillis;
        this.sequence = sequence;
        this.message = message == null ? "" : message;
        this.fields = fields == null
                ? Collections.<String, String>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<String, String>(fields));
    }

    public ShiftEventType type() {
        return type;
    }

    public ShiftEventLevel level() {
        return level;
    }

    public long timestampMillis() {
        return timestampMillis;
    }

    public long sequence() {
        return sequence;
    }

    public String message() {
        return message;
    }

    public Map<String, String> fields() {
        return fields;
    }

    public String field(String key) {
        return fields.get(key);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type.name());
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            builder.append(' ').append(entry.getKey()).append('=').append(entry.getValue());
        }
        if (!message.isEmpty()) {
            builder.append(' ').append(message);
        }
        return builder.toString();
    }

    public static Builder builder(ShiftEventType type, ShiftEventLevel level) {
        return new Builder(type, level);
    }

    public static final class Builder {
        private final ShiftEventType type;
        private final ShiftEventLevel level;
        private long timestampMillis;
        private long sequence;
        private String message = "";
        private final Map<String, String> fields = new LinkedHashMap<String, String>();

        Builder(ShiftEventType type, ShiftEventLevel level) {
            this.type = type;
            this.level = level;
        }

        public Builder timestampMillis(long timestampMillis) {
            this.timestampMillis = timestampMillis;
            return this;
        }

        public Builder sequence(long sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder field(String key, String value) {
            if (key != null && value != null) {
                fields.put(key, value);
            }
            return this;
        }

        public Builder field(String key, Object value) {
            if (key != null && value != null) {
                fields.put(key, String.valueOf(value));
            }
            return this;
        }

        public ShiftEvent build() {
            return new ShiftEvent(type, level, timestampMillis, sequence, message, fields);
        }
    }
}
