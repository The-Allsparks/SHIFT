package org.allsparks.shift.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.allsparks.shift.Shift;
import org.allsparks.shift.ftc.FtcShift;
import org.allsparks.shift.intent.Intent;
import org.allsparks.shift.intent.IntentFrame;
import org.allsparks.shift.intent.Vector2;

/**
 * Example FTC OpMode showing how TeamCode consumes SHIFT.
 *
 * <p>This class is a compile-checked illustration. It is {@link Disabled} so it
 * cannot be selected on a Driver Station by accident. Copy the pattern into the
 * team's robot project; do not treat this as a competition teleop.
 *
 * <p>SHIFT tells the application <em>what the operator wants</em>. Mechanism
 * code, Pedro Pathing, MIMIC, and HELM remain responsible for <em>how</em>.
 *
 * <p>Named per-driver maps: parse JSON into {@link org.allsparks.shift.profile.ProfileBank}
 * during {@code init()}, then {@code loadProfile(bank.compose(driverId, operatorId))}.
 * Call {@link Shift#activate} between matches, not from {@code loop()}.
 */
@TeleOp(name = "SHIFT Example TeleOp", group = "SHIFT")
@Disabled
public class ShiftExampleOpMode extends OpMode {
    private Shift shift;

    @Override
    public void init() {
        shift = FtcShift.builder(gamepad1, gamepad2)
                .registerIntents(ExampleIntents.all())
                .registerFeedback("TARGET_ACQUIRED")
                .registerFeedback("ACTION_REJECTED")
                .registerFeedback("LAYER_CHANGED")
                .loadProfile(readClasspath("/org/allsparks/shift/examples/competition-example.json"))
                .build();
        telemetry.addData("shiftProfile", shift.profile().id());
        telemetry.addData("shiftFallback", shift.usedFallback());
        if (shift.usedFallback()) {
            telemetry.addLine("Preferred SHIFT profile failed; robot is on embedded idle.");
        }
        telemetry.update();
    }

    @Override
    public void loop() {
        IntentFrame frame = shift.update();

        Vector2 translation = frame.vector2(ExampleIntents.DRIVE_TRANSLATION);
        double rotation = frame.analog(ExampleIntents.DRIVE_ROTATION);
        double cor = frame.analog(ExampleIntents.DRIVE_COR_LONGITUDINAL);
        boolean slow = frame.held(ExampleIntents.DRIVE_SLOW);
        boolean acquire = frame.held(ExampleIntents.HELM_ACQUIRE);

        // Robot application / HELM / MIMIC consume semantic intent here.
        // Do not read gamepad1 / gamepad2 for operator mappings.
        useIntent(translation, rotation, cor, slow, acquire);

        for (int i = 0; i < frame.events().size(); i++) {
            Intent event = frame.events().get(i);
            if (ExampleIntents.HELM_SCORE.equals(event.id().value())) {
                shift.emitFeedback("TARGET_ACQUIRED");
            }
        }
    }

    private static void useIntent(Vector2 translation, double rotation, double cor, boolean slow, boolean acquire) {
        // Placeholder for the robot application. Intentionally empty: SHIFT
        // examples must not command motors.
        if (translation == null || slow || acquire) {
            return;
        }
        if (rotation == 0.0 && cor == 0.0) {
            return;
        }
    }

    static String readClasspath(String resource) {
        InputStream stream = ShiftExampleOpMode.class.getResourceAsStream(resource);
        if (stream == null) {
            throw new IllegalStateException("Missing example profile " + resource);
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            int read;
            while ((read = stream.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), Charset.forName("UTF-8"));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read " + resource, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ignored) {
                // ignore
            }
        }
    }
}
