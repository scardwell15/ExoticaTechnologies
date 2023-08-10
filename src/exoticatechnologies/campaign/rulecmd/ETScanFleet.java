package exoticatechnologies.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.ScanUtils;
import exoticatechnologies.modifications.ShipModLoader;
import exoticatechnologies.modifications.ShipModifications;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j
public class ETScanFleet extends BaseCommandPlugin {
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

            BattleAPI battle = context.getBattle();
            BattleAPI.BattleSide playerSide = battle.pickSide(Global.getSector().getPlayerFleet());
            CampaignFleetAPI otherFleet = battle.getOtherSideCombined(playerSide);

            for (FleetMemberAPI member : otherFleet.getMembersWithFightersCopy()) {
                if (member.isFighterWing()) continue;

                ShipModifications mods = ShipModLoader.get(member, member.getVariant());
                if (mods != null) {
                    if (ScanUtils.doesEntityHaveNotableMods(mods)) {
                        validSelectionList.add(member);
                    }
                }
            }

            log.info(String.format("Found [%s] notable modified ships.", validSelectionList.size()));
        }

        ScanUtils.showNotableShipsPanel(dialog, validSelectionList);
        return true;
    }
}
