package extrasystemreloaded.dialog.modifications;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import extrasystemreloaded.dialog.DialogState;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import org.lwjgl.input.Keyboard;

public class SystemPickerState extends DialogState {
    @Override
    public String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        return StringUtils.getString("ShipDialog", "BackToShip");
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin) {
        FleetMemberAPI fm = plugin.getShip();
        ExtraSystems es = ExtraSystems.getForFleetMember(fm);
        OptionPanelAPI option = dialog.getOptionPanel();

        for(SystemState state : SystemOptionsHandler.getValidSystemsOptions()) {
            state.addToOptions(option, plugin, fm, es);
            state.modifyInteractionPanel(dialog, plugin);
        }

        ESInteractionDialogPlugin.SHIP_PICKER.addToOptions(option, plugin, fm, es);
        ESInteractionDialogPlugin.RETURN_TO_MARKET.addToOptions(option, plugin, fm, es, Keyboard.KEY_ESCAPE);
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
