package exoticatechnologies.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
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
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.campaign.ScanUtils;
import exoticatechnologies.util.StringUtils;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j
public class ETScanDebrisField extends BaseCommandPlugin {
    private static float NOTABLE_BANDWIDTH = 180f;

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

            if(data.ships.size() > 1) {
                scanMultipleShips(dialog, data.ships);
            } else {
                scanPerShipData(0, data.ships.get(0), dialog);
            }
        }
        return true;
    }

    private static void scanMultipleShips(final InteractionDialogAPI dialog, final List<ShipRecoverySpecial.PerShipData> shipsData) {
        final List<FleetMemberAPI> validSelectionList = new ArrayList<>();
        for (int i = 0; i < shipsData.size(); i++) {
            ShipRecoverySpecial.PerShipData shipData = shipsData.get(i);
            if (!ScanUtils.isPerShipDataNotable(shipData, i)) continue;

            FleetMemberAPI fm = Global.getFactory().createFleetMember(FleetMemberType.SHIP, shipData.getVariant());
            validSelectionList.add(fm);
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
                            int index = validSelectionList.indexOf(members.get(0));
                            scanPerShipData(index, shipsData.get(index), dialog);
                        }
                    }

                    @Override
                    public void cancelledFleetMemberPicking() {
                    }
                });
    }

    private static void scanPerShipData(int index, ShipRecoverySpecial.PerShipData shipData, InteractionDialogAPI dialog) {
        String entityId = ScanUtils.getPerShipDataId(shipData, index);

        ScanUtils.addModificationsToTextPanel(dialog.getTextPanel(),
                shipData.shipName != null ? shipData.shipName : "???",
                ETModPlugin.getData(entityId),
                shipData.getVariant().getHullSize(),
                null);
    }
}
