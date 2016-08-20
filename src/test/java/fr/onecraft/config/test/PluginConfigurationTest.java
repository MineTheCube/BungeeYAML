package fr.onecraft.config.test;

import fr.onecraft.config.FileConfiguration;
import fr.onecraft.config.exception.ConfigurationException;
import fr.onecraft.config.plugin.PluginConfigurable;
import fr.onecraft.config.util.Config;
import fr.onecraft.config.util.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PluginConfigurable.class)
public class PluginConfigurationTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private PluginConfigurable getPlugin() {

        PluginConfigurable plugin;
        plugin = mock(PluginConfigurable.class);

        File dataFolder = folder.getRoot();

        when(plugin.getDataFolder()).thenReturn(dataFolder);
        when(plugin.getLogger()).thenReturn(mock(Logger.class));
        when(plugin.getResourceAsStream(anyString())).thenAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                return Config.PLUGIN_RESOURCE.getStream();
            }
        });

        doCallRealMethod().when(plugin).getConfig();
        doCallRealMethod().when(plugin).saveDefaultConfig();
        doCallRealMethod().when(plugin).reloadConfig();
        doCallRealMethod().when(plugin).saveConfig();

        return plugin;
    }

    private File getConfigFile() {
        return new File(folder.getRoot(), "config.yml");
    }

    @Test
    public void newConfig() throws ConfigurationException {
        // Prepare
        File configFile = getConfigFile();
        PluginConfigurable plugin = getPlugin();

        // Test
        assertFalse(configFile.exists());

        plugin.saveDefaultConfig();
        assertTrue(configFile.exists());

        assertEquals(Config.PLUGIN_RESOURCE.getString(), Files.read(configFile));

        List<? extends Number> numbers = plugin.getConfig().getIntList("test-lists.numbers");
        assertEquals(2, numbers.size());
    }

    @Test
    public void withoutDefaults() throws ConfigurationException {
        // Prepare
        File configFile = getConfigFile();
        Files.copy(Config.USER_EDITED.getFile(), configFile);
        PluginConfigurable plugin = getPlugin();

        // Should not overwrite
        plugin.saveDefaultConfig();

        // Keep user header
        plugin.getConfig().options().copyHeader(false);
        plugin.getConfig().options().copyDefaults(false);

        // Test before saving
        assertEquals("100.001", plugin.getConfig().get("wrong-type.level.c"));
        assertEquals("ok", plugin.getConfig().get("wrong-type.level.b"));

        // Remove path
        plugin.getConfig().set("wrong-place.second", null);

        // Save to file
        plugin.saveConfig();

        // Test after saving
        assertEquals("100.001", plugin.getConfig().get("wrong-type.level.c"));
        assertEquals(Files.read(Config.EXPECTED_WITHOUT_DEFAULT.getFile()).replace("  second: 2\n", ""), Files.read(configFile));
    }

    @Test
    public void withDefaults() {
        // Prepare
        File configFile = getConfigFile();
        Files.copy(Config.USER_EDITED.getFile(), configFile);
        PluginConfigurable plugin = getPlugin();

        // Should not overwrite
        plugin.saveDefaultConfig();

        // Keep user header
        plugin.getConfig().options().copyHeader(true);
        plugin.getConfig().options().copyDefaults(true);

        // Test before saving
        assertEquals("100.001", plugin.getConfig().get("wrong-type.level.c"));
        assertEquals("ok", plugin.getConfig().get("wrong-type.level.b"));

        // Defaults should override
        plugin.getConfig().set("wrong-place", null);

        // Save to file
        plugin.saveConfig();

        // Test after saving
        assertEquals(true, plugin.getConfig().get("wrong-type.level.c"));
        assertEquals(Files.read(Config.EXPECTED_WITH_DEFAULT.getFile()), Files.read(configFile));
    }

    @Test
    public void runtimeChanges() throws ConfigurationException {
        // Prepare
        File configFile = getConfigFile();
        PluginConfigurable plugin = getPlugin();

        // Modify options
        plugin.getConfig().options().header("LINE 1\nLINE 2\n");
        plugin.getConfig().options().copyDefaults(false);

        // Clear config
        for (String s : plugin.getConfig().getKeys()) plugin.getConfig().set(s, null);

        // Section
        FileConfiguration section = new FileConfiguration();
        section.set("section", Byte.MAX_VALUE);
        plugin.getConfig().set("test", section);

        // Test
        String expected = "# LINE 1\n" +
                "# LINE 2\n" +
                "\n" +
                "test:\n" +
                "  section: 127\n";

        assertEquals(expected, plugin.getConfig().saveToString());

    }


}
