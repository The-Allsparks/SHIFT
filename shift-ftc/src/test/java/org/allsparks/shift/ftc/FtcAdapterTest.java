package org.allsparks.shift.ftc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qualcomm.robotcore.hardware.Gamepad;
import org.allsparks.shift.feedback.LedCommand;
import org.allsparks.shift.feedback.RumbleCommand;
import org.allsparks.shift.feedback.RumblePattern;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.input.InputSnapshot;
import org.junit.jupiter.api.Test;

class FtcAdapterTest {
    @Test
    void samplesGamepadFieldsIntoSnapshot() {
        Gamepad gamepad = new Gamepad();
        gamepad.left_stick_y = -0.5f;
        gamepad.a = true;
        gamepad.y = true;
        gamepad.timestamp = 99L;
        gamepad.id = 7;
        gamepad.type = Gamepad.Type.SONY_PS4;
        gamepad.touchpad = true;
        gamepad.touchpad_finger_1 = true;
        gamepad.touchpad_finger_1_x = 0.25f;
        gamepad.touchpad_finger_1_y = 0.75f;
        gamepad.left_trigger = 0.9f;
        gamepad.left_trigger_pressed = true;
        gamepad.right_trigger_pressed = true;

        FtcGamepadDevice device = new FtcGamepadDevice(gamepad, "driver");
        InputSnapshot snapshot = device.sample();
        assertEquals(ControllerRole.DRIVER, snapshot.role());
        assertEquals(-0.5, snapshot.analog(Control.LEFT_STICK_Y), 1e-5);
        assertTrue(snapshot.digital(Control.A));
        assertTrue(snapshot.digital(Control.parse("TRIANGLE")));
        assertEquals(99L, snapshot.timestampMillis());
        assertEquals("SONY_PS4", snapshot.metadata().type());
        assertTrue(snapshot.digital(Control.TOUCHPAD));
        assertTrue(snapshot.digital(Control.TOUCHPAD_FINGER_1));
        assertEquals(0.25, snapshot.analog(Control.TOUCHPAD_FINGER_1_X), 1e-5);
        assertEquals(0.75, snapshot.analog(Control.TOUCHPAD_FINGER_1_Y), 1e-5);
        assertEquals(0.9, snapshot.analog(Control.LEFT_TRIGGER), 1e-5);
        assertTrue(snapshot.digital(Control.LEFT_TRIGGER_PRESSED));
        assertTrue(snapshot.digital(Control.parse("RTCLICK")));
    }

    @Test
    void rumbleAndLedCommandsReachTheGamepad() {
        RecordingGamepad gamepad = new RecordingGamepad();
        FtcFeedbackActuator actuator = new FtcFeedbackActuator().add("driver", gamepad);
        actuator.rumble(RumbleCommand.of(ControllerRole.DRIVER, RumblePattern.DOUBLE));
        actuator.led(new LedCommand(ControllerRole.DRIVER, 0, 1, 0, 200));
        assertEquals(2, gamepad.rumbleBlips);
        assertEquals(1.0, gamepad.ledG, 1e-9);
        assertEquals(200, gamepad.ledDuration);
    }

    static final class RecordingGamepad extends Gamepad {
        int rumbleBlips;
        double ledR;
        double ledG;
        double ledB;
        int ledDuration;

        @Override
        public void rumbleBlips(int count) {
            rumbleBlips = count;
        }

        @Override
        public void setLedColor(double r, double g, double b, int durationMs) {
            ledR = r;
            ledG = g;
            ledB = b;
            ledDuration = durationMs;
        }
    }
}
