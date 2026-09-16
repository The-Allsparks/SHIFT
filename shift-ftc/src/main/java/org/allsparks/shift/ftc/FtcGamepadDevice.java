package org.allsparks.shift.ftc;

import com.qualcomm.robotcore.hardware.Gamepad;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.input.DeviceMetadata;
import org.allsparks.shift.input.InputDevice;
import org.allsparks.shift.input.InputSnapshot;

/**
 * Samples an FTC {@link Gamepad} once into an immutable {@link InputSnapshot}.
 *
 * <p>Reads public fields directly rather than calling {@code Gamepad.copy}, so a
 * SHIFT cycle never observes a torn controller state and does not depend on
 * {@code RobotCoreException}. PlayStation aliases ({@code circle}, {@code cross},
 * ...) are not stored separately: the FTC SDK already mirrors them onto
 * {@code a}/{@code b}/{@code x}/{@code y}. DualSense-style pads (official Sony
 * DualSense and IWGAME wired PS-5/PC clones) expose the same fields; the Driver
 * Station still reports {@code type()} as {@code SONY_PS4} or {@code UNKNOWN}.
 * Touchpad finger contacts and XY are sampled. Digital trigger clicks
 * ({@code left_trigger_pressed} / {@code right_trigger_pressed}) are distinct
 * from analog trigger axes. Rear paddles are not RobotCore fields: program
 * them on the controller onto existing buttons.
 */
public final class FtcGamepadDevice implements InputDevice {
    private final Gamepad gamepad;
    private final ControllerRole role;
    private long sequence;

    public FtcGamepadDevice(Gamepad gamepad) {
        this(gamepad, ControllerRole.DRIVER);
    }

    public FtcGamepadDevice(Gamepad gamepad, String role) {
        this(gamepad, ControllerRole.of(role));
    }

    public FtcGamepadDevice(Gamepad gamepad, ControllerRole role) {
        if (gamepad == null) {
            throw new IllegalArgumentException("Gamepad is required");
        }
        if (role == null) {
            throw new IllegalArgumentException("Controller role is required");
        }
        this.gamepad = gamepad;
        this.role = role;
    }

    public ControllerRole role() {
        return role;
    }

    @Override
    public InputSnapshot sample() {
        sequence++;
        Gamepad.Type type = gamepad.type();
        return InputSnapshot.builder()
                .role(role)
                .timestampMillis(gamepad.timestamp)
                .sequence(sequence)
                .metadata(new DeviceMetadata(type == null ? "UNKNOWN" : type.name(), gamepad.getGamepadId(), role.id()))
                .setAnalog(Control.LEFT_STICK_X, gamepad.left_stick_x)
                .setAnalog(Control.LEFT_STICK_Y, gamepad.left_stick_y)
                .setAnalog(Control.RIGHT_STICK_X, gamepad.right_stick_x)
                .setAnalog(Control.RIGHT_STICK_Y, gamepad.right_stick_y)
                .setAnalog(Control.LEFT_TRIGGER, gamepad.left_trigger)
                .setAnalog(Control.RIGHT_TRIGGER, gamepad.right_trigger)
                .setDigital(Control.LEFT_TRIGGER_PRESSED, gamepad.left_trigger_pressed)
                .setDigital(Control.RIGHT_TRIGGER_PRESSED, gamepad.right_trigger_pressed)
                .setDigital(Control.DPAD_UP, gamepad.dpad_up)
                .setDigital(Control.DPAD_DOWN, gamepad.dpad_down)
                .setDigital(Control.DPAD_LEFT, gamepad.dpad_left)
                .setDigital(Control.DPAD_RIGHT, gamepad.dpad_right)
                .setDigital(Control.LEFT_BUMPER, gamepad.left_bumper)
                .setDigital(Control.RIGHT_BUMPER, gamepad.right_bumper)
                .setDigital(Control.LEFT_STICK_BUTTON, gamepad.left_stick_button)
                .setDigital(Control.RIGHT_STICK_BUTTON, gamepad.right_stick_button)
                .setDigital(Control.START, gamepad.start)
                .setDigital(Control.BACK, gamepad.back)
                .setDigital(Control.GUIDE, gamepad.guide)
                .setDigital(Control.A, gamepad.a)
                .setDigital(Control.B, gamepad.b)
                .setDigital(Control.X, gamepad.x)
                .setDigital(Control.Y, gamepad.y)
                .setDigital(Control.TOUCHPAD, gamepad.touchpad)
                .setDigital(Control.TOUCHPAD_FINGER_1, gamepad.touchpad_finger_1)
                .setDigital(Control.TOUCHPAD_FINGER_2, gamepad.touchpad_finger_2)
                .setAnalog(Control.TOUCHPAD_FINGER_1_X, gamepad.touchpad_finger_1_x)
                .setAnalog(Control.TOUCHPAD_FINGER_1_Y, gamepad.touchpad_finger_1_y)
                .setAnalog(Control.TOUCHPAD_FINGER_2_X, gamepad.touchpad_finger_2_x)
                .setAnalog(Control.TOUCHPAD_FINGER_2_Y, gamepad.touchpad_finger_2_y)
                .build();
    }

    @Override
    public String describe() {
        return "ftc:" + role.id();
    }
}
