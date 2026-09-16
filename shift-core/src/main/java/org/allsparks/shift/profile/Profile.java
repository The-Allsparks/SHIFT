package org.allsparks.shift.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.allsparks.shift.binding.Binding;
import org.allsparks.shift.feedback.FeedbackEffect;
import org.allsparks.shift.feedback.FeedbackId;
import org.allsparks.shift.input.ControllerRole;

/**
 * Compiled operator profile. JSON is parsed and validated once at load; the
 * robot loop only evaluates already-compiled bindings.
 */
public final class Profile {
    public static final int SCHEMA_VERSION = 1;

    private final int schemaVersion;
    private final String id;
    private final String name;
    private final String description;
    private final String person;
    private final String set;
    private final Map<ControllerRole, ControllerAssignment> controllers;
    private final List<Binding> bindings;
    private final Map<FeedbackId, FeedbackEffect> feedback;
    private final Map<String, String> settings;
    private final Map<String, String> layersByRoleDefault;
    private final List<String> knownLayers;

    Profile(Builder builder) {
        this.schemaVersion = builder.schemaVersion;
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.person = builder.person;
        this.set = builder.set;
        this.controllers = Collections.unmodifiableMap(
                new LinkedHashMap<ControllerRole, ControllerAssignment>(builder.controllers));
        this.bindings = Collections.unmodifiableList(new ArrayList<Binding>(builder.bindings));
        this.feedback = Collections.unmodifiableMap(new LinkedHashMap<FeedbackId, FeedbackEffect>(builder.feedback));
        this.settings = Collections.unmodifiableMap(new LinkedHashMap<String, String>(builder.settings));
        this.layersByRoleDefault = Collections.unmodifiableMap(new LinkedHashMap<String, String>(builder.defaults));
        this.knownLayers = Collections.unmodifiableList(new ArrayList<String>(builder.knownLayers));
    }

    public int schemaVersion() {
        return schemaVersion;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String person() {
        return person;
    }

    /**
     * Named variant inside a person folder such as {@code general} or {@code offense}.
     */
    public String set() {
        return set;
    }

    public Map<ControllerRole, ControllerAssignment> controllers() {
        return controllers;
    }

    public List<Binding> bindings() {
        return bindings;
    }

    public Map<FeedbackId, FeedbackEffect> feedback() {
        return feedback;
    }

    public Map<String, String> settings() {
        return settings;
    }

    public String defaultLayer(ControllerRole role) {
        String found = layersByRoleDefault.get(role.id());
        return found == null ? "" : found;
    }

    public List<String> knownLayers() {
        return knownLayers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int schemaVersion = SCHEMA_VERSION;
        private String id = "unnamed";
        private String name = "";
        private String description = "";
        private String person = "";
        private String set = "";
        private final Map<ControllerRole, ControllerAssignment> controllers =
                new LinkedHashMap<ControllerRole, ControllerAssignment>();
        private final List<Binding> bindings = new ArrayList<Binding>();
        private final Map<FeedbackId, FeedbackEffect> feedback = new LinkedHashMap<FeedbackId, FeedbackEffect>();
        private final Map<String, String> settings = new LinkedHashMap<String, String>();
        private final Map<String, String> defaults = new LinkedHashMap<String, String>();
        private final List<String> knownLayers = new ArrayList<String>();

        public Builder schemaVersion(int schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }

        public Builder id(String id) {
            this.id = id == null ? "unnamed" : id;
            return this;
        }

        public Builder name(String name) {
            this.name = name == null ? "" : name;
            return this;
        }

        public Builder description(String description) {
            this.description = description == null ? "" : description;
            return this;
        }

        public Builder person(String person) {
            this.person = person == null ? "" : person;
            return this;
        }

        public Builder set(String set) {
            this.set = set == null ? "" : set;
            return this;
        }

        public Builder controller(ControllerAssignment assignment) {
            controllers.put(assignment.role(), assignment);
            defaults.put(assignment.role().id(), assignment.defaultLayer());
            return this;
        }

        public Builder binding(Binding binding) {
            bindings.add(binding);
            return this;
        }

        public Builder feedback(FeedbackEffect effect) {
            feedback.put(effect.id(), effect);
            return this;
        }

        public Builder setting(String key, String value) {
            settings.put(key, value);
            return this;
        }

        public Builder knownLayer(String layer) {
            if (layer != null && !layer.isEmpty() && !knownLayers.contains(layer)) {
                knownLayers.add(layer);
            }
            return this;
        }

        public Profile build() {
            return new Profile(this);
        }
    }
}
