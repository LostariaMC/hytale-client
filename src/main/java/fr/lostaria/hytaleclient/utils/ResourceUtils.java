package fr.lostaria.hytaleclient.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ResourceUtils {

    public static void copyResourceIfMissing(Class<?> anchorClass, String resourceName, Path targetPath) throws IOException {
        if (Files.exists(targetPath)) return;

        try (InputStream in = anchorClass.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IOException("Resource '" + resourceName + "' not found in jar. Put it in src/main/resources/" + resourceName);
            }
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
