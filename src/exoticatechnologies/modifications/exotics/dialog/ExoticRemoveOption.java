package exoticatechnologies.modifications.exotics.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.dialog.DialogOption;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ExoticRemoveOption extends DialogOption {
    @Getter
    private final Exotic exotic;

    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        return StringUtils.getString("ExoticsDialog", "DestroyExoticOption");
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        MarketAPI market = plugin.getMarket();
        FleetMemberAPI fm = plugin.getShip();
        ShipModifications mods = plugin.getExtraSystems();
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();

        if (exotic.getKey().equals("AlphaSubcore")) {
            if (fm.getVariant() != null) {
                fm.getVariant().removeMod("AlphaSubcoreHM");
            }
        }

        mods.removeExotic(exotic);
        mods.save(fm);
        ExoticaTechHM.addToFleetMember(fm);

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f);
        textPanel.addParagraph(StringUtils.getString("ExoticsDialog", "ExoticDestroyedSuccessfully"));
    }
}
