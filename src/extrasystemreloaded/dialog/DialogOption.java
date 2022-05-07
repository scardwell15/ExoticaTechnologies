package extrasystemreloaded.dialog;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.util.ExtraSystems;

public abstract class DialogOption {
    public abstract String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es);

    public void execute(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin) {

    }

    public boolean canBeConsumedByState(DialogState state) {
        return true;
    }

    public boolean requiresFleetMember() {
        return false;
    }

    public final void addToOptions(OptionPanelAPI options, ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        addToOptions(options, plugin, fm, es, null, -1);
    }

    public final void addToOptions(OptionPanelAPI options, ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es, int hotkey) {
        addToOptions(options, plugin, fm, es, null, hotkey);
    }

    public final void addToOptions(OptionPanelAPI options, ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es, String tooltip) {
        addToOptions(options, plugin, fm, es, tooltip, -1);
    }

    public void addToOptions(OptionPanelAPI options, ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es, String tooltip, int hotkey) {
        if(options.hasOption(this)) {
            options.setOptionText(getOptionText(plugin, fm, es), this);
            return;
        }

        options.addOption(getOptionText(plugin, fm, es), this, tooltip);

        if(hotkey >= 0) {
            options.setShortcut(this, hotkey, false, false, false, true);
        }
    }

    public void hovered(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin) {
    }
}
