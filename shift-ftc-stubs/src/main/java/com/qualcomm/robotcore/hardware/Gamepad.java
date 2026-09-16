package com.qualcomm.robotcore.hardware;

/**
 * Compile-only stand-in for {@code com.qualcomm.robotcore.hardware.Gamepad}.
 *
 * <p>Robot projects must compile against official RobotCore. Field names and
 * methods match FTC SDK 11.2.1 Gamepad as used by SHIFT adapters.
 */
public class Gamepad {
    public static final int ID_UNASSOCIATED = -1;
    public static final int RUMBLE_DURATION_CONTINUOUS = -1;
    public static final int LED_DURATION_CONTINUOUS = -1;

    public enum Type {
        UNKNOWN,
        LOGITECH_F310,
        XBOX_360,
        SONY_PS4,
        SONY_PS4_SUPPORTED_BY_KERNEL
    }

    public volatile Type type = Type.UNKNOWN;

    public volatile float left_stick_x;
    public volatile float left_stick_y;
    public volatile float right_stick_x;
    public volatile float right_stick_y;
    public volatile float left_trigger;
    public volatile float right_trigger;

    public volatile boolean dpad_up;
    public volatile boolean dpad_down;
    public volatile boolean dpad_left;
    public volatile boolean dpad_right;
    public volatile boolean a;
    public volatile boolean b;
    public volatile boolean x;
    public volatile boolean y;
    public volatile boolean guide;
    public volatile boolean start;
    public volatile boolean back;
    public volatile boolean left_bumper;
    public volatile boolean right_bumper;
    public volatile boolean left_stick_button;
    public volatile boolean right_stick_button;

    public volatile boolean circle;
    public volatile boolean cross;
    public volatile boolean triangle;
    public volatile boolean square;
    public volatile boolean share;
    public volatile boolean options;
    public volatile boolean ps;
    public volatile boolean touchpad;
    public volatile boolean touchpad_finger_1;
    public volatile boolean touchpad_finger_2;
    public volatile float touchpad_finger_1_x;
    public volatile float touchpad_finger_1_y;
    public volatile float touchpad_finger_2_x;
    public volatile float touchpad_finger_2_y;

    public volatile boolean left_trigger_pressed;
    public volatile boolean right_trigger_pressed;

    public volatile int id = ID_UNASSOCIATED;
    public volatile long timestamp;

    public Type type() {
        return type;
    }

    public int getGamepadId() {
        return id;
    }

    public void rumble(int durationMs) {}

    public void rumble(double rumble1, double rumble2, int durationMs) {}

    public void rumbleBlips(int count) {}

    public void stopRumble() {}

    public void setLedColor(double r, double g, double b, int durationMs) {}

    public static final class LedEffect {
        public static final class Builder {
            public Builder addStep(double r, double g, double b, int durationMs) {
                return this;
            }

            public Builder setRepeating(boolean repeating) {
                return this;
            }

            public LedEffect build() {
                return new LedEffect();
            }
        }
    }

    public static final class RumbleEffect {
        public static final class Builder {
            public Builder addStep(double rumble1, double rumble2, int durationMs) {
                return this;
            }

            public RumbleEffect build() {
                return new RumbleEffect();
            }
        }
    }

    public void runLedEffect(LedEffect effect) {}

    public void runRumbleEffect(RumbleEffect effect) {}
}
