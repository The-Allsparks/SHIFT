package org.allsparks.shift.feedback;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Optional catalog of legal semantic feedback ids. When empty, any id may be
 * mapped; when populated, profile feedback keys must be members.
 */
public final class FeedbackRegistry {
    private final Map<String, FeedbackId> byId = new LinkedHashMap<String, FeedbackId>();

    public FeedbackRegistry register(String id) {
        FeedbackId feedbackId = FeedbackId.of(id);
        byId.put(feedbackId.value(), feedbackId);
        return this;
    }

    public boolean isEmpty() {
        return byId.isEmpty();
    }

    public boolean contains(String id) {
        try {
            return byId.containsKey(FeedbackId.of(id).value());
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public Set<String> ids() {
        return Collections.unmodifiableSet(byId.keySet());
    }

    public String suggestionSuffix(String unknown) {
        if (unknown == null || byId.isEmpty()) {
            return "";
        }
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        String needle = FeedbackId.of(unknown).value();
        for (String candidate : byId.keySet()) {
            int distance = org.allsparks.shift.intent.IntentRegistry.levenshtein(needle, candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        if (best != null && bestDistance <= Math.max(2, needle.length() / 3)) {
            return ". Did you mean '" + best + "'?";
        }
        return "";
    }
}
