package org.allsparks.shift.input;

/**
 * Distinguishes digital (boolean) controls from analog (continuous) axes and
 * triggers. Analog controls may still be used as digital conditions via a
 * threshold predicate.
 */
public enum ControlKind {
    DIGITAL,
    ANALOG
}
