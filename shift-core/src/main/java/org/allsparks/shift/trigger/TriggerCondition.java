package org.allsparks.shift.trigger;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.InputSnapshot;

/**
 * Boolean condition evaluated against a single snapshot. Composites are
 * precompiled at profile load; {@link #evaluate} is allocation-free.
 */
public interface TriggerCondition {
    boolean evaluate(InputSnapshot snapshot);

    /**
     * Positive (non-negated) controls that participate in this condition. Used
     * for specificity: a chord is more specific than a subset of itself.
     */
    Set<Control> requiredControls();

    /**
     * Analog predicates add extra specificity beyond the control set.
     */
    int analogPredicateCount();

    abstract class Base implements TriggerCondition {
        @Override
        public int analogPredicateCount() {
            return 0;
        }
    }

    final class AlwaysTrue extends Base {
        public static final AlwaysTrue INSTANCE = new AlwaysTrue();

        @Override
        public boolean evaluate(InputSnapshot snapshot) {
            return true;
        }

        @Override
        public Set<Control> requiredControls() {
            return Collections.emptySet();
        }
    }

    final class ControlPressed extends Base {
        private final Control control;
        private final double analogThreshold;
        private final Set<Control> required;

        public ControlPressed(Control control, double analogThreshold) {
            this.control = control;
            this.analogThreshold = analogThreshold;
            Set<Control> set = new LinkedHashSet<Control>();
            set.add(control);
            this.required = Collections.unmodifiableSet(set);
        }

        public Control control() {
            return control;
        }

        @Override
        public boolean evaluate(InputSnapshot snapshot) {
            if (control.analog()) {
                return Math.abs(snapshot.analog(control)) >= analogThreshold;
            }
            return snapshot.digital(control);
        }

        @Override
        public Set<Control> requiredControls() {
            return required;
        }
    }

    final class AnalogCompare extends Base {
        public enum Op {
            GT,
            GTE,
            LT,
            LTE
        }

        private final Control control;
        private final Op op;
        private final double value;
        private final Set<Control> required;

        public AnalogCompare(Control control, Op op, double value) {
            this.control = control;
            this.op = op;
            this.value = value;
            Set<Control> set = new LinkedHashSet<Control>();
            set.add(control);
            this.required = Collections.unmodifiableSet(set);
        }

        @Override
        public boolean evaluate(InputSnapshot snapshot) {
            double actual = snapshot.analog(control);
            switch (op) {
                case GT:
                    return actual > value;
                case GTE:
                    return actual >= value;
                case LT:
                    return actual < value;
                case LTE:
                    return actual <= value;
                default:
                    return false;
            }
        }

        @Override
        public Set<Control> requiredControls() {
            return required;
        }

        @Override
        public int analogPredicateCount() {
            return 1;
        }

        public Control control() {
            return control;
        }

        public Op op() {
            return op;
        }

        public double value() {
            return value;
        }
    }

    final class Not extends Base {
        private final TriggerCondition inner;

        public Not(TriggerCondition inner) {
            this.inner = inner;
        }

        @Override
        public boolean evaluate(InputSnapshot snapshot) {
            return !inner.evaluate(snapshot);
        }

        @Override
        public Set<Control> requiredControls() {
            // Negation does not require the inner controls to be pressed, so it
            // does not contribute to chord specificity.
            return Collections.emptySet();
        }

        @Override
        public int analogPredicateCount() {
            return inner.analogPredicateCount();
        }
    }

    final class All extends Base {
        private final TriggerCondition[] children;
        private final Set<Control> required;
        private final int analogPredicates;

        public All(TriggerCondition[] children) {
            this.children = children;
            Set<Control> set = new LinkedHashSet<Control>();
            int analog = 0;
            for (int i = 0; i < children.length; i++) {
                set.addAll(children[i].requiredControls());
                analog += children[i].analogPredicateCount();
            }
            this.required = Collections.unmodifiableSet(set);
            this.analogPredicates = analog;
        }

        @Override
        public boolean evaluate(InputSnapshot snapshot) {
            for (int i = 0; i < children.length; i++) {
                if (!children[i].evaluate(snapshot)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Set<Control> requiredControls() {
            return required;
        }

        @Override
        public int analogPredicateCount() {
            return analogPredicates;
        }
    }

    final class Any extends Base {
        private final TriggerCondition[] children;
        private final Set<Control> required;
        private final int analogPredicates;

        public Any(TriggerCondition[] children) {
            this.children = children;
            Set<Control> set = new LinkedHashSet<Control>();
            int analog = 0;
            for (int i = 0; i < children.length; i++) {
                set.addAll(children[i].requiredControls());
                analog += children[i].analogPredicateCount();
            }
            this.required = Collections.unmodifiableSet(set);
            this.analogPredicates = analog;
        }

        @Override
        public boolean evaluate(InputSnapshot snapshot) {
            for (int i = 0; i < children.length; i++) {
                if (children[i].evaluate(snapshot)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Set<Control> requiredControls() {
            return required;
        }

        @Override
        public int analogPredicateCount() {
            return analogPredicates;
        }
    }
}
