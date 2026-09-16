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
 * Executable package boundaries. Architecture lives in documentation
 * <em>and</em> these tests so new AI-generated imports fail CI.
 */
class PackageBoundaryTest {

    private static final Set<String> FORBIDDEN_CORE_PREFIXES = new HashSet<String>(Arrays.asList(
            "com.qualcomm.",
            "org.firstinspires.ftc.",
            "android.",
            "org.allsparks.shift.ftc.",
            "org.allsparks.shift.examples."));

    private static final Set<String> RUNTIME_PACKAGES = new HashSet<String>(Arrays.asList(
            "clock", "input", "intent", "trigger", "transform", "binding", "observe", "feedback", "config"));

    @Test
    void coreDoesNotImportFtcAndroidOrAdapters() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path main = SourceScan.coreMain();
        for (Path path : SourceScan.javaFiles(main)) {
            String[] lines = SourceScan.read(path).split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.startsWith("import ")) {
                    continue;
                }
                String imported = SourceScan.stripImport(line);
                for (String prefix : FORBIDDEN_CORE_PREFIXES) {
                    if (imported.startsWith(prefix)) {
                        hits.add(SourceScan.rel(main, path) + ":" + (i + 1) + " " + imported);
                    }
                }
            }
        }
        failIf(hits, "shift-core imported FTC, Android, shift-ftc, or shift-examples");
    }

    @Test
    void runtimePackagesDoNotImportProfile() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path main = SourceScan.coreMain();
        for (Path path : SourceScan.javaFiles(main)) {
            String child = SourceScan.shiftChildPackage(SourceScan.packageOf(path, main));
            if (!RUNTIME_PACKAGES.contains(child)) {
                continue;
            }
            String[] lines = SourceScan.read(path).split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.startsWith("import org.allsparks.shift.profile.")) {
                    hits.add(SourceScan.rel(main, path) + ":" + (i + 1));
                }
            }
        }
        failIf(hits, "runtime packages imported org.allsparks.shift.profile (load-time only)");
    }

    @Test
    void jsonParsingStaysInProfileAndSnapshot() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path main = SourceScan.coreMain();
        for (Path path : SourceScan.javaFiles(main)) {
            String child = SourceScan.shiftChildPackage(SourceScan.packageOf(path, main));
            if ("profile".equals(child) || path.getFileName().toString().equals("InputSnapshot.java")) {
                continue;
            }
            String[] lines = SourceScan.read(path).split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.startsWith("import org.json.")) {
                    hits.add(SourceScan.rel(main, path) + ":" + (i + 1) + " " + SourceScan.stripImport(line));
                }
            }
        }
        failIf(hits, "org.json imported outside profile load and InputSnapshot replay JSON");
    }

    private static void failIf(List<String> hits, String message) {
        if (!hits.isEmpty()) {
            fail(message + ":\n" + String.join("\n", hits));
        }
    }
}
