package org.allsparks.shift.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allsparks.shift.Shift;
import org.allsparks.shift.ShiftFixtures;
import org.allsparks.shift.clock.ManualClock;
import org.allsparks.shift.config.ConfigException;
import org.allsparks.shift.input.Control;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.input.SimulatedInputDevice;
import org.allsparks.shift.observe.RecordingEventSink;
import org.allsparks.shift.observe.ShiftEventType;
import org.junit.jupiter.api.Test;

class ProfileBankTest {
    @Test
    void personSlashSetBecomesCanonicalId() {
        ProfileId id = ProfileId.of("studenta/offense", "", "");
        assertEquals("studenta/offense", id.id());
        assertEquals("studenta", id.person());
        assertEquals("offense", id.set());
        assertTrue(id.grouped());
    }

    @Test
    void personAndSetFieldsRewriteId() {
        ProfileId id = ProfileId.of("ignored", "StudentA", "Defense");
        assertEquals("studenta/defense", id.id());
        assertEquals("studenta", id.person());
        assertEquals("defense", id.set());
    }

    @Test
    void bankGroupsSetsAndComposesRoles() {
        ProfileBank bank = ProfileBank.builder(ShiftFixtures.exampleIntents())
                .addJson(driverJson("studenta/general", "RIGHT_BUMPER"))
                .addJson(driverJson("studenta/offense", "LEFT_BUMPER"))
                .addJson(operatorJson("studentb/general"))
                .build();
        assertEquals(3, bank.size());
        assertTrue(bank.persons().contains("studenta"));
        assertTrue(bank.sets("studenta").contains("offense"));
        Profile session = bank.compose("studenta", "offense", "studentb", "general");
        assertEquals("studenta/offense+studentb/general", session.id());
        assertTrue(session.controllers().containsKey(ControllerRole.DRIVER));
        assertTrue(session.controllers().containsKey(ControllerRole.CODRIVER));

        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        Shift shift = ShiftFixtures.base(driver, codriver).loadProfile(session).build();
        driver.hold(Control.LEFT_BUMPER);
        assertTrue(shift.update().held("drive.slow"));
        driver.release(Control.LEFT_BUMPER);
        shift.update();
        driver.hold(Control.RIGHT_BUMPER);
        assertFalse(shift.update().held("drive.slow"));
        codriver.hold(Control.A);
        assertTrue(shift.update().held("intake.collect"));
    }

    @Test
    void activateSwitchesCompiledProfileWithoutJson() {
        ProfileBank bank = ProfileBank.builder(ShiftFixtures.exampleIntents())
                .addJson(driverJson("studenta/general", "RIGHT_BUMPER"))
                .addJson(driverJson("studenta/offense", "LEFT_BUMPER"))
                .addJson(operatorJson("studentb/general"))
                .build();
        ManualClock clock = new ManualClock(1000);
        SimulatedInputDevice driver = ShiftFixtures.driver(clock);
        SimulatedInputDevice codriver = ShiftFixtures.codriver(clock);
        RecordingEventSink sink = new RecordingEventSink();
        Shift shift = ShiftFixtures.base(driver, codriver)
                .eventSink(sink)
                .loadProfile(bank.compose("studenta/general", "studentb/general"))
                .build();
        driver.hold(Control.RIGHT_BUMPER);
        assertTrue(shift.update().held("drive.slow"));
        shift.activate(bank.compose("studenta/offense", "studentb/general"));
        assertEquals("studenta/offense+studentb/general", shift.profile().id());
        assertEquals(
                "studenta/offense+studentb/general",
                sink.last(ShiftEventType.PROFILE_LOADED).field("profile"));
        driver.release(Control.RIGHT_BUMPER);
        driver.hold(Control.LEFT_BUMPER);
        assertTrue(shift.update().held("drive.slow"));
    }

    @Test
    void jsonPersonSetFieldsBecomeCanonicalId() {
        ProfileBank bank = ProfileBank.builder(ShiftFixtures.exampleIntents())
                .addJson("{ \"schemaVersion\": 1, \"person\": \"StudentA\", \"set\": \"Red\", \"controllers\": {"
                        + "\"driver\": { \"slot\": 1, \"defaultLayer\": \"drive\" }"
                        + "}, \"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": [] } } }")
                .build();
        assertEquals("studenta/red", bank.require("studenta", "red").id());
        assertEquals("studenta", bank.require("studenta/red").person());
        assertEquals("red", bank.require("studenta/red").set());
    }

    @Test
    void composeRejectsDriverFileWithoutDriverController() {
        final ProfileBank bank = ProfileBank.builder(ShiftFixtures.exampleIntents())
                .addJson(operatorJson("studentb/general"))
                .build();
        assertThrows(ConfigException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                bank.compose("studentb/general", "studentb/general");
            }
        });
    }

    @Test
    void unknownProfileFails() {
        ProfileBank bank = ProfileBank.builder(ShiftFixtures.exampleIntents())
                .addJson(driverJson("studenta/general", "RIGHT_BUMPER"))
                .build();
        assertThrows(ConfigException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                bank.require("alex", "general");
            }
        });
    }

    private static String driverJson(String id, String slowSource) {
        return "{ \"schemaVersion\": 1, \"id\": \"" + id + "\", \"controllers\": {"
                + "\"driver\": { \"slot\": 1, \"defaultLayer\": \"drive\" }"
                + "}, \"layers\": { \"drive\": { \"controller\": \"driver\", \"bindings\": ["
                + "{ \"source\": \"LEFT_STICK_X\", \"intent\": \"drive.rotation\", \"event\": \"ANALOG\" },"
                + "{ \"source\": \"" + slowSource + "\", \"event\": \"WHILE_HELD\", \"intent\": \"drive.slow\" }"
                + "] } } }";
    }

    private static String operatorJson(String id) {
        return "{ \"schemaVersion\": 1, \"id\": \"" + id + "\", \"controllers\": {"
                + "\"codriver\": { \"slot\": 2, \"defaultLayer\": \"mechanism\" }"
                + "}, \"layers\": { \"mechanism\": { \"controller\": \"codriver\", \"bindings\": ["
                + "{ \"source\": \"A\", \"event\": \"WHILE_HELD\", \"intent\": \"intake.collect\" }"
                + "] } } }";
    }
}
