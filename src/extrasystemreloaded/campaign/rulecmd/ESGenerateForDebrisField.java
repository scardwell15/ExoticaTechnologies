package extrasystemreloaded.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.util.Misc;
import extrasystemreloaded.Es_ModPlugin;
import extrasystemreloaded.campaign.ScanUtils;
import extrasystemreloaded.campaign.listeners.ESDerelictsEFScript;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
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
public class ESGenerateForDebrisField extends BaseCommandPlugin {

    @Override
    public boolean doesCommandAddOptions() {
        return false;
    }

    @Override
    public boolean execute(final String ruleId, final InteractionDialogAPI dialog, final List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        SectorEntityToken entity = dialog.getInteractionTarget();
        if (entity != null) {
            if (entity.getCustomEntityType().equals(Entities.DEBRIS_FIELD_SHARED)
                    && entity.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPECIAL_DATA)
                    && entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA) instanceof ShipRecoverySpecial.ShipRecoverySpecialData) {
                ShipRecoverySpecial.ShipRecoverySpecialData data = (ShipRecoverySpecial.ShipRecoverySpecialData) entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA);

                boolean notableESGenerated = false;
                if (data.ships != null
                        && !data.ships.isEmpty()) {

                    Map<String, ExtraSystems> derelictVariantMap = new LinkedHashMap<>();

                    int i = 0;
                    for (ShipRecoverySpecial.PerShipData shipData : data.ships) {

                        ShipVariantAPI var = shipData.getVariant();
                        long seed = var.getHullVariantId().hashCode() + (i++);
                        if (shipData.fleetMemberId != null) {
                            seed = shipData.fleetMemberId.hashCode();
                        }

                        ExtraSystems es = new ExtraSystems(seed);
                        es.generate(seed, var, null);
                        //note: saving here isn't really an issue because the cleanup script searches for fleet members with this ID.
                        //it will never find one.

                        String esId = entity.getId() + String.valueOf(i);
                        Es_ModPlugin.saveData(esId, es);

                        derelictVariantMap.put(var.getHullVariantId(), es);

                        if(ScanUtils.isESNotable(es)) {
                            notableESGenerated = true;
                        }
                    }

                    Global.getSector().addTransientScript(new ESDerelictsEFScript(derelictVariantMap));
                }

                if(notableESGenerated) {
                    StringUtils.getTranslation("FleetScanner", "DebrisFieldHasNotableExtraSystems")
                            .addToTextPanel(dialog.getTextPanel());
                    dialog.getOptionPanel().setEnabled("ESScanDebrisField", true);
                } else {
                    dialog.getOptionPanel().setEnabled("ESScanDebrisField", false);
                }
            }
        }

        return false;
    }
}
