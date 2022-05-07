package extrasystemreloaded.dialog;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;

public class ReturnToMarketOption extends DialogOption {
    @Override
    public String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        return StringUtils.getString("MainMenu", "BackToMainMenu");
    }
}
