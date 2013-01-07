package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.flags.FlagManager.Value;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;

public class FlagTags implements Listener {

    Denizen denizen;
    
    public FlagTags(Denizen denizen) {
        this.denizen = denizen;
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    // FOR LISTENER
    
    private enum ReplaceType { ASSTRING, ASINT, ASDOUBLE, ASLIST, ASMONEY }

    /**
     * Replaces FLAG TAGs. Called automatically by the dScript ScriptBuilder and Executer.
     * 
     * @param event 
     *      ReplaceableTagEvent
     */
    
    @EventHandler
    public void flagTag(ReplaceableTagEvent event) {
        if (!event.matches("FLAG")) return;

        // Replace <FLAG...> TAGs.
        String flagName = event.getValue().split(":").length > 1
                ? event.getValue().split(":")[0].toUpperCase() : event.getValue().toUpperCase();
        String flagFallback = event.getFallback() != null ? event.getFallback() : "EMPTY";
        int index = -1;
        ReplaceType replaceType = ReplaceType.ASSTRING;

        // Get format, if specified
        if (flagName.contains("\\.")) {
            if (flagName.split("\\.")[1].equalsIgnoreCase("ASSTRING")) replaceType = ReplaceType.ASSTRING;
            else if (flagName.split("\\.")[1].equalsIgnoreCase("ASCSLIST")) replaceType = ReplaceType.ASLIST;
            else if (flagName.split("\\.")[1].equalsIgnoreCase("ASINT")) replaceType = ReplaceType.ASINT;
            else if (flagName.split("\\.")[1].equalsIgnoreCase("ASDOUBLE")) replaceType = ReplaceType.ASDOUBLE;
            else if (flagName.split("\\.")[1].equalsIgnoreCase("ASMONEY")) replaceType = ReplaceType.ASMONEY;
        }

        // Get index, if specified
        if (flagName.contains("[")) {
            index = Integer.valueOf(flagName.split("\\[")[1].replace("]", ""));
            flagName = flagName.split("\\[")[0];
        }

        // Check flag replacement type
        if (event.getType().toUpperCase().startsWith("G")) {
            if (denizen.flagManager().getGlobalFlag(flagName).get(index).isEmpty()) {
                dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' flag not found, using fallback!", flagName);
                // event.setReplaceable(flagFallback);
            } else {
                dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value.", flagName);
                event.setReplaceable(getReplaceable(denizen.flagManager().getGlobalFlag(flagName).get(index), replaceType));
            }

        } else if (event.getType().toUpperCase().startsWith("D") || event.getType().toUpperCase().startsWith("N")) {
            if (denizen.flagManager().getNPCFlag(event.getNPC().getId(), flagName).get(index).isEmpty()) {
                dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' flag not found, using fallback!", flagName);
                // event.setReplaceable(flagFallback);
            } else {
                dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value.", flagName);
                event.setReplaceable(getReplaceable(denizen.flagManager().getNPCFlag(event.getNPC().getId(), flagName).get(index), replaceType));
            }

        } else if (event.getType().toUpperCase().startsWith("P")) {
            if (denizen.flagManager().getPlayerFlag(event.getPlayer().getName(), flagName).get(index).isEmpty()) {
                dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' flag not found, using fallback!", flagName);
                // event.setReplaceable(flagFallback);
            } else {
                dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value.", flagName);
                event.setReplaceable(getReplaceable(
                        denizen.flagManager().getPlayerFlag(event.getPlayer().getName(), flagName).get(index), replaceType));
            }
        }               
    }

    private String getReplaceable(Value flag, ReplaceType replaceType) {
        switch (replaceType) {
        case ASINT:
            return String.valueOf(flag.asInteger());
        case ASDOUBLE:
            return String.valueOf(flag.asDouble());
        case ASSTRING:
            return flag.asString();
        case ASLIST:
            return String.valueOf(flag.asCommaSeparatedList());
        case ASMONEY:
            DecimalFormat d = new DecimalFormat("0.00");
            return String.valueOf(d.format(flag.asDouble()));
        }
        return null;
    }
    
}