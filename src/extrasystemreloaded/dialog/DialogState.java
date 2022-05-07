package extrasystemreloaded.dialog;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;

import java.util.Map;

public abstract class DialogState extends DialogOption {
    public boolean clearsOptions() {
        return true;
    }

    public boolean consumesOptionPickedEvent(Object option) {
        return false;
    }

    public void optionPicked(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin, Object option) {

    }

    public void switchedToDifferentState(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin, DialogState newState) {
    }

    public void modifyResourcesPanel(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin, Map<String, Float> resourceCosts) {}
}
