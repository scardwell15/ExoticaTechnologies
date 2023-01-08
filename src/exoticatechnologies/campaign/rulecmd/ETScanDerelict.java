package exoticatechnologies.campaign.rulecmd;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.ScanUtils;
import exoticatechnologies.modifications.*;
import exoticatechnologies.util.StringUtils;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.Map;

@Log4j
public class ETScanDerelict extends BaseCommandPlugin {
    @Override
    public boolean doesCommandAddOptions() {
        return false;
    }

    @Override
    public boolean execute(final String ruleId, final InteractionDialogAPI dialog, final List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        SectorEntityToken interactionTarget = dialog.getInteractionTarget();
        DerelictShipEntityPlugin plugin = (DerelictShipEntityPlugin) interactionTarget.getCustomPlugin();
        ShipRecoverySpecial.PerShipData shipData = plugin.getData().ship;

        ShipModifications mods = ShipModLoader.getForSpecialData(shipData);
        if (mods == null) {
            dialog.getOptionPanel().setEnabled(ruleId, false);

            StringUtils.getTranslation("FleetScanner", "ModsMissingText")
                    .addToTextPanel(dialog.getTextPanel());
        } else {
            ScanUtils.addModificationsToTextPanel(dialog.getTextPanel(),
                    shipData.shipName != null ? shipData.shipName : "???",
                    mods,
                    shipData.getVariant().getHullSize(),
                    null);
        }

        return true;
    }
}
