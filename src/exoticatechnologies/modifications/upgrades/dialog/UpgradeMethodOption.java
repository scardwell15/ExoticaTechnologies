package exoticatechnologies.modifications.upgrades.dialog;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.ETModSettings;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.dialog.DialogOption;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.methods.UpgradeMethod;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpgradeMethodOption extends DialogOption {
    @Getter
    private final UpgradeMethod method;
    @Getter
    private final Upgrade upgrade;

    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        return method.getOptionText(fm, mods, upgrade, plugin.getMarket());
    }

    @Override
    public void addToOptions(OptionPanelAPI options, ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods, String tooltip, int hotkey) {
        super.addToOptions(options, plugin, fm, mods, tooltip, hotkey);

        MarketAPI market = plugin.getMarket();
        options.setTooltip(this, method.getOptionTooltip(fm, mods, upgrade, market));
        if (!method.canUse(fm, mods, upgrade, market)) {
            options.setEnabled(this, false);
        }
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {
        FleetMemberAPI fm = plugin.getShip();
        ShipModifications mods = ShipModFactory.getForFleetMember(fm);
        MarketAPI market = plugin.getMarket();
        TextPanelAPI textPanel = dialog.getTextPanel();
        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();
        int max = upgrade.getMaxLevel(hullSize);
        int currentLevel = mods.getUpgrade(upgrade);

        boolean success = ETModSettings.getBoolean(ETModSettings.UPGRADE_ALWAYS_SUCCEED);
        if(!success && currentLevel != 0) {
            float minChanceOfFailure = ETModSettings.getFloat(ETModSettings.UPGRADE_FAILURE_CHANCE);
            float possibility = (float) Math.cos(Math.PI * currentLevel * 0.5f / max)
                    * (1f - minChanceOfFailure) + minChanceOfFailure;

            success = ((float) Math.random()) < possibility;
        }

        if(success) {
            method.apply(dialog, plugin, mods, upgrade, market, fm);

            mods.save(fm);
            ExoticaTechHM.addToFleetMember(fm);
        } else {
            StringUtils.getTranslation("UpgradesDialog", "UpgradePerformedFailure")
                    .addToTextPanel(textPanel);
        }
    }
}
