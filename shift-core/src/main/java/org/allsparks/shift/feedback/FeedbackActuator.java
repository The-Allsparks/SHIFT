package org.allsparks.shift.feedback;

/**
 * Hardware sink for rumble and LED commands. FTC adapters implement this;
 * tests use {@link RecordingFeedbackActuator}. SHIFT core never imports Gamepad.
 */
public interface FeedbackActuator {
    void rumble(RumbleCommand command);

    void led(LedCommand command);

    FeedbackActuator NOOP = new FeedbackActuator() {
        @Override
        public void rumble(RumbleCommand command) {
            // intentionally empty
        }

        @Override
        public void led(LedCommand command) {
            // intentionally empty
        }
    };
}
