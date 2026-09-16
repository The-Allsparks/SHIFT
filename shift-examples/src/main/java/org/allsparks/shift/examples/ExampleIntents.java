package org.allsparks.shift.examples;

import org.allsparks.shift.intent.IntentRegistry;
import org.allsparks.shift.intent.IntentType;

/**
 * Example semantic intents for the AllSparks-style demonstration profile.
 *
 * <p>These names describe operator intent, not motors, encoder counts, or
 * servo positions. A real robot project should declare its own registry.
 */
public final class ExampleIntents {
    private ExampleIntents() {}

    public static final String DRIVE_TRANSLATION = "drive.translation";
    public static final String DRIVE_ROTATION = "drive.rotation";
    public static final String DRIVE_COR_LONGITUDINAL = "drive.cor.longitudinal";
    public static final String DRIVE_SLOW = "drive.slow";
    public static final String HELM_ACQUIRE = "helm.acquire";
    public static final String HELM_SCORE = "helm.score";
    public static final String INTAKE_COLLECT = "intake.collect";
    public static final String ELEVATOR_SCORE_HIGH = "elevator.score.high";
    public static final String ELEVATOR_SCORE_LOW = "elevator.score.low";
    public static final String SYSTEM_SAFE_RETRACT = "system.safeRetract";
    public static final String VISION_NEXT_TARGET = "vision.nextTarget";
    public static final String VISION_LOCK_TARGET = "vision.lockTarget";
    public static final String CALIBRATION_HOME = "calibration.home";

    public static IntentRegistry all() {
        return new IntentRegistry()
                .register(DRIVE_TRANSLATION, IntentType.VECTOR2, "DRIVE_TRANSLATION")
                .register(DRIVE_ROTATION, IntentType.ANALOG, "DRIVE_ROTATION")
                .register(DRIVE_COR_LONGITUDINAL, IntentType.ANALOG, "DRIVE_TRANSLATION")
                .register(DRIVE_SLOW, IntentType.BOOLEAN, "DRIVE_TRANSLATION")
                .register(HELM_ACQUIRE, IntentType.BOOLEAN)
                .register(HELM_SCORE, IntentType.EVENT)
                .register(INTAKE_COLLECT, IntentType.BOOLEAN, "INTAKE")
                .register(ELEVATOR_SCORE_HIGH, IntentType.EVENT, "ELEVATOR")
                .register(ELEVATOR_SCORE_LOW, IntentType.EVENT, "ELEVATOR")
                .register(SYSTEM_SAFE_RETRACT, IntentType.EVENT, "ELEVATOR")
                .register(VISION_NEXT_TARGET, IntentType.EVENT, "VISION_SELECTION")
                .register(VISION_LOCK_TARGET, IntentType.EVENT, "VISION_SELECTION")
                .register(CALIBRATION_HOME, IntentType.EVENT);
    }
}
