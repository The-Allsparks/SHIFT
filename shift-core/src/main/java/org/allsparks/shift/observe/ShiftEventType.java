package org.allsparks.shift.observe;

/**
 * Structured lifecycle event type. TRACE or another logger implements
 * {@link ShiftEventSink}; SHIFT never depends on TRACE.
 */
public enum ShiftEventType {
    INPUT,
    BINDING_MATCH,
    INTENT_EMITTED,
    LAYER_CHANGED,
    PROFILE_LOADED,
    PROFILE_REJECTED,
    FEEDBACK,
    CONFLICT,
    ERROR
}
