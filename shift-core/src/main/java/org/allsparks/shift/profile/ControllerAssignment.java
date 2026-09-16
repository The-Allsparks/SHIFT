package org.allsparks.shift.profile;

import org.allsparks.shift.input.ControllerModel;
import org.allsparks.shift.input.ControllerRole;

/**
 * Maps a semantic role to a physical slot, default layer, and optional hardware
 * model.
 */
public final class ControllerAssignment {
    private final ControllerRole role;
    private final int slot;
    private final String defaultLayer;
    private final ControllerModel model;

    public ControllerAssignment(ControllerRole role, int slot, String defaultLayer) {
        this(role, slot, defaultLayer, null);
    }

    public ControllerAssignment(ControllerRole role, int slot, String defaultLayer, ControllerModel model) {
        this.role = role;
        this.slot = slot;
        this.defaultLayer = defaultLayer == null ? "" : defaultLayer;
        this.model = model;
    }

    public ControllerRole role() {
        return role;
    }

    public int slot() {
        return slot;
    }

    public String defaultLayer() {
        return defaultLayer;
    }

    /**
     * Declared hardware model, or {@code null} when the profile omitted
     * {@code model}.
     */
    public ControllerModel model() {
        return model;
    }
}
