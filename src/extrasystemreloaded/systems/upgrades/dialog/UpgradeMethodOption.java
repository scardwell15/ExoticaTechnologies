package extrasystemreloaded.systems.upgrades.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import extrasystemreloaded.ESModSettings;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.dialog.DialogOption;
import extrasystemreloaded.hullmods.ExtraSystemHM;
import extrasystemreloaded.systems.upgrades.Upgrade;
import extrasystemreloaded.systems.upgrades.methods.UpgradeMethod;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpgradeMethodOption extends DialogOption {
    @Getter
    private final UpgradeMethod method;
    @Getter
    private final Upgrade upgrade;

    @Override
    public String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        return method.getOptionText(fm, es, upgrade, plugin.getMarket());
    }

    @Override
    public void addToOptions(OptionPanelAPI options, ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es, String tooltip, int hotkey) {
        super.addToOptions(options, plugin, fm, es, tooltip, hotkey);

        MarketAPI market = plugin.getMarket();
        options.setTooltip(this, method.getOptionTooltip(fm, es, upgrade, market));
        if (!method.canUse(fm, es, upgrade, market)) {
            options.setEnabled(this, false);
        }
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin) {
        FleetMemberAPI fm = plugin.getShip();
        ExtraSystems es = ExtraSystems.getForFleetMember(fm);
        MarketAPI market = plugin.getMarket();
        TextPanelAPI textPanel = dialog.getTextPanel();
        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();
        int max = upgrade.getMaxLevel(hullSize);
        int currentLevel = es.getUpgrade(upgrade);

        boolean success = ESModSettings.getBoolean(ESModSettings.UPGRADE_ALWAYS_SUCCEED);
        if(!success && currentLevel != 0) {
            float minChanceOfFailure = ESModSettings.getFloat(ESModSettings.UPGRADE_FAILURE_CHANCE);
            float possibility = (float) Math.cos(Math.PI * currentLevel * 0.5f / max)
                    * (1f - minChanceOfFailure) + minChanceOfFailure;

            success = ((float) Math.random()) < possibility;
        }

        if(success) {
            method.apply(fm, es, upgrade, market);

            es.save(fm);
            ExtraSystemHM.addToFleetMember(fm);

            Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f);
            StringUtils.getTranslation("UpgradesDialog", "UpgradePerformedSuccessfully")
                    .format("name", upgrade.getName())
                    .format("level", es.getUpgrade(upgrade))
                    .addToTextPanel(textPanel);
        } else {
            StringUtils.getTranslation("UpgradesDialog", "UpgradePerformedFailure")
                    .addToTextPanel(textPanel);
        }
    }
}
