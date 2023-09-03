package de.darkfinst.drugsadder;

import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.api.events.drug.DrugAddAddictionEvent;
import de.darkfinst.drugsadder.api.events.drug.DrugReduceAddictionEvent;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class DAPlayer {

    @Getter
    @NotNull
    private final Player player;

    private final HashMap<DADrug, Integer> addicted = new HashMap<>();


    public DAPlayer(@NotNull Player player) {
        this.player = player;
    }

    public void consumeDrug(DADrug daDrug) {
        if (daDrug.isAddictionAble()) {
            int addictionPointsOld = this.addicted.getOrDefault(daDrug, 0);
            int addictionPoints = addictionPointsOld + daDrug.getAddictionPoints();
            DrugAddAddictionEvent drugAddAddictionEvent = new DrugAddAddictionEvent(daDrug, addictionPointsOld, addictionPoints);
            Bukkit.getPluginManager().callEvent(drugAddAddictionEvent);
            if (!drugAddAddictionEvent.isCancelled()) {
                addictionPoints = drugAddAddictionEvent.getNewAddiction();
                this.addicted.put(daDrug, addictionPoints);
            } else {
                addictionPoints = addictionPointsOld;
            }
            if (!daDrug.getConsummation().isEmpty()) {
                int[] keys = daDrug.getConsummation().keySet().stream().mapToInt(i -> i).toArray();
                int closest = DAUtil.findClosest(keys, addictionPoints);
                daDrug.getConsummation().get(closest).forEach(daEffect -> daEffect.applyEffect(this.player));
            }
        } else {
            daDrug.getDaEffects().forEach(daEffect -> daEffect.applyEffect(this.player));
            daDrug.getServerCommands().forEach(commandLine -> DA.getInstance.getServer().dispatchCommand(DA.getInstance.getServer().getConsoleSender(), commandLine.replace("%player%", this.player.getName())));
            daDrug.getPlayerCommands().forEach(commandLine -> DA.getInstance.getServer().dispatchCommand(this.player, commandLine));
        }
        if (daDrug.getConsumeMessage() != null) {
            DA.loader.msg(this.player, ChatColor.translateAlternateColorCodes('&', daDrug.getConsumeMessage()), DrugsAdderSendMessageEvent.Type.PLAYER);
        }
        if (daDrug.getConsumeTitle() != null) {
            this.player.sendTitle(ChatColor.translateAlternateColorCodes('&', daDrug.getConsumeTitle()), "", 10, 70, 20);
        }
    }

    public void reduceAddictions() {
        this.addicted.forEach((daDrug, integer) -> reduceAddiction(daDrug, false));
    }

    public void clearAddictions() {
        this.addicted.clear();
        this.checkToRemove();
    }

    public void clearAddiction(DADrug daDrug) {
        this.addicted.remove(daDrug);
        this.checkToRemove();
    }

    public void reduceAddiction(DADrug daDrug, boolean isAsync) {
        int addictionPoints = this.addicted.getOrDefault(daDrug, 0);
        reduceAddiction(daDrug, addictionPoints, isAsync);
    }

    private void reduceAddiction(DADrug daDrug, Integer addictionPoints, boolean isAsync) {
        if (addictionPoints > 0) {
            addictionPoints -= daDrug.getReductionAmount();
            DrugReduceAddictionEvent drugReduceAddictionEvent = new DrugReduceAddictionEvent(daDrug, addictionPoints + daDrug.getReductionAmount(), addictionPoints, isAsync);
            Bukkit.getPluginManager().callEvent(drugReduceAddictionEvent);
            if (drugReduceAddictionEvent.isCancelled()) {
                return;
            }
            addictionPoints = drugReduceAddictionEvent.getNewAddiction();
            if (addictionPoints < 0) {
                addictionPoints = 0;
                this.addicted.remove(daDrug);
            } else {
                this.addicted.put(daDrug, addictionPoints);
            }

            if (!daDrug.getDeprivation().isEmpty()) {
                int[] keys = daDrug.getDeprivation().keySet().stream().mapToInt(i -> i).toArray();
                int closest = DAUtil.findClosest(keys, addictionPoints);
                if (isAsync) {
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> daDrug.getDeprivation().get(closest).forEach(daEffect -> daEffect.applyEffect(this.player)));
                } else {
                    daDrug.getDeprivation().get(closest).forEach(daEffect -> daEffect.applyEffect(this.player));
                }
            }
        }
        this.checkToRemove();
    }

    private void checkToRemove() {
        if (this.addicted.isEmpty()) {
            DA.loader.removeDaPlayer(this);
        }
    }

}
