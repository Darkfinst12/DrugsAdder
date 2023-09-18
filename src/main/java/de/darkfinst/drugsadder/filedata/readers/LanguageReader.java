package de.darkfinst.drugsadder.filedata.readers;

import de.darkfinst.drugsadder.DA;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class LanguageReader {

    private final Map<String, String> entries = new HashMap<>(128);
    private final File file;

    public LanguageReader(File file, String defaultPath) {
        /* Load */
        this.file = file;

        FileConfiguration configFile = YamlConfiguration.loadConfiguration(file);

        Set<String> keySet = configFile.getKeys(false);
        for (String key : keySet) {
            entries.put(key, configFile.getString(key));
        }

        /* Check */
        check(defaultPath);
    }

    /**
     * This method is used to check if the language file is up-to-date.
     *
     * @param defaultPath The path to the default language file.
     */
    private void check(String defaultPath) {
        FileConfiguration defaults = null;
        String line;
        InputStream resource = DA.getInstance.getResource(defaultPath);
        if (resource == null) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
            while ((line = reader.readLine()) != null) {
                int index = line.indexOf(':');
                if (index != -1) {
                    String key = line.substring(0, index);
                    if (!entries.containsKey(key)) {
                        if (defaults == null) {
                            defaults = new YamlConfiguration();
                            defaults.load(new BufferedReader(new InputStreamReader(Objects.requireNonNull(DA.getInstance.getResource(defaultPath)))));
                        }
                        entries.put(key, defaults.getString(key));
                    }
                }
            }
        } catch (Exception e) {
            DA.loader.errorLog("Language File could not be updated");
            Arrays.stream(e.getStackTrace()).toList().forEach(stackTraceElement -> DA.loader.log(stackTraceElement.toString()));
        }
    }

    /**
     * This method is used to get a string from the language file.
     *
     * @param key  The key of the string.
     * @param args The arguments to replace the placeholders, if there are any.
     * @return The found string with the placeholders replaced. - If no entry is found, it will return "%placeholder%".
     */
    public String get(String key, String... args) {
        String entry = entries.get(key);

        if (entry != null) {
            int i = 0;
            for (String arg : args) {
                if (arg != null) {
                    i++;
                    entry = entry.replace("&v" + i, arg);
                }
            }
        } else {
            entry = String.format("Key: %s not found", key);
        }

        return entry;
    }


}
