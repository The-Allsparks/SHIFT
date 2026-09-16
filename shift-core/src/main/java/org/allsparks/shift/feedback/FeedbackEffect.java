package org.allsparks.shift.feedback;

import org.allsparks.shift.input.ControllerRole;

/**
 * Combined physical effects for one semantic feedback event.
 */
public final class FeedbackEffect {
    private final FeedbackId id;
    private final ControllerRole role;
    private final RumbleCommand rumble;
    private final LedCommand led;

    public FeedbackEffect(FeedbackId id, ControllerRole role, RumbleCommand rumble, LedCommand led) {
        this.id = id;
        this.role = role;
        this.rumble = rumble;
        this.led = led;
    }

    public FeedbackId id() {
        return id;
    }

    public ControllerRole role() {
        return role;
    }

    public RumbleCommand rumble() {
        return rumble;
    }

    public LedCommand led() {
        return led;
    }
}
