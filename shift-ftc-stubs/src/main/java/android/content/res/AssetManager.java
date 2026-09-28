package android.content.res;

import java.io.IOException;
import java.io.InputStream;

/**
 * Compile-only stand-in for {@code android.content.res.AssetManager}.
 *
 * <p>Robot projects compile against the platform class. SHIFT calls {@link
 * #open(String)} at init to read a TeamCode profile packed in the APK.
 */
public class AssetManager {
    public InputStream open(String fileName) throws IOException {
        throw new IOException("Missing asset " + fileName);
    }
}
