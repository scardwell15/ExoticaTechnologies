package extrasystemreloaded.dialog;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import org.lwjgl.input.Keyboard;

public class ESMainMenu extends DialogState {
    @Override
    public String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        return StringUtils.getString("MainMenu", "BackToMainMenu");
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin) {
        dialog.getOptionPanel().addOption(StringUtils.getString("MainMenu", "UpgradeShips"), ESInteractionDialogPlugin.SHIP_PICKER);
        dialog.getOptionPanel().addOption(StringUtils.getString("MainMenu", "BackToMainMenu"), ESInteractionDialogPlugin.RETURN_TO_MARKET);
        dialog.getOptionPanel().setShortcut(ESInteractionDialogPlugin.RETURN_TO_MARKET, Keyboard.KEY_ESCAPE, false, false, false, true);
    }

    @Override
    public boolean canBeConsumedByState(DialogState state) {
        return false;
    }
}
