package fr.onecraft.config;

public class ConfigurationOptions {

    private final FileConfiguration configuration;
    private String header = null;
    private boolean copyHeader = true;
    private boolean copyDefaults = false;

    protected ConfigurationOptions(FileConfiguration configuration) {
        this.configuration = configuration;
    }

    public FileConfiguration configuration() {
        return configuration;
    }

    public boolean copyDefaults() {
        return copyDefaults;
    }

    public ConfigurationOptions copyDefaults(boolean value) {
        this.copyDefaults = value;
        return this;
    }

    public String header() {
        return header;
    }

    public ConfigurationOptions header(String value) {
        this.header = value;
        return this;
    }
    public boolean copyHeader() {
        return copyHeader;
    }

    public ConfigurationOptions copyHeader(boolean value) {
        copyHeader = value;
        return this;
    }

}
