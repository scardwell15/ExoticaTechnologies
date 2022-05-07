package extrasystemreloaded.systems.exotics.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
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
public class ExoticRemoveOption extends DialogOption {
    @Getter
    private final Exotic exotic;

    @Override
    public String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        return StringUtils.getString("ExoticsDialog", "DestroyExoticOption");
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        MarketAPI market = plugin.getMarket();
        FleetMemberAPI fm = plugin.getShip();
        ExtraSystems es = plugin.getExtraSystems();
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();

        es.removeExotic(exotic);
        es.save(fm);
        ExtraSystemHM.addToFleetMember(fm);

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f);
        textPanel.addParagraph(StringUtils.getString("ExoticsDialog", "ExoticDestroyedSuccessfully"));
    }
}
