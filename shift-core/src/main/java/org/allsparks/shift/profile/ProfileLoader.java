package org.allsparks.shift.profile;

import org.allsparks.shift.config.ConfigException;
import org.allsparks.shift.feedback.FeedbackRegistry;
import org.allsparks.shift.intent.IntentRegistry;

/**
 * Loads and validates a versioned JSON profile. Parsing happens only during
 * initialization, never on the robot loop.
 */
public final class ProfileLoader {
    private final IntentRegistry intents;
    private final FeedbackRegistry feedback;

    public ProfileLoader(IntentRegistry intents) {
        this(intents, new FeedbackRegistry());
    }

    public ProfileLoader(IntentRegistry intents, FeedbackRegistry feedback) {
        if (intents == null) {
            throw new IllegalArgumentException("Intent registry is required");
        }
        this.intents = intents;
        this.feedback = feedback == null ? new FeedbackRegistry() : feedback;
    }

    public Profile loadJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new ConfigException("", "Profile JSON is empty");
        }
        try {
            return JsonProfileParser.parse(json, intents, feedback);
        } catch (ConfigException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ConfigException("", "Malformed JSON: " + ex.getMessage());
        }
    }
}
