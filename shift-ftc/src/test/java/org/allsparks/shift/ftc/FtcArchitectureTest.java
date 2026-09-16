package org.allsparks.shift.ftc;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * FTC adapter and example sources must remain observation-only. Javadoc may
 * mention motors; executable calls must not.
 */
class FtcArchitectureTest {

    @Test
    void ftcAdaptersDoNotCallActuatorWrites() throws IOException {
        failIf(scanWrites(ftcMain()), "shift-ftc must not call actuator writes");
    }

    @Test
    void examplesDoNotCallActuatorWrites() throws IOException {
        failIf(scanWrites(examplesMain()), "shift-examples must not call actuator writes");
    }

    @Test
    void ftcDoesNotDependOnExamples() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path main = ftcMain();
        for (Path path : javaFiles(main)) {
            String[] lines = read(path).split("\n");
            for (int i = 0; i < lines.length; i++) {
                String trimmed = lines[i].trim();
                if (trimmed.startsWith("import org.allsparks.shift.examples.")) {
                    hits.add(rel(main, path) + ":" + (i + 1));
                }
            }
        }
        failIf(hits, "shift-ftc imported shift-examples");
    }

    private static List<String> scanWrites(Path main) throws IOException {
        List<String> hits = new ArrayList<String>();
        for (Path path : javaFiles(main)) {
            String[] lines = read(path).split("\n");
            for (int i = 0; i < lines.length; i++) {
                String trimmed = lines[i].trim();
                if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) {
                    continue;
                }
                if (trimmed.contains(".setPower(") || trimmed.contains(".setVelocity(")) {
                    hits.add(rel(main, path) + ":" + (i + 1));
                }
            }
        }
        return hits;
    }

    private static List<Path> javaFiles(Path root) throws IOException {
        List<Path> files = new ArrayList<Path>();
        if (!Files.isDirectory(root)) {
            return files;
        }
        try (java.util.stream.Stream<Path> walk = Files.walk(root)) {
            walk.forEach(path -> {
                if (path.toString().endsWith(".java") && Files.isRegularFile(path)) {
                    files.add(path);
                }
            });
        }
        return files;
    }

    private static Path repoRoot() {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path cur = cwd;
        for (int i = 0; i < 8 && cur != null; i++) {
            if (Files.isRegularFile(cur.resolve("settings.gradle"))) {
                return cur;
            }
            cur = cur.getParent();
        }
        return cwd;
    }

    private static Path ftcMain() {
        Path nested = repoRoot().resolve("shift-ftc/src/main/java");
        if (Files.isDirectory(nested)) {
            return nested;
        }
        return Paths.get("").toAbsolutePath().normalize().resolve("src/main/java");
    }

    private static Path examplesMain() {
        return repoRoot().resolve("shift-examples/src/main/java");
    }

    private static String read(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    private static String rel(Path root, Path path) {
        return root.relativize(path).toString().replace('\\', '/');
    }

    private static void failIf(List<String> hits, String message) {
        if (!hits.isEmpty()) {
            fail(message + ":\n" + String.join("\n", hits));
        }
    }
}
