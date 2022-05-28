package exoticatechnologies.dialog;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;

public class ReturnToMarketOption extends DialogOption {
    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        return StringUtils.getString("MainMenu", "BackToMainMenu");
    }
}
