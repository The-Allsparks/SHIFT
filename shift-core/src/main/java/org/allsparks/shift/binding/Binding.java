package org.allsparks.shift.binding;

import java.util.Set;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.intent.IntentId;
import org.allsparks.shift.intent.IntentPriority;
import org.allsparks.shift.intent.IntentType;
import org.allsparks.shift.intent.LogicalResource;
import org.allsparks.shift.transform.AnalogTransform;
import org.allsparks.shift.trigger.BindingEvent;
import org.allsparks.shift.trigger.TriggerCondition;

/**
 * Compiled binding. Created at profile load; evaluated every robot loop.
 *
 * <p>A binding either emits a semantic intent or performs a layer action
 * ({@link #setLayer} / {@link #cycleLayers}). JSON declaration order is not
 * used for conflict resolution.
 */
public final class Binding {
    private final String id;
    private final String path;
    private final ControllerRole role;
    private final String layer;
    private final TriggerCondition trigger;
    private final BindingEvent event;
    private final IntentId intentId;
    private final IntentType intentType;
    private final IntentPriority priority;
    private final LogicalResource resource;
    private final AnalogTransform transform;
    private final Control analogSource;
    private final Control vectorX;
    private final Control vectorY;
    private final AnalogTransform transformX;
    private final AnalogTransform transformY;
    private final String setLayer;
    private final String[] cycleLayers;
    private final boolean allowOverlap;
    private final int declarationIndex;

    Binding(Builder builder) {
        this.id = builder.id;
        this.path = builder.path;
        this.role = builder.role;
        this.layer = builder.layer;
        this.trigger = builder.trigger;
        this.event = builder.event;
        this.intentId = builder.intentId;
        this.intentType = builder.intentType;
        this.priority = builder.priority;
        this.resource = builder.resource;
        this.transform = builder.transform;
        this.analogSource = builder.analogSource;
        this.vectorX = builder.vectorX;
        this.vectorY = builder.vectorY;
        this.transformX = builder.transformX;
        this.transformY = builder.transformY;
        this.setLayer = builder.setLayer;
        this.cycleLayers = builder.cycleLayers;
        this.allowOverlap = builder.allowOverlap;
        this.declarationIndex = builder.declarationIndex;
    }

    public String id() {
        return id;
    }

    public String path() {
        return path;
    }

    public ControllerRole role() {
        return role;
    }

    public String layer() {
        return layer;
    }

    public TriggerCondition trigger() {
        return trigger;
    }

    public BindingEvent event() {
        return event;
    }

    public IntentId intentId() {
        return intentId;
    }

    public IntentType intentType() {
        return intentType;
    }

    public IntentPriority priority() {
        return priority;
    }

    public LogicalResource resource() {
        return resource;
    }

    public AnalogTransform transform() {
        return transform;
    }

    public Control analogSource() {
        return analogSource;
    }

    public Control vectorX() {
        return vectorX;
    }

    public Control vectorY() {
        return vectorY;
    }

    public AnalogTransform transformX() {
        return transformX;
    }

    public AnalogTransform transformY() {
        return transformY;
    }

    public String setLayer() {
        return setLayer;
    }

    public String[] cycleLayers() {
        return cycleLayers;
    }

    public boolean layerAction() {
        return setLayer != null || cycleLayers != null;
    }

    public boolean allowOverlap() {
        return allowOverlap;
    }

    public int declarationIndex() {
        return declarationIndex;
    }

    public Set<Control> requiredControls() {
        return trigger.requiredControls();
    }

    public int specificity() {
        return org.allsparks.shift.trigger.EdgeDetector.specificityScore(trigger);
    }

    public String conflictKey() {
        StringBuilder builder = new StringBuilder();
        builder.append(role.id()).append('|').append(layer).append('|').append(event.name());
        for (Control control : trigger.requiredControls()) {
            builder.append('|').append(control.token());
        }
        builder.append('|').append(trigger.analogPredicateCount());
        return builder.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id = "";
        private String path = "";
        private ControllerRole role = ControllerRole.DRIVER;
        private String layer = "";
        private TriggerCondition trigger = TriggerCondition.AlwaysTrue.INSTANCE;
        private BindingEvent event = BindingEvent.ON_PRESS;
        private IntentId intentId;
        private IntentType intentType = IntentType.EVENT;
        private IntentPriority priority = IntentPriority.DEFAULT;
        private LogicalResource resource;
        private AnalogTransform transform = AnalogTransform.IDENTITY;
        private Control analogSource;
        private Control vectorX;
        private Control vectorY;
        private AnalogTransform transformX = AnalogTransform.IDENTITY;
        private AnalogTransform transformY = AnalogTransform.IDENTITY;
        private String setLayer;
        private String[] cycleLayers;
        private boolean allowOverlap;
        private int declarationIndex;

        public Builder id(String id) {
            this.id = id == null ? "" : id;
            return this;
        }

        public Builder path(String path) {
            this.path = path == null ? "" : path;
            return this;
        }

        public Builder role(ControllerRole role) {
            this.role = role;
            return this;
        }

        public Builder layer(String layer) {
            this.layer = layer == null ? "" : layer;
            return this;
        }

        public Builder trigger(TriggerCondition trigger) {
            this.trigger = trigger;
            return this;
        }

        public Builder event(BindingEvent event) {
            this.event = event;
            return this;
        }

        public Builder intentId(IntentId intentId) {
            this.intentId = intentId;
            return this;
        }

        public Builder intentType(IntentType intentType) {
            this.intentType = intentType;
            return this;
        }

        public Builder priority(IntentPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder resource(LogicalResource resource) {
            this.resource = resource;
            return this;
        }

        public Builder transform(AnalogTransform transform) {
            this.transform = transform == null ? AnalogTransform.IDENTITY : transform;
            return this;
        }

        public Builder analogSource(Control analogSource) {
            this.analogSource = analogSource;
            return this;
        }

        public Builder vector(Control x, Control y, AnalogTransform tx, AnalogTransform ty) {
            this.vectorX = x;
            this.vectorY = y;
            this.transformX = tx == null ? AnalogTransform.IDENTITY : tx;
            this.transformY = ty == null ? AnalogTransform.IDENTITY : ty;
            return this;
        }

        public Builder setLayer(String setLayer) {
            this.setLayer = setLayer;
            return this;
        }

        public Builder cycleLayers(String[] cycleLayers) {
            this.cycleLayers = cycleLayers;
            return this;
        }

        public Builder allowOverlap(boolean allowOverlap) {
            this.allowOverlap = allowOverlap;
            return this;
        }

        public Builder declarationIndex(int declarationIndex) {
            this.declarationIndex = declarationIndex;
            return this;
        }

        public Binding build() {
            return new Binding(this);
        }
    }
}
