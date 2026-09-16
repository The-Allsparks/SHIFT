package org.allsparks.shift.ftc;

import com.qualcomm.robotcore.hardware.Gamepad;
import org.allsparks.shift.Shift;
import org.allsparks.shift.input.ControllerRole;

/**
 * Convenience entry point for FTC OpModes. Core SHIFT remains free of FTC types;
 * this helper only wires {@link FtcGamepadDevice} and {@link FtcFeedbackActuator}.
 *
 * <p>{@link #builder(Gamepad, Gamepad)} always enables the embedded idle
 * fallback. After {@code build()}, check {@link Shift#usedFallback()} and
 * surface it on Driver Station telemetry. To fail {@code init()} on a bad
 * profile instead, construct with {@link Shift#builder()} and omit
 * {@link Shift.Builder#fallbackToEmbedded()}.
 */
public final class FtcShift {
    private FtcShift() {}

    public static Shift.Builder builder(Gamepad gamepad1, Gamepad gamepad2) {
        FtcFeedbackActuator actuator =
                new FtcFeedbackActuator().add(ControllerRole.DRIVER, gamepad1).add(ControllerRole.CODRIVER, gamepad2);
        return Shift.builder()
                .addDevice(ControllerRole.DRIVER, new FtcGamepadDevice(gamepad1, ControllerRole.DRIVER))
                .addDevice(ControllerRole.CODRIVER, new FtcGamepadDevice(gamepad2, ControllerRole.CODRIVER))
                .feedbackActuator(actuator)
                .fallbackToEmbedded();
    }
}
