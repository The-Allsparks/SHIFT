package org.allsparks.shift.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;

class ShiftProfilesTest {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Test
    void readUtf8CopiesBytesAndCloses() throws IOException {
        TrackingStream stream = new TrackingStream("{ \"schemaVersion\": 1 }".getBytes(UTF_8));
        String json = ShiftProfiles.readUtf8(stream, "shift/drive.json");
        assertEquals("{ \"schemaVersion\": 1 }", json);
        assertTrue(stream.closed);
    }

    @Test
    void readUtf8NullStreamNamesTheSource() {
        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> ShiftProfiles.readUtf8(null, "shift/drive.json"));
        assertTrue(ex.getMessage().contains("shift/drive.json"));
    }

    @Test
    void readUtf8WrapsIoFailureWithTheSource() {
        InputStream stream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("disk");
            }
        };
        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> ShiftProfiles.readUtf8(stream, "shift/drive.json"));
        assertTrue(ex.getMessage().contains("shift/drive.json"));
        assertTrue(ex.getCause() instanceof IOException);
    }

    @Test
    void readUtf8RejectsBlankSource() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ShiftProfiles.readUtf8(new ByteArrayInputStream(new byte[0]), "  "));
    }

    @Test
    void readResourceLoadsClasspathUtf8() {
        String body = ShiftProfiles.readResource(ShiftProfilesTest.class, "shift-profiles-sample.txt");
        assertEquals("sample-profile", body.trim());
    }

    @Test
    void readResourceMissingNamesThePath() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> ShiftProfiles.readResource(ShiftProfilesTest.class, "missing-profile.json"));
        assertTrue(ex.getMessage().contains("missing-profile.json"));
    }

    static final class TrackingStream extends ByteArrayInputStream {
        boolean closed;

        TrackingStream(byte[] buf) {
            super(buf);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
