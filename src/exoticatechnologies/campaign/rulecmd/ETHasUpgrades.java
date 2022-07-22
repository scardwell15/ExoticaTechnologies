package exoticatechnologies.campaign.rulecmd;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.campaign.ScanUtils;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.Map;

@Log4j
public class ETHasUpgrades extends BaseCommandPlugin {

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
                if (ETModPlugin.hasData(entity.getId())) {
                    return ScanUtils.doesEntityHaveNotableMods(ETModPlugin.getData(entity.getId()));
                }
            }

            if (entity.getCustomEntityType() != null) {
                if (entity.getCustomEntityType().equals(Entities.DEBRIS_FIELD_SHARED)
                        && entity.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPECIAL_DATA)
                        && entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA) instanceof ShipRecoverySpecial.ShipRecoverySpecialData) {
                    ShipRecoverySpecial.ShipRecoverySpecialData data = (ShipRecoverySpecial.ShipRecoverySpecialData) entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA);

                    if (data.ships != null
                            && !data.ships.isEmpty()) {
                        int i = 0;
                        for (ShipRecoverySpecial.PerShipData shipData : data.ships) {
                            String entityId = entity.getId() + String.valueOf(++i);
                            log.info(String.format("searching for entity ID [%s]", entityId));
                            if (ETModPlugin.hasData(entityId)
                                    && ScanUtils.doesEntityHaveNotableMods(ETModPlugin.getData(entityId))) {
                                return true;
                            }
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
