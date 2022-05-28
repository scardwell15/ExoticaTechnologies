package exoticatechnologies.modifications.upgrades.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.dialog.DialogOption;
import exoticatechnologies.dialog.DialogState;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import exoticatechnologies.modifications.upgrades.methods.RecoverMethod;
import exoticatechnologies.modifications.upgrades.methods.UpgradeMethod;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@Log4j
@RequiredArgsConstructor
public class ChosenUpgradeState extends DialogState {
    @Getter
    private final Upgrade upgrade;
    private final Map<UpgradeMethod, UpgradeMethodOption> upgradeMethodOptionList = new HashMap<>();

    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        if (mods.isMaxLevel(fm, upgrade)) {
            return StringUtils.getTranslation("UpgradesDialog", "UpgradeNameMaxed")
                    .format("upgradeName", upgrade.getName())
                    .toString();
        } else {
            return StringUtils.getTranslation("UpgradesDialog", "UpgradeNameWithLevelAndMax")
                    .format("upgradeName", upgrade.getName())
                    .format("level", mods.getUpgrade(upgrade))
                    .format("max", upgrade.getMaxLevel(fm))
                    .toString();
        }
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {
        FleetMemberAPI fm = plugin.getShip();
        ShipModifications mods = ShipModFactory.getForFleetMember(fm);
        MarketAPI market = plugin.getMarket();

        TextPanelAPI textPanel = dialog.getTextPanel();
        OptionPanelAPI options = dialog.getOptionPanel();

        dialog.getVisualPanel().showFleetMemberInfo(fm);

        textPanel.addParagraph(upgrade.getName(), new Color(150, 220, 255));
        textPanel.addParagraph(upgrade.getDescription());

        if (mods.isMaxLevel(fm, upgrade)) {
            StringUtils.getTranslation("UpgradesDialog", "CannotPerformUpgradeMaxLevel").addToTextPanel(textPanel);
        } else if (mods.getUsedBandwidth() + upgrade.getBandwidthUsage() > mods.getBandwidth(fm)) {
            StringUtils.getTranslation("UpgradesDialog", "CannotPerformUpgradeBandwidth").addToTextPanel(textPanel);
        } else {
            StringUtils.getTranslation("UpgradesDialog", "CanPerformUpgradePicked").addToTextPanel(textPanel);
        }

        addUpgradeMethodOptions(options, plugin, fm, mods, market);

        UpgradesHandler.UPGRADE_PICKER_DIALOG.addToOptions(options, plugin, fm, mods, Keyboard.KEY_ESCAPE);
    }

    private void addUpgradeMethodOptions(OptionPanelAPI options, ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications es, MarketAPI market) {
        RecoverMethod recovery = null;
        for (UpgradeMethod method : UpgradesHandler.UPGRADE_METHODS) {
            if (method instanceof RecoverMethod) {
                recovery = (RecoverMethod) method;
                continue;
            }

            if (method.canShow(fm, es, upgrade, market)) {
                UpgradeMethodOption option;
                if (upgradeMethodOptionList.containsKey(method)) {
                    option = upgradeMethodOptionList.get(method);
                } else {
                    option = new UpgradeMethodOption(method, upgrade);
                    upgradeMethodOptionList.put(method, option);
                }

                option.addToOptions(options, plugin, fm, es);

                if (es.isMaxLevel(fm, upgrade)) {
                    options.setTooltip(option, null);
                    options.setEnabled(option, false);
                } else if (es.getUsedBandwidth() + upgrade.getBandwidthUsage() > es.getBandwidth(fm)) {
                    options.setTooltip(option, null);
                    options.setEnabled(option, false);
                }
            }
        }

        //add recovery option after all others
        if (recovery.canShow(fm, es, upgrade, market)) {
            UpgradeMethodOption option;
            if (upgradeMethodOptionList.containsKey(recovery)) {
                option = upgradeMethodOptionList.get(recovery);
            } else {
                option = new UpgradeMethodOption(recovery, upgrade);
                upgradeMethodOptionList.put(recovery, option);
            }

            option.addToOptions(options, plugin, fm, es);
        }
    }

    @Override
    public boolean consumesOptionPickedEvent(Object option) {
        return upgradeMethodOptionList.values().contains(option);
    }

    @Override
    public void optionPicked(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, Object option) {
        ((DialogOption) option).execute(dialog, plugin);

        OptionPanelAPI options = dialog.getOptionPanel();
        TextPanelAPI textPanel = dialog.getTextPanel();

        options.clearOptions();

        plugin.redrawResourcesPanel();

        FleetMemberAPI fm = plugin.getShip();
        MarketAPI market = plugin.getMarket();
        ShipModifications mods = ShipModFactory.getForFleetMember(fm);
        float bandwidth = mods.getBandwidth(fm);

        if (fm != null && upgrade != null) {
            ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();
            int max = upgrade.getMaxLevel(hullSize);
            int level = mods.getUpgrade(upgrade.getKey());

            StringUtils.getTranslation("UpgradesDialog", "UpgradeNameWithLevelAndMax")
                    .format("upgradeName", upgrade.getName())
                    .format("level", level)
                    .format("max", max)
                    .addToTextPanel(textPanel);

            float usedBandwidth = mods.getUsedBandwidth();
            float upgradeBandwidth = upgrade.getBandwidthUsage();
            log.info(String.format("BANDWIDTH: [%s]", bandwidth));
            if (level >= max) {
                StringUtils.getTranslation("UpgradesDialog", "CannotPerformUpgradeMaxLevel").addToTextPanel(textPanel);
            } else if (usedBandwidth + upgradeBandwidth > bandwidth) {
                StringUtils.getTranslation("UpgradesDialog", "CannotPerformUpgradeBandwidth").addToTextPanel(textPanel);
            }

            addUpgradeMethodOptions(options, plugin, fm, mods, market);
        }

        UpgradesHandler.UPGRADE_PICKER_DIALOG.addToOptions(options, plugin, fm, mods, Keyboard.KEY_ESCAPE);
    }

    @Override
    public void addToOptions(OptionPanelAPI options, ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods, String tooltip, int hotkey) {
        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();
        int level = mods.getUpgrade(upgrade.getKey());
        int max = upgrade.getMaxLevel(hullSize);

        Color color = Misc.getButtonTextColor();

        if (mods.isMaxLevel(fm, upgrade)) {
            color = new Color(218, 218, 79);
        } else if (!UpgradesHandler.canUseUpgradeMethods(fm, mods, hullSize, upgrade, Global.getSector().getPlayerFleet(), plugin.getMarket())) {
            color = new Color(208, 88, 88);
        }

        tooltip = tooltip != null ? tooltip : upgrade.getDescription();
        options.addOption(getOptionText(plugin, fm, mods), this, color, tooltip);

        if (hotkey >= 0) {
            options.setShortcut(this, hotkey, false, false, false, true);
        }
    }

    @Override
    public void modifyResourcesPanel(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts) {
        FleetMemberAPI fm = plugin.getShip();
        DialogOption hoveredOption = plugin.getHoveredOption();

        resourceCosts.put(Bandwidth.BANDWIDTH_RESOURCE, upgrade.getBandwidthUsage());

        for (UpgradeMethod method : UpgradesHandler.UPGRADE_METHODS) {
            boolean hovered = false;

            if(hoveredOption instanceof UpgradeMethodOption) {
                if (((UpgradeMethodOption) hoveredOption).getMethod().equals(method)) {
                    hovered = true;
                }
            }

            method.modifyResourcesPanel(plugin, resourceCosts, fm, upgrade, hovered);
        }
    }
}
