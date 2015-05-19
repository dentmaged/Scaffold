package net.avicus.scaffold;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Scaffold extends JavaPlugin {

    private String saveDirectory;
    private String saveMessage;

    public void onEnable() {
        loadConfig();

        getCommand("scaffold").setExecutor(new ScaffoldCmd(this));
    }

    public void loadConfig() {
        if (!new File(getDataFolder(), "config.yml").exists())
            saveDefaultConfig();

        this.saveDirectory = getConfig().getString("save-directory");
        this.saveMessage = getConfig().getString("save-message");
    }

    public void onDisable() {

    }

    public String getSaveDirectory() {
        return saveDirectory;
    }

    public String getSaveMessage() {
        return saveMessage;
    }
}
