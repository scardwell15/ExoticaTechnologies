package extrasystemreloaded.systems.exotics.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.dialog.DialogOption;
import extrasystemreloaded.hullmods.ExtraSystemHM;
import extrasystemreloaded.systems.exotics.Exotic;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ExoticRecoverOption extends DialogOption {
    @Getter
    private final Exotic exotic;

    @Override
    public String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        return StringUtils.getString("ExoticsDialog", "RecoverExoticStoryOption");
    }

    @Override
    public void addToOptions(OptionPanelAPI options, ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es, String tooltip, int hotkey) {
        super.addToOptions(options, plugin, fm, es, tooltip, hotkey);
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        MarketAPI market = plugin.getMarket();
        FleetMemberAPI fm = plugin.getShip();
        ExtraSystems es = plugin.getExtraSystems();
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();

        exotic.restoreItemsToFleet(fleet, fm);
        es.removeExotic(exotic);
        es.save(fm);
        ExtraSystemHM.addToFleetMember(fm);

        textPanel.addParagraph(StringUtils.getString("ExoticsDialog", "ExoticRecoveredSuccessfully"));
    }
}
