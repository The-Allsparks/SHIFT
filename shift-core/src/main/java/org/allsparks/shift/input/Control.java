package org.allsparks.shift.input;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Hardware-neutral physical controls on a standard gamepad.
 *
 * <p>Face-button, PlayStation, and a few convenience aliases resolve to the same
 * canonical control. They never duplicate snapshot state. Canonical names follow
 * the FTC SDK {@code Gamepad} fields ({@code a}/{@code b}/{@code x}/{@code y})
 * because those are the fields the SDK actually stores; {@code SOUTH}/{@code CROSS}
 * and friends are aliases only.
 */
public enum Control {
    LEFT_STICK_X(ControlKind.ANALOG),
    LEFT_STICK_Y(ControlKind.ANALOG),
    RIGHT_STICK_X(ControlKind.ANALOG),
    RIGHT_STICK_Y(ControlKind.ANALOG),
    LEFT_TRIGGER(ControlKind.ANALOG),
    RIGHT_TRIGGER(ControlKind.ANALOG),
    LEFT_TRIGGER_PRESSED(ControlKind.DIGITAL),
    RIGHT_TRIGGER_PRESSED(ControlKind.DIGITAL),
    DPAD_UP(ControlKind.DIGITAL),
    DPAD_DOWN(ControlKind.DIGITAL),
    DPAD_LEFT(ControlKind.DIGITAL),
    DPAD_RIGHT(ControlKind.DIGITAL),
    LEFT_BUMPER(ControlKind.DIGITAL),
    RIGHT_BUMPER(ControlKind.DIGITAL),
    LEFT_STICK_BUTTON(ControlKind.DIGITAL),
    RIGHT_STICK_BUTTON(ControlKind.DIGITAL),
    START(ControlKind.DIGITAL),
    BACK(ControlKind.DIGITAL),
    GUIDE(ControlKind.DIGITAL),
    A(ControlKind.DIGITAL),
    B(ControlKind.DIGITAL),
    X(ControlKind.DIGITAL),
    Y(ControlKind.DIGITAL),
    TOUCHPAD(ControlKind.DIGITAL),
    TOUCHPAD_FINGER_1(ControlKind.DIGITAL),
    TOUCHPAD_FINGER_2(ControlKind.DIGITAL),
    TOUCHPAD_FINGER_1_X(ControlKind.ANALOG),
    TOUCHPAD_FINGER_1_Y(ControlKind.ANALOG),
    TOUCHPAD_FINGER_2_X(ControlKind.ANALOG),
    TOUCHPAD_FINGER_2_Y(ControlKind.ANALOG);

    private static final Map<String, Control> LOOKUP = buildLookup();

    private final ControlKind kind;

    Control(ControlKind kind) {
        this.kind = kind;
    }

    public ControlKind kind() {
        return kind;
    }

    public boolean analog() {
        return kind == ControlKind.ANALOG;
    }

    public boolean digital() {
        return kind == ControlKind.DIGITAL;
    }

    /**
     * Parses a control name or alias. Matching is case-insensitive and ignores
     * hyphens, underscores, and spaces.
     *
     * @throws IllegalArgumentException if the name is unknown
     */
    public static Control parse(String name) {
        Control found = tryParse(name);
        if (found == null) {
            throw new IllegalArgumentException("Unknown control '" + name + "'");
        }
        return found;
    }

    /**
     * Parses a control name or alias, returning {@code null} when unknown.
     */
    public static Control tryParse(String name) {
        if (name == null) {
            return null;
        }
        return LOOKUP.get(normalize(name));
    }

    public static String normalize(String name) {
        StringBuilder builder = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == '_' || ch == '-' || ch == ' ' || ch == '.') {
                continue;
            }
            builder.append(Character.toUpperCase(ch));
        }
        return builder.toString();
    }

    private static Map<String, Control> buildLookup() {
        Map<String, Control> map = new HashMap<String, Control>();
        for (Control control : values()) {
            map.put(normalize(control.name()), control);
        }
        // Face-button semantic aliases (Nintendo/Xbox layout independent).
        map.put("SOUTH", A);
        map.put("EAST", B);
        map.put("WEST", X);
        map.put("NORTH", Y);
        // PlayStation aliases. FTC SDK maps these onto a/b/x/y.
        map.put("CROSS", A);
        map.put("CIRCLE", B);
        map.put("SQUARE", X);
        map.put("TRIANGLE", Y);
        // PlayStation system-button aliases. FTC SDK maps share/options/ps.
        // DualSense renamed Share to Create; IWGAME DualSense-style pads use that label.
        map.put("SHARE", BACK);
        map.put("CREATE", BACK);
        map.put("OPTIONS", START);
        map.put("PS", GUIDE);
        map.put("TOUCHPAD1", TOUCHPAD_FINGER_1);
        map.put("TOUCHPAD2", TOUCHPAD_FINGER_2);
        map.put("FINGER1", TOUCHPAD_FINGER_1);
        map.put("FINGER2", TOUCHPAD_FINGER_2);
        // Common shorthand.
        map.put("LB", LEFT_BUMPER);
        map.put("RB", RIGHT_BUMPER);
        map.put("LT", LEFT_TRIGGER);
        map.put("RT", RIGHT_TRIGGER);
        // SDK 11.1+ digital trigger click, distinct from analog LEFT_TRIGGER/RIGHT_TRIGGER.
        map.put("LTPRESSED", LEFT_TRIGGER_PRESSED);
        map.put("RTPRESSED", RIGHT_TRIGGER_PRESSED);
        map.put("LEFTTRIGGERBUTTON", LEFT_TRIGGER_PRESSED);
        map.put("RIGHTTRIGGERBUTTON", RIGHT_TRIGGER_PRESSED);
        map.put("LTCLICK", LEFT_TRIGGER_PRESSED);
        map.put("RTCLICK", RIGHT_TRIGGER_PRESSED);
        map.put("L3", LEFT_STICK_BUTTON);
        map.put("R3", RIGHT_STICK_BUTTON);
        map.put("LEFTSTICK", LEFT_STICK_BUTTON);
        map.put("RIGHTSTICK", RIGHT_STICK_BUTTON);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Canonical token used in configuration and logs, e.g. {@code LEFT_STICK_Y}.
     */
    public String token() {
        return name();
    }

    @Override
    public String toString() {
        return name();
    }
}
