package org.allsparks.shift;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.allsparks.shift.binding.Binding;
import org.allsparks.shift.clock.ShiftClock;
import org.allsparks.shift.clock.SystemShiftClock;
import org.allsparks.shift.config.ConfigError;
import org.allsparks.shift.config.ConfigException;
import org.allsparks.shift.feedback.FeedbackActuator;
import org.allsparks.shift.feedback.FeedbackEffect;
import org.allsparks.shift.feedback.FeedbackId;
import org.allsparks.shift.feedback.FeedbackRegistry;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.input.InputDevice;
import org.allsparks.shift.input.InputSnapshot;
import org.allsparks.shift.intent.Intent;
import org.allsparks.shift.intent.IntentFrame;
import org.allsparks.shift.intent.IntentPriority;
import org.allsparks.shift.intent.IntentRegistry;
import org.allsparks.shift.intent.IntentType;
import org.allsparks.shift.intent.IntentValue;
import org.allsparks.shift.observe.FilteringEventSink;
import org.allsparks.shift.observe.ShiftEvent;
import org.allsparks.shift.observe.ShiftEventLevel;
import org.allsparks.shift.observe.ShiftEventSink;
import org.allsparks.shift.observe.ShiftEventType;
import org.allsparks.shift.observe.ShiftInputListener;
import org.allsparks.shift.profile.ControllerAssignment;
import org.allsparks.shift.profile.Profile;
import org.allsparks.shift.profile.ProfileLoader;
import org.allsparks.shift.trigger.BindingEvent;
import org.allsparks.shift.trigger.EdgeDetector;

/**
 * Semantic Human Input Framework for Teleoperation.
 *
 * <p>Call {@link #update()} once per robot loop. SHIFT translates physical
 * operator input into semantic intent. It does not command motors, schedule
 * subsystems, or plan motion.
 *
 * <p>The {@link IntentFrame} returned by {@link #update()} is reused on the next
 * call. Copy values that must survive across loops.
 */
public final class Shift {
    public static final String EMBEDDED_FALLBACK_RESOURCE = "/org/allsparks/shift/profile/fallback.json";

    private final Map<ControllerRole, InputDevice> devices;
    private Profile profile;
    private boolean usedFallback;
    private List<ConfigError> fallbackErrors;
    private final ShiftEventSink sink;
    private final ShiftInputListener inputListener;
    private final FeedbackActuator actuator;
    private final ShiftClock clock;
    private final Map<ControllerRole, String> activeLayers;
    private final Map<ControllerRole, InputSnapshot> previous;
    private final Map<ControllerRole, InputSnapshot> current;
    private final Map<ControllerRole, String> pendingLayers = new LinkedHashMap<ControllerRole, String>();
    private final IntentFrame frame = new IntentFrame();
    private final List<Binding> candidates = new ArrayList<Binding>();
    private long sequence;

    Shift(Builder builder, Profile profile, boolean usedFallback, List<ConfigError> fallbackErrors) {
        this.devices = new LinkedHashMap<ControllerRole, InputDevice>(builder.devices);
        this.profile = profile;
        this.usedFallback = usedFallback;
        this.fallbackErrors = fallbackErrors;
        this.sink = new FilteringEventSink(builder.sink, builder.minimumLevel);
        this.inputListener = builder.inputListener == null ? ShiftInputListener.NOOP : builder.inputListener;
        this.actuator = builder.actuator;
        this.clock = builder.clock;
        this.activeLayers = new LinkedHashMap<ControllerRole, String>();
        this.previous = new LinkedHashMap<ControllerRole, InputSnapshot>();
        this.current = new LinkedHashMap<ControllerRole, InputSnapshot>();
        applyProfileLayers(profile);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Profile profile() {
        return profile;
    }

    public boolean usedFallback() {
        return usedFallback;
    }

    public List<ConfigError> fallbackErrors() {
        return fallbackErrors;
    }

    public String activeLayer(ControllerRole role) {
        String layer = activeLayers.get(role);
        return layer == null ? "" : layer;
    }

    public InputSnapshot snapshot(ControllerRole role) {
        return current.get(role);
    }

    /**
     * Switches to an already-compiled profile. Does not parse JSON. Resets
     * layers to the profile defaults and clears edge history so the next
     * {@link #update()} does not inherit presses from the previous mapping.
     * Call from {@code init_loop} or between matches, not from {@code update()}.
     */
    public void activate(Profile next) {
        if (next == null) {
            throw new IllegalArgumentException("Profile is required");
        }
        this.profile = next;
        this.usedFallback = false;
        this.fallbackErrors = new ArrayList<ConfigError>();
        previous.clear();
        current.clear();
        applyProfileLayers(next);
        ShiftEvent.Builder loaded = ShiftEvent.builder(ShiftEventType.PROFILE_LOADED, ShiftEventLevel.INFO)
                .sequence(sequence)
                .timestampMillis(clock.nowMillis())
                .field("profile", next.id())
                .field("fallback", "false")
                .field("schemaVersion", Integer.toString(next.schemaVersion()));
        if (!next.person().isEmpty()) {
            loaded.field("person", next.person()).field("set", next.set());
        }
        emit(loaded.build());
    }

    private void applyProfileLayers(Profile next) {
        activeLayers.clear();
        for (Map.Entry<ControllerRole, InputDevice> entry : devices.entrySet()) {
            ControllerRole role = entry.getKey();
            String layer = next.defaultLayer(role);
            ControllerAssignment assignment = next.controllers().get(role);
            if ((layer == null || layer.isEmpty()) && assignment != null) {
                layer = assignment.defaultLayer();
            }
            activeLayers.put(role, layer == null ? "" : layer);
        }
    }

    /**
     * Samples every assigned device once, evaluates compiled bindings against
     * those snapshots, and returns the semantic intent frame for this loop.
     * Must be called from the robot loop thread. Does not block, parse JSON,
     * or start threads.
     */
    public IntentFrame update() {
        sequence++;
        long now = clock.nowMillis();
        current.clear();
        for (Map.Entry<ControllerRole, InputDevice> entry : devices.entrySet()) {
            InputSnapshot snapshot = entry.getValue().sample();
            current.put(entry.getKey(), snapshot);
            inputListener.onInput(sequence, entry.getKey(), snapshot);
            emitDigitalInputEvents(entry.getKey(), previous.get(entry.getKey()), snapshot);
        }
        long timestamp = firstTimestamp(now);
        frame.begin(sequence, timestamp, profile.id(), usedFallback);
        for (Map.Entry<ControllerRole, String> entry : activeLayers.entrySet()) {
            frame.setLayer(entry.getKey(), entry.getValue());
        }

        pendingLayers.clear();
        evaluateLayerActions(timestamp);
        evaluateAnalog(timestamp);
        evaluateDigital(timestamp);

        applyLayerChanges(timestamp);
        frame.freeze();

        previous.clear();
        previous.putAll(current);
        return frame;
    }

    /**
     * Emits semantic operator feedback. The active profile maps the id onto
     * rumble and/or LED commands. Unknown ids are reported as ERROR events and
     * do not throw on the robot loop.
     */
    public void emitFeedback(String feedbackName) {
        FeedbackId id;
        try {
            id = FeedbackId.of(feedbackName);
        } catch (IllegalArgumentException ex) {
            emit(ShiftEvent.builder(ShiftEventType.ERROR, ShiftEventLevel.ERROR)
                    .sequence(sequence)
                    .timestampMillis(clock.nowMillis())
                    .field("reason", ex.getMessage())
                    .build());
            return;
        }
        FeedbackEffect effect = profile.feedback().get(id);
        if (effect == null) {
            emit(ShiftEvent.builder(ShiftEventType.ERROR, ShiftEventLevel.WARN)
                    .sequence(sequence)
                    .timestampMillis(clock.nowMillis())
                    .field("feedback", id.value())
                    .message("No feedback mapping in profile '" + profile.id() + "'")
                    .build());
            return;
        }
        emit(ShiftEvent.builder(ShiftEventType.FEEDBACK, ShiftEventLevel.INFO)
                .sequence(sequence)
                .timestampMillis(clock.nowMillis())
                .field("feedback", id.value())
                .field("role", effect.role().id())
                .build());
        if (effect.rumble() != null) {
            actuator.rumble(effect.rumble());
        }
        if (effect.led() != null) {
            actuator.led(effect.led());
        }
    }

    private void evaluateLayerActions(long timestamp) {
        List<Binding> bindings = profile.bindings();
        for (int i = 0; i < bindings.size(); i++) {
            Binding binding = bindings.get(i);
            if (!binding.layerAction()) {
                continue;
            }
            if (!layerMatches(binding)) {
                continue;
            }
            InputSnapshot curr = current.get(binding.role());
            InputSnapshot prev = previous.get(binding.role());
            if (curr == null) {
                continue;
            }
            if (!EdgeDetector.firing(binding.event(), binding.trigger(), prev, curr)) {
                continue;
            }
            String next = nextLayer(binding);
            if (next != null) {
                pendingLayers.put(binding.role(), next);
                emitBindingMatch(binding, timestamp);
            }
        }
    }

    private String nextLayer(Binding binding) {
        if (binding.setLayer() != null) {
            return binding.setLayer();
        }
        String[] cycle = binding.cycleLayers();
        if (cycle == null || cycle.length == 0) {
            return null;
        }
        String currentLayer = activeLayers.get(binding.role());
        if (currentLayer == null) {
            currentLayer = "";
        }
        int index = -1;
        for (int i = 0; i < cycle.length; i++) {
            if (cycle[i].equals(currentLayer)) {
                index = i;
                break;
            }
        }
        return cycle[(index + 1) % cycle.length];
    }

    private void applyLayerChanges(long timestamp) {
        for (Map.Entry<ControllerRole, String> entry : pendingLayers.entrySet()) {
            String previousLayer = activeLayers.get(entry.getKey());
            String next = entry.getValue();
            if (previousLayer != null && previousLayer.equals(next)) {
                continue;
            }
            activeLayers.put(entry.getKey(), next);
            frame.setLayer(entry.getKey(), next);
            emit(ShiftEvent.builder(ShiftEventType.LAYER_CHANGED, ShiftEventLevel.INFO)
                    .sequence(sequence)
                    .timestampMillis(timestamp)
                    .field("role", entry.getKey().id())
                    .field("previousLayer", previousLayer == null ? "" : previousLayer)
                    .field("layer", next)
                    .build());
        }
    }

    private void evaluateAnalog(long timestamp) {
        List<Binding> bindings = profile.bindings();
        for (int i = 0; i < bindings.size(); i++) {
            Binding binding = bindings.get(i);
            if (binding.layerAction() || binding.intentId() == null) {
                continue;
            }
            if (binding.event() != BindingEvent.ANALOG) {
                continue;
            }
            if (!layerMatches(binding)) {
                continue;
            }
            InputSnapshot curr = current.get(binding.role());
            if (curr == null) {
                continue;
            }
            Intent intent = analogIntent(binding, curr, timestamp);
            if (intent == null) {
                continue;
            }
            acceptContinuous(intent, binding, timestamp);
        }
    }

    private Intent analogIntent(Binding binding, InputSnapshot curr, long timestamp) {
        if (binding.intentType() == IntentType.VECTOR2 && binding.vectorX() != null) {
            double x = binding.transformX().apply(curr.analog(binding.vectorX()));
            double y = binding.transformY().apply(curr.analog(binding.vectorY()));
            return intentBuilder(binding, timestamp)
                    .value(IntentValue.vector2(x, y))
                    .build();
        }
        if (binding.intentType() == IntentType.ANALOG && binding.analogSource() != null) {
            double value = binding.transform().apply(curr.analog(binding.analogSource()));
            return intentBuilder(binding, timestamp)
                    .value(IntentValue.analog(value))
                    .build();
        }
        if (binding.intentType() == IntentType.BOOLEAN && binding.analogSource() != null) {
            boolean pressed = binding.transform().asDigital(curr.analog(binding.analogSource()));
            return intentBuilder(binding, timestamp)
                    .value(IntentValue.bool(pressed))
                    .build();
        }
        return null;
    }

    private void evaluateDigital(long timestamp) {
        candidates.clear();
        List<Binding> bindings = profile.bindings();
        for (int i = 0; i < bindings.size(); i++) {
            Binding binding = bindings.get(i);
            if (binding.layerAction() || binding.intentId() == null) {
                continue;
            }
            if (binding.event() == BindingEvent.ANALOG) {
                continue;
            }
            if (!layerMatches(binding)) {
                continue;
            }
            InputSnapshot curr = current.get(binding.role());
            InputSnapshot prev = previous.get(binding.role());
            if (curr == null) {
                continue;
            }
            if (EdgeDetector.firing(binding.event(), binding.trigger(), prev, curr)) {
                candidates.add(binding);
            }
        }
        for (int i = 0; i < candidates.size(); i++) {
            Binding binding = candidates.get(i);
            if (suppressed(binding, candidates)) {
                emit(ShiftEvent.builder(ShiftEventType.CONFLICT, ShiftEventLevel.DEBUG)
                        .sequence(sequence)
                        .timestampMillis(timestamp)
                        .field("binding", binding.id())
                        .field("reason", "suppressed-by-more-specific")
                        .build());
                continue;
            }
            Intent intent = digitalIntent(binding, timestamp);
            if (binding.event() == BindingEvent.WHILE_HELD) {
                acceptContinuous(intent, binding, timestamp);
            } else {
                emitBindingMatch(binding, timestamp);
                frame.addEvent(intent);
                emitIntent(intent);
            }
        }
    }

    private boolean suppressed(Binding candidate, List<Binding> firing) {
        if (candidate.allowOverlap()) {
            return false;
        }
        for (int i = 0; i < firing.size(); i++) {
            Binding other = firing.get(i);
            if (other == candidate || other.allowOverlap()) {
                continue;
            }
            if (!other.role().equals(candidate.role()) || !other.layer().equals(candidate.layer())) {
                continue;
            }
            if (EdgeDetector.suppresses(other.trigger(), candidate.trigger())) {
                return true;
            }
        }
        return false;
    }

    private Intent digitalIntent(Binding binding, long timestamp) {
        IntentValue value;
        if (binding.intentType() == IntentType.BOOLEAN) {
            value = IntentValue.TRUE;
        } else if (binding.intentType() == IntentType.EVENT) {
            value = IntentValue.EVENT;
        } else {
            value = IntentValue.EVENT;
        }
        return intentBuilder(binding, timestamp).value(value).build();
    }

    private Intent.Builder intentBuilder(Binding binding, long timestamp) {
        return Intent.builder(binding.intentId(), binding.intentType())
                .source(binding.role())
                .layer(activeLayers.get(binding.role()))
                .priority(binding.priority() == null ? IntentPriority.DEFAULT : binding.priority())
                .resource(binding.resource())
                .bindingId(binding.id())
                .timestampMillis(timestamp)
                .sequence(sequence);
    }

    private void acceptContinuous(Intent intent, Binding binding, long timestamp) {
        Intent existing = frame.continuousNow(intent.id().value());
        if (existing != null) {
            int cmp = intent.priority().rank() - existing.priority().rank();
            if (cmp < 0) {
                return;
            }
            if (cmp == 0 && existing.bindingId().compareTo(binding.id()) <= 0) {
                emit(ShiftEvent.builder(ShiftEventType.CONFLICT, ShiftEventLevel.DEBUG)
                        .sequence(sequence)
                        .timestampMillis(timestamp)
                        .field("intent", intent.id().value())
                        .field("kept", existing.bindingId())
                        .field("rejected", binding.id())
                        .build());
                return;
            }
        }
        frame.putContinuous(intent);
        if (intent.type() != IntentType.ANALOG && intent.type() != IntentType.VECTOR2) {
            emitIntent(intent);
        } else {
            emit(ShiftEvent.builder(ShiftEventType.INTENT_EMITTED, ShiftEventLevel.TRACE)
                    .sequence(sequence)
                    .timestampMillis(timestamp)
                    .field("intent", intent.id().value())
                    .field("value", intent.value().toString())
                    .field("source", intent.source().id())
                    .build());
        }
        emitBindingMatch(binding, timestamp);
    }

    private boolean layerMatches(Binding binding) {
        if (!devices.containsKey(binding.role())) {
            return false;
        }
        String active = activeLayers.get(binding.role());
        if (active == null) {
            active = "";
        }
        if (binding.layer().isEmpty()) {
            return true;
        }
        return binding.layer().equals(active);
    }

    private void emitDigitalInputEvents(ControllerRole role, InputSnapshot prev, InputSnapshot curr) {
        for (Control control : Control.values()) {
            if (control.analog()) {
                continue;
            }
            boolean now = curr.digital(control);
            boolean then = prev != null && prev.digital(control);
            if (now == then) {
                continue;
            }
            emit(ShiftEvent.builder(ShiftEventType.INPUT, ShiftEventLevel.TRACE)
                    .sequence(sequence)
                    .timestampMillis(curr.timestampMillis())
                    .field("role", role.id())
                    .field("control", control.token())
                    .field("state", now ? "PRESSED" : "RELEASED")
                    .build());
        }
    }

    private void emitBindingMatch(Binding binding, long timestamp) {
        emit(ShiftEvent.builder(ShiftEventType.BINDING_MATCH, ShiftEventLevel.DEBUG)
                .sequence(sequence)
                .timestampMillis(timestamp)
                .field("profile", profile.id())
                .field("layer", binding.layer().isEmpty() ? activeLayer(binding.role()) : binding.layer())
                .field("binding", binding.id())
                .field("role", binding.role().id())
                .build());
    }

    private void emitIntent(Intent intent) {
        emit(ShiftEvent.builder(ShiftEventType.INTENT_EMITTED, ShiftEventLevel.DEBUG)
                .sequence(intent.sequence())
                .timestampMillis(intent.timestampMillis())
                .field("intent", intent.id().value())
                .field("source", intent.source().id())
                .field("layer", intent.layer())
                .field("priority", intent.priority().name())
                .field("value", intent.value().toString())
                .build());
    }

    private void emit(ShiftEvent event) {
        sink.onEvent(event);
    }

    private long firstTimestamp(long fallbackNow) {
        for (InputSnapshot snapshot : current.values()) {
            if (snapshot.timestampMillis() > 0L) {
                return snapshot.timestampMillis();
            }
        }
        return fallbackNow;
    }

    public static String embeddedFallbackJson() {
        InputStream stream = Shift.class.getResourceAsStream(EMBEDDED_FALLBACK_RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Missing embedded fallback profile " + EMBEDDED_FALLBACK_RESOURCE);
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            int read;
            while ((read = stream.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), Charset.forName("UTF-8"));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read embedded fallback profile", ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ignored) {
                // ignore
            }
        }
    }

    /**
     * Fluent construction of a SHIFT runtime. Profile JSON is validated here,
     * not during {@link Shift#update()}.
     */
    public static final class Builder {
        private final Map<ControllerRole, InputDevice> devices = new LinkedHashMap<ControllerRole, InputDevice>();
        private final IntentRegistry intents = new IntentRegistry();
        private final FeedbackRegistry feedback = new FeedbackRegistry();
        private String preferredJson;
        private Profile preferredProfile;
        private String fallbackJson;
        private boolean fallbackToEmbedded;
        private ShiftEventSink sink = ShiftEventSink.NOOP;
        private ShiftInputListener inputListener = ShiftInputListener.NOOP;
        private ShiftEventLevel minimumLevel = ShiftEventLevel.INFO;
        private FeedbackActuator actuator = FeedbackActuator.NOOP;
        private ShiftClock clock = SystemShiftClock.INSTANCE;

        public Builder addDevice(String role, InputDevice device) {
            return addDevice(ControllerRole.of(role), device);
        }

        public Builder addDevice(ControllerRole role, InputDevice device) {
            if (device == null) {
                throw new IllegalArgumentException("InputDevice is required for role " + role);
            }
            devices.put(role, device);
            return this;
        }

        public Builder registerIntents(IntentRegistry registry) {
            intents.registerAll(registry);
            return this;
        }

        public Builder registerIntent(String id, IntentType type) {
            intents.register(id, type);
            return this;
        }

        public Builder registerIntent(String id, IntentType type, String resource) {
            intents.register(id, type, resource);
            return this;
        }

        public Builder registerFeedback(String id) {
            feedback.register(id);
            return this;
        }

        public Builder loadProfile(String json) {
            this.preferredJson = json;
            this.preferredProfile = null;
            return this;
        }

        public Builder loadProfile(Profile profile) {
            if (profile == null) {
                throw new IllegalArgumentException("Profile is required");
            }
            this.preferredProfile = profile;
            this.preferredJson = null;
            return this;
        }

        public Builder fallbackProfile(String json) {
            this.fallbackJson = json;
            return this;
        }

        public Builder fallbackToEmbedded() {
            this.fallbackToEmbedded = true;
            return this;
        }

        public Builder eventSink(ShiftEventSink sink) {
            this.sink = sink == null ? ShiftEventSink.NOOP : sink;
            return this;
        }

        public Builder inputListener(ShiftInputListener listener) {
            this.inputListener = listener == null ? ShiftInputListener.NOOP : listener;
            return this;
        }

        public Builder minimumEventLevel(ShiftEventLevel level) {
            this.minimumLevel = level == null ? ShiftEventLevel.INFO : level;
            return this;
        }

        public Builder feedbackActuator(FeedbackActuator actuator) {
            this.actuator = actuator == null ? FeedbackActuator.NOOP : actuator;
            return this;
        }

        public Builder clock(ShiftClock clock) {
            this.clock = clock == null ? SystemShiftClock.INSTANCE : clock;
            return this;
        }

        public Shift build() {
            if (devices.isEmpty()) {
                throw new IllegalStateException("SHIFT requires at least one InputDevice");
            }
            ShiftEventSink loadSink = new FilteringEventSink(sink, minimumLevel);
            if (preferredProfile != null) {
                emitLoaded(loadSink, preferredProfile, false, "");
                return new Shift(this, preferredProfile, false, new ArrayList<ConfigError>());
            }
            if (preferredJson == null || preferredJson.trim().isEmpty()) {
                throw new IllegalStateException(
                        "SHIFT requires a profile. Call loadProfile(json) or loadProfile(profile).");
            }
            ProfileLoader loader = new ProfileLoader(intents, feedback);
            try {
                Profile profile = loader.loadJson(preferredJson);
                emitLoaded(loadSink, profile, false, "");
                return new Shift(this, profile, false, new ArrayList<ConfigError>());
            } catch (ConfigException ex) {
                String fallback = resolveFallback();
                emitRejected(loadSink, ex);
                if (fallback == null) {
                    throw ex;
                }
                try {
                    Profile profile = loader.loadJson(fallback);
                    emitLoaded(loadSink, profile, true, ConfigError.format(ex.errors()));
                    return new Shift(this, profile, true, ex.errors());
                } catch (ConfigException fallbackEx) {
                    throw new ConfigException(
                            "Preferred profile was invalid and the fallback profile also failed:\n"
                                    + ConfigError.format(ex.errors())
                                    + "\n--- fallback ---\n"
                                    + ConfigError.format(fallbackEx.errors()),
                            fallbackEx.errors());
                }
            }
        }

        private String resolveFallback() {
            if (fallbackJson != null && !fallbackJson.trim().isEmpty()) {
                return fallbackJson;
            }
            if (fallbackToEmbedded) {
                return embeddedFallbackJson();
            }
            return null;
        }

        private void emitLoaded(ShiftEventSink loadSink, Profile profile, boolean fallback, String reason) {
            ShiftEvent.Builder event = ShiftEvent.builder(ShiftEventType.PROFILE_LOADED, ShiftEventLevel.INFO)
                    .field("profile", profile.id())
                    .field("fallback", Boolean.toString(fallback))
                    .field("schemaVersion", Integer.toString(profile.schemaVersion()));
            if (!profile.person().isEmpty()) {
                event.field("person", profile.person()).field("set", profile.set());
            }
            if (fallback && reason != null && !reason.isEmpty()) {
                event.field("reason", reason);
            }
            loadSink.onEvent(event.build());
        }

        private void emitRejected(ShiftEventSink loadSink, ConfigException ex) {
            loadSink.onEvent(ShiftEvent.builder(ShiftEventType.PROFILE_REJECTED, ShiftEventLevel.ERROR)
                    .field("reason", ex.getMessage())
                    .build());
        }
    }
}
