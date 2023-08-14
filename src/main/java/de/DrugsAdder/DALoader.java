package main.java.de.DrugsAdder;

import lombok.Getter;

@Getter
public class DALoader {

    private final DA plugin;


    public DALoader(DA plugin) {
        this.plugin = plugin;
    }

    public void init() {
    }

    public void unload() {
    }

}
