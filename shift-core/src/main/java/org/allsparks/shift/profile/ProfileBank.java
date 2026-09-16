package org.allsparks.shift.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.allsparks.shift.binding.Binding;
import org.allsparks.shift.config.ConfigException;
import org.allsparks.shift.feedback.FeedbackEffect;
import org.allsparks.shift.feedback.FeedbackId;
import org.allsparks.shift.feedback.FeedbackRegistry;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.intent.IntentRegistry;

/**
 * Validated catalog of named profiles. JSON is parsed at bank construction, never
 * on the robot loop. Use {@link #compose(String, String)} to mix a driver map
 * with an operator map, then {@link org.allsparks.shift.Shift#activate(Profile)}
 * or {@link org.allsparks.shift.Shift.Builder#loadProfile(Profile)}.
 *
 * <p>There is no coded cap on how many profiles a bank may hold. Group variants
 * with {@code person} / {@code set} or an id such as {@code garrett/offense}.
 */
public final class ProfileBank {
    private final Map<String, Profile> byId;
    private final Map<String, List<String>> setsByPerson;

    private ProfileBank(Map<String, Profile> byId, Map<String, List<String>> setsByPerson) {
        this.byId = byId;
        this.setsByPerson = setsByPerson;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(IntentRegistry intents) {
        return new Builder().registerIntents(intents);
    }

    public Profile get(String id) {
        if (id == null) {
            return null;
        }
        Profile found = byId.get(id);
        if (found != null) {
            return found;
        }
        return byId.get(ProfileId.of(id, "", "").id());
    }

    public Profile require(String id) {
        Profile found = get(id);
        if (found == null) {
            throw new ConfigException("profiles." + id, "Unknown profile '" + id + "'");
        }
        return found;
    }

    public Profile require(String person, String set) {
        return require(ProfileId.qualified(person, set));
    }

    public List<String> ids() {
        return Collections.unmodifiableList(new ArrayList<String>(byId.keySet()));
    }

    public List<String> persons() {
        return Collections.unmodifiableList(new ArrayList<String>(setsByPerson.keySet()));
    }

    public List<String> sets(String person) {
        List<String> found =
                setsByPerson.get(person == null ? "" : person.trim().toLowerCase(Locale.ROOT));
        if (found == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(found);
    }

    public int size() {
        return byId.size();
    }

    /**
     * Builds a session profile: driver bindings from {@code driverProfileId},
     * operator (codriver) bindings from {@code operatorProfileId}.
     */
    public Profile compose(String driverProfileId, String operatorProfileId) {
        Profile driver = require(driverProfileId);
        Profile operator = require(operatorProfileId);
        if (!driver.controllers().containsKey(ControllerRole.DRIVER)) {
            throw new ConfigException(
                    "compose.driver",
                    "Profile '" + driver.id() + "' has no driver controller. Person-role files for gamepad 1 must "
                            + "declare controllers.driver.");
        }
        if (!operator.controllers().containsKey(ControllerRole.CODRIVER)) {
            throw new ConfigException(
                    "compose.operator",
                    "Profile '" + operator.id() + "' has no codriver controller. Person-role files for gamepad 2 must "
                            + "declare controllers.codriver.");
        }
        Profile.Builder session = Profile.builder()
                .schemaVersion(Profile.SCHEMA_VERSION)
                .id(driver.id() + "+" + operator.id())
                .name(display(driver) + " / " + display(operator))
                .description("Composed session: driver=" + driver.id() + " operator=" + operator.id())
                .controller(driver.controllers().get(ControllerRole.DRIVER))
                .controller(operator.controllers().get(ControllerRole.CODRIVER));
        copyRole(session, driver, ControllerRole.DRIVER);
        copyRole(session, operator, ControllerRole.CODRIVER);
        return session.build();
    }

    public Profile compose(String driverPerson, String driverSet, String operatorPerson, String operatorSet) {
        return compose(ProfileId.qualified(driverPerson, driverSet), ProfileId.qualified(operatorPerson, operatorSet));
    }

    private static void copyRole(Profile.Builder session, Profile source, ControllerRole role) {
        List<Binding> bindings = source.bindings();
        for (int i = 0; i < bindings.size(); i++) {
            Binding binding = bindings.get(i);
            if (role.equals(binding.role())) {
                session.binding(binding);
            }
        }
        for (Map.Entry<FeedbackId, FeedbackEffect> entry : source.feedback().entrySet()) {
            if (role.equals(entry.getValue().role())) {
                session.feedback(entry.getValue());
            }
        }
        for (Map.Entry<String, String> entry : source.settings().entrySet()) {
            session.setting(entry.getKey(), entry.getValue());
        }
        List<String> layers = source.knownLayers();
        for (int i = 0; i < layers.size(); i++) {
            session.knownLayer(layers.get(i));
        }
    }

    private static String display(Profile profile) {
        if (profile.name() != null && !profile.name().isEmpty()) {
            return profile.name();
        }
        return profile.id();
    }

    public static final class Builder {
        private final IntentRegistry intents = new IntentRegistry();
        private final FeedbackRegistry feedback = new FeedbackRegistry();
        private final List<String> jsonDocuments = new ArrayList<String>();
        private final List<Profile> compiled = new ArrayList<Profile>();

        public Builder registerIntents(IntentRegistry registry) {
            if (registry != null) {
                intents.registerAll(registry);
            }
            return this;
        }

        public Builder registerFeedback(String id) {
            feedback.register(id);
            return this;
        }

        public Builder addJson(String json) {
            if (json == null || json.trim().isEmpty()) {
                throw new ConfigException("profiles", "Profile JSON is empty");
            }
            jsonDocuments.add(json);
            return this;
        }

        public Builder addProfile(Profile profile) {
            if (profile == null) {
                throw new IllegalArgumentException("Profile is required");
            }
            compiled.add(profile);
            return this;
        }

        public ProfileBank build() {
            ProfileLoader loader = new ProfileLoader(intents, feedback);
            Map<String, Profile> byId = new LinkedHashMap<String, Profile>();
            ConfigException.Builder errors = ConfigException.builder();
            for (int i = 0; i < jsonDocuments.size(); i++) {
                try {
                    put(byId, loader.loadJson(jsonDocuments.get(i)), errors);
                } catch (ConfigException ex) {
                    errors.add("profiles[" + i + "]", ex.getMessage());
                }
            }
            for (int i = 0; i < compiled.size(); i++) {
                put(byId, compiled.get(i), errors);
            }
            errors.throwIfAny();
            if (byId.isEmpty()) {
                throw new ConfigException("profiles", "ProfileBank requires at least one profile");
            }
            Map<String, List<String>> setsByPerson = new LinkedHashMap<String, List<String>>();
            for (Profile profile : byId.values()) {
                if (!profile.person().isEmpty()) {
                    List<String> sets = setsByPerson.get(profile.person());
                    if (sets == null) {
                        sets = new ArrayList<String>();
                        setsByPerson.put(profile.person(), sets);
                    }
                    if (!sets.contains(profile.set())) {
                        sets.add(profile.set());
                    }
                }
            }
            return new ProfileBank(Collections.unmodifiableMap(byId), freeze(setsByPerson));
        }

        private static void put(Map<String, Profile> byId, Profile profile, ConfigException.Builder errors) {
            Profile previous = byId.get(profile.id());
            if (previous != null) {
                errors.add("profiles." + profile.id(), "Duplicate profile id '" + profile.id() + "'");
                return;
            }
            byId.put(profile.id(), profile);
        }

        private static Map<String, List<String>> freeze(Map<String, List<String>> source) {
            Map<String, List<String>> frozen = new LinkedHashMap<String, List<String>>();
            for (Map.Entry<String, List<String>> entry : source.entrySet()) {
                frozen.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<String>(entry.getValue())));
            }
            return Collections.unmodifiableMap(frozen);
        }
    }
}
