package org.allsparks.shift.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thrown when a profile cannot be loaded or validated. Contains every problem
 * found during the pass, not just the first.
 */
public final class ConfigException extends RuntimeException {
    private final List<ConfigError> errors;

    public ConfigException(String message, List<ConfigError> errors) {
        super(message);
        this.errors = ConfigError.copy(errors);
    }

    public ConfigException(List<ConfigError> errors) {
        this(ConfigError.format(errors), errors);
    }

    public ConfigException(String path, String message) {
        this(Collections.singletonList(new ConfigError(path, message)));
    }

    public List<ConfigError> errors() {
        return errors;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<ConfigError> errors = new ArrayList<ConfigError>();

        public Builder add(String path, String message) {
            errors.add(new ConfigError(path, message));
            return this;
        }

        public Builder add(ConfigError error) {
            errors.add(error);
            return this;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public List<ConfigError> errors() {
            return ConfigError.copy(errors);
        }

        public void throwIfAny() {
            if (!errors.isEmpty()) {
                throw new ConfigException(errors);
            }
        }
    }
}
