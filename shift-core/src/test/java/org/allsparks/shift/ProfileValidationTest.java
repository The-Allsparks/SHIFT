package org.allsparks.shift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allsparks.shift.config.ConfigException;
import org.allsparks.shift.input.ControllerModel;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.profile.Profile;
import org.allsparks.shift.profile.ProfileLoader;
import org.junit.jupiter.api.Test;

class ProfileValidationTest {
    private final ProfileLoader loader = new ProfileLoader(ShiftFixtures.exampleIntents());

    @Test
    void validConfigurationLoads() {
        loader.loadJson(ShiftFixtures.profile("\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": ["
                + "{ \"source\": \"Y\", \"event\": \"ON_PRESS\", \"intent\": \"helm.score\" }] } }"));
    }

    @Test
    void malformedJsonFails() {
        ConfigException ex = assertThrows(ConfigException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                loader.loadJson("{ not json");
            }
        });
        assertTrue(ex.getMessage().contains("Malformed JSON"));
    }

    @Test
    void unsupportedSchemaFails() {
        ConfigException ex = assertThrows(ConfigException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                loader.loadJson("{ \"schemaVersion\": 99, \"controllers\": { \"driver\": { \"slot\": 1 } } }");
            }
        });
        assertTrue(ex.getMessage().contains("Unsupported schemaVersion"));
    }

    @Test
    void unknownIntentSuggestsCorrection() {
        ConfigException ex = assertThrows(ConfigException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                loader.loadJson(
                        ShiftFixtures.profile("\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": ["
                                + "{ \"source\": \"Y\", \"intent\": \"elevtor.score.high\" }] } }"));
            }
        });
        assertTrue(ex.getMessage().contains("Unknown intent 'elevtor.score.high'"));
        assertTrue(ex.getMessage().contains("Did you mean 'elevator.score.high'"));
        assertTrue(ex.getMessage().contains("bindings[0].intent")
                || ex.getMessage().contains("layers.drive.bindings[0].intent"));
    }

    @Test
    void unknownControlSuggestsCorrection() {
        ConfigException ex = assertThrows(ConfigException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                loader.loadJson(
                        ShiftFixtures.profile("\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": ["
                                + "{ \"source\": \"LEFT_STIK_Y\", \"intent\": \"drive.rotation\" }] } }"));
            }
        });
        assertTrue(ex.getMessage().contains("Unknown control"));
        assertTrue(ex.getMessage().contains("Did you mean"));
    }

    @Test
    void duplicateBindingsAreRejected() {
        ConfigException ex = assertThrows(ConfigException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                loader.loadJson(
                        ShiftFixtures.profile("\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": ["
                                + "{ \"source\": \"Y\", \"event\": \"ON_PRESS\", \"intent\": \"helm.score\" },"
                                + "{ \"source\": \"Y\", \"event\": \"ON_PRESS\", \"intent\": \"helm.score\" }"
                                + "] } }"));
            }
        });
        assertTrue(ex.getMessage().contains("Duplicate binding"));
    }

    @Test
    void equalTriggerDifferentIntentsConflict() {
        ConfigException ex = assertThrows(ConfigException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                loader.loadJson(
                        ShiftFixtures.profile("\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": ["
                                + "{ \"source\": \"Y\", \"event\": \"ON_PRESS\", \"intent\": \"helm.score\" },"
                                + "{ \"source\": \"NORTH\", \"event\": \"ON_PRESS\", \"intent\": \"elevator.score.high\" }"
                                + "] } }"));
            }
        });
        assertTrue(ex.getMessage().contains("Conflicting bindings"));
        assertFalse(ex.getMessage().equals("Invalid configuration"));
    }

    @Test
    void unreachableLayerIsRejected() {
        ConfigException ex = assertThrows(ConfigException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                loader.loadJson(ShiftFixtures.profile(
                        "\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": [] },"
                                + "\"secret\": { \"controller\": \"driver\", \"bindings\": ["
                                + "{ \"source\": \"A\", \"intent\": \"helm.score\" }] } }"));
            }
        });
        assertTrue(ex.getMessage().contains("unreachable"));
    }

    @Test
    void analogIntentCannotUseOnPress() {
        ConfigException ex = assertThrows(ConfigException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                loader.loadJson(
                        ShiftFixtures.profile("\"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": ["
                                + "{ \"source\": \"RIGHT_STICK_X\", \"event\": \"ON_PRESS\", \"intent\": \"drive.rotation\" }"
                                + "] } }"));
            }
        });
        assertTrue(ex.getMessage().contains("ANALOG"));
    }

    @Test
    void knownControllerModelLoads() {
        Profile profile = loader.loadJson("{ \"schemaVersion\": 1, \"id\": \"test\", \"controllers\": {"
                + "\"driver\": { \"slot\": 1, \"defaultLayer\": \"drive\", \"model\": \"iwgame-ps5\" },"
                + "\"codriver\": { \"slot\": 2, \"defaultLayer\": \"drive\" }"
                + "}, \"layers\": { \"drive\": { \"bindings\": [] } } }");
        assertEquals(
                ControllerModel.IWGAME_WIRED_PS5,
                profile.controllers().get(ControllerRole.DRIVER).model());
    }

    @Test
    void unknownControllerModelSuggestsCorrection() {
        ConfigException ex = assertThrows(ConfigException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                loader.loadJson("{ \"schemaVersion\": 1, \"id\": \"test\", \"controllers\": {"
                        + "\"driver\": { \"slot\": 1, \"defaultLayer\": \"drive\", \"model\": \"iwgame-wired-ps\" }"
                        + "}, \"layers\": { \"drive\": { \"bindings\": [] } } }");
            }
        });
        assertTrue(ex.getMessage().contains("Unknown controller model"));
        assertTrue(ex.getMessage().contains("Did you mean 'iwgame-wired-ps5'"));
    }
}
