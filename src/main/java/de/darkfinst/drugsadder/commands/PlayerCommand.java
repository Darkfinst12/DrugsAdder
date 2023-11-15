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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerCommand {

    //Handel Command
    //arg[0] = targetPlayer
    //arg[1] = action
    //arg[2] = drug (if required)
    //arg[3] = amount (if required)
    public static void execute(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Assistance_Player"));
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
            try {
                PossibleArgs possibleArgs = PossibleArgs.valueOfIgnoreCase(args[1]);
                if (!commandSender.hasPermission(possibleArgs.getPermission())) {
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

    private static void set(CommandSender commandSender, OfflinePlayer target, String drugID, int amount) {
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
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Player_Set", target.getName(), drugID, amount + ""));
        }
    }

    private static void get(CommandSender commandSender, OfflinePlayer offlinePlayer, String drugID) {
        DAPlayer daPlayer = DA.loader.getDaPlayer(offlinePlayer);
        if (daPlayer == null) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_PlayerNotAddicted", offlinePlayer.getName()));
        } else {
            int addictionPoints = daPlayer.getAddiction(drugID);
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Player_Get", offlinePlayer.getName(), drugID, addictionPoints + ""));
        }
    }

    private static void remove(CommandSender commandSender, OfflinePlayer offlinePlayer, String drugID) {
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

    private static void clear(CommandSender commandSender, OfflinePlayer offlinePlayer) {
        DAPlayer daPlayer = DA.loader.getDaPlayer(offlinePlayer);
        if (daPlayer == null) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_PlayerNotAddicted", offlinePlayer.getName()));
        } else {
            daPlayer.clearAddictions();
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Player_Clear", offlinePlayer.getName()));
        }
    }

    private static void info(CommandSender commandSender, OfflinePlayer offlinePlayer) {
        DAPlayer daPlayer = DA.loader.getDaPlayer(offlinePlayer);
        if (daPlayer == null) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_PlayerNotAddicted", offlinePlayer.getName()));
        } else {
            AtomicReference<Component> component = new AtomicReference<>(DA.loader.languageReader.getComponentWithFallback("Command_Player_Info", offlinePlayer.getName()));
            daPlayer.getAddicted().forEach((daDrug, integer) -> component.set(component.get().appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Command_Player_Info_Drug", daDrug.getID(), integer + ""))));
            DA.loader.msg(commandSender, component.get());
        }
    }


    //TabComplete
    public static @NotNull List<String> complete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
                return Collections.singletonList(DA.loader.languageReader.getString("Command_Args_Amount"));
            }
        }
        return new ArrayList<>();
    }

    //Enum for possible arguments
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

        PossibleArgs(String languageKey, String permission) {
            this.languageKey = languageKey;
            this.permission = permission;
        }

        public String getArg() {
            return DA.loader.languageReader.getString(languageKey);
        }

        public static PossibleArgs valueOfIgnoreCase(String translation) {
            return Arrays.stream(PossibleArgs.values())
                    .filter(possibleArgs -> possibleArgs.getArg().equalsIgnoreCase(translation))
                    .findFirst()
                    .orElse(null);
        }

    }
}
