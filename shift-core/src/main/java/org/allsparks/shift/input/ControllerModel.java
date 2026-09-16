package org.allsparks.shift.input;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Named physical controller families that a profile may declare.
 *
 * <p>The FTC Driver Station maps USB HID reports onto {@code Gamepad} fields
 * before SHIFT samples them. A model does not change axis/button mapping at
 * runtime. It records which hardware the operator is holding so validation,
 * docs, and traces can distinguish a Logitech F310 from a DualSense-style pad.
 *
 * <p>{@link #IWGAME_WIRED_PS5} is the AllSparks DualSense-layout wired clone
 * (IWGAME PS-5/PC). Rear paddles on that hardware are firmware-remapped onto
 * existing buttons; bind the remapped control, not a SHIFT-only paddle name.
 */
public enum ControllerModel {
    UNKNOWN("unknown", "Unknown controller", Family.UNKNOWN, false, false, false, false, false),
    LOGITECH_F310("logitech-f310", "Logitech F310", Family.LOGITECH, false, false, false, false, false),
    XBOX_360("xbox-360", "Xbox 360", Family.XBOX, true, false, false, false, false),
    SONY_PS4("sony-ps4", "Sony DualShock 4 / Etpark PS4", Family.PLAYSTATION, true, true, true, false, false),
    SONY_DUALSENSE("sony-dualsense", "Sony DualSense", Family.PLAYSTATION, true, true, true, false, false),
    IWGAME_WIRED_PS5(
            "iwgame-wired-ps5",
            "IWGAME Wired Controller for PS-5/PC",
            Family.PLAYSTATION,
            true,
            true,
            true,
            true,
            true);

    public enum Family {
        UNKNOWN,
        LOGITECH,
        XBOX,
        PLAYSTATION
    }

    private static final Map<String, ControllerModel> LOOKUP = buildLookup();

    private final String token;
    private final String displayName;
    private final Family family;
    private final boolean rumble;
    private final boolean rgbLed;
    private final boolean touchpad;
    private final boolean hallAnalog;
    private final boolean firmwarePaddles;

    ControllerModel(
            String token,
            String displayName,
            Family family,
            boolean rumble,
            boolean rgbLed,
            boolean touchpad,
            boolean hallAnalog,
            boolean firmwarePaddles) {
        this.token = token;
        this.displayName = displayName;
        this.family = family;
        this.rumble = rumble;
        this.rgbLed = rgbLed;
        this.touchpad = touchpad;
        this.hallAnalog = hallAnalog;
        this.firmwarePaddles = firmwarePaddles;
    }

    public String token() {
        return token;
    }

    public String displayName() {
        return displayName;
    }

    public Family family() {
        return family;
    }

    public boolean rumble() {
        return rumble;
    }

    public boolean rgbLed() {
        return rgbLed;
    }

    public boolean touchpad() {
        return touchpad;
    }

    public boolean hallAnalog() {
        return hallAnalog;
    }

    /**
     * True when the hardware has rear buttons that the controller firmware
     * remaps onto existing HID buttons. FTC RobotCore 11.2 has no paddle
     * fields, so SHIFT never samples them as independent controls.
     */
    public boolean firmwarePaddles() {
        return firmwarePaddles;
    }

    /**
     * Parses a model token or alias. Matching is case-insensitive and ignores
     * hyphens, underscores, and spaces.
     *
     * @throws IllegalArgumentException if the name is unknown
     */
    public static ControllerModel parse(String name) {
        ControllerModel found = tryParse(name);
        if (found == null) {
            throw new IllegalArgumentException("Unknown controller model '" + name + "'");
        }
        return found;
    }

    /**
     * Parses a model token or alias, returning {@code null} when unknown.
     */
    public static ControllerModel tryParse(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return LOOKUP.get(Control.normalize(trimmed));
    }

    private static Map<String, ControllerModel> buildLookup() {
        Map<String, ControllerModel> map = new HashMap<String, ControllerModel>();
        for (ControllerModel model : values()) {
            map.put(Control.normalize(model.token()), model);
            map.put(Control.normalize(model.name()), model);
        }
        map.put("F310", LOGITECH_F310);
        map.put("LOGITECH", LOGITECH_F310);
        map.put("XBOX", XBOX_360);
        map.put("XBOX360", XBOX_360);
        map.put("PS4", SONY_PS4);
        map.put("DUALSHOCK4", SONY_PS4);
        map.put("DUALSHOCK", SONY_PS4);
        map.put("ETPARK", SONY_PS4);
        map.put("ETPARKPS4", SONY_PS4);
        map.put("PS5", SONY_DUALSENSE);
        map.put("DUALSENSE", SONY_DUALSENSE);
        map.put("SONYPS5", SONY_DUALSENSE);
        map.put("IWGAME", IWGAME_WIRED_PS5);
        map.put("IWGAMEPS5", IWGAME_WIRED_PS5);
        map.put("IWGAMEWIRED", IWGAME_WIRED_PS5);
        map.put("IWGAMEWIREDCONTROLLER", IWGAME_WIRED_PS5);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String toString() {
        return token;
    }
}
