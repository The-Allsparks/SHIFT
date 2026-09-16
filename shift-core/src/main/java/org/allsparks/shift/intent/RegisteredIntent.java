package org.allsparks.shift.intent;

/**
 * One registered semantic intent and its declared type, default priority, and
 * optional logical resource.
 */
public final class RegisteredIntent {
    private final IntentId id;
    private final IntentType type;
    private final IntentPriority priority;
    private final LogicalResource resource;

    public RegisteredIntent(IntentId id, IntentType type, IntentPriority priority, LogicalResource resource) {
        this.id = id;
        this.type = type;
        this.priority = priority;
        this.resource = resource;
    }

    public IntentId id() {
        return id;
    }

    public IntentType type() {
        return type;
    }

    public IntentPriority priority() {
        return priority;
    }

    public LogicalResource resource() {
        return resource;
    }
}
