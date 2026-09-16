package org.allsparks.shift.observe;

import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.input.InputSnapshot;

/**
 * Called once per assigned controller after {@code Shift.update()} samples it.
 * TRACE adapters record the snapshot as {@code RecordCategory.INPUT}. Do not
 * retain the snapshot across loops unless you copy it. Must not block.
 *
 * <p>SHIFT does not depend on TRACE. TeamCode (or a tiny adapter module)
 * implements this.
 */
public interface ShiftInputListener {
    void onInput(long sequence, ControllerRole role, InputSnapshot snapshot);

    ShiftInputListener NOOP = new ShiftInputListener() {
        @Override
        public void onInput(long sequence, ControllerRole role, InputSnapshot snapshot) {
            // intentionally empty
        }
    };
}
