package exoticatechnologies.dialog;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.modifications.ShipModifications;

public abstract class DialogOption {
    public abstract String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods);

    public void execute(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {

    }

    public boolean canBeConsumedByState(DialogState state) {
        return true;
    }

    public boolean requiresFleetMember() {
        return false;
    }

    public final void addToOptions(OptionPanelAPI options, ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        addToOptions(options, plugin, fm, mods, null, -1);
    }

    public final void addToOptions(OptionPanelAPI options, ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods, int hotkey) {
        addToOptions(options, plugin, fm, mods, null, hotkey);
    }

    public final void addToOptions(OptionPanelAPI options, ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods, String tooltip) {
        addToOptions(options, plugin, fm, mods, tooltip, -1);
    }

    public void addToOptions(OptionPanelAPI options, ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods, String tooltip, int hotkey) {
        if(options.hasOption(this)) {
            options.setOptionText(getOptionText(plugin, fm, mods), this);
            return;
        }

        options.addOption(getOptionText(plugin, fm, mods), this, tooltip);

        if(hotkey >= 0) {
            options.setShortcut(this, hotkey, false, false, false, true);
        }
    }

    public void hovered(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {
    }
}
