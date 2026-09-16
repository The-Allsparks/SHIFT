package org.allsparks.shift.intent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Application-owned catalog of legal semantic intents. Profile validation
 * rejects unknown ids, type mismatches, and obvious typos (with suggestions).
 */
public final class IntentRegistry {
    private final Map<String, RegisteredIntent> byId = new LinkedHashMap<String, RegisteredIntent>();

    public IntentRegistry register(String id, IntentType type) {
        return register(id, type, IntentPriority.DEFAULT, null);
    }

    public IntentRegistry register(String id, IntentType type, IntentPriority priority) {
        return register(id, type, priority, null);
    }

    public IntentRegistry register(String id, IntentType type, String resource) {
        return register(id, type, IntentPriority.DEFAULT, resource);
    }

    public IntentRegistry register(IntentId id, IntentType type) {
        return register(id.value(), type, IntentPriority.DEFAULT, null);
    }

    public IntentRegistry register(String id, IntentType type, IntentPriority priority, String resource) {
        IntentId intentId = IntentId.of(id);
        if (type == null) {
            throw new IllegalArgumentException("Intent type is required for " + id);
        }
        String key = intentId.value();
        if (byId.containsKey(key)) {
            RegisteredIntent existing = byId.get(key);
            if (existing.type() != type) {
                throw new IllegalArgumentException(
                        "Intent '" + key + "' already registered as " + existing.type() + ", not " + type);
            }
            return this;
        }
        byId.put(
                key,
                new RegisteredIntent(
                        intentId,
                        type,
                        priority == null ? IntentPriority.DEFAULT : priority,
                        LogicalResource.of(resource)));
        return this;
    }

    public IntentRegistry registerAll(IntentRegistry other) {
        for (RegisteredIntent registered : other.all()) {
            register(
                    registered.id().value(),
                    registered.type(),
                    registered.priority(),
                    registered.resource() == null ? null : registered.resource().id());
        }
        return this;
    }

    public boolean contains(String id) {
        return byId.containsKey(id);
    }

    public boolean contains(IntentId id) {
        return contains(id.value());
    }

    public RegisteredIntent require(String id) {
        RegisteredIntent found = byId.get(id);
        if (found == null) {
            throw new IllegalArgumentException("Unknown intent '" + id + "'" + suggestionSuffix(id));
        }
        return found;
    }

    public RegisteredIntent get(String id) {
        return byId.get(id);
    }

    public List<RegisteredIntent> all() {
        return Collections.unmodifiableList(new ArrayList<RegisteredIntent>(byId.values()));
    }

    public List<String> ids() {
        return Collections.unmodifiableList(new ArrayList<String>(byId.keySet()));
    }

    public String suggestionSuffix(String unknown) {
        String suggestion = closest(unknown);
        if (suggestion == null) {
            return "";
        }
        return ". Did you mean '" + suggestion + "'?";
    }

    public String closest(String unknown) {
        if (unknown == null || byId.isEmpty()) {
            return null;
        }
        String needle = unknown.toLowerCase(Locale.ROOT);
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (String candidate : byId.keySet()) {
            int distance = levenshtein(needle, candidate.toLowerCase(Locale.ROOT));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        if (best != null && bestDistance <= Math.max(2, needle.length() / 3)) {
            return best;
        }
        return null;
    }

    public static int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= b.length(); j++) {
                int cost = ca == b.charAt(j - 1) ? 0 : 1;
                int deletion = prev[j] + 1;
                int insertion = curr[j - 1] + 1;
                int substitution = prev[j - 1] + cost;
                curr[j] = Math.min(deletion, Math.min(insertion, substitution));
            }
            int[] swap = prev;
            prev = curr;
            curr = swap;
        }
        return prev[b.length()];
    }
}
