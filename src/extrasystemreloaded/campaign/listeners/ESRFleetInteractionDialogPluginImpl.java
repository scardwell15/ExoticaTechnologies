package extrasystemreloaded.campaign.listeners;

import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;

/**
 * This plugin is only used if Nexerelin is not installed.
 * Changes from vanilla:
 * Fire the UpdateEngagementChoice trigger after updateEngagementChoice.
 */
public class ESRFleetInteractionDialogPluginImpl extends FleetInteractionDialogPluginImpl {
    @Override
    protected void updateEngagementChoice(boolean withText) {
        super.updateEngagementChoice(withText);
        conversationDelegate.fireAll("UpdateEngagementChoice");
    }
}
