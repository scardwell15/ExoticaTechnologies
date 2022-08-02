package exoticatechnologies.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.ScanUtils;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j
public class ETScanDebrisField extends BaseCommandPlugin {
    private static final float NOTABLE_BANDWIDTH = 180f;

    @Override
    public boolean doesCommandAddOptions() {
        return false;
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        SectorEntityToken interactionTarget = dialog.getInteractionTarget();
        if (interactionTarget.getCustomEntityType().equals(Entities.DEBRIS_FIELD_SHARED)
                && interactionTarget.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPECIAL_DATA)
                && interactionTarget.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA) instanceof ShipRecoverySpecial.ShipRecoverySpecialData) {
            ShipRecoverySpecial.ShipRecoverySpecialData data = (ShipRecoverySpecial.ShipRecoverySpecialData) interactionTarget.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA);

            scanMultipleShips(dialog, data.ships);
        }
        return true;
    }

    private static void scanMultipleShips(final InteractionDialogAPI dialog, final List<ShipRecoverySpecial.PerShipData> shipsData) {
        final List<FleetMemberAPI> validSelectionList = new ArrayList<>();
        for (int i = 0; i < shipsData.size(); i++) {
            ShipRecoverySpecial.PerShipData shipData = shipsData.get(i);
            if (!ScanUtils.isPerShipDataNotable(shipData, i)) continue;

            FleetMemberAPI fm = Global.getFactory().createFleetMember(FleetMemberType.SHIP, shipData.getVariant());
            log.info("debris field: scanning fleet member ID " + shipData.fleetMemberId);
            fm.setId(shipData.fleetMemberId);

            validSelectionList.add(fm);
        }

        ScanUtils.showNotableShipsPanel(dialog, validSelectionList);
    }
}
