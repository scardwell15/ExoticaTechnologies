package exoticatechnologies.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.ui.impl.ShipModificationDialogDelegate;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.Map;

@Log4j
public class ETPrototypeUI extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        ShipModificationDialogDelegate delegate = new ShipModificationDialogDelegate(dialog, dialog.getInteractionTarget().getMarket());
        dialog.showCustomDialog(delegate.getPanelWidth(), delegate.getPanelHeight(), delegate);
        return true;
    }
}
