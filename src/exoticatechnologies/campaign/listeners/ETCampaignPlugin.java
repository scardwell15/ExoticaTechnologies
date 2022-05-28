package exoticatechnologies.campaign.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;

public class ETCampaignPlugin extends BaseCampaignPlugin {
    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        if (interactionTarget instanceof CampaignFleetAPI
            && !Global.getSettings().getModManager().isModEnabled("nexerelin")) {
            return new PluginPick<InteractionDialogPlugin>(new ETFleetInteractionDialogPluginImpl(), PickPriority.MOD_GENERAL);
        }
        return null;
    }
}
