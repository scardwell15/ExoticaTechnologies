package exoticatechnologies.campaign.rulecmd;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.listeners.CampaignEventListener;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.Map;

/**
 * for some reason defense fleets stopped showing my ship modifications hullmod. i can only assume this means that
 * it was never being applied, or i fucked somehting up somewhere and i'm not sure where.
 * i know the mods are being applied, but they disappear when the fleet is shown. so here i am, re-generating them.
 */
@Log4j
public class ETGenerateForDefenseFleet extends BaseCommandPlugin {

    @Override
    public boolean doesCommandAddOptions() {
        return false;
    }

    @Override
    public boolean execute(final String ruleId, final InteractionDialogAPI dialog, final List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        SectorEntityToken interactionTarget = dialog.getInteractionTarget();
        CampaignFleetAPI defenderFleet = (CampaignFleetAPI) interactionTarget;
        if (defenderFleet != null) {
            String newId = interactionTarget.getId();
            defenderFleet.setId(newId);
            CampaignEventListener.applyExtraSystemsToFleet(defenderFleet);
        }

        return false;
    }
}
