package org.allsparks.shift.profile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Reads SHIFT profile JSON as UTF-8. Schema validation stays in {@link
 * ProfileLoader}; this class only owns how bytes become a string.
 *
 * <p>Hub-safe: Java 8 stream copy, no {@code Files.readString()}, no {@code
 * File.toPath()}. Call from {@code init()}, never from {@code Shift.update()}.
 *
 * <p>The robot project chooses the file (APK asset path, classpath resource).
 * SHIFT chooses charset, chunking, close behavior, and error wrapping.
 */
public final class ShiftProfiles {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private ShiftProfiles() {}

    /**
     * Reads the whole stream as UTF-8 and closes it. {@code sourceName} is the
     * asset path or resource id shown when the read fails.
     */
    public static String readUtf8(InputStream stream, String sourceName) {
        String source = requireSource(sourceName);
        if (stream == null) {
            throw new IllegalStateException("Missing SHIFT profile " + source);
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            int read;
            while ((read = stream.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read SHIFT profile " + source, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ignored) {
                // Close failed after we already have the bytes — do not hide the successful read.
            }
        }
    }

    /**
     * Opens a classpath resource with the same UTF-8 contract. Used for the
     * embedded idle fallback and SHIFT examples.
     */
    public static String readResource(Class<?> anchor, String resourcePath) {
        String source = requireSource(resourcePath);
        if (anchor == null) {
            throw new IllegalArgumentException("Class is required to read " + source);
        }
        InputStream stream = anchor.getResourceAsStream(source);
        if (stream == null) {
            throw new IllegalStateException("Missing SHIFT profile resource " + source);
        }
        return readUtf8(stream, source);
    }

    private static String requireSource(String sourceName) {
        if (sourceName == null || sourceName.trim().isEmpty()) {
            throw new IllegalArgumentException("SHIFT profile source name is required");
        }
        return sourceName;
    }
}
