package org.allsparks.shift.profile;

import java.util.Locale;

/**
 * Person + set identity for a profile, e.g. {@code studenta/offense}.
 *
 * <p>{@code id} is the unique bank key. When {@code person} and {@code set} are
 * present (from JSON fields or an id of the form {@code person/set}), the
 * canonical id is {@code person/set}. Existing single-id profiles such as
 * {@code competition-example} keep a blank person.
 */
public final class ProfileId {
    public static final String DEFAULT_SET = "general";

    private final String id;
    private final String person;
    private final String set;

    private ProfileId(String id, String person, String set) {
        this.id = id;
        this.person = person;
        this.set = set;
    }

    public static ProfileId of(String rawId, String personField, String setField) {
        String id = normalize(rawId);
        if (id.isEmpty()) {
            id = "unnamed";
        }
        String person = normalize(personField);
        String set = normalize(setField);
        if (person.isEmpty()) {
            int slash = id.indexOf('/');
            if (slash > 0 && slash < id.length() - 1) {
                person = id.substring(0, slash);
                if (set.isEmpty()) {
                    set = id.substring(slash + 1);
                }
            }
        }
        if (!person.isEmpty() && set.isEmpty()) {
            set = DEFAULT_SET;
        }
        if (!person.isEmpty()) {
            id = person + "/" + set;
        }
        return new ProfileId(id, person, set);
    }

    public static String qualified(String person, String set) {
        return of("", person, set).id();
    }

    public String id() {
        return id;
    }

    public String person() {
        return person;
    }

    public String set() {
        return set;
    }

    public boolean grouped() {
        return !person.isEmpty();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
