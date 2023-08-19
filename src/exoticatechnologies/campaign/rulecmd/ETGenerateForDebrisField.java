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
import exoticatechnologies.campaign.listeners.DerelictsEFScript;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.ShipModLoader;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import lombok.extern.log4j.Log4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * vanilla does not have a way to add ES upgrades in a place where we can add options to the debris salvage dialog afterwards.
 * this script generates them for ships located in the debris field, and rules.csv is assumed to populate the option
 * for viewing them afterwards.
 */
@Log4j
public class ETGenerateForDebrisField extends BaseCommandPlugin {

    @Override
    public boolean doesCommandAddOptions() {
        return true;
    }

    public int getOptionOrder(List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        return 2;
    }

    @Override
    public boolean execute(final String ruleId, final InteractionDialogAPI dialog, final List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        SectorEntityToken entity = dialog.getInteractionTarget();
        boolean notableModsGenerated = false;
        if (entity != null) {
            if (entity.getCustomEntityType().equals(Entities.DEBRIS_FIELD_SHARED)
                    && entity.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPECIAL_DATA)
                    && entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA) instanceof ShipRecoverySpecial.ShipRecoverySpecialData) {
                ShipRecoverySpecial.ShipRecoverySpecialData data = (ShipRecoverySpecial.ShipRecoverySpecialData) entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA);

                if (data.ships != null
                        && !data.ships.isEmpty()) {

                    Map<String, ShipModifications> derelictVariantMap = new LinkedHashMap<>();

                    for (int i = 0; i < data.ships.size(); i++) {
                        ShipRecoverySpecial.PerShipData shipData = data.ships.get(i);

                        ShipModifications mods = ShipModLoader.getForSpecialData(shipData);
                        if (mods == null) {
                            if (shipData.getVariant() == null) continue;

                            FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, shipData.getVariant());
                            if (shipData.fleetMemberId == null) {
                                shipData.fleetMemberId = member.getId();
                            } else {
                                member.setId(shipData.fleetMemberId);
                            }

                            log.info("debris field: generating for fmId " + shipData.fleetMemberId);

                            //note: saving here isn't really an issue because the cleanup script searches for fleet members with this ID.
                            //it will never find one.

                            ShipModFactory.random.setSeed(shipData.fleetMemberId.hashCode());
                            mods = ShipModFactory.generateRandom(member);
                            ShipModLoader.set(member, member.getVariant(), mods);
                        }

                        derelictVariantMap.put(shipData.fleetMemberId, mods);

                        if(ScanUtils.doesEntityHaveNotableMods(mods)) {
                            notableModsGenerated = true;
                        }
                    }

                    Global.getSector().addTransientScript(new DerelictsEFScript(derelictVariantMap));
                }
            }
        }

        if(notableModsGenerated) {
            StringUtils.getTranslation("FleetScanner", "DebrisFieldHasNotableMods")
                    .addToTextPanel(dialog.getTextPanel());
            dialog.getOptionPanel().addOption(StringUtils.getString("FleetScanner","DebrisFieldScanOption"), "ETScanDebrisField");
        }

        return false;
    }
}
