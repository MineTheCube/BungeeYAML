package fr.onecraft.config.test;

import fr.onecraft.config.FileConfiguration;
import fr.onecraft.config.exception.InvalidConfigurationException;
import fr.onecraft.config.util.Config;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FileConfigurationTest {

    private static final boolean DEBUG = false;

    @Test
    public void test() throws Exception {

        FileConfiguration defaults = new FileConfiguration();
        FileConfiguration config = new FileConfiguration();

        defaults.load(Config.PLUGIN_RESOURCE.getStream());
        config.load(Config.USER_EDITED.getFile(), defaults);

        try {
            new FileConfiguration().load(Config.INVALID.getFile());
            fail();
        } catch (InvalidConfigurationException ignored) {}

        println("------------------  CONFIG NO DEFAULT  ------------------");
        config.options().copyDefaults(false);
        config.options().copyHeader(false);
        println(config.saveToString());
        assertEquals(Config.EXPECTED_WITHOUT_DEFAULT.getString(), config.saveToString());


        println("------------------ CONFIG WITH DEFAULT ------------------");
        config.options().copyDefaults(true);
        config.options().copyHeader(true);
        println(config.saveToString());
        assertEquals(Config.EXPECTED_WITH_DEFAULT.getString(), config.saveToString());

    }

    private void println(String line) {
        if (DEBUG) System.out.println(line);
    }

    private void println() {
        if (DEBUG) System.out.println();
    }

}
