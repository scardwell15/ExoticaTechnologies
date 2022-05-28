package exoticatechnologies.dialog;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaginationOption extends DialogOption {
    @Getter
    private final boolean isNext;

    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        if(isNext) {
            return StringUtils.getString("CommonOptions", "NextPage");
        } else {
            return StringUtils.getString("CommonOptions", "PreviousPage");
        }
    }
}
