package org.allsparks.shift.input;

/**
 * Mutable builder for {@link InputSnapshot}. Used by adapters, the simulator,
 * and JSON replay. The built snapshot copies arrays so later mutations of this
 * builder cannot tear previously published state.
 */
public final class InputSnapshotBuilder {
    private ControllerRole role = ControllerRole.DRIVER;
    private long timestampMillis;
    private long sequence;
    private DeviceMetadata metadata = DeviceMetadata.UNKNOWN;
    private final boolean[] digital = new boolean[InputSnapshot.controlCount()];
    private final double[] analog = new double[InputSnapshot.controlCount()];

    public InputSnapshotBuilder role(ControllerRole role) {
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }
        this.role = role;
        return this;
    }

    public InputSnapshotBuilder timestampMillis(long timestampMillis) {
        this.timestampMillis = timestampMillis;
        return this;
    }

    public InputSnapshotBuilder sequence(long sequence) {
        this.sequence = sequence;
        return this;
    }

    public InputSnapshotBuilder metadata(DeviceMetadata metadata) {
        this.metadata = metadata == null ? DeviceMetadata.UNKNOWN : metadata;
        return this;
    }

    public InputSnapshotBuilder setDigital(Control control, boolean pressed) {
        if (control.analog()) {
            analog[control.ordinal()] = pressed ? 1.0 : 0.0;
        } else {
            digital[control.ordinal()] = pressed;
        }
        return this;
    }

    public InputSnapshotBuilder setAnalog(Control control, double value) {
        if (control.digital()) {
            digital[control.ordinal()] = value > 0.5;
        } else {
            analog[control.ordinal()] = value;
        }
        return this;
    }

    public InputSnapshotBuilder copyFrom(InputSnapshot snapshot) {
        this.role = snapshot.role();
        this.timestampMillis = snapshot.timestampMillis();
        this.sequence = snapshot.sequence();
        this.metadata = snapshot.metadata();
        for (Control control : Control.values()) {
            if (control.analog()) {
                analog[control.ordinal()] = snapshot.analog(control);
            } else {
                digital[control.ordinal()] = snapshot.digital(control);
            }
        }
        return this;
    }

    public InputSnapshot build() {
        boolean[] digitalCopy = new boolean[digital.length];
        double[] analogCopy = new double[analog.length];
        System.arraycopy(digital, 0, digitalCopy, 0, digital.length);
        System.arraycopy(analog, 0, analogCopy, 0, analog.length);
        return new InputSnapshot(role, timestampMillis, sequence, metadata, digitalCopy, analogCopy);
    }
}
