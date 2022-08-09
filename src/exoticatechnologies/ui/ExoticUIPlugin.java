package exoticatechnologies.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.modifications.bandwidth.BandwidthUtil;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticsHandler;
import exoticatechnologies.util.RenderUtils;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ExoticUIPlugin implements CustomUIPanelPlugin {
    private final ExclusiveButtonGroup exoticButtons = new ExclusiveButtonGroup();
    private final ExclusiveButtonGroup methodButtons = new ExclusiveButtonGroup();
    private final Map<String, Float> resourceCosts = new HashMap<>();

    @Setter
    private CustomPanelAPI panel;
    private final List<TooltipMakerAPI> exoticIconTooltips = new ArrayList<>();
    private TooltipMakerAPI descriptionTooltip;
    private TooltipMakerAPI resourceTooltip;
    private TooltipMakerAPI methodTooltip;

    private final ShipModificationUIPanelPlugin plugin;
    private final FleetMemberAPI fm;
    private final ShipModifications mods;
    private final MarketAPI market;
    private final float panelWidth;
    private final float panelHeight;
    private float exoticInfoPanelWidth = 0f;

    private Exotic displayedExotic = null;
    private ButtonData activeExoticButton = null;

    private ButtonData switchDescriptionButton = null;
    private boolean switchDescriptionDisplayed = true;

    private ButtonData installButtonData = null;
    private ButtonData recoverButtonData = null;
    private ButtonData destroyButtonData = null;

    @Override
    public void advance(float amount) {
    }

    public void initialize() {
        redisplayExoticIcons();

        exoticButtons.addListener(new ExoticIconsButtonListener());
        methodButtons.addListener(new MethodButtonsListener());
    }

    public void redisplayExoticIcons() {
        for (TooltipMakerAPI iconButton : exoticIconTooltips) {
            panel.removeComponent(iconButton);
        }
        exoticIconTooltips.clear();
        exoticButtons.clear();

        TooltipMakerAPI lastImg = null;

        int rowLength = 6; //in upgrades
        float rowYOffset = panelHeight / 3 - 6;
        int index = 0;
        for (Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if (!exotic.shouldShow(fm, mods, market) || !exotic.canApply(fm)) {
                continue;
            }

            TooltipMakerAPI upgIcon = panel.createUIElement(64, 64, false);
            upgIcon.addImage(exotic.getIcon(), 64, 0);
            UIComponentAPI imgComponent = upgIcon.getPrev();

            if (mods.hasExotic(exotic)) {
                upgIcon.addImage("graphics/icons/insignia/16x_star_circle.png", 0);
                upgIcon.getPrev().getPosition().rightOfTop(imgComponent, 0).setYAlignOffset(-3).setXAlignOffset(-16f);
            }

            if (lastImg == null) {
                panel.addUIElement(upgIcon).inTL(3, rowYOffset);
            } else {
                panel.addUIElement(upgIcon).rightOfTop(lastImg, 3);
            }
            exoticButtons.addButton(new ButtonData(imgComponent, exotic));
            exoticIconTooltips.add(upgIcon);

            index++;
            if (index >= rowLength) {
                rowYOffset += upgIcon.getPosition().getHeight();
                index = 0;
                lastImg = null;
            } else {
                lastImg = upgIcon;
            }
        }
    }

    public void redisplayExotic(Exotic exotic) {
        removePanels();

        if (exotic == null) return;

        //39f = exotics * 6f (for padding between each icon) + 3f for the panel padding.
        exoticInfoPanelWidth = panelWidth - plugin.getSwitcherPanelUpperOffset(ShipModificationUIPanelPlugin.EXOTICS_INDEX) - 39f;

        redisplayDescription(exotic);
        redisplayResourceMap(exotic, null);
        addOptionButtons();
    }

    public void redisplayDescription(Exotic exotic) {
        if (descriptionTooltip != null) {
            panel.removeComponent(descriptionTooltip);
            descriptionTooltip = null;
        }

        descriptionTooltip = panel.createUIElement(exoticInfoPanelWidth / 2, panelHeight - 6, false);
        descriptionTooltip.addTitle(exotic.getName(), exotic.getMainColor());
        UIComponentAPI title = descriptionTooltip.getPrev();

        if (switchDescriptionDisplayed) {
            LabelAPI switchDescriptionLabel = descriptionTooltip.addPara(exotic.getDescription(), 0);
            switchDescriptionLabel.getPosition().belowLeft(title, 3f);
        } else {
            exotic.modifyToolTip(descriptionTooltip, title, fm, mods, true);
        }

        ButtonAPI descButton = descriptionTooltip.addButton(StringUtils.getString("ShipListDialog", "ModDescriptionButtonText"), "switchDescriptionButton",
                exoticInfoPanelWidth / 2 - 6f, 18f, 0f);
        descButton.getPosition().inTL(3f, 0).setYAlignOffset(-panelHeight + 24f);

        panel.addUIElement(descriptionTooltip).inTL(plugin.getSwitcherPanelUpperOffset(ShipModificationUIPanelPlugin.UPGRADES_INDEX) + 32, 3);

        switchDescriptionButton = new ButtonData(descButton, null);
        switchDescriptionButton.addListener(new DescriptionSwapButtonListener());
    }

    private void redisplayResourceMap(Exotic exotic, ButtonData data) {
        resourceCosts.clear();

        if (resourceTooltip != null) {
            panel.removeComponent(resourceTooltip);
            resourceTooltip = null;
        }

        if (exotic == null) return;

        resourceTooltip = panel.createUIElement(exoticInfoPanelWidth / 2, 2 * panelHeight / 3 - 6, false);
        resourceTooltip.addTitle(StringUtils.getString("ShipListDialog", "UpgradeCostTitle"));

        Map<String, Float> methodResources = null;
        if (data != null) {
            if (data.equals(installButtonData)) {
                CargoStackAPI exoticStack = Utilities.getExoticChip(fm.getFleetData().getFleet().getCargo(), exotic.getKey());
                if (exoticStack != null) {
                    methodResources = new HashMap<>();
                    methodResources.put(Utilities.formatSpecialItem(exoticStack.getSpecialDataIfSpecial()), 1f);
                } else {
                    methodResources = exotic.getResourceCostMap(fm, mods, market);
                }
            } else if (data.equals(recoverButtonData)) {
                methodResources = new HashMap<>();

                CargoStackAPI stack = Utilities.getExoticChip(fm.getFleetData().getFleet().getCargo(), exotic.getKey());
                if (stack != null) {
                    resourceCosts.put(Utilities.formatSpecialItem(stack.getSpecialDataIfSpecial()), -1f);
                } else {
                    methodResources.put("&"
                            + StringUtils.getTranslation("ShipListDialog", "UpgradeChipText")
                            .format("upgradeName", exotic.getName())
                            .toStringNoFormats(), -1f);
                }

                methodResources.put(Utilities.STORY_POINTS, 1f);
            }
        }

        if (methodResources != null) {
            for (String resource : methodResources.keySet()) {
                float cost = methodResources.get(resource);
                if (resourceCosts.containsKey(resource)) {
                    cost += resourceCosts.get(resource);
                }
                resourceCosts.put(resource, cost);
            }
        }

        float used = mods.getUsedBandwidth();
        if (exotic.getExtraBandwidth(fm, mods) != 0f) {
            float upgradeBandwidth = exotic.getExtraBandwidth(fm, mods);
            resourceCosts.put(Bandwidth.BANDWIDTH_RESOURCE, upgradeBandwidth);

            if (upgradeBandwidth > 0) {
                String upgBandwidthString = "-" + BandwidthUtil.getFormattedBandwidth(upgradeBandwidth);

                StringUtils.getTranslation("CommonOptions", "BandwidthGivenByUpgrade")
                        .format("upgradeBandwidth", upgBandwidthString)
                        .addToTooltip(resourceTooltip);
            } else {
                String upgBandwidthString = BandwidthUtil.getFormattedBandwidth(Math.abs(upgradeBandwidth));

                StringUtils.getTranslation("CommonOptions", "BandwidthUsedWithUpgrade")
                        .format("usedBandwidth", BandwidthUtil.getFormattedBandwidth(used))
                        .format("upgradeBandwidth", upgBandwidthString)
                        .addToTooltip(resourceTooltip);
            }
        }

        UIUtils.addResourceMap(resourceTooltip, resourceCosts);

        panel.addUIElement(resourceTooltip).inTR(3, 3);
    }

    private void addOptionButtons() {
        if (methodTooltip != null) {
            panel.removeComponent(methodTooltip);
            methodTooltip = null;
        }
        methodButtons.clear();

        float methodPanelWidth = exoticInfoPanelWidth / 2 - 6;
        float methodPanelHeight = panelHeight / 2.6f - 6;
        methodTooltip = panel.createUIElement(methodPanelWidth, methodPanelHeight, false);
        methodTooltip.addTitle(StringUtils.getString("UpgradeMethods", "UpgradeMethodsTitle"));
        UIComponentAPI title = methodTooltip.getPrev();

        float buttonWidth = methodPanelWidth - 12f;

        String installButtonText = StringUtils.getString("ShipListDialog", "InstallExotic");

        boolean hasExoticChip = Utilities.hasExoticChip(fm.getFleetData().getFleet().getCargo(), displayedExotic.getKey());
        if (hasExoticChip) {
            installButtonText = StringUtils.getString("ShipListDialog", "InstallExoticChip");
        }

        ButtonAPI installButton = methodTooltip.addButton(installButtonText, "installExotic",
                buttonWidth, 18f, 0f);
        installButton.getPosition().belowMid(title, 3f);

        installButtonData = new ButtonData(installButton, null);

        if (mods.hasExotic(displayedExotic) || !(hasExoticChip || displayedExotic.canAfford(fm.getFleetData().getFleet(), market))) {
            installButton.setEnabled(false);
        }

        ButtonAPI recoverButton = methodTooltip.addButton(StringUtils.getString("ShipListDialog", "RecoverExotic"), "recoverExotic",
                Misc.getStoryOptionColor(), Misc.getStoryDarkColor(),
                buttonWidth, 18f, 0f);
        recoverButton.getPosition().belowMid(installButton, 3f);

        recoverButtonData = new ButtonData(recoverButton, null);

        int sp = Global.getSector().getPlayerStats().getStoryPoints();
        if (!mods.hasExotic(displayedExotic) || sp < 1) {
            recoverButton.setEnabled(false);
        }

        ButtonAPI destroyButton = methodTooltip.addButton(StringUtils.getString("ShipListDialog", "DestroyExotic"), "destroyExotic",
                buttonWidth, 18f, 0f);
        destroyButton.getPosition().belowMid(recoverButton, 3f);

        destroyButtonData = new ButtonData(destroyButton, null);

        if (!mods.hasExotic(displayedExotic)) {
            destroyButton.setEnabled(false);
        }

        methodButtons.addButton(installButtonData);
        methodButtons.addButton(recoverButtonData);
        methodButtons.addButton(destroyButtonData);

        panel.addUIElement(methodTooltip).inBR(9, 3);
    }

    private void displayStringOverResourcePanel(String display, Color color) {
        if (methodTooltip != null) {
            panel.removeComponent(methodTooltip);
            methodTooltip = null;
        }

        float methodPanelWidth = exoticInfoPanelWidth / 2 - 6;
        float methodPanelHeight = panelHeight / 2.6f - 6;

        TimedUIPlugin timedPlugin = new TimedUIPlugin(0.75f, new UpgradedUIListener(color));

        methodTooltip = panel.createUIElement(methodPanelWidth, methodPanelHeight, false);
        CustomPanelAPI upgradedPanel = panel.createCustomPanel(methodPanelWidth, methodPanelHeight, timedPlugin);
        TooltipMakerAPI upgradedTooltip = upgradedPanel.createUIElement(methodPanelWidth, methodPanelHeight, false);
        upgradedTooltip.addPara(display, 0f).getPosition().inMid();
        upgradedPanel.addUIElement(upgradedTooltip).inTL(0, 0);
        methodTooltip.addCustom(upgradedPanel, 0);
        panel.addUIElement(methodTooltip).inBR(9, 3);
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

        if (activeExoticButton != null) {
            alphaMult = alphaMult * 2.5f;
            focusedButton = activeExoticButton;
        }

        if (focusedButton != null) {
            for (ButtonData data : exoticButtons) {
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
        exoticButtons.checkListeners(events);
        methodButtons.checkListeners(events);

        if (switchDescriptionButton != null) {
            switchDescriptionButton.checkListeners(events);
        }
    }

    public void removePanels() {
        if (descriptionTooltip != null) {
            panel.removeComponent(descriptionTooltip);
            descriptionTooltip = null;
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

    private class ExoticIconsButtonListener extends ButtonListener {
        @Override
        public void checked(ButtonData button) {
            displayedExotic = (Exotic) button.getData();
            activeExoticButton = button;
            redisplayExotic(displayedExotic);
        }

        @Override
        public void unchecked(ButtonData button) {
            if (displayedExotic != null && displayedExotic.equals(button.getData())) {
                displayedExotic = null;
                activeExoticButton = null;
            }
            redisplayExotic(displayedExotic);
        }

        @Override
        public void highlighted(ButtonData button) {
            if (activeExoticButton == null) {
                displayedExotic = (Exotic) button.getData();
                redisplayExotic(displayedExotic);
            }
        }

        @Override
        public void unhighlighted(ButtonData button) {
            if (activeExoticButton == null && displayedExotic != null && displayedExotic.equals(button.getData())) {
                displayedExotic = null;
            }
            redisplayExotic(displayedExotic);
        }
    }

    private class DescriptionSwapButtonListener extends ButtonListener {
        @Override
        public void checked(ButtonData button) {
            if (displayedExotic != null) {
                switchDescriptionDisplayed = !switchDescriptionDisplayed;
                redisplayDescription(displayedExotic);
            }
        }

    }

    private class MethodButtonsListener extends ButtonListener {
        @Override
        public void checked(ButtonData button) {
            if (displayedExotic != null) {
                if (button.equals(installButtonData)) {
                    install(button);
                } else if (button.equals(recoverButtonData)) {
                    recover(button);
                } else if (button.equals(destroyButtonData)) {
                    destroy(button);
                }
            }
        }

        @Override
        public void highlighted(ButtonData button) {
            redisplayResourceMap(displayedExotic, button);
        }

        @Override
        public void unhighlighted(ButtonData button) {
            redisplayResourceMap(displayedExotic, button);
        }

        private void install(ButtonData button) {
            if (!button.isEnabled()) return;
            button.setEnabled(false);

            Exotic exotic = displayedExotic;

            CargoStackAPI stack = Utilities.getExoticChip(fm.getFleetData().getFleet().getCargo(), displayedExotic.getKey());
            if (stack != null) {
                Utilities.takeItem(stack);
            } else {
                exotic.removeItemsFromFleet(fm.getFleetData().getFleet(), fm);
            }

            mods.putExotic(exotic);
            mods.save(fm);
            ExoticaTechHM.addToFleetMember(fm);

            exotic.onInstall(fm);

            displayStringOverResourcePanel(StringUtils.getString("ShipListDialog", "ExoticInstalled"), Color.yellow);

            redisplayExoticIcons();
        }

        private void recover(ButtonData button) {
            if (!button.isEnabled()) return;
            button.setEnabled(false);

            Exotic exotic = displayedExotic;
            CampaignFleetAPI fleet = fm.getFleetData().getFleet();

            CargoStackAPI stack = Utilities.getExoticChip(fleet.getCargo(), exotic.getKey());
            if (stack != null) {
                stack.add(1);
            } else {
                fleet.getCargo().addSpecial(exotic.getNewSpecialItemData(), 1);
            }

            mods.removeExotic(exotic);
            exotic.onDestroy(fm);

            mods.save(fm);
            ExoticaTechHM.addToFleetMember(fm);

            displayStringOverResourcePanel(StringUtils.getString("ShipListDialog", "ExoticRecovered"), Misc.getStoryBrightColor());

            redisplayExoticIcons();
        }

        private void destroy(ButtonData button) {
            if (!button.isEnabled()) return;
            button.setEnabled(false);

            Exotic exotic = displayedExotic;

            mods.removeExotic(exotic);
            exotic.onDestroy(fm);

            mods.save(fm);
            ExoticaTechHM.addToFleetMember(fm);

            displayStringOverResourcePanel(StringUtils.getString("ShipListDialog", "ExoticDestroyed"), Misc.getNegativeHighlightColor());

            redisplayExoticIcons();
        }
    }

    private class UpgradedUIListener implements TimedUIPlugin.Listener {
        @Setter
        private Color backgroundColor = Color.yellow;

        public UpgradedUIListener() {

        }

        public UpgradedUIListener(Color color) {
            this.backgroundColor = color;
        }

        @Override
        public void end() {
            redisplayResourceMap(displayedExotic, null);
            addOptionButtons();
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
            RenderUtils.renderBox(panelX, panelY, panelW, panelH, backgroundColor, alphaMult * (endLife - currLife) / endLife);

            RenderUtils.popUIRenderingStack();
        }
    }
}
