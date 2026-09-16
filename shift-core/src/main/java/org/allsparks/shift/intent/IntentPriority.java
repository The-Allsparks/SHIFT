package org.allsparks.shift.intent;

import java.util.Locale;

/**
 * Semantic priority attached to an emitted intent so a downstream coordinator
 * (for example HELM) can arbitrate. SHIFT itself does not schedule robot
 * subsystems.
 *
 * <p>The built-in names are conventional, not a closed set: {@link #of(String)}
 * accepts application-defined priorities.
 */
public final class IntentPriority {
    public static final IntentPriority SAFETY = IntentPriority.of("SAFETY", 100);
    public static final IntentPriority MANUAL_OVERRIDE = IntentPriority.of("MANUAL_OVERRIDE", 80);
    public static final IntentPriority AUTOMATION = IntentPriority.of("AUTOMATION", 60);
    public static final IntentPriority ASSIST = IntentPriority.of("ASSIST", 40);
    public static final IntentPriority DEFAULT = IntentPriority.of("DEFAULT", 20);

    private final String name;
    private final int rank;

    private IntentPriority(String name, int rank) {
        this.name = name;
        this.rank = rank;
    }

    public static IntentPriority of(String name) {
        return of(name, 0);
    }

    public static IntentPriority of(String name, int rank) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Priority name must not be empty");
        }
        return new IntentPriority(name.trim().toUpperCase(Locale.ROOT), rank);
    }

    public static IntentPriority parse(String name) {
        if (name == null || name.trim().isEmpty()) {
            return DEFAULT;
        }
        String key = name.trim().toUpperCase(Locale.ROOT);
        if (key.equals("SAFETY")) {
            return SAFETY;
        }
        if (key.equals("MANUAL_OVERRIDE") || key.equals("MANUAL")) {
            return MANUAL_OVERRIDE;
        }
        if (key.equals("AUTOMATION")) {
            return AUTOMATION;
        }
        if (key.equals("ASSIST")) {
            return ASSIST;
        }
        if (key.equals("DEFAULT")) {
            return DEFAULT;
        }
        return of(key, 0);
    }

    public String name() {
        return name;
    }

    public int rank() {
        return rank;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IntentPriority)) {
            return false;
        }
        return name.equals(((IntentPriority) other).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
