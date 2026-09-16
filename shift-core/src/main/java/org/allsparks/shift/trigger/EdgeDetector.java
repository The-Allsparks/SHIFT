package org.allsparks.shift.trigger;

import java.util.Set;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.InputSnapshot;

/**
 * Edge detector wrapping a {@link TriggerCondition}. Previous-cycle state is
 * supplied by the caller so every binding in a cycle observes the same pair of
 * snapshots.
 */
public final class EdgeDetector {
    private EdgeDetector() {}

    public static boolean firing(
            BindingEvent event, TriggerCondition condition, InputSnapshot previous, InputSnapshot current) {
        boolean now = condition.evaluate(current);
        boolean then = previous == null ? false : condition.evaluate(previous);
        switch (event) {
            case ON_PRESS:
                return now && !then;
            case ON_RELEASE:
                return !now && then;
            case WHILE_HELD:
                return now;
            case ANALOG:
                return now;
            default:
                return false;
        }
    }

    public static int specificityScore(TriggerCondition condition) {
        Set<Control> required = condition.requiredControls();
        return required.size() * 10 + condition.analogPredicateCount();
    }

    /**
     * True when {@code moreSpecific} should suppress {@code lessSpecific} because
     * its required control set is a proper superset.
     */
    public static boolean suppresses(TriggerCondition moreSpecific, TriggerCondition lessSpecific) {
        Set<Control> a = moreSpecific.requiredControls();
        Set<Control> b = lessSpecific.requiredControls();
        if (a.size() <= b.size()) {
            return false;
        }
        return a.containsAll(b);
    }
}
