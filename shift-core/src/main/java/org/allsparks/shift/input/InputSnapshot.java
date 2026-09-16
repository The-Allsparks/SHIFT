package org.allsparks.shift.input;

import java.util.Arrays;
import java.util.Locale;
import org.json.JSONObject;

/**
 * Immutable capture of one controller at one instant.
 *
 * <p>Every SHIFT update samples each assigned device once and holds those
 * snapshots for the entire cycle so bindings cannot observe tearing.
 *
 * <p>The snapshot is replay-ready: {@link #toJson()} / {@link #fromJson(String)}
 * round-trip the full physical state without depending on FTC types.
 */
public final class InputSnapshot {
    private static final int COUNT = Control.values().length;

    private final ControllerRole role;
    private final long timestampMillis;
    private final long sequence;
    private final DeviceMetadata metadata;
    private final boolean[] digital;
    private final double[] analog;

    InputSnapshot(
            ControllerRole role,
            long timestampMillis,
            long sequence,
            DeviceMetadata metadata,
            boolean[] digital,
            double[] analog) {
        this.role = role;
        this.timestampMillis = timestampMillis;
        this.sequence = sequence;
        this.metadata = metadata == null ? DeviceMetadata.UNKNOWN : metadata;
        this.digital = digital;
        this.analog = analog;
    }

    public static InputSnapshotBuilder builder() {
        return new InputSnapshotBuilder();
    }

    public static InputSnapshot idle(ControllerRole role, long timestampMillis, long sequence) {
        return builder()
                .role(role)
                .timestampMillis(timestampMillis)
                .sequence(sequence)
                .build();
    }

    public ControllerRole role() {
        return role;
    }

    public long timestampMillis() {
        return timestampMillis;
    }

    public long sequence() {
        return sequence;
    }

    public DeviceMetadata metadata() {
        return metadata;
    }

    public boolean digital(Control control) {
        if (control.analog()) {
            return analog(control) > 0.0;
        }
        return digital[control.ordinal()];
    }

    public double analog(Control control) {
        if (control.digital()) {
            return digital[control.ordinal()] ? 1.0 : 0.0;
        }
        return analog[control.ordinal()];
    }

    public boolean pressed(Control control) {
        return digital(control);
    }

    /**
     * Serializes this snapshot to a compact JSON object suitable for recording.
     */
    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("role", role.id());
        json.put("timestampMillis", timestampMillis);
        json.put("sequence", sequence);
        json.put("type", metadata.type());
        json.put("hardwareId", metadata.hardwareId());
        json.put("label", metadata.label());
        JSONObject controls = new JSONObject();
        for (Control control : Control.values()) {
            if (control.analog()) {
                double value = analog[control.ordinal()];
                if (value != 0.0) {
                    controls.put(control.token(), value);
                }
            } else if (digital[control.ordinal()]) {
                controls.put(control.token(), true);
            }
        }
        json.put("controls", controls);
        return json.toString();
    }

    public static InputSnapshot fromJson(String jsonText) {
        JSONObject json = new JSONObject(jsonText);
        InputSnapshotBuilder builder = builder()
                .role(ControllerRole.of(json.getString("role")))
                .timestampMillis(json.optLong("timestampMillis", 0L))
                .sequence(json.optLong("sequence", 0L))
                .metadata(new DeviceMetadata(
                        json.optString("type", "unknown"), json.optInt("hardwareId", -1), json.optString("label", "")));
        JSONObject controls = json.optJSONObject("controls");
        if (controls != null) {
            for (String key : controls.keySet()) {
                Control control = Control.parse(key);
                if (control.analog()) {
                    builder.setAnalog(control, controls.getDouble(key));
                } else {
                    builder.setDigital(control, controls.getBoolean(key));
                }
            }
        }
        return builder.build();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof InputSnapshot)) {
            return false;
        }
        InputSnapshot that = (InputSnapshot) other;
        return timestampMillis == that.timestampMillis
                && sequence == that.sequence
                && role.equals(that.role)
                && metadata.equals(that.metadata)
                && Arrays.equals(digital, that.digital)
                && Arrays.equals(analog, that.analog);
    }

    @Override
    public int hashCode() {
        int result = role.hashCode();
        result = 31 * result + (int) (timestampMillis ^ (timestampMillis >>> 32));
        result = 31 * result + (int) (sequence ^ (sequence >>> 32));
        result = 31 * result + metadata.hashCode();
        result = 31 * result + Arrays.hashCode(digital);
        result = 31 * result + Arrays.hashCode(analog);
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.ROOT,
                "InputSnapshot{role=%s seq=%d ts=%d}",
                role,
                Long.valueOf(sequence),
                Long.valueOf(timestampMillis));
    }

    static int controlCount() {
        return COUNT;
    }
}
