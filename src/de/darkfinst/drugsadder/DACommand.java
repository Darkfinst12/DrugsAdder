package de.darkfinst.drugsadder;

import org.bukkit.command.*;
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
        if (args[0].equalsIgnoreCase("listStructure")) {
            loader.msg(commandSender, "---------------------- Structure List ----------------------");
            loader.getStructureList().forEach(structure -> {
                loader.msg(commandSender, "Structure:" + Arrays.toString(structure.getBody().blocks.toArray()));
            });
            loader.msg(commandSender, "------------------------------------------------------------");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
