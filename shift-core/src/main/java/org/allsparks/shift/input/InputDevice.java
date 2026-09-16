package org.allsparks.shift.input;

/**
 * A source of controller state. Implementations must return an immutable
 * {@link InputSnapshot} that captures every control at a single instant.
 *
 * <p>FTC, simulation, replay, and future keyboard/web sources all implement
 * this interface. Core SHIFT never depends on a concrete device type.
 */
public interface InputDevice {
    /**
     * Captures the current physical state. Called once per SHIFT cycle per
     * assigned role. Must not block.
     */
    InputSnapshot sample();

    /**
     * Optional human-readable identifier used in logs.
     */
    String describe();
}
