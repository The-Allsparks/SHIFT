package org.allsparks.shift.ftc;

import com.qualcomm.robotcore.hardware.Gamepad;
import java.util.LinkedHashMap;
import java.util.Map;
import org.allsparks.shift.feedback.FeedbackActuator;
import org.allsparks.shift.feedback.LedCommand;
import org.allsparks.shift.feedback.RumbleCommand;
import org.allsparks.shift.feedback.RumblePattern;
import org.allsparks.shift.input.ControllerRole;

/**
 * Applies SHIFT feedback commands to FTC gamepads using the SDK rumble and LED
 * APIs. Hardware that lacks rumble or an RGB light silently no-ops inside the
 * SDK; SHIFT does not emulate missing hardware. DualSense-style pads (including
 * IWGAME wired PS-5/PC) use the same rumble and {@code setLedColor} calls as
 * PS4 once the Driver Station maps the device as a PlayStation gamepad.
 */
public final class FtcFeedbackActuator implements FeedbackActuator {
    private final Map<ControllerRole, Gamepad> gamepads = new LinkedHashMap<ControllerRole, Gamepad>();

    public FtcFeedbackActuator add(String role, Gamepad gamepad) {
        return add(ControllerRole.of(role), gamepad);
    }

    public FtcFeedbackActuator add(ControllerRole role, Gamepad gamepad) {
        if (role == null || gamepad == null) {
            throw new IllegalArgumentException("role and gamepad are required");
        }
        gamepads.put(role, gamepad);
        return this;
    }

    @Override
    public void rumble(RumbleCommand command) {
        Gamepad gamepad = gamepads.get(command.role());
        if (gamepad == null) {
            return;
        }
        if (command.pattern() == RumblePattern.STOP) {
            gamepad.stopRumble();
            return;
        }
        if (command.pattern() == RumblePattern.DOUBLE) {
            gamepad.rumbleBlips(2);
            return;
        }
        gamepad.rumble(command.large(), command.small(), command.durationMs());
    }

    @Override
    public void led(LedCommand command) {
        Gamepad gamepad = gamepads.get(command.role());
        if (gamepad == null) {
            return;
        }
        gamepad.setLedColor(command.red(), command.green(), command.blue(), command.durationMs());
    }
}
