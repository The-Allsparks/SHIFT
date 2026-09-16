package org.allsparks.shift.intent;

import org.allsparks.shift.input.ControllerRole;

/**
 * Immutable semantic operator intent produced by one SHIFT update.
 *
 * <p>SHIFT answers "what does the operator want?" It does not describe motors,
 * setpoints, or how the robot should achieve the intent.
 */
public final class Intent {
    private final IntentId id;
    private final IntentType type;
    private final IntentValue value;
    private final ControllerRole source;
    private final String layer;
    private final IntentPriority priority;
    private final LogicalResource resource;
    private final String bindingId;
    private final long timestampMillis;
    private final long sequence;

    Intent(
            IntentId id,
            IntentType type,
            IntentValue value,
            ControllerRole source,
            String layer,
            IntentPriority priority,
            LogicalResource resource,
            String bindingId,
            long timestampMillis,
            long sequence) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.source = source;
        this.layer = layer;
        this.priority = priority;
        this.resource = resource;
        this.bindingId = bindingId;
        this.timestampMillis = timestampMillis;
        this.sequence = sequence;
    }

    public static Builder builder(IntentId id, IntentType type) {
        return new Builder(id, type);
    }

    public IntentId id() {
        return id;
    }

    public IntentType type() {
        return type;
    }

    public IntentValue value() {
        return value;
    }

    public ControllerRole source() {
        return source;
    }

    public String layer() {
        return layer;
    }

    public IntentPriority priority() {
        return priority;
    }

    public LogicalResource resource() {
        return resource;
    }

    public String bindingId() {
        return bindingId;
    }

    public long timestampMillis() {
        return timestampMillis;
    }

    public long sequence() {
        return sequence;
    }

    @Override
    public String toString() {
        return id + "=" + value + " src=" + source + " layer=" + layer;
    }

    public static final class Builder {
        private final IntentId id;
        private final IntentType type;
        private IntentValue value = IntentValue.EVENT;
        private ControllerRole source = ControllerRole.DRIVER;
        private String layer = "";
        private IntentPriority priority = IntentPriority.DEFAULT;
        private LogicalResource resource;
        private String bindingId = "";
        private long timestampMillis;
        private long sequence;

        Builder(IntentId id, IntentType type) {
            this.id = id;
            this.type = type;
            if (type == IntentType.EVENT) {
                this.value = IntentValue.EVENT;
            } else if (type == IntentType.BOOLEAN) {
                this.value = IntentValue.TRUE;
            } else if (type == IntentType.ANALOG) {
                this.value = IntentValue.analog(0.0);
            } else {
                this.value = IntentValue.vector2(0.0, 0.0);
            }
        }

        public Builder value(IntentValue value) {
            this.value = value;
            return this;
        }

        public Builder source(ControllerRole source) {
            this.source = source;
            return this;
        }

        public Builder layer(String layer) {
            this.layer = layer == null ? "" : layer;
            return this;
        }

        public Builder priority(IntentPriority priority) {
            this.priority = priority == null ? IntentPriority.DEFAULT : priority;
            return this;
        }

        public Builder resource(LogicalResource resource) {
            this.resource = resource;
            return this;
        }

        public Builder bindingId(String bindingId) {
            this.bindingId = bindingId == null ? "" : bindingId;
            return this;
        }

        public Builder timestampMillis(long timestampMillis) {
            this.timestampMillis = timestampMillis;
            return this;
        }

        public Builder sequence(long sequence) {
            this.sequence = sequence;
            return this;
        }

        public Intent build() {
            if (value.type() != type) {
                throw new IllegalArgumentException(
                        "Intent " + id + " has type " + type + " but value type " + value.type());
            }
            return new Intent(id, type, value, source, layer, priority, resource, bindingId, timestampMillis, sequence);
        }
    }
}
