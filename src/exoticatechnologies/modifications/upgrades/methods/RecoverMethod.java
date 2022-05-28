package exoticatechnologies.modifications.upgrades.methods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;

import java.util.Map;

public class RecoverMethod implements UpgradeMethod {
    @Override
    public String getOptionText(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market) {
        return StringUtils.getTranslation("UpgradeMethods", "RecoverOption").toString();
    }

    @Override
    public String getOptionTooltip(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market) {
        return StringUtils.getTranslation("UpgradeMethods", "RecoverOptionTooltip").toString();
    }

    @Override
    public boolean canUse(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        return mods.hasUpgrade(upgrade);
    }

    @Override
    public boolean canShow(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        return mods.hasUpgrade(upgrade);
    }

    @Override
    public void apply(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, ShipModifications mods, Upgrade upgrade, MarketAPI market, FleetMemberAPI fm) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();

        CargoStackAPI stack = Utilities.getUpgradeChip(fleet.getCargo(), upgrade.getKey(), mods.getUpgrade(upgrade));
        if (stack != null) {
            stack.add(1);
        } else {
            fleet.getCargo().addSpecial(new SpecialItemData(Upgrade.ITEM, String.format("%s,%s",upgrade.getKey(), mods.getUpgrade(upgrade))), 1);
        }

        mods.removeUpgrade(upgrade);
        mods.save(fm);
        ExoticaTechHM.addToFleetMember(fm);

        textPanel.addParagraph(StringUtils.getString("UpgradesDialog", "UpgradeRecoveredSuccessfully"));
    }

    @Override
    public void modifyResourcesPanel(ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm, Upgrade upgrade, boolean hovered) {
    }
}
