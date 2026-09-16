package org.allsparks.shift.input;

import java.util.Locale;

/**
 * Application-defined controller role such as {@code driver} or {@code codriver}.
 *
 * <p>Roles are not a closed enum. Applications may introduce additional names.
 * Matching is case-insensitive.
 */
public final class ControllerRole {
    public static final ControllerRole DRIVER = ControllerRole.of("driver");
    public static final ControllerRole CODRIVER = ControllerRole.of("codriver");
    public static final ControllerRole TEST = ControllerRole.of("test");

    private final String id;

    private ControllerRole(String id) {
        this.id = id;
    }

    public static ControllerRole of(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Controller role must not be null");
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Controller role must not be empty");
        }
        return new ControllerRole(trimmed.toLowerCase(Locale.ROOT));
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ControllerRole)) {
            return false;
        }
        return id.equals(((ControllerRole) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
