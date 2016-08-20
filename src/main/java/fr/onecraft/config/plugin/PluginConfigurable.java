package fr.onecraft.config.plugin;

import fr.onecraft.config.PluginConfiguration;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Plugin with config.yml
 */
public abstract class PluginConfigurable extends Plugin {

    private PluginConfiguration config = null;

    public void saveDefaultConfig() {
        getConfig().saveDefault();
    }

    public PluginConfiguration getConfig() {
        if (config == null) {
            config = new PluginConfiguration(this, "config.yml");
        }
        return config;
    }

    public void reloadConfig() {
        getConfig().reload();
    }

    public void saveConfig() {
        getConfig().save();
    }

}
