package org.allsparks.shift.input;

import org.allsparks.shift.clock.ShiftClock;
import org.allsparks.shift.clock.SystemShiftClock;

/**
 * Mutable simulated controller for unit tests and desktop rehearsal.
 *
 * <p>Digital helpers ({@link #press}, {@link #hold}, {@link #release}) update the
 * next snapshot. {@link #sample()} publishes an immutable copy and advances the
 * sequence. Held buttons remain pressed across samples until {@link #release} is
 * called.
 */
public final class SimulatedInputDevice implements InputDevice {
    private final ControllerRole role;
    private final ShiftClock clock;
    private final DeviceMetadata metadata;
    private final InputSnapshotBuilder state = new InputSnapshotBuilder();
    private long sequence;

    public SimulatedInputDevice(ControllerRole role) {
        this(role, SystemShiftClock.INSTANCE);
    }

    public SimulatedInputDevice(ControllerRole role, ShiftClock clock) {
        this(role, clock, new DeviceMetadata("simulated", -1, role.id()));
    }

    public SimulatedInputDevice(ControllerRole role, ShiftClock clock, DeviceMetadata metadata) {
        this.role = role;
        this.clock = clock;
        this.metadata = metadata;
        this.state.role(role).metadata(metadata);
    }

    public ControllerRole role() {
        return role;
    }

    public SimulatedInputDevice press(Control control) {
        state.setDigital(control, true);
        return this;
    }

    public SimulatedInputDevice hold(Control control) {
        return press(control);
    }

    public SimulatedInputDevice release(Control control) {
        state.setDigital(control, false);
        return this;
    }

    public SimulatedInputDevice set(Control control, boolean pressed) {
        state.setDigital(control, pressed);
        return this;
    }

    public SimulatedInputDevice setAnalog(Control control, double value) {
        state.setAnalog(control, value);
        return this;
    }

    public SimulatedInputDevice setStick(double leftX, double leftY, double rightX, double rightY) {
        state.setAnalog(Control.LEFT_STICK_X, leftX);
        state.setAnalog(Control.LEFT_STICK_Y, leftY);
        state.setAnalog(Control.RIGHT_STICK_X, rightX);
        state.setAnalog(Control.RIGHT_STICK_Y, rightY);
        return this;
    }

    public SimulatedInputDevice reset() {
        for (Control control : Control.values()) {
            if (control.analog()) {
                state.setAnalog(control, 0.0);
            } else {
                state.setDigital(control, false);
            }
        }
        return this;
    }

    @Override
    public InputSnapshot sample() {
        sequence++;
        return state.timestampMillis(clock.nowMillis()).sequence(sequence).build();
    }

    @Override
    public String describe() {
        return "simulated:" + role.id();
    }
}
