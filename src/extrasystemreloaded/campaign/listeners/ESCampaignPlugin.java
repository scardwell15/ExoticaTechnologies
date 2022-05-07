package extrasystemreloaded.campaign.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;

public class ESCampaignPlugin extends BaseCampaignPlugin {
    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        if (interactionTarget instanceof CampaignFleetAPI
            && !Global.getSettings().getModManager().isModEnabled("nexerelin")) {
            return new PluginPick<InteractionDialogPlugin>(new ESRFleetInteractionDialogPluginImpl(), PickPriority.MOD_GENERAL);
        }
        return null;
    }
}
