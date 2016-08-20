package fr.onecraft.config;

import fr.onecraft.config.exception.ConfigurationException;
import fr.onecraft.config.exception.IOConfigurationException;
import fr.onecraft.config.exception.InvalidConfigurationException;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.*;
import java.util.*;

/**
 * YML Configuration
 */
public class FileConfiguration {

    // ----------------------------------------
    // VARIABLES
    // ----------------------------------------

    private final ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);

    protected Configuration config = new Configuration();
    protected FileConfiguration defaults = null;
    protected ConfigurationOptions options = null;

    // ----------------------------------------
    // LOAD
    // ----------------------------------------

    public void load(File file) throws ConfigurationException {
        load(file, null);
    }

    public void load(String file) throws ConfigurationException {
        load(file, null);
    }

    public void load(Reader reader) throws ConfigurationException {
        load(reader, null);
    }

    public void load(InputStream is) throws ConfigurationException {
        load(is, null);
    }

    public void load(String file, FileConfiguration defaults) throws ConfigurationException {
        load(new File(file), defaults);
    }

    public void load(File file, FileConfiguration defaults) throws ConfigurationException {
        try (Reader reader = new FileReader(file)) {
            load(reader, defaults);
        } catch (IOException e) {
            throw new IOConfigurationException(e);
        }
    }

    public void load(Reader reader, FileConfiguration defaults) throws ConfigurationException {

        StringBuilder builder = new StringBuilder();

        try (BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {

            String line;
            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        } catch (IOException e) {
            throw new IOConfigurationException(e);
        }

        loadFromString(builder.toString(), defaults);
    }

    public void load(InputStream is, FileConfiguration defaults) throws ConfigurationException {
        // Reader used by org.yaml.snakeyaml.Yaml
        load(new UnicodeReader(is), defaults);
    }

    public void loadFromString(String contents) throws ConfigurationException {
        loadFromString(contents, null);
    }

    public void loadFromString(String contents, FileConfiguration defaults) throws ConfigurationException {

        try {
            config = provider.load(contents, defaults != null ? defaults.config : null);
        } catch (YAMLException e) {
            throw new InvalidConfigurationException(e);
        } catch (ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }

        String header = parseHeader(contents);
        if (!header.isEmpty()) {
            options().header(header);
        }

        this.defaults = defaults;
    }

    // ----------------------------------------
    // SAVE
    // ----------------------------------------

    public void save(File file) throws ConfigurationException {
        try (Writer writer = new FileWriter(file)) {
            save(writer);
        } catch (IOException e) {
            throw new IOConfigurationException(e);
        }
    }

    public void save(Writer writer) throws ConfigurationException {
        try {
            String header = buildHeader();
            if (header != null) {
                writer.write(header);
            }
            if (defaults != null && defaults.config != null && options().copyDefaults()) {
                copyDefaults(config, defaults.config);
            }
            provider.save(config, writer);
        } catch (YAMLException | IOException e) {
            throw new IOConfigurationException(e);
        }
    }

    public String saveToString() throws ConfigurationException {
        try (StringWriter writer = new StringWriter()) {
            save(writer);
            return writer.toString();
        } catch (IOException e) {
            throw new IOConfigurationException(e);
        }
    }

    // ----------------------------------------
    // GETTERS
    // ----------------------------------------

    public Configuration getConfig() {
        return config;
    }

    public FileConfiguration getDefaults() {
        return defaults;
    }

    public ConfigurationOptions options() {
        if (options == null) {
            options = new ConfigurationOptions(this);
        }
        return options;
    }

    // ----------------------------------------
    // INTERNAL
    // ----------------------------------------

    protected static final String COMMENT_PREFIX = "# ";

    protected String buildHeader() {

        // Get default header if exists
        if (options().copyHeader()) {
            FileConfiguration def = getDefaults();

            if (def != null) {
                String defaultsHeader = def.buildHeader();

                if (defaultsHeader != null && !defaultsHeader.isEmpty()) {
                    return defaultsHeader;
                }
            }
        }

        // Copy header from current config
        String header = options().header();
        if (header == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        String[] lines = header.split("\r?\n", -1);
        boolean startedHeader = false;

        for (int i = lines.length - 1; i >= 0; i--) {
            builder.insert(0, "\n");

            if (startedHeader || !lines[i].isEmpty()) {
                builder.insert(0, lines[i]);
                builder.insert(0, COMMENT_PREFIX);
                startedHeader = true;
            }
        }

        return builder.toString();
    }

    protected String parseHeader(String input) {
        String[] lines = input.split("\r?\n", -1);
        StringBuilder result = new StringBuilder();
        boolean foundHeader = false;

        for (String line : lines) {
            if (line.startsWith(COMMENT_PREFIX)) {

                if (result.length() > 0) {
                    result.append("\n");
                }

                if (line.length() > COMMENT_PREFIX.length()) {
                    result.append(line.substring(COMMENT_PREFIX.length()));
                }

                foundHeader = true;

            } else if (line.isEmpty()) {
                if (foundHeader) {
                    result.append("\n");
                }
            } else {
                break;
            }
        }

        return result.toString();
    }

    protected void copyDefaults(Configuration input, Configuration def) {

        // Get keys of current path
        Collection<String> inputKeys = input.getKeys();
        Collection<String> defKeys = def.getKeys();

        // Looping through default keys
        for (String key : defKeys) {
            if (!inputKeys.contains(key)) {
                // Missing key in user configuration
                input.set(key, def.get(key));
            } else {
                // Get values
                Object inputValue = input.get(key);
                Object defValue = def.get(key);

                // Sometimes a Map is returned instead of a Configuration
                Configuration inputSection = inputValue instanceof Map ? input.getSection(key) : null;
                Configuration defSection = defValue instanceof Map ? def.getSection(key) : null;

                if (defSection != null) {
                    // There is a default section
                    if (inputSection != null) {
                        // And a user section, so apply default into it
                        copyDefaults(inputSection, defSection);
                    } else {
                        // But no user section, so just copy it
                        input.set(key, defSection);
                    }
                }

                if (inputValue == null) {
                    // No user value
                    input.set(key, defValue);
                } else if (defValue instanceof Configuration) {
                    // There is a default section
                    if (inputValue instanceof Configuration) {
                        // And a user section, so apply default into it
                        copyDefaults((Configuration) inputValue, (Configuration) defValue);
                    } else {
                        // But no user section, so just copy it
                        input.set(key, defValue);
                    }
                } else if (!inputValue.getClass().equals(defValue.getClass())) {
                    // Values are not the same type
                    // We get the base class, as for instance Integer ≠ Float but both are Numbers
                    Class inputClass = getBaseClass(inputValue);
                    Class defClass = getBaseClass(defValue);
                    if (inputClass == null || !inputClass.equals(defClass)) {
                        // Still different types, so override user value
                        input.set(key, defValue);
                    }
                }
            }
        }

        reorderKeys(input, def);
    }

    private boolean reorderKeys(Configuration input, Configuration def) {

        // Get keys of current path
        Collection<String> inputKeys = input.getKeys();
        Collection<String> defKeys = def.getKeys();

        // It won't work if we have more default keys
        if (inputKeys.size() < defKeys.size()) return false;

        // We ensure that input has at least all default keys
        for (String key : defKeys) if (!inputKeys.contains(key)) return false;

        // Iterate through both keys
        Iterator<String> inputIterator = inputKeys.iterator();
        for (String defKey : defKeys) {

            // Should never happen
            if (!inputIterator.hasNext()) return false;

            String inputKey = inputIterator.next();

            // We have different key
            if (!defKey.equals(inputKey)) {

                // So we need to reorder
                Map<String, Object> cache = new HashMap<>();

                // Move user config to cache
                for (String key : inputKeys) {
                    cache.put(key, input.get(key));
                    input.set(key, null);
                }

                // Add in right order
                for (String key : defKeys) {
                    input.set(key, cache.remove(key));
                }

                // Add remaining user keys
                for (Map.Entry<String, Object> entry : cache.entrySet()) {
                    input.set(entry.getKey(), entry.getValue());
                }

                // Stop here, keys are reordered
                return true;
            }
        }

        // No reorder done
        return true;
    }

    private Class getBaseClass(Object object) {
        Class<?>[] classes = { Configuration.class, Boolean.class, Number.class, Character.class, String.class, List.class };
        for (Class<?> clazz : classes) {
            if (clazz.isInstance(object)) {
                return clazz;
            }
        }
        return null;
    }

    // ----------------------------------------
    // DELEGATE CONFIGURATION METHODS
    // ----------------------------------------

    public <T> T get(String path, T def) {
        return getConfig().get(path, def);
    }

    public Object get(String path) {
        return getConfig().get(path);
    }

    public Object getDefault(String path) {
        return getConfig().getDefault(path);
    }

    public void set(String path, Object value) {
        getConfig().set(path, value instanceof FileConfiguration ? ((FileConfiguration) value).config : value);
    }

    public Configuration getSection(String path) {
        return getConfig().getSection(path);
    }

    public Collection<String> getKeys() {
        return getConfig().getKeys();
    }

    public byte getByte(String path) {
        return getConfig().getByte(path);
    }

    public byte getByte(String path, byte def) {
        return getConfig().getByte(path, def);
    }

    public List<Byte> getByteList(String path) {
        return getConfig().getByteList(path);
    }

    public short getShort(String path) {
        return getConfig().getShort(path);
    }

    public short getShort(String path, short def) {
        return getConfig().getShort(path, def);
    }

    public List<Short> getShortList(String path) {
        return getConfig().getShortList(path);
    }

    public int getInt(String path) {
        return getConfig().getInt(path);
    }

    public int getInt(String path, int def) {
        return getConfig().getInt(path, def);
    }

    public List<Integer> getIntList(String path) {
        return getConfig().getIntList(path);
    }

    public long getLong(String path) {
        return getConfig().getLong(path);
    }

    public long getLong(String path, long def) {
        return getConfig().getLong(path, def);
    }

    public List<Long> getLongList(String path) {
        return getConfig().getLongList(path);
    }

    public float getFloat(String path) {
        return getConfig().getFloat(path);
    }

    public float getFloat(String path, float def) {
        return getConfig().getFloat(path, def);
    }

    public List<Float> getFloatList(String path) {
        return getConfig().getFloatList(path);
    }

    public double getDouble(String path) {
        return getConfig().getDouble(path);
    }

    public double getDouble(String path, double def) {
        return getConfig().getDouble(path, def);
    }

    public List<Double> getDoubleList(String path) {
        return getConfig().getDoubleList(path);
    }

    public boolean getBoolean(String path) {
        return getConfig().getBoolean(path);
    }

    public boolean getBoolean(String path, boolean def) {
        return getConfig().getBoolean(path, def);
    }

    public List<Boolean> getBooleanList(String path) {
        return getConfig().getBooleanList(path);
    }

    public char getChar(String path) {
        return getConfig().getChar(path);
    }

    public char getChar(String path, char def) {
        return getConfig().getChar(path, def);
    }

    public List<Character> getCharList(String path) {
        return getConfig().getCharList(path);
    }

    public String getString(String path) {
        return getConfig().getString(path);
    }

    public String getString(String path, String def) {
        return getConfig().getString(path, def);
    }

    public List<String> getStringList(String path) {
        return getConfig().getStringList(path);
    }

    public List<?> getList(String path) {
        return getConfig().getList(path);
    }

    public List<?> getList(String path, List<?> def) {
        return getConfig().getList(path, def);
    }

}
