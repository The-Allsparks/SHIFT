package org.allsparks.shift.ftc;

import android.content.res.AssetManager;
import com.qualcomm.robotcore.hardware.Gamepad;
import java.io.IOException;
import java.io.InputStream;
import org.allsparks.shift.Shift;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.profile.ShiftProfiles;

/**
 * Convenience entry point for FTC OpModes. Core SHIFT remains free of FTC types;
 * this helper wires {@link FtcGamepadDevice}, {@link FtcFeedbackActuator}, and
 * APK asset reads for profile JSON.
 *
 * <p>{@link #builder(Gamepad, Gamepad)} always enables the embedded idle
 * fallback. After {@code build()}, check {@link Shift#usedFallback()} and
 * surface it on Driver Station telemetry. To fail {@code init()} on a bad
 * profile instead, construct with {@link Shift#builder()} and omit
 * {@link Shift.Builder#fallbackToEmbedded()}.
 *
 * <p>Call {@link #readAsset(AssetManager, String)} from {@code init()} with the
 * TeamCode asset path. SHIFT owns UTF-8 reading and the JSON schema. TeamCode
 * owns the file.
 */
public final class FtcShift {
    private FtcShift() {}

    /**
     * Reads a SHIFT profile packed under {@code src/main/assets/}. Missing or
     * unreadable files throw {@link IllegalStateException} with the path so INIT
     * cannot silently idle. Does not parse JSON; pass the string to
     * {@link Shift.Builder#loadProfile(String)}.
     */
    public static String readAsset(AssetManager assets, String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("SHIFT profile asset path is required");
        }
        if (assets == null) {
            throw new IllegalArgumentException("AssetManager is required to read " + path);
        }
        InputStream stream;
        try {
            stream = assets.open(path);
        } catch (IOException ex) {
            throw new IllegalStateException("Missing SHIFT profile asset " + path, ex);
        }
        return ShiftProfiles.readUtf8(stream, path);
    }

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
