package exoticatechnologies.dialog;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import org.lwjgl.input.Keyboard;

public class ESMainMenu extends DialogState {
    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        return StringUtils.getString("MainMenu", "BackToMainMenu");
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {
        dialog.getOptionPanel().addOption(StringUtils.getString("MainMenu", "UpgradeShips"), ETInteractionDialogPlugin.SHIP_PICKER);
        dialog.getOptionPanel().addOption(StringUtils.getString("MainMenu", "BackToMainMenu"), ETInteractionDialogPlugin.RETURN_TO_MARKET);
        dialog.getOptionPanel().setShortcut(ETInteractionDialogPlugin.RETURN_TO_MARKET, Keyboard.KEY_ESCAPE, false, false, false, true);
    }

    @Override
    public boolean canBeConsumedByState(DialogState state) {
        return false;
    }
}
