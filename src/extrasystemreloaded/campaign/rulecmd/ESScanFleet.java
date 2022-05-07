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
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import extrasystemreloaded.Es_ModPlugin;
import extrasystemreloaded.campaign.ScanUtils;
import extrasystemreloaded.systems.exotics.Exotic;
import extrasystemreloaded.systems.exotics.ExoticsHandler;
import extrasystemreloaded.systems.bandwidth.Bandwidth;
import extrasystemreloaded.systems.bandwidth.BandwidthUtil;
import extrasystemreloaded.systems.upgrades.Upgrade;
import extrasystemreloaded.systems.upgrades.UpgradesHandler;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j
public class ESScanFleet extends BaseCommandPlugin {
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

            FleetInteractionDialogPluginImpl interactionPlugin = (FleetInteractionDialogPluginImpl) dialog.getPlugin();
            FleetEncounterContext context = (FleetEncounterContext) interactionPlugin.getContext();
            CampaignFleetAPI otherFleet = context.getBattle().getNonPlayerCombined();

            for (FleetMemberAPI fm : otherFleet.getMembersWithFightersCopy()) {
                if (fm.isFighterWing()) continue;

                if (Es_ModPlugin.hasData(fm.getId())) {
                    ExtraSystems es = Es_ModPlugin.getData(fm.getId());

                    log.info(String.format("ExtraSystems info for ship [%s]: upg [%s] aug [%s] bdw [%s]",
                            fm.getShipName(),
                            es.hasUpgrades(),
                            es.hasExotics(),
                            es.getBandwidth(fm)));

                    if (es.hasUpgrades() || es.hasExotics() || es.getBandwidth(fm) >= NOTABLE_BANDWIDTH) {
                        validSelectionList.add(fm);
                    }
                }
            }

            log.info(String.format("Found [%s] notable modified ships.", validSelectionList.size()));
        }


        int rows = validSelectionList.size() > 8 ? (int) Math.ceil(validSelectionList.size() / 8f) : 1;
        int cols = Math.min(validSelectionList.size(), 10);
        cols = Math.max(cols, 4);

        dialog.showFleetMemberPickerDialog(
                StringUtils.getString("ShipListDialog", "SelectShip"),
                StringUtils.getString("ShipListDialog", "Confirm"),
                StringUtils.getString("ShipListDialog", "Cancel"),
                rows,
                cols, 88f, true, false, validSelectionList, new FleetMemberPickerListener() {
                    @Override
                    public void pickedFleetMembers(List<FleetMemberAPI> members) {
                        if (members != null && !members.isEmpty()) {
                            finishPicking(members.get(0), dialog);
                        } else {
                            finishPicking(null, dialog);
                        }
                    }

                    @Override
                    public void cancelledFleetMemberPicking() {
                        finishPicking(null, dialog);
                    }
                });

        return true;
    }

    private static void finishPicking(FleetMemberAPI fm, InteractionDialogAPI dialog) {
        if (fm != null) {
            ExtraSystems es = ExtraSystems.getForFleetMember(fm);
            TextPanelAPI textPanel = dialog.getTextPanel();

            ScanUtils.addSystemsToTextPanel(textPanel, fm.getShipName(), es, fm.getVariant().getHullSize(), fm.getFleetData().getFleet().getFaction().getColor());
        }
    }
}
