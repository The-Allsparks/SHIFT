package org.allsparks.shift.intent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.allsparks.shift.input.ControllerRole;

/**
 * Intents produced by one {@code shift.update()} call.
 *
 * <p>Continuous values (analog, vector2, while-held booleans) are queried by id.
 * Discrete {@link IntentType#EVENT} values are in {@link #events()}.
 *
 * <p>The returned frame is reused on the next {@code update()}. Copy values you
 * need to retain across loops.
 */
public final class IntentFrame {
    private long sequence;
    private long timestampMillis;
    private String profileId = "";
    private boolean fallback;
    private final Map<String, Intent> continuous = new LinkedHashMap<String, Intent>();
    private final List<Intent> events = new ArrayList<Intent>();
    private final Map<String, String> layers = new LinkedHashMap<String, String>();
    private List<Intent> eventsView = Collections.emptyList();

    public void begin(long sequence, long timestampMillis, String profileId, boolean fallback) {
        this.sequence = sequence;
        this.timestampMillis = timestampMillis;
        this.profileId = profileId;
        this.fallback = fallback;
        continuous.clear();
        events.clear();
        eventsView = Collections.emptyList();
        layers.clear();
    }

    public void putContinuous(Intent intent) {
        continuous.put(intent.id().value(), intent);
    }

    public Intent continuousNow(String id) {
        return continuous.get(id);
    }

    public void addEvent(Intent intent) {
        events.add(intent);
        eventsView = Collections.emptyList();
    }

    public void setLayer(ControllerRole role, String layer) {
        layers.put(role.id(), layer);
    }

    public void freeze() {
        eventsView = Collections.unmodifiableList(new ArrayList<Intent>(events));
    }

    public long sequence() {
        return sequence;
    }

    public long timestampMillis() {
        return timestampMillis;
    }

    public String profileId() {
        return profileId;
    }

    public boolean fallback() {
        return fallback;
    }

    public Intent continuous(String intentId) {
        return continuous.get(intentId);
    }

    public double analog(String intentId) {
        return analog(intentId, 0.0);
    }

    public double analog(String intentId, double defaultValue) {
        Intent intent = continuous.get(intentId);
        if (intent == null || intent.type() != IntentType.ANALOG) {
            return defaultValue;
        }
        return intent.value().asAnalog();
    }

    public Vector2 vector2(String intentId) {
        Intent intent = continuous.get(intentId);
        if (intent == null || intent.type() != IntentType.VECTOR2) {
            return Vector2.ZERO;
        }
        return new Vector2(intent.value().x(), intent.value().y());
    }

    public boolean held(String intentId) {
        Intent intent = continuous.get(intentId);
        if (intent == null) {
            return false;
        }
        if (intent.type() == IntentType.BOOLEAN) {
            return intent.value().asBoolean();
        }
        return false;
    }

    public boolean emitted(String intentId) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).id().value().equals(intentId)) {
                return true;
            }
        }
        return false;
    }

    public List<Intent> events() {
        return eventsView;
    }

    public String layer(ControllerRole role) {
        String found = layers.get(role.id());
        return found == null ? "" : found;
    }

    public String layer(String role) {
        String found = layers.get(ControllerRole.of(role).id());
        return found == null ? "" : found;
    }

    public Map<String, String> layers() {
        return Collections.unmodifiableMap(layers);
    }

    public Map<String, Intent> continuousSnapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, Intent>(continuous));
    }
}
