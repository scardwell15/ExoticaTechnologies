package exoticatechnologies.campaign.rulecmd;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.campaign.ScanUtils;
import exoticatechnologies.modifications.ShipModifications;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j
public class ETScanFleet extends BaseCommandPlugin {
    private static final float NOTABLE_BANDWIDTH = 180f;

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

                if (ETModPlugin.hasData(fm.getId())) {
                    ShipModifications mods = ETModPlugin.getData(fm.getId());

                    log.info(String.format("ShipModifications info for ship [%s]: upg [%s] aug [%s] bdw [%s]",
                            fm.getShipName(),
                            mods.hasUpgrades(),
                            mods.hasExotics(),
                            mods.getBandwidthWithExotics(fm)));

                    if (mods.hasUpgrades() || mods.hasExotics() || mods.getBandwidthWithExotics(fm) >= NOTABLE_BANDWIDTH) {
                        validSelectionList.add(fm);
                    }
                }
            }

            log.info(String.format("Found [%s] notable modified ships.", validSelectionList.size()));
        }

        ScanUtils.showNotableShipsPanel(dialog, validSelectionList);
        return true;
    }
}
