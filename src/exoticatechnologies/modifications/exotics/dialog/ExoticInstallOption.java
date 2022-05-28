package exoticatechnologies.modifications.exotics.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.dialog.DialogOption;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.GenericExoticItemPlugin;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ExoticInstallOption extends DialogOption {
    @Getter
    private final Exotic exotic;

    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        return StringUtils.getString("ExoticsDialog", "InstallExoticOption");
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        MarketAPI market = plugin.getMarket();
        FleetMemberAPI fm = plugin.getShip();
        ShipModifications mods = plugin.getExtraSystems();
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();

        if (!ETModPlugin.isDebugUpgradeCosts()) {
            if (Utilities.hasExoticChip(fleet.getCargo(), exotic.getKey())) {
                Utilities.takeExoticChip(fleet.getCargo(), exotic.getKey());
            } else {
                exotic.removeItemsFromFleet(fleet, fm);
            }
        }

        mods.putExotic(exotic);
        mods.save(fm);
        ExoticaTechHM.addToFleetMember(fm);

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f);
        textPanel.addParagraph(StringUtils.getString("ExoticsDialog", "ExoticInstalledSuccessfully"));
    }
}
