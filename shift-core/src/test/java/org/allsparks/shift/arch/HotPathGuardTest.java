package org.allsparks.shift.arch;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Static guards for the robot loop. These are not Control Hub timings; they
 * keep JSON parse, filesystem, and extra threads out of {@code Shift.update()}.
 */
class HotPathGuardTest {

    private static final Set<String> LOOP_PACKAGES =
            new HashSet<String>(Arrays.asList("trigger", "transform", "binding", "clock", "intent", "observe"));

    private static final String[] FORBIDDEN_SNIPPETS = {
        "Thread.sleep",
        "java.net.",
        "java.nio.file",
        "FileOutputStream",
        "FileWriter",
        "Socket ",
        "HttpURLConnection",
        "Executors.",
        "new Thread("
    };

    @Test
    void loopPackagesDoNotBlockOrStartThreads() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path main = SourceScan.coreMain();
        for (Path path : SourceScan.javaFiles(main)) {
            String child = SourceScan.shiftChildPackage(SourceScan.packageOf(path, main));
            if (!LOOP_PACKAGES.contains(child)) {
                continue;
            }
            String[] lines = SourceScan.read(path).split("\n");
            for (int i = 0; i < lines.length; i++) {
                String trimmed = lines[i].trim();
                if (SourceScan.isComment(trimmed)) {
                    continue;
                }
                for (int s = 0; s < FORBIDDEN_SNIPPETS.length; s++) {
                    if (trimmed.contains(FORBIDDEN_SNIPPETS[s])) {
                        hits.add(SourceScan.rel(main, path) + ":" + (i + 1) + " " + FORBIDDEN_SNIPPETS[s]);
                    }
                }
            }
        }
        failIf(hits, "loop package used blocking I/O, networking, sleep, or extra threads");
    }

    @Test
    void updatePathDoesNotParseJsonOrTouchTheFilesystem() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path shift = SourceScan.coreMain().resolve("org/allsparks/shift/Shift.java");
        rejectAfterMethod(hits, shift, "public IntentFrame update()", new String[] {
            "JSONObject",
            "JSONArray",
            "loadJson",
            "embeddedFallbackJson",
            "getResourceAsStream",
            "java.nio.file",
            "FileOutputStream",
            "FileWriter",
            "Thread.sleep",
            "new Thread(",
            "Executors.",
            ".stream(",
            "Collectors."
        });
        failIf(hits, "Shift.update() parsed JSON, hit the filesystem, streamed, or started threads");
    }

    @Test
    void updatePathDoesNotAllocateScratchMaps() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path shift = SourceScan.coreMain().resolve("org/allsparks/shift/Shift.java");
        rejectAfterMethod(hits, shift, "public IntentFrame update()", new String[] {
            "new LinkedHashMap", "new HashMap", "new ArrayList"
        });
        failIf(hits, "Shift.update() allocated a fresh list/map");
    }

    private static void rejectAfterMethod(List<String> hits, Path path, String methodSig, String[] snippets) {
        String[] lines = SourceScan.read(path).split("\n");
        boolean inMethod = false;
        int depth = 0;
        String name = path.getFileName().toString();
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (!inMethod) {
                if (trimmed.contains(methodSig)) {
                    inMethod = true;
                    depth = braceDelta(lines[i]);
                }
                continue;
            }
            depth += braceDelta(lines[i]);
            if (SourceScan.isComment(trimmed)) {
                if (depth <= 0) {
                    break;
                }
                continue;
            }
            for (int s = 0; s < snippets.length; s++) {
                if (trimmed.contains(snippets[s])) {
                    hits.add(name + ":" + (i + 1) + " " + snippets[s]);
                }
            }
            if (depth <= 0) {
                break;
            }
        }
    }

    private static int braceDelta(String line) {
        int delta = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '{') {
                delta++;
            } else if (c == '}') {
                delta--;
            }
        }
        return delta;
    }

    private static void failIf(List<String> hits, String message) {
        if (!hits.isEmpty()) {
            fail(message + ":\n" + String.join("\n", hits));
        }
    }
}
