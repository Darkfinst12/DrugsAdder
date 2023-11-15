package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DADrug;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.items.DAPlantItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GiveCommand {

    public static void execute(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(DA.loader.languageReader.getComponentWithFallback("Command_Assistance_Reload"));
        } else {
            if (commandSender instanceof Player player) {
                try {
                    GiveCommand.PossibleArgs possibleArgs = GiveCommand.PossibleArgs.valueOfIgnoreCase(args[0]);
                    if (!commandSender.hasPermission(possibleArgs.getPermission())) {
                        DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
                    }
                    switch (possibleArgs) {
                        case DRUGS ->
                                GiveCommand.drugs(player, args[1], args.length > 2 ? Integer.parseInt(args[2]) : 1);
                        case CUSTOM_ITEMS ->
                                GiveCommand.customItems(player, args[1], args.length > 2 ? Integer.parseInt(args[2]) : 1);
                        case PLANTS ->
                                GiveCommand.plants(player, args[1], args.length > 2 ? Integer.parseInt(args[2]) : 1);
                    }
                } catch (Exception e) {
                    DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_WrongArgs"));
                }
            } else {
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPlayer"));
            }
        }
    }

    private static void plants(Player player, String id, int amount) {
        if (!player.hasPermission(PossibleArgs.PLANTS.getPermission())) {
            DA.loader.msg(player, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
        }
        DAPlantItem daPlantItem = DAConfig.seedReader.getSeed(id);
        if (daPlantItem == null) {
            DA.loader.msg(player, DA.loader.languageReader.getComponentWithFallback("Command_Error_PlantNotFound"));
            return;
        }
        ItemStack itemStack = daPlantItem.getItemStack();
        itemStack.setAmount(amount);
        DAUtil.addToInventory(player.getInventory(), itemStack);
        DA.loader.msg(player, DA.loader.languageReader.getComponentWithFallback("Command_Info_PlantGiven", id, amount + ""));
    }

    private static void customItems(Player player, String id, int amount) {
        if (!player.hasPermission(PossibleArgs.CUSTOM_ITEMS.getPermission())) {
            DA.loader.msg(player, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
        }
        DAItem daItem = DAUtil.getItemStackByNamespacedID(id);
        if (daItem == null) {
            DA.loader.msg(player, DA.loader.languageReader.getComponentWithFallback("Command_Error_CustomItemNotFound"));
            return;
        }
        ItemStack itemStack = daItem.getItemStack();
        itemStack.setAmount(amount);
        DAUtil.addToInventory(player.getInventory(), itemStack);
        DA.loader.msg(player, DA.loader.languageReader.getComponentWithFallback("Command_Info_CustomItemGiven", id, amount + ""));
    }

    private static void drugs(Player player, String id, int amount) {
        if (!player.hasPermission(PossibleArgs.DRUGS.getPermission())) {
            DA.loader.msg(player, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
        }
        DADrug daDrug = DAConfig.drugReader.getDrug(id);
        if (daDrug == null) {
            DA.loader.msg(player, DA.loader.languageReader.getComponentWithFallback("Command_Error_DrugNotFound"));
            return;
        }
        ItemStack itemStack = daDrug.getItem().getItemStack();
        itemStack.setAmount(amount);
        DAUtil.addToInventory(player.getInventory(), itemStack);
        DA.loader.msg(player, DA.loader.languageReader.getComponentWithFallback("Command_Info_DrugGiven", id, amount + ""));
    }


    public static @NotNull List<String> complete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            return Arrays.stream(GiveCommand.PossibleArgs.values()).filter(possibleArgs -> sender.hasPermission(possibleArgs.getPermission())).map(GiveCommand.PossibleArgs::getArg).filter(possArg -> possArg.toLowerCase().contains(args[0])).toList();
        }
        PossibleArgs possibleArg = PossibleArgs.valueOfIgnoreCase(args[0]);
        if (possibleArg != null && sender.hasPermission(possibleArg.getPermission())) {
            if (args.length == 2) {
                switch (possibleArg) {
                    case DRUGS -> {
                        return DAConfig.drugReader.getRegisteredDrugs().stream().map(DADrug::getID).filter(possArg -> possArg.toLowerCase().contains(args[1])).toList();
                    }
                    case CUSTOM_ITEMS -> {
                        return DAConfig.customItemReader.getRegisteredItems().keySet().stream().filter(possArg -> possArg.toLowerCase().contains(args[1])).toList();
                    }
                    case PLANTS -> {
                        return DAConfig.seedReader.getSeedNames().stream().filter(possArg -> possArg.toLowerCase().contains(args[1])).toList();
                    }
                }

            }
            if (args.length == 3) {
                return Collections.singletonList(DA.loader.languageReader.getString("Command_Args_Amount"));
            }
        }
        return new ArrayList<>();

    }

    @Getter
    public enum PossibleArgs {
        DRUGS("Command_Arg_Drugs", "drugsadder.cmd.give.drugs"),
        CUSTOM_ITEMS("Command_Arg_CustomItems", "drugsadder.cmd.give.customitems"),
        PLANTS("Command_Arg_Plants", "drugsadder.cmd.give.plants"),
        ;
        private final String languageKey;
        private final String permission;

        PossibleArgs(String languageKey, String permission) {
            this.languageKey = languageKey;
            this.permission = permission;
        }

        public String getArg() {
            return DA.loader.languageReader.getString(languageKey);
        }

        public static PossibleArgs valueOfIgnoreCase(String translation) {
            return Arrays.stream(PossibleArgs.values()).filter(possibleArgs -> possibleArgs.getArg().equalsIgnoreCase(translation)).findFirst().orElse(null);
        }
    }

}
