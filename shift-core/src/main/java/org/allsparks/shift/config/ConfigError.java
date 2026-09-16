package org.allsparks.shift.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Structured configuration error with a JSON-pointer-style path.
 */
public final class ConfigError {
    private final String path;
    private final String message;

    public ConfigError(String path, String message) {
        this.path = path == null ? "" : path;
        this.message = message;
    }

    public String path() {
        return path;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        if (path.isEmpty()) {
            return message;
        }
        return path + ":\n" + message;
    }

    public static String format(List<ConfigError> errors) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(errors.get(i).toString());
        }
        return builder.toString();
    }

    public static List<ConfigError> copy(List<ConfigError> errors) {
        return Collections.unmodifiableList(new ArrayList<ConfigError>(errors));
    }
}
