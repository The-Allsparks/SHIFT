package org.allsparks.shift.observe;

import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.input.InputSnapshot;

/**
 * Fan-out input listener. Register TRACE and any other recorder without coupling
 * them to each other or to SHIFT.
 */
public final class CompositeInputListener implements ShiftInputListener {
    private final ShiftInputListener[] listeners;

    public CompositeInputListener(ShiftInputListener[] listeners) {
        this.listeners = copy(listeners);
    }

    public static CompositeInputListener of(ShiftInputListener first, ShiftInputListener second) {
        return new CompositeInputListener(new ShiftInputListener[] {first, second});
    }

    @Override
    public void onInput(long sequence, ControllerRole role, InputSnapshot snapshot) {
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].onInput(sequence, role, snapshot);
        }
    }

    private static ShiftInputListener[] copy(ShiftInputListener[] listeners) {
        if (listeners == null || listeners.length == 0) {
            return new ShiftInputListener[] {ShiftInputListener.NOOP};
        }
        ShiftInputListener[] copy = new ShiftInputListener[listeners.length];
        int count = 0;
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] != null) {
                copy[count] = listeners[i];
                count++;
            }
        }
        if (count == 0) {
            return new ShiftInputListener[] {ShiftInputListener.NOOP};
        }
        if (count == copy.length) {
            return copy;
        }
        ShiftInputListener[] trimmed = new ShiftInputListener[count];
        System.arraycopy(copy, 0, trimmed, 0, count);
        return trimmed;
    }
}
