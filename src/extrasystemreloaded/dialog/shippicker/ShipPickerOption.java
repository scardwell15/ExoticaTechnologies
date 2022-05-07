package extrasystemreloaded.dialog.shippicker;

import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import extrasystemreloaded.dialog.DialogOption;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.dialog.DialogState;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;

import java.util.List;

public class ShipPickerOption extends DialogOption {
    private Object nextOption;
    private Object noPickOption;

    public ShipPickerOption(Object pickedOption, Object noPickOption) {
        this.nextOption = pickedOption;
        this.noPickOption = noPickOption;
    }

    public ShipPickerOption(Object pickedOption) {
        this(pickedOption, null);
    }

    @Override
    public String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        return StringUtils.getString("ShipListDialog", "BackToShipList");
    }

    @Override
    public void execute(final InteractionDialogAPI dialog, final ESInteractionDialogPlugin plugin) {

        dialog.getVisualPanel().fadeVisualOut();

        plugin.getMemoryMap().get(MemKeys.LOCAL).set(ESInteractionDialogPlugin.SHIP_MEMKEY, null);
        plugin.redrawResourcesPanel();

        //pick ship then execute next option.
        //if no ship then execute noPickOption if it exists, or return to ESMainMenu otherwise.
        List<FleetMemberAPI> validSelectionList = plugin.getFleetMembers();
        int rows = validSelectionList.size() > 8 ? (int) Math.ceil(validSelectionList.size() / 8f) : 1;
        int cols = Math.min(validSelectionList.size(), 10);
        cols = Math.max(cols, 4);

        dialog.showFleetMemberPickerDialog(
                StringUtils.getString("ShipListDialog", "SelectShip"),
                StringUtils.getString("ShipListDialog", "Confirm"),
                StringUtils.getString("ShipListDialog", "Cancel"),
                rows,
                cols, 88f, true, false, validSelectionList, new FleetMemberPickerListener() {
                    @Override
                    public void pickedFleetMembers(List<FleetMemberAPI> members) {
                        if (members != null && !members.isEmpty()) {
                            plugin.getMemoryMap().get(MemKeys.LOCAL).set(ESInteractionDialogPlugin.SHIP_MEMKEY, members.get(0));
                            plugin.optionSelected(members.get(0).getShipName(), nextOption);
                        } else {
                            cancelledFleetMemberPicking();
                        }
                    }

                    @Override
                    public void cancelledFleetMemberPicking() {
                        if (noPickOption == null) {
                            plugin.optionSelected("No ship selected.", ESInteractionDialogPlugin.RETURN_TO_MARKET);
                        } else {
                            plugin.optionSelected("No ship selected.", noPickOption);
                        }
                    }
                });
    }

    @Override
    public boolean canBeConsumedByState(DialogState state) {
        return false;
    }
}
