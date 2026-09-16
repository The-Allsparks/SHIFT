package org.allsparks.shift.feedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test actuator that records rumble and LED commands.
 */
public final class RecordingFeedbackActuator implements FeedbackActuator {
    private final List<RumbleCommand> rumbles = new ArrayList<RumbleCommand>();
    private final List<LedCommand> leds = new ArrayList<LedCommand>();

    @Override
    public void rumble(RumbleCommand command) {
        rumbles.add(command);
    }

    @Override
    public void led(LedCommand command) {
        leds.add(command);
    }

    public List<RumbleCommand> rumbles() {
        return Collections.unmodifiableList(rumbles);
    }

    public List<LedCommand> leds() {
        return Collections.unmodifiableList(leds);
    }

    public void clear() {
        rumbles.clear();
        leds.clear();
    }
}
