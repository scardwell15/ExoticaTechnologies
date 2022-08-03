package exoticatechnologies.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.modifications.bandwidth.BandwidthUtil;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import exoticatechnologies.modifications.upgrades.methods.ChipMethod;
import exoticatechnologies.modifications.upgrades.methods.UpgradeMethod;
import exoticatechnologies.util.RenderUtils;
import exoticatechnologies.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.awt.*;
import java.util.*;
import java.util.List;

@RequiredArgsConstructor
public class UpgradeUIPlugin implements CustomUIPanelPlugin {
    private final ExclusiveButtonGroup upgradeButtons = new ExclusiveButtonGroup();
    private final ExclusiveButtonGroup methodButtons = new ExclusiveButtonGroup();
    private final Map<String, Float> resourceCosts = new HashMap<>();

    @Setter
    private CustomPanelAPI panel;
    private final List<TooltipMakerAPI> upgradeIconTooltips = new ArrayList<>();
    private TooltipMakerAPI descriptionTooltip;
    private TooltipMakerAPI statsTooltip;
    private TooltipMakerAPI resourceTooltip;
    private TooltipMakerAPI methodTooltip;

    private final ShipModificationUIPanelPlugin plugin;
    private final FleetMemberAPI fm;
    private final ShipModifications mods;
    private final MarketAPI market;
    private final float panelWidth;
    private final float panelHeight;
    private float upgradeInfoPanelWidth = 0f;

    private Upgrade displayedUpgrade = null;
    private UpgradeMethod displayedMethod = null;

    private ButtonData activeUpgradeButton = null;

    private ButtonData switchDescriptionButton = null;
    private boolean switchDescriptionDisplayed = true;

    @Override
    public void advance(float amount) {
    }

    public void initialize() {
        redisplayUpgradeIcons();

        upgradeButtons.addListener(new UpgradeGroupButtonListener());
        methodButtons.addListener(new UpgradeMethodGroupButtonListener());
    }

    public void redisplayUpgradeIcons() {
        for (TooltipMakerAPI iconButton : upgradeIconTooltips) {
            panel.removeComponent(iconButton);
        }
        upgradeIconTooltips.clear();
        upgradeButtons.clear();

        TooltipMakerAPI lastImg = null;

        int rowLength = 6; //in upgrades
        float rowYOffset = panelHeight / 3 - 6;
        int index = 0;
        for (Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            if (!mods.hasUpgrade(upgrade) && !upgrade.shouldShow(fm, mods, market)) {
                continue;
            }

            TooltipMakerAPI upgIcon = panel.createUIElement(64, 64, false);
            upgIcon.addImage(upgrade.getIcon(), 64, 0);
            UIComponentAPI imgComponent = upgIcon.getPrev();
            upgIcon.addPara("LVL" + mods.getUpgrade(upgrade), 0).getPosition().rightOfTop(imgComponent, -32);

            if (lastImg == null) {
                panel.addUIElement(upgIcon).inTL(3, rowYOffset);
            } else {
                panel.addUIElement(upgIcon).rightOfTop(lastImg, 3);
            }
            upgradeButtons.addButton(new ButtonData(imgComponent, upgrade));
            upgradeIconTooltips.add(upgIcon);

            index++;
            if (index >= rowLength) {
                rowYOffset += upgIcon.getPosition().getHeight() - 6;
                index = 0;
                lastImg = null;
            } else {
                lastImg = upgIcon;
            }
        }
    }

    public void redisplayUpgrade(Upgrade upgrade) {
        removePanels();
        displayedMethod = null;

        if (upgrade == null) return;

        //39f = upgrades * 6f (for padding between each icon) + 3f for the panel padding.
        upgradeInfoPanelWidth = panelWidth - plugin.getSwitcherPanelUpperOffset(ShipModificationUIPanelPlugin.UPGRADES_INDEX) - 39f;

        redisplayDescription(upgrade);
        redisplayResourceMap(upgrade, displayedMethod);
        redisplayMethods(upgrade);
    }

    public void redisplayDescription(Upgrade upgrade) {
        if (descriptionTooltip != null) {
            panel.removeComponent(descriptionTooltip);
            descriptionTooltip = null;
        }

        descriptionTooltip = panel.createUIElement(upgradeInfoPanelWidth / 2, panelHeight - 6, false);

        descriptionTooltip.addTitle(upgrade.getName(), upgrade.getColor());
        if (switchDescriptionDisplayed) {
            UIComponentAPI title = descriptionTooltip.getPrev();
            LabelAPI switchDescriptionLabel = descriptionTooltip.addPara(upgrade.getDescription(), 0);
            switchDescriptionLabel.getPosition().belowLeft(title, 3f);
        } else {
            upgrade.printStatInfoToTooltip(descriptionTooltip, fm, mods);
        }

        ButtonAPI descButton = descriptionTooltip.addButton(StringUtils.getString("ShipListDialog", "ModDescriptionButtonText"), "switchDescriptionButton",
                upgradeInfoPanelWidth / 2 - 6f, 18f, 0f);
        descButton.getPosition().inTL(3f, 0).setYAlignOffset(-panelHeight + 24f);

        panel.addUIElement(descriptionTooltip).inTL(plugin.getSwitcherPanelUpperOffset(ShipModificationUIPanelPlugin.UPGRADES_INDEX) + 32, 3);

        switchDescriptionButton = new ButtonData(descButton, null);
        switchDescriptionButton.addListener(new DescriptionSwapButtonListener());
    }

    private void redisplayResourceMap(Upgrade upgrade, UpgradeMethod displayedMethod) {
        resourceCosts.clear();

        for (UpgradeMethod method : UpgradesHandler.UPGRADE_METHODS) {
            boolean hovered = method.equals(displayedMethod);
            Map<String, Float> methodResources = method.getResourceCostMap(fm, mods, upgrade, market, hovered);

            for (String resource : methodResources.keySet()) {
                float cost = methodResources.get(resource);
                if (resourceCosts.containsKey(resource)) {
                    cost += resourceCosts.get(resource);
                }
                resourceCosts.put(resource, cost);
            }
        }

        if (resourceTooltip != null) {
            panel.removeComponent(resourceTooltip);
            resourceTooltip = null;
        }

        resourceTooltip = panel.createUIElement(upgradeInfoPanelWidth / 2, 2 * panelHeight / 3 - 6, false);
        resourceTooltip.addTitle(StringUtils.getString("ShipListDialog","UpgradeCostTitle"));

        float used = mods.getUsedBandwidth();
        if (upgrade.getBandwidthUsage() != 0f) {
            float upgradeBandwidth = upgrade.getBandwidthUsage();
            resourceCosts.put(Bandwidth.BANDWIDTH_RESOURCE, upgradeBandwidth);

            String upgBandwidthString = BandwidthUtil.getFormattedBandwidth(upgradeBandwidth);
            if (upgradeBandwidth > 0) {
                upgBandwidthString = "+" + upgBandwidthString;
            }

            StringUtils.getTranslation("CommonOptions", "BandwidthUsedWithUpgrade")
                    .format("usedBandwidth", BandwidthUtil.getFormattedBandwidth(used))
                    .format("upgradeBandwidth", upgBandwidthString)
                    .addToTooltip(resourceTooltip);
        } else {
            StringUtils.getTranslation("CommonOptions", "BandwidthUsed")
                    .format("usedBandwidth", BandwidthUtil.getFormattedBandwidth(used))
                    .addToTooltip(resourceTooltip);
        }

        UIUtils.addResourceMap(resourceTooltip, resourceCosts);

        panel.addUIElement(resourceTooltip).inTR(3, 3);
    }

    public void redisplayMethods(Upgrade upgrade) {
        if (methodTooltip != null) {
            panel.removeComponent(methodTooltip);
            methodTooltip = null;
        }

        float methodPanelWidth = upgradeInfoPanelWidth / 2;
        float methodPanelHeight = panelHeight / 3f - 6;
        methodTooltip = panel.createUIElement(methodPanelWidth, methodPanelHeight, false);
        methodTooltip.addTitle(StringUtils.getString("UpgradeMethods","UpgradeMethodsTitle"));

        UIComponentAPI lastButton = null;
        int nextButtonX = 0;
        int rowYOffset = 25;
        for (UpgradeMethod method : UpgradesHandler.UPGRADE_METHODS) {
            String buttonText = method.getOptionText(fm, mods, upgrade, market);

            methodTooltip.setButtonFontDefault();
            float buttonWidth = methodTooltip.computeStringWidth(buttonText) + 10f;

            if (nextButtonX + buttonWidth >= methodPanelWidth) {
                nextButtonX = 0;
                rowYOffset += 24;
                lastButton = null;
            }

            if (upgrade.canUseUpgradeMethod(fm, mods, method)) {

                boolean canUse = method.canUse(fm, mods, upgrade, market);
                if (canUse) {
                    if (method.usesBandwidth()) {
                        float shipBandwidth = mods.getBandwidthWithExotics(fm);
                        float usedBandwidth = mods.getUsedBandwidth();
                        float upgradeUsage = upgrade.getBandwidthUsage();

                        if (method instanceof ChipMethod) {
                            CargoStackAPI stack = ChipMethod.getDesiredChip(fm, mods, upgrade);
                            if (stack != null) {
                                UpgradeSpecialItemPlugin plugin = (UpgradeSpecialItemPlugin) stack.getPlugin();

                                upgradeUsage = upgrade.getBandwidthUsage() * (plugin.getUpgradeLevel() - mods.getUpgrade(upgrade));
                            }
                        }

                        canUse = (usedBandwidth + upgradeUsage) <= shipBandwidth;
                    }
                }

                ButtonAPI methodButton = methodTooltip.addButton(buttonText, "", buttonWidth, 18, 2f);

                String tooltipText = method.getOptionTooltip(fm, mods, upgrade, market);
                if (tooltipText != null) {
                    methodTooltip.addTooltipToPrevious(new StringTooltip(methodTooltip, tooltipText),
                            TooltipMakerAPI.TooltipLocation.BELOW);
                }

                ButtonData buttonData = new ButtonData(methodButton, method);
                buttonData.setEnabled(canUse);
                methodButton.setEnabled(buttonData.isEnabled());

                methodButtons.addButton(buttonData);

                if (lastButton == null) {
                    methodButton.getPosition().inTL(0, rowYOffset);
                } else {
                    methodButton.getPosition().rightOfTop(lastButton, 3);
                }
                lastButton = methodButton;
                nextButtonX += 100;
            }
        }

        panel.addUIElement(methodTooltip).inBR(3, 3);
        methodTooltip.getPosition().setSize(methodPanelWidth, methodPanelHeight);
    }

    public void doUpgradeWithMethod(Upgrade upgrade, UpgradeMethod method) {
        if (methodTooltip != null) {
            panel.removeComponent(methodTooltip);
            methodTooltip = null;
        }
        methodButtons.clear();

        FactionAPI playerFaction = Global.getSector().getPlayerFaction();

        String displayString = method.apply(fm, mods, upgrade, market);

        TimedUIPlugin timedPlugin = new TimedUIPlugin(0.75f, new UpgradedUIListener());

        methodTooltip = panel.createUIElement(upgradeInfoPanelWidth / 2, panelHeight / 3 - 6, false);
        CustomPanelAPI upgradedPanel = panel.createCustomPanel(upgradeInfoPanelWidth / 2, panelHeight / 3 - 6, timedPlugin);
        TooltipMakerAPI upgradedTooltip = upgradedPanel.createUIElement(upgradeInfoPanelWidth / 2, panelHeight / 3 - 6, false);
        upgradedTooltip.addPara(displayString, 0f).getPosition().inMid();
        upgradedPanel.addUIElement(upgradedTooltip).inTL(0, 0);
        methodTooltip.addCustom(upgradedPanel, 0);
        panel.addUIElement(methodTooltip).inBR(3,3);
    }

    @Override
    public void positionChanged(PositionAPI position) {
    }

    @Override
    public void renderBelow(float alphaMult) {
    }

    @Override
    public void render(float alphaMult) {
        float boxWidth = 64;
        float boxHeight = 64;

        alphaMult = alphaMult * 0.33f;
        ButtonData focusedButton = null;

        RenderUtils.pushUIRenderingStack();

        /*float panelX = panel.getPosition().getX();
        float panelY = panel.getPosition().getY();
        float panelW = panel.getPosition().getWidth();
        float panelH = panel.getPosition().getHeight();
        RenderUtils.renderBox(panelX, panelY, panelW, panelH, Color.white, alphaMult * 0.2f);

        if (descriptionTooltip != null) {
            float tooltipX = descriptionTooltip.getPosition().getX();
            float tooltipY = descriptionTooltip.getPosition().getY();
            float tooltipW = descriptionTooltip.getPosition().getWidth();
            float tooltipH = descriptionTooltip.getPosition().getHeight();

            RenderUtils.renderBox(tooltipX, tooltipY, tooltipW, tooltipH, Color.red, alphaMult * 0.2f);
        }

        if (resourceTooltip != null) {
            float tooltipX = resourceTooltip.getPosition().getX();
            float tooltipY = resourceTooltip.getPosition().getY();
            float tooltipW = resourceTooltip.getPosition().getWidth();
            float tooltipH = resourceTooltip.getPosition().getHeight();

            RenderUtils.renderBox(tooltipX, tooltipY, tooltipW, tooltipH, Color.blue, alphaMult * 0.2f);
        }

        if (methodTooltip != null) {
            float tooltipX = methodTooltip.getPosition().getX();
            float tooltipY = methodTooltip.getPosition().getY();
            float tooltipW = methodTooltip.getPosition().getWidth();
            float tooltipH = methodTooltip.getPosition().getHeight();

            RenderUtils.renderBox(tooltipX, tooltipY, tooltipW, tooltipH, Color.green, alphaMult * 0.2f);
        }*/

        if (activeUpgradeButton != null) {
            alphaMult = alphaMult * 2.5f;
            focusedButton = activeUpgradeButton;
        }

        if (focusedButton != null) {
            for (ButtonData data : upgradeButtons) {
                if (data.getData().equals(focusedButton.getData())) continue;
                float buttonX = data.getButton().getPosition().getX();
                float buttonY = data.getButton().getPosition().getY();
                RenderUtils.renderBox(buttonX, buttonY, boxWidth, boxHeight, Color.black, alphaMult);
            }
        }

        RenderUtils.popUIRenderingStack();
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        upgradeButtons.checkListeners(events);
        methodButtons.checkListeners(events);

        if (switchDescriptionButton != null) {
            switchDescriptionButton.checkListeners(events);
        }
    }

    public void removePanels() {
        methodButtons.clear();

        if (descriptionTooltip != null) {
            panel.removeComponent(descriptionTooltip);
            descriptionTooltip = null;
        }

        if (statsTooltip != null) {
            panel.removeComponent(statsTooltip);
            statsTooltip = null;
        }

        if (resourceTooltip != null) {
            panel.removeComponent(resourceTooltip);
            resourceTooltip = null;
        }

        if (methodTooltip != null) {
            panel.removeComponent(methodTooltip);
            methodTooltip = null;
        }
    }

    private class UpgradeGroupButtonListener extends ButtonListener {
        @Override
        public void checked(ButtonData button) {
            displayedUpgrade = (Upgrade) button.getData();
            activeUpgradeButton = button;
            redisplayUpgrade(displayedUpgrade);
        }

        @Override
        public void unchecked(ButtonData button) {
            if (displayedUpgrade != null && displayedUpgrade.equals(button.getData())) {
                displayedUpgrade = null;
                activeUpgradeButton = null;
            }
            redisplayUpgrade(displayedUpgrade);
        }

        @Override
        public void highlighted(ButtonData button) {
            if (activeUpgradeButton == null) {
                displayedUpgrade = (Upgrade) button.getData();
                redisplayUpgrade(displayedUpgrade);
            }
        }

        @Override
        public void unhighlighted(ButtonData button) {
            if (activeUpgradeButton == null && displayedUpgrade != null && displayedUpgrade.equals(button.getData())) {
                displayedUpgrade = null;
            }
            redisplayUpgrade(displayedUpgrade);
        }
    }

    private class DescriptionSwapButtonListener extends ButtonListener {
        @Override
        public void checked(ButtonData button) {
            if (displayedUpgrade != null) {
                switchDescriptionDisplayed = !switchDescriptionDisplayed;
                redisplayDescription(displayedUpgrade);
            }
        }
    }

    private class UpgradeMethodGroupButtonListener extends ButtonListener {
        @Override
        public void checked(ButtonData button) {
            if (displayedUpgrade == null) return;
            if (!button.isEnabled()) return;
            doUpgradeWithMethod(displayedUpgrade, (UpgradeMethod) button.getData());
            redisplayUpgradeIcons();
            redisplayDescription(displayedUpgrade);
        }

        @Override
        public void highlighted(ButtonData button) {
            displayedMethod = (UpgradeMethod) button.getData();
            redisplayResourceMap(displayedUpgrade, displayedMethod);
        }
    }

    private class UpgradedUIListener implements TimedUIPlugin.Listener {
        @Override
        public void end() {
            redisplayResourceMap(displayedUpgrade, displayedMethod);
            redisplayMethods(displayedUpgrade);
        }

        @Override
        public void render(PositionAPI pos, float alphaMult, float currLife, float endLife) {
        }

        @Override
        public void renderBelow(PositionAPI pos, float alphaMult, float currLife, float endLife) {
            RenderUtils.pushUIRenderingStack();

            float panelX = pos.getX();
            float panelY = pos.getY();
            float panelW = pos.getWidth();
            float panelH = pos.getHeight();
            RenderUtils.renderBox(panelX, panelY, panelW, panelH, Color.yellow, alphaMult * (endLife - currLife) / endLife);

            RenderUtils.popUIRenderingStack();
        }
    }

    @RequiredArgsConstructor
    protected static class StringTooltip extends BaseTooltipCreator {
        private final TooltipMakerAPI tooltip;
        private final String description;

        @Override
        public float getTooltipWidth(Object tooltipParam) {
            return Math.min(tooltip.computeStringWidth(description), 300f);
        }

        @Override
        public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
            tooltip.addPara(description, 3f);
        }
    }
}
