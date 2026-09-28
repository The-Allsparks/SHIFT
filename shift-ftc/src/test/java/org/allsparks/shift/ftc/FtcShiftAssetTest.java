package org.allsparks.shift.ftc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import android.content.res.AssetManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FtcShiftAssetTest {
    @Test
    void readAssetReturnsUtf8FromTheNamedFile() {
        FakeAssets assets = new FakeAssets();
        assets.put("shift/bumblebee-drive.json", "{ \"id\": \"drive\" }");
        assertEquals("{ \"id\": \"drive\" }", FtcShift.readAsset(assets, "shift/bumblebee-drive.json"));
    }

    @Test
    void readAssetMissingFileNamesThePath() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class, () -> FtcShift.readAsset(new FakeAssets(), "shift/missing.json"));
        assertTrue(ex.getMessage().contains("shift/missing.json"));
    }

    @Test
    void readAssetRejectsNullManager() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> FtcShift.readAsset(null, "shift/drive.json"));
        assertTrue(ex.getMessage().contains("shift/drive.json"));
    }

    @Test
    void readAssetRejectsBlankPath() {
        assertThrows(IllegalArgumentException.class, () -> FtcShift.readAsset(new FakeAssets(), " "));
    }

    static final class FakeAssets extends AssetManager {
        private final Map<String, byte[]> files = new HashMap<String, byte[]>();

        void put(String path, String body) {
            files.put(path, body.getBytes(Charset.forName("UTF-8")));
        }

        @Override
        public InputStream open(String fileName) throws IOException {
            byte[] body = files.get(fileName);
            if (body == null) {
                throw new IOException("Missing asset " + fileName);
            }
            return new ByteArrayInputStream(body);
        }
    }
}
