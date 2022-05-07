package extrasystemreloaded.campaign.rulecmd;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
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
public class ESHasUpgrades extends BaseCommandPlugin {

    @Override
    public boolean doesCommandAddOptions() {
        return false;
    }

    @Override
    public boolean execute(final String ruleId, final InteractionDialogAPI dialog, final List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        SectorEntityToken entity = dialog.getInteractionTarget();
        if (entity != null) {
            if (dialog.getPlugin() instanceof FleetInteractionDialogPluginImpl) {
                FleetInteractionDialogPluginImpl interactionPlugin = (FleetInteractionDialogPluginImpl) dialog.getPlugin();
                FleetEncounterContext context = (FleetEncounterContext) interactionPlugin.getContext();
                CampaignFleetAPI otherFleet = context.getBattle().getNonPlayerCombined();

                return !ScanUtils.getNotableFleetMembers(otherFleet).isEmpty();
            }

            if (entity.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
                if (Es_ModPlugin.hasData(entity.getId())) {
                    return ScanUtils.isESNotable(Es_ModPlugin.getData(entity.getId()));
                }
            }

            if (entity.getCustomEntityType().equals(Entities.DEBRIS_FIELD_SHARED)
                    && entity.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPECIAL_DATA)
                    && entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA) instanceof ShipRecoverySpecial.ShipRecoverySpecialData) {
                ShipRecoverySpecial.ShipRecoverySpecialData data = (ShipRecoverySpecial.ShipRecoverySpecialData) entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA);

                if (data.ships != null
                        && !data.ships.isEmpty()) {
                    int i = 0;
                    for (ShipRecoverySpecial.PerShipData shipData : data.ships) {
                        String esId = entity.getId() + String.valueOf(++i);
                        log.info(String.format("searching for ES ID [%s]", esId));
                        if (Es_ModPlugin.hasData(esId)
                            && ScanUtils.isESNotable(Es_ModPlugin.getData(esId))) {
                            return true;
                        }
                    }
                }
            }


            CampaignFleetAPI defenderFleet = entity.getMemoryWithoutUpdate().getFleet("$defenderFleet");
            if (defenderFleet != null) {
                return !ScanUtils.getNotableFleetMembers(defenderFleet).isEmpty();
            }
        }

        return false;
    }
}
