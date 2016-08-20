package fr.onecraft.config;

import fr.onecraft.config.exception.ConfigurationException;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

/**
 * Configuration loaded from plugin
 */
public class PluginConfiguration extends FileConfiguration {

    protected final Plugin plugin;
    protected final String configPath;
    protected final File configFile;

    private boolean loaded = false;

    public PluginConfiguration(Plugin plugin, String configPath) {

        if (plugin == null) throw new IllegalArgumentException("plugin can't be null");
        if (configPath == null) throw new IllegalArgumentException("configPath can't be null");

        this.plugin = plugin;
        this.configPath = configPath.replace("/", File.separator);
        this.configFile = new File(plugin.getDataFolder(), configPath);

    }

    /**
     * Copy file in plugin folder if it doesn't exist
     *
     * @return true if file is created, false if an error occurred
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean saveDefault() {

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        if (!configFile.exists()) {
            try (InputStream in = plugin.getResourceAsStream(configPath)) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * Reload config from file
     */
    public boolean reload() {

        try (InputStream is = plugin.getResourceAsStream(configPath)) {
            FileConfiguration defaults = null;
            if (is != null) {
                defaults = new FileConfiguration();
                defaults.load(is);
            } else {
                plugin.getLogger().severe("Can't find resource \"" + configPath + "\" from plugin ! Is it packed in the JAR ?");
            }

            if (!configFile.exists()) {
                load("{}\n", defaults);
            } else {
                load(configFile, defaults);
            }
            return true;
        } catch (IOException | ConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Can't load config \"" + configPath + "\" !", e);
            return false;
        }
    }

    /**
     * Save config to file
     */
    public boolean save() {
        load();
        try {
            save(configFile);
            return true;
        } catch (ConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configPath, e);
            return false;
        }
    }

    @Override
    public Configuration getConfig() {
        load();
        return super.getConfig();
    }

    private void load() {
        if (!loaded) {
            loaded = true;
            reload();
        }
    }

}
