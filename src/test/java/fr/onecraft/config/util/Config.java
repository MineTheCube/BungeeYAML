package fr.onecraft.config.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public enum Config {

    INVALID,
    USER_EDITED,
    PLUGIN_RESOURCE,
    EXPECTED_WITH_DEFAULT,
    EXPECTED_WITHOUT_DEFAULT,
    ;

    public File getFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(getFileName());
        if (resource == null) throw new IllegalStateException("Missing resource " + getFileName());
        return new File(resource.getFile());
    }

    public InputStream getStream() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream(getFileName());
        if (is == null) throw new IllegalStateException("Missing resource " + getFileName());
        return is;
    }

    public String getString() {
        try {
            return new String(Files.readAllBytes(this.getFile().toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileName() {
        return name().toLowerCase().replace("_", "-") + ".yml";
    }

}
