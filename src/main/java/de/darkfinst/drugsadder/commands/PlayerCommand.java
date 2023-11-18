package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DADrug;
import de.darkfinst.drugsadder.DAPlayer;
import de.darkfinst.drugsadder.filedata.DAConfig;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerCommand {


    /**
     * Handels the player command with the given arguments and calls the required method to execute the command
     * <br>
     * arg[0] = targetPlayer
     * <br>
     * arg[1] = action
     * <br>
     * arg[2] = drug (if required)
     * <br>
     * arg[3] = amount (if required)
     * <br>
     * Possible arguments: {@link PossibleArgs}
     *
     * @param commandSender The sender of the command
     * @param args          The arguments of the command
     */
    public static void execute(@NotNull CommandSender commandSender, @NotNull String[] args) {
        if (args.length == 0) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Assistance_Player"));
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
            try {
                PossibleArgs possibleArgs = PossibleArgs.valueOfIgnoreCase(args[1]);
                if (!commandSender.hasPermission(Objects.requireNonNull(possibleArgs).getPermission())) {
                    DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
                    return;
                }
                switch (possibleArgs) {
                    case SET -> PlayerCommand.set(commandSender, offlinePlayer, args[2], Integer.parseInt(args[3]));
                    case GET -> PlayerCommand.get(commandSender, offlinePlayer, args[2]);
                    case REMOVE -> PlayerCommand.remove(commandSender, offlinePlayer, args[2]);
                    case CLEAR -> PlayerCommand.clear(commandSender, offlinePlayer);
                    case INFO -> PlayerCommand.info(commandSender, offlinePlayer);
                }
            } catch (Exception e) {
                DA.log.logException(e);
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_WrongArgs"));
            }
        }
    }

    /**
     * Set the addiction of a player
     *
     * @param commandSender The sender of the command
     * @param target        The target player
     * @param drugID        The ID of the drug
     * @param amount        The amount of addiction
     */
    private static void set(@NotNull CommandSender commandSender, @NotNull OfflinePlayer target, @NotNull String drugID, int amount) {
        DAPlayer daPlayer = DA.loader.getDaPlayer(target);
        if (daPlayer == null) {
            daPlayer = new DAPlayer(target.getUniqueId());
            DA.loader.getDaPlayerList().add(daPlayer);
        }
        DADrug daDrug = DAConfig.drugReader.getDrug(drugID);
        if (daDrug == null) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_DrugNotFound", drugID));
        } else {
            daPlayer.addDrug(daDrug.getID(), amount);
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Player_Set", target.getName(), amount + "", drugID));
        }
    }

    /**
     * Get the addiction of a player
     *
     * @param commandSender The sender of the command
     * @param offlinePlayer The target player
     * @param drugID        The ID of the drug
     */
    private static void get(@NotNull CommandSender commandSender, @NotNull OfflinePlayer offlinePlayer, @NotNull String drugID) {
        DAPlayer daPlayer = DA.loader.getDaPlayer(offlinePlayer);
        if (daPlayer == null) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_PlayerNotAddicted", offlinePlayer.getName()));
        } else {
            int addictionPoints = daPlayer.getAddiction(drugID);
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Player_Get", offlinePlayer.getName(), drugID, addictionPoints + ""));
        }
    }

    /**
     * Remove the addiction of a player
     *
     * @param commandSender The sender of the command
     * @param offlinePlayer The target player
     * @param drugID        The ID of the drug
     */
    private static void remove(@NotNull CommandSender commandSender, @NotNull OfflinePlayer offlinePlayer, @NotNull String drugID) {
        DAPlayer daPlayer = DA.loader.getDaPlayer(offlinePlayer);
        if (daPlayer == null) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_PlayerNotAddicted", offlinePlayer.getName()));
        } else {
            DADrug daDrug = DAConfig.drugReader.getDrug(drugID);
            if (daDrug == null) {
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_DrugNotFound", drugID));
            } else {
                daPlayer.clearAddiction(daDrug);
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Player_Remove", offlinePlayer.getName(), drugID));
            }
        }
    }

    /**
     * Clear the addiction of a player
     *
     * @param commandSender The sender of the command
     * @param offlinePlayer The target player
     */
    private static void clear(@NotNull CommandSender commandSender, @NotNull OfflinePlayer offlinePlayer) {
        DAPlayer daPlayer = DA.loader.getDaPlayer(offlinePlayer);
        if (daPlayer == null || daPlayer.getAddicted().isEmpty()) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_PlayerNotAddicted", offlinePlayer.getName()));
        } else {
            daPlayer.clearAddictions();
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Player_Clear", offlinePlayer.getName()));
        }
    }

    /**
     * Get the addiction of a player
     *
     * @param commandSender The sender of the command
     * @param offlinePlayer The target player
     */
    private static void info(@NotNull CommandSender commandSender, @NotNull OfflinePlayer offlinePlayer) {
        DAPlayer daPlayer = DA.loader.getDaPlayer(offlinePlayer);
        if (daPlayer == null) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_PlayerNotAddicted", offlinePlayer.getName()));
        } else {
            AtomicReference<Component> component = new AtomicReference<>(DA.loader.languageReader.getComponentWithFallback("Command_Player_Info", offlinePlayer.getName()));
            daPlayer.getAddicted().forEach((daDrug, integer) -> component.set(component.get().appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Command_Player_Info_Drug", daDrug.getID(), integer + ""))));
            DA.loader.msg(commandSender, component.get());
        }
    }


    /**
     * Manages the tab completion for the player command
     *
     * @param sender The sender of the command
     * @param args   The arguments of the command
     * @return A list of possible completions
     */
    public static @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length <= 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().contains(args[0]))
                    .toList();
        } else if (args.length == 2) {
            return Arrays.stream(PossibleArgs.values())
                    .filter(possibleArgs -> sender.hasPermission(possibleArgs.getPermission()))
                    .map(PossibleArgs::getArg)
                    .filter(possArg -> possArg.toLowerCase().contains(args[1]))
                    .toList();
        } else if (args.length == 3) {
            //Handel SET, GET, REMOVE
            if ((args[1].equalsIgnoreCase(PossibleArgs.SET.getArg()) && sender.hasPermission(PossibleArgs.SET.getPermission()))
                    || (args[1].equalsIgnoreCase(PossibleArgs.GET.getArg()) && sender.hasPermission(PossibleArgs.GET.getPermission()))
                    || args[1].equalsIgnoreCase(PossibleArgs.REMOVE.getArg()) && sender.hasPermission(PossibleArgs.REMOVE.getPermission())) {
                return DAConfig.drugReader.getDrugNames().stream()
                        .filter(name -> name.contains(args[2].toLowerCase()))
                        .toList();
            }
        } else if (args.length == 4) {
            //Handel SET
            if (args[1].equalsIgnoreCase(PossibleArgs.SET.getArg()) && sender.hasPermission(PossibleArgs.SET.getPermission())) {
                return Collections.singletonList(DA.loader.languageReader.getString("Command_Arg_Amount"));
            }
        }
        return new ArrayList<>();
    }

    /**
     * The Enum for the possible arguments of the player command
     */
    @Getter
    public enum PossibleArgs {
        SET("Command_Arg_Set", "drugsadder.cmd.player.set"),
        GET("Command_Arg_Get", "drugsadder.cmd.player.get"),
        REMOVE("Command_Arg_Remove", "drugsadder.cmd.player.remove"),
        CLEAR("Command_Arg_Clear", "drugsadder.cmd.player.clear"),
        INFO("Command_Arg_Info", "drugsadder.cmd.player.info"),
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
            return Arrays.stream(PossibleArgs.values())
                    .filter(possibleArgs -> possibleArgs.getArg().equalsIgnoreCase(translation))
                    .findFirst()
                    .orElse(null);
        }

    }
}
