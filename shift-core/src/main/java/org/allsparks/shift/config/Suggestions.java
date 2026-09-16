package org.allsparks.shift.config;

import java.util.Locale;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.ControllerModel;
import org.allsparks.shift.intent.IntentId;

/**
 * Suggestion helpers for typo-prone configuration tokens.
 */
public final class Suggestions {
    private Suggestions() {}

    public static String forControl(String unknown) {
        if (unknown == null) {
            return "";
        }
        String needle = Control.normalize(unknown);
        Control best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Control control : Control.values()) {
            int distance =
                    org.allsparks.shift.intent.IntentRegistry.levenshtein(needle, Control.normalize(control.name()));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = control;
            }
        }
        // Also consider well-known aliases by parsing attempts already failed.
        if (best != null && bestDistance <= Math.max(2, needle.length() / 3)) {
            return ". Did you mean '" + best.token() + "'?";
        }
        return "";
    }

    public static String forModel(String unknown) {
        if (unknown == null) {
            return "";
        }
        String needle = Control.normalize(unknown);
        ControllerModel best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (ControllerModel model : ControllerModel.values()) {
            int distance =
                    org.allsparks.shift.intent.IntentRegistry.levenshtein(needle, Control.normalize(model.token()));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = model;
            }
        }
        if (best != null && bestDistance <= Math.max(2, needle.length() / 3)) {
            return ". Did you mean '" + best.token() + "'?";
        }
        return "";
    }

    public static String forIntent(org.allsparks.shift.intent.IntentRegistry registry, String unknown) {
        return registry.suggestionSuffix(unknown);
    }

    public static boolean looksLikeIntentId(String value) {
        try {
            IntentId.of(value);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public static String normalizeToken(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
