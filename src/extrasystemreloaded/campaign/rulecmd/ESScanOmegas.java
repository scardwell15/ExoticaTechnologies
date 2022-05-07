package extrasystemreloaded.campaign.rulecmd;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import extrasystemreloaded.Es_ModPlugin;
import extrasystemreloaded.campaign.ScanUtils;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j
public class ESScanOmegas extends BaseCommandPlugin {
    private static float NOTABLE_BANDWIDTH = 180f;

    @Override
    public boolean doesCommandAddOptions() {
        return false;
    }

    @Override
    public boolean execute(final String ruleId, final InteractionDialogAPI dialog, final List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        List<FleetMemberAPI> validSelectionList = new ArrayList<>();

        if (dialog.getInteractionTarget() != null) {
            CampaignFleetAPI defenderFleet = (CampaignFleetAPI) dialog.getInteractionTarget();

            for (FleetMemberAPI fm : defenderFleet.getMembersWithFightersCopy()) {
                if (fm.isFighterWing()) continue;

                scanFleetMember(fm, dialog);
            }
        }
        return true;
    }

    private static void scanFleetMember(FleetMemberAPI fm, InteractionDialogAPI dialog) {
        if (fm != null) {
            ExtraSystems es = ExtraSystems.getForFleetMember(fm);
            TextPanelAPI textPanel = dialog.getTextPanel();

            ScanUtils.addSystemsToTextPanel(textPanel, fm.getShipName(), es, fm.getVariant().getHullSize(), fm.getFleetData().getFleet().getFaction().getColor());
        }
    }
}
