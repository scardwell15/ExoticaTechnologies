package exoticatechnologies.dialog.modifications;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.dialog.DialogState;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import org.lwjgl.input.Keyboard;

public class SystemPickerState extends DialogState {
    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        return StringUtils.getString("ShipDialog", "BackToShip");
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {
        FleetMemberAPI fm = plugin.getShip();
        ShipModifications mods = ShipModFactory.getForFleetMember(fm);
        OptionPanelAPI option = dialog.getOptionPanel();

        for(SystemState state : SystemOptionsHandler.getValidSystemsOptions()) {
            state.addToOptions(option, plugin, fm, mods);
            state.modifyInteractionPanel(dialog, plugin);
        }

        ETInteractionDialogPlugin.SHIP_PICKER.addToOptions(option, plugin, fm, mods);
        ETInteractionDialogPlugin.RETURN_TO_MARKET.addToOptions(option, plugin, fm, mods, Keyboard.KEY_ESCAPE);
    }

    @Override
    public boolean canBeConsumedByState(DialogState state) {
        return false;
    }

    @Override
    public boolean requiresFleetMember() {
        return true;
    }
}
