package exoticatechnologies.dialog;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;

import java.util.Map;

public abstract class DialogState extends DialogOption {
    public boolean clearsOptions() {
        return true;
    }

    public boolean consumesOptionPickedEvent(Object option) {
        return false;
    }

    public void optionPicked(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, Object option) {

    }

    public void switchedToDifferentState(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, DialogState newState) {
    }

    public void modifyResourcesPanel(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts) {}
}
