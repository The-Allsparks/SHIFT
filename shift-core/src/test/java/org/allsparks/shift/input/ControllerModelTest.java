package org.allsparks.shift.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ControllerModelTest {
    @Test
    void iwgameAliasesResolveToWiredPs5() {
        assertSame(ControllerModel.IWGAME_WIRED_PS5, ControllerModel.parse("iwgame-wired-ps5"));
        assertSame(ControllerModel.IWGAME_WIRED_PS5, ControllerModel.parse("IWGAME"));
        assertSame(ControllerModel.IWGAME_WIRED_PS5, ControllerModel.parse("iwgame-ps5"));
        assertEquals(ControllerModel.Family.PLAYSTATION, ControllerModel.IWGAME_WIRED_PS5.family());
        assertTrue(ControllerModel.IWGAME_WIRED_PS5.rgbLed());
        assertTrue(ControllerModel.IWGAME_WIRED_PS5.rumble());
        assertTrue(ControllerModel.IWGAME_WIRED_PS5.touchpad());
        assertTrue(ControllerModel.IWGAME_WIRED_PS5.hallAnalog());
        assertTrue(ControllerModel.IWGAME_WIRED_PS5.firmwarePaddles());
    }

    @Test
    void dualsenseAliasesMatchOfficialPs5() {
        assertSame(ControllerModel.SONY_DUALSENSE, ControllerModel.parse("ps5"));
        assertSame(ControllerModel.SONY_DUALSENSE, ControllerModel.parse("dualsense"));
        assertSame(ControllerModel.SONY_PS4, ControllerModel.parse("etpark"));
    }

    @Test
    void unknownModelThrows() {
        assertThrows(IllegalArgumentException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                ControllerModel.parse("steam-deck");
            }
        });
    }
}
