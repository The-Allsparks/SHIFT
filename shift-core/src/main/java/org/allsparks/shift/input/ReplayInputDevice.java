package org.allsparks.shift.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Replay-ready {@link InputDevice} that yields recorded snapshots in order.
 *
 * <p>V1 does not include a recorder, but this device plus
 * {@link InputSnapshot#fromJson(String)} is enough for a future recording
 * pipeline to feed SHIFT without redesign.
 */
public final class ReplayInputDevice implements InputDevice {
    private final List<InputSnapshot> frames;
    private int index;
    private final InputSnapshot last;

    public ReplayInputDevice(InputSnapshot... frames) {
        this(Arrays.asList(frames));
    }

    public ReplayInputDevice(List<InputSnapshot> frames) {
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("ReplayInputDevice requires at least one snapshot");
        }
        this.frames = Collections.unmodifiableList(new ArrayList<InputSnapshot>(frames));
        this.last = this.frames.get(this.frames.size() - 1);
    }

    @Override
    public InputSnapshot sample() {
        if (index < frames.size()) {
            InputSnapshot next = frames.get(index);
            index++;
            return next;
        }
        return last;
    }

    public boolean exhausted() {
        return index >= frames.size();
    }

    public void rewind() {
        index = 0;
    }

    @Override
    public String describe() {
        return "replay[" + frames.size() + "]";
    }
}
