package org.allsparks.shift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.allsparks.shift.input.ControllerRole;
import org.allsparks.shift.profile.Profile;
import org.allsparks.shift.profile.ProfileBank;
import org.allsparks.shift.profile.ProfileLoader;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class ProfileSchemaConformanceTest {
    @Test
    void jsonSchemaDeclaresVersionOne() throws IOException {
        Path schema = findRepoRoot().resolve("schema/shift-profile-v1.json");
        JSONObject json = new JSONObject(new String(Files.readAllBytes(schema), Charset.forName("UTF-8")));
        assertEquals("SHIFT profile", json.getString("title"));
        assertTrue(
                json.getJSONObject("properties").getJSONObject("schemaVersion").has("const"));
        assertEquals(
                1,
                json.getJSONObject("properties").getJSONObject("schemaVersion").getInt("const"));
        assertTrue(json.getJSONObject("properties").has("person"));
        assertTrue(json.getJSONObject("properties").has("set"));
    }

    @Test
    void exampleProfilesLoadAgainstTheRegistry() throws IOException {
        ProfileLoader loader = new ProfileLoader(ShiftFixtures.exampleIntents());
        Profile competition =
                loader.loadJson(read(findRepoRoot().resolve("examples/profiles/competition-example.json")));
        assertEquals("competition-example", competition.id());
        assertEquals(1, competition.schemaVersion());
        assertTrue(competition.bindings().size() > 8);

        Profile fallback = loader.loadJson(read(findRepoRoot().resolve("examples/profiles/fallback.json")));
        assertEquals("shift-safe-idle", fallback.id());

        ProfileBank bank = ProfileBank.builder(ShiftFixtures.exampleIntents())
                .addJson(read(findRepoRoot().resolve("examples/profiles/garrett-general.json")))
                .addJson(read(findRepoRoot().resolve("examples/profiles/garrett-offense.json")))
                .addJson(read(findRepoRoot().resolve("examples/profiles/sam-general.json")))
                .build();
        assertEquals("garrett", bank.require("garrett/general").person());
        assertEquals("offense", bank.require("garrett/offense").set());
        Profile session = bank.compose("garrett/offense", "sam/general");
        assertEquals("garrett/offense+sam/general", session.id());
        assertTrue(session.controllers().containsKey(ControllerRole.DRIVER));
        assertTrue(session.controllers().containsKey(ControllerRole.CODRIVER));
    }

    @Test
    void embeddedFallbackMatchesExample() throws IOException {
        Path root = findRepoRoot();
        String example = read(root.resolve("examples/profiles/fallback.json"));
        String testCopy = read(root.resolve("shift-core/src/test/resources/profiles/fallback.json"));
        String embeddedFile =
                read(root.resolve("shift-core/src/main/resources/org/allsparks/shift/profile/fallback.json"));
        assertEquals(example, testCopy);
        assertEquals(example, embeddedFile);
        assertEquals(example, Shift.embeddedFallbackJson());
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), Charset.forName("UTF-8"));
    }

    private static Path findRepoRoot() {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        if (Files.exists(cwd.resolve("schema/shift-profile-v1.json"))) {
            return cwd;
        }
        Path parent = cwd.getParent();
        if (parent != null && Files.exists(parent.resolve("schema/shift-profile-v1.json"))) {
            return parent;
        }
        throw new IllegalStateException("Cannot locate SHIFT repo root from " + cwd);
    }
}
