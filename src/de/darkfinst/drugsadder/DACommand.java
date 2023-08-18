package de.darkfinst.drugsadder;

import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class DACommand implements CommandExecutor, TabCompleter {

    public void register() {
        PluginCommand command = DA.getInstance.getCommand("drugsadder");
        assert command != null;
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        DALoader loader = DA.loader;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                loader.reloadConfig();
                commandSender.sendMessage("Reloaded config");
            }
        } else if (args.length == 2 && commandSender instanceof Player player) {
            if (args[0].equalsIgnoreCase("getCustomItem")) {
               DAItem customItem = DAConfig.customItemReader.getCustomItem(args[1]);
               if(customItem != null){
                   player.getInventory().addItem(customItem.getItemStack());
               }
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
