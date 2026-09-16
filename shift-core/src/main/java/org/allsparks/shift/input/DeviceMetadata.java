package org.allsparks.shift.input;

import java.util.Objects;

/**
 * Optional identification of a physical controller captured with a snapshot.
 * Values are informational; SHIFT never requires hardware identity to function.
 */
public final class DeviceMetadata {
    public static final DeviceMetadata UNKNOWN = new DeviceMetadata("unknown", -1, "");

    private final String type;
    private final int hardwareId;
    private final String label;

    public DeviceMetadata(String type, int hardwareId, String label) {
        this.type = type == null ? "unknown" : type;
        this.hardwareId = hardwareId;
        this.label = label == null ? "" : label;
    }

    public String type() {
        return type;
    }

    public int hardwareId() {
        return hardwareId;
    }

    public String label() {
        return label;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DeviceMetadata)) {
            return false;
        }
        DeviceMetadata that = (DeviceMetadata) other;
        return hardwareId == that.hardwareId && type.equals(that.type) && label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, Integer.valueOf(hardwareId), label);
    }

    @Override
    public String toString() {
        return type + "#" + hardwareId;
    }
}
