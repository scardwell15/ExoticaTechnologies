package exoticatechnologies.campaign.rulecmd;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.ScanUtils;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j
public class ETScanOmegas extends BaseCommandPlugin {
    private static final float NOTABLE_BANDWIDTH = 180f;

    @Override
    public boolean doesCommandAddOptions() {
        return false;
    }

    @Override
    public boolean execute(final String ruleId, final InteractionDialogAPI dialog, final List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;


        if (dialog.getInteractionTarget() != null) {
            List<FleetMemberAPI> validSelectionList = new ArrayList<>();

            CampaignFleetAPI defenderFleet = (CampaignFleetAPI) dialog.getInteractionTarget();
            for (FleetMemberAPI fm : defenderFleet.getMembersWithFightersCopy()) {
                if (fm.isFighterWing()) continue;
                validSelectionList.add(fm);
            }

            ScanUtils.showNotableShipsPanel(dialog, validSelectionList);
        }
        return true;
    }
}
