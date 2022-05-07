package extrasystemreloaded.dialog;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaginationOption extends DialogOption {
    @Getter
    private final boolean isNext;

    @Override
    public String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        if(isNext) {
            return StringUtils.getString("CommonOptions", "NextPage");
        } else {
            return StringUtils.getString("CommonOptions", "PreviousPage");
        }
    }
}
