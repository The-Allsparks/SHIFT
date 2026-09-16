package org.allsparks.shift.intent;

/**
 * Value shape carried by an intent. Keep this set small: each type must have a
 * real operator-input use case.
 */
public enum IntentType {
    /** Discrete one-shot such as {@code elevator.score.high}. */
    EVENT,
    /** Latched or held boolean such as {@code intake.collect} while a bumper is held. */
    BOOLEAN,
    /** Scalar in approximately {@code [-1, 1]} or {@code [0, 1]}. */
    ANALOG,
    /** Two-axis stick or planar command such as {@code drive.translation}. */
    VECTOR2
}
