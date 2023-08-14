package main.java.de.DrugsAdder;

import lombok.Getter;
import main.java.de.DrugsAdder.filedata.DAConfig;

@Getter
public class DALoader {

    private final DA plugin;


    public DALoader(DA plugin) {
        this.plugin = plugin;
    }

    public void init() {
        this.initConfig();
    }

    private void initConfig() {

    }

    private void initCommands() {
        new DACommand().register();
    }

    public void unload() {
    }

}
