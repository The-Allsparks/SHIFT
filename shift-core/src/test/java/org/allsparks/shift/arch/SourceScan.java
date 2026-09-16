package org.allsparks.shift.arch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Locates Gradle source trees from unit-test working directories. */
final class SourceScan {
    private SourceScan() {}

    static Path repoRoot() {
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

    static Path coreMain() {
        return repoRoot().resolve("shift-core/src/main/java");
    }

    static Path ftcMain() {
        return repoRoot().resolve("shift-ftc/src/main/java");
    }

    static Path examplesMain() {
        return repoRoot().resolve("shift-examples/src/main/java");
    }

    static List<Path> javaFiles(Path root) throws IOException {
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

    static String read(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    static String packageOf(Path javaFile, Path sourceRoot) {
        Path relative = sourceRoot.relativize(javaFile).getParent();
        if (relative == null) {
            return "";
        }
        return relative.toString().replace('\\', '/').replace('/', '.');
    }

    static String shiftChildPackage(String fullPackage) {
        String prefix = "org.allsparks.shift.";
        if (!fullPackage.startsWith(prefix)) {
            return fullPackage.equals("org.allsparks.shift") ? "" : fullPackage;
        }
        String rest = fullPackage.substring(prefix.length());
        int dot = rest.indexOf('.');
        if (dot < 0) {
            return rest;
        }
        return rest.substring(0, dot);
    }

    static String stripImport(String line) {
        String imported = line.substring("import ".length()).trim();
        if (imported.endsWith(";")) {
            imported = imported.substring(0, imported.length() - 1).trim();
        }
        if (imported.startsWith("static ")) {
            imported = imported.substring("static ".length()).trim();
        }
        return imported;
    }

    static String rel(Path root, Path path) {
        return root.relativize(path).toString().replace('\\', '/');
    }

    static boolean isComment(String trimmed) {
        return trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*");
    }
}
