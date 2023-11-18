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
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GiveCommand {

    /**
     * Handels the give command and calls the right method to execute it
     * <br>
     * args[0] = type of item
     * <br>
     * args[1] = id of the item
     * <br>
     * args[2] = amount of the item
     * <br>
     * Possible arguments: {@link PossibleArgs}
     *
     * @param commandSender The sender of the command
     * @param args          The arguments of the command
     */
    public static void execute(@NotNull CommandSender commandSender, @NotNull String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(DA.loader.languageReader.getComponentWithFallback("Command_Assistance_Give"));
        } else {
            if (commandSender instanceof Player player) {
                try {
                    GiveCommand.PossibleArgs possibleArgs = GiveCommand.PossibleArgs.valueOfIgnoreCase(args[0]);
                    if (!commandSender.hasPermission(Objects.requireNonNull(possibleArgs).getPermission())) {
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
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NotPlayer"));
            }
        }
    }

    /**
     * Gives the player the given number of plants
     *
     * @param player The player, which should receive the plants
     * @param id     The id of the plant
     * @param amount The number of plants to give
     */
    private static void plants(@NotNull Player player, @NotNull String id, int amount) {
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

    /**
     * Gives the player the given number of custom items
     *
     * @param player       The player, which should receive the custom items
     * @param namespacedID The namespacedID of the custom item
     * @param amount       The number of custom items to give
     */
    private static void customItems(@NotNull Player player, @NotNull String namespacedID, int amount) {
        if (!player.hasPermission(PossibleArgs.CUSTOM_ITEMS.getPermission())) {
            DA.loader.msg(player, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
        }
        DAItem daItem = DAUtil.getItemStackByNamespacedID(namespacedID);
        if (daItem == null) {
            DA.loader.msg(player, DA.loader.languageReader.getComponentWithFallback("Command_Error_CustomItemNotFound"));
            return;
        }
        ItemStack itemStack = daItem.getItemStack();
        itemStack.setAmount(amount);
        DAUtil.addToInventory(player.getInventory(), itemStack);
        DA.loader.msg(player, DA.loader.languageReader.getComponentWithFallback("Command_Info_CustomItemGiven", namespacedID, amount + ""));
    }

    /**
     * Gives the player the given number of drugs
     *
     * @param player The player, which should receive the drugs
     * @param id     The id of the drug
     * @param amount The number of drugs to give
     */
    private static void drugs(@NotNull Player player, @NotNull String id, int amount) {
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


    /**
     * Manges the tab completion for the give command
     *
     * @param sender The sender of the command
     * @param args   The arguments of the command
     * @return A list of possible arguments
     */
    public static @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String[] args) {
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
                return Collections.singletonList(DA.loader.languageReader.getString("Command_Arg_Amount"));
            }
        }
        return new ArrayList<>();

    }

    /**
     * This enum contains all possible arguments for the give command
     */
    @Getter
    public enum PossibleArgs {
        DRUGS("Command_Arg_Drugs", "drugsadder.cmd.give.drugs"),
        CUSTOM_ITEMS("Command_Arg_CustomItems", "drugsadder.cmd.give.customitems"),
        PLANTS("Command_Arg_Plants", "drugsadder.cmd.give.plants"),
        ;
        private final String languageKey;
        private final String permission;

        PossibleArgs(@NotNull String languageKey, @NotNull String permission) {
            this.languageKey = languageKey;
            this.permission = permission;
        }

        public @NotNull String getArg() {
            return DA.loader.languageReader.getString(languageKey);
        }

        public static @Nullable PossibleArgs valueOfIgnoreCase(@Nullable String translation) {
            return Arrays.stream(PossibleArgs.values()).filter(possibleArgs -> possibleArgs.getArg().equalsIgnoreCase(translation)).findFirst().orElse(null);
        }
    }

}
