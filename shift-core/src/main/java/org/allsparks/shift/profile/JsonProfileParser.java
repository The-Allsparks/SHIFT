package org.allsparks.shift.profile;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.allsparks.shift.binding.Binding;
import org.allsparks.shift.config.ConfigException;
import org.allsparks.shift.config.Suggestions;
import org.allsparks.shift.feedback.FeedbackEffect;
import org.allsparks.shift.feedback.FeedbackId;
import org.allsparks.shift.feedback.FeedbackRegistry;
import org.allsparks.shift.feedback.LedCommand;
import org.allsparks.shift.feedback.RumbleCommand;
import org.allsparks.shift.feedback.RumblePattern;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.ControllerModel;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.intent.IntentPriority;
import org.allsparks.shift.intent.IntentRegistry;
import org.allsparks.shift.intent.IntentType;
import org.allsparks.shift.intent.RegisteredIntent;
import org.allsparks.shift.transform.AnalogTransform;
import org.allsparks.shift.trigger.BindingEvent;
import org.allsparks.shift.trigger.TriggerCondition;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Parses schemaVersion 1 JSON into a compiled {@link Profile}. Validation errors
 * include JSON paths and, where practical, "did you mean" suggestions.
 */
final class JsonProfileParser {
    private final IntentRegistry intents;
    private final FeedbackRegistry feedback;
    private final ConfigException.Builder errors = ConfigException.builder();
    private final Set<String> knownLayers = new LinkedHashSet<String>();
    private final Set<String> reachableLayers = new LinkedHashSet<String>();
    private int bindingCounter;

    private JsonProfileParser(IntentRegistry intents, FeedbackRegistry feedback) {
        this.intents = intents;
        this.feedback = feedback;
    }

    static Profile parse(String json, IntentRegistry intents, FeedbackRegistry feedback) {
        JSONObject root = new JSONObject(json);
        return new JsonProfileParser(intents, feedback).parseRoot(root);
    }

    private Profile parseRoot(JSONObject root) {
        if (!root.has("schemaVersion")) {
            errors.add("schemaVersion", "Every configuration MUST declare schemaVersion. Expected 1.");
        }
        int schemaVersion = root.optInt("schemaVersion", -1);
        if (schemaVersion != Profile.SCHEMA_VERSION && schemaVersion != -1) {
            errors.add(
                    "schemaVersion",
                    "Unsupported schemaVersion " + schemaVersion + ". This runtime understands version "
                            + Profile.SCHEMA_VERSION + " only.");
        }

        Profile.Builder profile = Profile.builder()
                .schemaVersion(schemaVersion <= 0 ? Profile.SCHEMA_VERSION : schemaVersion)
                .description(root.optString("description", ""));
        ProfileId identity =
                ProfileId.of(root.optString("id", "unnamed"), root.optString("person", ""), root.optString("set", ""));
        profile.id(identity.id()).person(identity.person()).set(identity.set());
        profile.name(root.optString("name", identity.id()));

        parseSettings(root.optJSONObject("settings"), profile);
        parseControllers(root.optJSONObject("controllers"), profile);
        parseLayers(root.optJSONObject("layers"), profile);
        parseBindings(root.optJSONArray("bindings"), "", null, profile);
        parseFeedback(root.optJSONObject("feedback"), profile);
        checkUnreachableLayers();
        detectConflicts(profile.build().bindings());

        errors.throwIfAny();
        return profile.build();
    }

    private void parseSettings(JSONObject settings, Profile.Builder profile) {
        if (settings == null) {
            return;
        }
        for (String key : settings.keySet()) {
            profile.setting(key, String.valueOf(settings.get(key)));
        }
    }

    private void parseControllers(JSONObject controllers, Profile.Builder profile) {
        if (controllers == null) {
            errors.add("controllers", "At least one controller role must be declared.");
            return;
        }
        if (controllers.length() == 0) {
            errors.add("controllers", "At least one controller role must be declared.");
            return;
        }
        for (String roleName : controllers.keySet()) {
            String path = "controllers." + roleName;
            JSONObject spec = controllers.optJSONObject(roleName);
            if (spec == null) {
                errors.add(path, "Controller assignment must be an object.");
                continue;
            }
            ControllerRole role = ControllerRole.of(roleName);
            int slot = spec.optInt("slot", 0);
            String defaultLayer = normalizeLayer(spec.optString("defaultLayer", ""));
            if (!defaultLayer.isEmpty()) {
                knownLayers.add(defaultLayer);
                reachableLayers.add(defaultLayer);
            }
            ControllerModel model = parseModel(path + ".model", spec.optString("model", ""));
            profile.controller(new ControllerAssignment(role, slot, defaultLayer, model));
        }
    }

    private ControllerModel parseModel(String path, String raw) {
        if (raw == null) {
            return null;
        }
        String name = raw.trim();
        if (name.isEmpty()) {
            return null;
        }
        ControllerModel model = ControllerModel.tryParse(name);
        if (model == null) {
            errors.add(path, "Unknown controller model '" + name + "'" + Suggestions.forModel(name));
        }
        return model;
    }

    private void parseLayers(JSONObject layers, Profile.Builder profile) {
        if (layers == null) {
            return;
        }
        for (String layerName : layers.keySet()) {
            String layer = normalizeLayer(layerName);
            knownLayers.add(layer);
            profile.knownLayer(layer);
            JSONObject spec = layers.optJSONObject(layerName);
            if (spec == null) {
                errors.add("layers." + layerName, "Layer must be an object.");
                continue;
            }
            ControllerRole role = null;
            if (spec.has("controller")) {
                role = ControllerRole.of(spec.optString("controller"));
            }
            parseBindings(spec.optJSONArray("bindings"), layer, role, profile);
        }
    }

    private void parseBindings(JSONArray bindings, String layer, ControllerRole defaultRole, Profile.Builder profile) {
        if (bindings == null) {
            return;
        }
        for (int i = 0; i < bindings.length(); i++) {
            String path = layer.isEmpty() ? "bindings[" + i + "]" : "layers." + layer + ".bindings[" + i + "]";
            JSONObject spec = bindings.optJSONObject(i);
            if (spec == null) {
                errors.add(path, "Binding must be an object.");
                continue;
            }
            Binding binding = parseBinding(spec, path, layer, defaultRole, profile);
            if (binding != null) {
                profile.binding(binding);
            }
        }
    }

    private Binding parseBinding(
            JSONObject spec, String path, String layer, ControllerRole defaultRole, Profile.Builder profile) {
        bindingCounter++;
        String id = spec.optString("id", "binding-" + bindingCounter);
        ControllerRole role = defaultRole;
        if (spec.has("controller") || spec.has("role")) {
            role = ControllerRole.of(spec.optString("controller", spec.optString("role")));
        }
        if (role == null) {
            role = ControllerRole.DRIVER;
        }

        String bindingLayer = layer;
        if (spec.has("layer")) {
            bindingLayer = normalizeLayer(spec.optString("layer"));
            knownLayers.add(bindingLayer);
            profile.knownLayer(bindingLayer);
        }

        Binding.Builder builder = Binding.builder()
                .id(id)
                .path(path)
                .role(role)
                .layer(bindingLayer)
                .declarationIndex(bindingCounter)
                .allowOverlap(spec.optBoolean("allowOverlap", false));

        if (spec.has("priority")) {
            builder.priority(IntentPriority.parse(spec.optString("priority")));
        }

        parseLayerAction(spec, path, builder);
        parseVectorOrSource(spec, path, builder);
        parseTrigger(spec, path, builder);

        if (spec.has("event")) {
            try {
                builder.event(BindingEvent.parse(spec.get("event").toString()));
            } catch (IllegalArgumentException ex) {
                errors.add(path + ".event", ex.getMessage());
            }
        }

        if (spec.has("intent")) {
            String intentName = spec.optString("intent");
            if (!intents.contains(intentName)) {
                errors.add(
                        path + ".intent", "Unknown intent '" + intentName + "'" + intents.suggestionSuffix(intentName));
            } else {
                RegisteredIntent registered = intents.require(intentName);
                builder.intentId(registered.id());
                builder.intentType(registered.type());
                if (!spec.has("priority")) {
                    builder.priority(registered.priority());
                }
                builder.resource(registered.resource());
                if (!spec.has("event")) {
                    builder.event(defaultEvent(registered.type()));
                }
            }
        } else if (!spec.has("setLayer") && !spec.has("cycleLayers")) {
            errors.add(path + ".intent", "Binding must declare intent, setLayer, or cycleLayers.");
        }

        if (spec.has("intent") && intents.contains(spec.optString("intent"))) {
            validateIntentEventCompatibility(spec, path, builder);
        }

        Binding binding = builder.build();
        if (binding.setLayer() != null) {
            knownLayers.add(binding.setLayer());
            reachableLayers.add(binding.setLayer());
            profile.knownLayer(binding.setLayer());
        }
        if (binding.cycleLayers() != null) {
            for (int i = 0; i < binding.cycleLayers().length; i++) {
                reachableLayers.add(binding.cycleLayers()[i]);
                profile.knownLayer(binding.cycleLayers()[i]);
            }
        }
        return binding;
    }

    private void parseLayerAction(JSONObject spec, String path, Binding.Builder builder) {
        if (spec.has("setLayer")) {
            builder.setLayer(normalizeLayer(spec.optString("setLayer")));
        }
        if (spec.has("cycleLayers")) {
            JSONArray array = spec.optJSONArray("cycleLayers");
            if (array == null || array.length() == 0) {
                errors.add(path + ".cycleLayers", "cycleLayers must be a non-empty array of layer names.");
            } else {
                String[] layers = new String[array.length()];
                for (int i = 0; i < array.length(); i++) {
                    layers[i] = normalizeLayer(array.optString(i));
                    knownLayers.add(layers[i]);
                }
                builder.cycleLayers(layers);
            }
        }
    }

    private void parseVectorOrSource(JSONObject spec, String path, Binding.Builder builder) {
        AnalogTransform transform = parseTransform(spec.optJSONObject("transform"), path + ".transform");
        builder.transform(transform);
        if (spec.has("source")) {
            Control control = parseControl(spec.optString("source"), path + ".source");
            if (control != null) {
                builder.analogSource(control);
                if (!spec.has("when")) {
                    builder.trigger(new TriggerCondition.ControlPressed(control, transform.threshold()));
                }
            }
        }
        if (spec.has("x") || spec.has("y")) {
            JSONObject xSpec = spec.optJSONObject("x");
            JSONObject ySpec = spec.optJSONObject("y");
            if (xSpec == null || ySpec == null) {
                errors.add(path, "VECTOR2 bindings require object fields x and y, each with a source.");
                return;
            }
            Control x = parseControl(xSpec.optString("source"), path + ".x.source");
            Control y = parseControl(ySpec.optString("source"), path + ".y.source");
            AnalogTransform tx = parseTransform(xSpec.optJSONObject("transform"), path + ".x.transform");
            AnalogTransform ty = parseTransform(ySpec.optJSONObject("transform"), path + ".y.transform");
            if (x != null && y != null) {
                builder.vector(x, y, tx, ty);
            }
        }
    }

    private void parseTrigger(JSONObject spec, String path, Binding.Builder builder) {
        if (spec.has("when")) {
            builder.trigger(parseCondition(spec.get("when"), path + ".when"));
        }
    }

    private TriggerCondition parseCondition(Object node, String path) {
        if (node instanceof String) {
            Control control = parseControl((String) node, path);
            if (control == null) {
                return TriggerCondition.AlwaysTrue.INSTANCE;
            }
            return new TriggerCondition.ControlPressed(control, 0.5);
        }
        if (!(node instanceof JSONObject)) {
            errors.add(path, "Trigger condition must be a control name or object.");
            return TriggerCondition.AlwaysTrue.INSTANCE;
        }
        JSONObject object = (JSONObject) node;
        if (object.has("all")) {
            return new TriggerCondition.All(parseConditionArray(object.getJSONArray("all"), path + ".all"));
        }
        if (object.has("any")) {
            return new TriggerCondition.Any(parseConditionArray(object.getJSONArray("any"), path + ".any"));
        }
        if (object.has("not")) {
            return new TriggerCondition.Not(parseCondition(object.get("not"), path + ".not"));
        }
        TriggerCondition.AnalogCompare.Op op = null;
        String key = null;
        if (object.has("gt")) {
            op = TriggerCondition.AnalogCompare.Op.GT;
            key = "gt";
        } else if (object.has("gte")) {
            op = TriggerCondition.AnalogCompare.Op.GTE;
            key = "gte";
        } else if (object.has("lt")) {
            op = TriggerCondition.AnalogCompare.Op.LT;
            key = "lt";
        } else if (object.has("lte")) {
            op = TriggerCondition.AnalogCompare.Op.LTE;
            key = "lte";
        }
        if (op != null) {
            JSONObject cmp = object.optJSONObject(key);
            if (cmp == null) {
                errors.add(path + "." + key, "Analog comparison must be an object with source and value.");
                return TriggerCondition.AlwaysTrue.INSTANCE;
            }
            Control control = parseControl(cmp.optString("source"), path + "." + key + ".source");
            if (control == null) {
                return TriggerCondition.AlwaysTrue.INSTANCE;
            }
            if (!cmp.has("value")) {
                errors.add(path + "." + key + ".value", "Analog comparison requires a numeric value.");
                return TriggerCondition.AlwaysTrue.INSTANCE;
            }
            return new TriggerCondition.AnalogCompare(control, op, cmp.optDouble("value"));
        }
        if (object.has("source")) {
            Control control = parseControl(object.optString("source"), path + ".source");
            if (control == null) {
                return TriggerCondition.AlwaysTrue.INSTANCE;
            }
            double threshold = 0.5;
            if (object.has("threshold")) {
                threshold = object.optDouble("threshold");
            }
            return new TriggerCondition.ControlPressed(control, threshold);
        }
        errors.add(path, "Unknown trigger structure. Expected all, any, not, gt/gte/lt/lte, or a control name.");
        return TriggerCondition.AlwaysTrue.INSTANCE;
    }

    private TriggerCondition[] parseConditionArray(JSONArray array, String path) {
        if (array == null || array.length() == 0) {
            errors.add(path, "Composite trigger must contain at least one condition.");
            return new TriggerCondition[] {TriggerCondition.AlwaysTrue.INSTANCE};
        }
        TriggerCondition[] children = new TriggerCondition[array.length()];
        for (int i = 0; i < array.length(); i++) {
            children[i] = parseCondition(array.get(i), path + "[" + i + "]");
        }
        return children;
    }

    private Control parseControl(String raw, String path) {
        if (raw == null || raw.trim().isEmpty()) {
            errors.add(path, "Control name is required.");
            return null;
        }
        Control control = Control.tryParse(raw);
        if (control == null) {
            errors.add(path, "Unknown control '" + raw + "'" + Suggestions.forControl(raw));
            return null;
        }
        return control;
    }

    private AnalogTransform parseTransform(JSONObject spec, String path) {
        if (spec == null) {
            return AnalogTransform.IDENTITY;
        }
        AnalogTransform.Builder builder = AnalogTransform.builder();
        Iterator<String> keys = spec.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if ("deadband".equals(key)) {
                builder.deadband(spec.optDouble(key));
            } else if ("invert".equals(key)) {
                builder.invert(spec.optBoolean(key));
            } else if ("scale".equals(key)) {
                builder.scale(spec.optDouble(key));
            } else if ("exponent".equals(key)) {
                builder.exponent(spec.optDouble(key));
            } else if ("min".equals(key)) {
                builder.min(spec.optDouble(key));
            } else if ("max".equals(key)) {
                builder.max(spec.optDouble(key));
            } else if ("threshold".equals(key)) {
                builder.threshold(spec.optDouble(key));
            } else if ("slew".equals(key) || "calibration".equals(key) || "asymmetric".equals(key)) {
                errors.add(
                        path + "." + key,
                        "Transform '" + key
                                + "' is reserved for a later SHIFT release and is not available in schemaVersion 1.");
            } else {
                errors.add(path + "." + key, "Unknown transform parameter '" + key + "'.");
            }
        }
        try {
            return builder.build();
        } catch (IllegalArgumentException ex) {
            errors.add(path, "Invalid transform: " + ex.getMessage());
            return AnalogTransform.IDENTITY;
        }
    }

    private void parseFeedback(JSONObject feedbackJson, Profile.Builder profile) {
        if (feedbackJson == null) {
            return;
        }
        for (String key : feedbackJson.keySet()) {
            String path = "feedback." + key;
            FeedbackId id;
            try {
                id = FeedbackId.of(key);
            } catch (IllegalArgumentException ex) {
                errors.add(path, ex.getMessage());
                continue;
            }
            if (!feedback.isEmpty() && !feedback.contains(key)) {
                errors.add(path, "Unknown feedback '" + key + "'" + feedback.suggestionSuffix(key));
                continue;
            }
            JSONObject spec = feedbackJson.optJSONObject(key);
            if (spec == null) {
                errors.add(path, "Feedback mapping must be an object.");
                continue;
            }
            ControllerRole role = ControllerRole.DRIVER;
            if (spec.has("role") || spec.has("controller")) {
                role = ControllerRole.of(spec.optString("role", spec.optString("controller")));
            }
            RumbleCommand rumble = null;
            if (spec.has("rumble")) {
                rumble = parseRumble(spec.get("rumble"), path + ".rumble", role);
            }
            LedCommand led = null;
            if (spec.has("led")) {
                led = parseLed(spec.optJSONObject("led"), path + ".led", role);
            }
            if (rumble == null && led == null) {
                errors.add(path, "Feedback mapping must declare rumble and/or led.");
                continue;
            }
            profile.feedback(new FeedbackEffect(id, role, rumble, led));
        }
    }

    private RumbleCommand parseRumble(Object node, String path, ControllerRole role) {
        if (node instanceof String) {
            try {
                return RumbleCommand.of(role, RumblePattern.parse((String) node));
            } catch (IllegalArgumentException ex) {
                errors.add(path, ex.getMessage());
                return null;
            }
        }
        if (node instanceof JSONObject) {
            JSONObject spec = (JSONObject) node;
            RumblePattern pattern = RumblePattern.SHORT;
            if (spec.has("pattern")) {
                try {
                    pattern = RumblePattern.parse(spec.optString("pattern"));
                } catch (IllegalArgumentException ex) {
                    errors.add(path + ".pattern", ex.getMessage());
                    return null;
                }
            }
            RumbleCommand base = RumbleCommand.of(role, pattern);
            double large = spec.optDouble("large", spec.optDouble("rumble1", base.large()));
            double small = spec.optDouble("small", spec.optDouble("rumble2", base.small()));
            int duration = spec.optInt("durationMs", base.durationMs());
            return new RumbleCommand(role, pattern, large, small, duration);
        }
        errors.add(path, "Rumble must be a pattern name or object.");
        return null;
    }

    private LedCommand parseLed(JSONObject spec, String path, ControllerRole role) {
        if (spec == null) {
            errors.add(path, "LED mapping must be an object with r, g, b.");
            return null;
        }
        return new LedCommand(
                role,
                spec.optDouble("r", spec.optDouble("red", 0.0)),
                spec.optDouble("g", spec.optDouble("green", 0.0)),
                spec.optDouble("b", spec.optDouble("blue", 0.0)),
                spec.optInt("durationMs", 200));
    }

    private void validateIntentEventCompatibility(JSONObject spec, String path, Binding.Builder builder) {
        Binding probe = builder.build();
        if (probe.intentId() == null) {
            return;
        }
        IntentType type = probe.intentType();
        BindingEvent event = probe.event();
        if (type == IntentType.EVENT && event == BindingEvent.ANALOG) {
            errors.add(path, "Intent '" + probe.intentId() + "' is EVENT and cannot use ANALOG bindings.");
        }
        if ((type == IntentType.ANALOG || type == IntentType.VECTOR2) && event.discrete()) {
            errors.add(
                    path,
                    "Intent '" + probe.intentId() + "' is " + type + " and cannot use " + event + " (use ANALOG).");
        }
        if (type == IntentType.VECTOR2 && probe.vectorX() == null) {
            errors.add(path, "VECTOR2 intent '" + probe.intentId() + "' requires x and y sources.");
        }
        if (type == IntentType.ANALOG && probe.analogSource() == null && probe.vectorX() == null) {
            errors.add(path, "ANALOG intent '" + probe.intentId() + "' requires a source axis.");
        }
        if (type == IntentType.BOOLEAN && event == BindingEvent.ANALOG && probe.analogSource() == null) {
            errors.add(path, "BOOLEAN intent '" + probe.intentId() + "' cannot use ANALOG without a source.");
        }
        if (event == BindingEvent.WHILE_HELD && type == IntentType.EVENT) {
            errors.add(
                    path,
                    "Intent '" + probe.intentId()
                            + "' is EVENT; WHILE_HELD requires a BOOLEAN intent so consumers can read frame.held().");
        }
    }

    private BindingEvent defaultEvent(IntentType type) {
        switch (type) {
            case ANALOG:
            case VECTOR2:
                return BindingEvent.ANALOG;
            case BOOLEAN:
                return BindingEvent.WHILE_HELD;
            case EVENT:
            default:
                return BindingEvent.ON_PRESS;
        }
    }

    private void checkUnreachableLayers() {
        for (String layer : knownLayers) {
            if (!reachableLayers.contains(layer)) {
                errors.add(
                        "layers." + layer,
                        "Layer '" + layer
                                + "' is unreachable: it is not a default layer and no binding sets or cycles to it.");
            }
        }
    }

    private void detectConflicts(List<Binding> bindings) {
        for (int i = 0; i < bindings.size(); i++) {
            Binding a = bindings.get(i);
            if (a.layerAction() || a.allowOverlap()) {
                continue;
            }
            for (int j = i + 1; j < bindings.size(); j++) {
                Binding b = bindings.get(j);
                if (b.layerAction() || b.allowOverlap()) {
                    continue;
                }
                if (!a.role().equals(b.role())) {
                    continue;
                }
                if (!a.layer().equals(b.layer())) {
                    continue;
                }
                if (a.event() != b.event()) {
                    continue;
                }
                if (!a.requiredControls().equals(b.requiredControls())) {
                    continue;
                }
                if (a.trigger().analogPredicateCount() != b.trigger().analogPredicateCount()) {
                    continue;
                }
                if (a.intentId() != null && a.intentId().equals(b.intentId())) {
                    errors.add(
                            b.path(),
                            "Duplicate binding of intent '" + b.intentId() + "' on layer '" + b.layer()
                                    + "' with the same trigger as " + a.path()
                                    + ". JSON order is not used to resolve this; remove the duplicate.");
                } else if (a.intentId() != null && b.intentId() != null) {
                    errors.add(
                            b.path(),
                            "Conflicting bindings on layer '" + b.layer() + "': " + a.path() + " emits '"
                                    + a.intentId() + "' and this binding emits '" + b.intentId()
                                    + "' for the same controls and event. Make one more specific, set allowOverlap, or remove one. JSON order does not decide the winner.");
                }
            }
        }
    }

    private static String normalizeLayer(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }
}
