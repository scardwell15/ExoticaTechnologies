package exoticatechnologies.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.rulecmd.ETPrototypeUI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.modifications.bandwidth.BandwidthHandler;
import exoticatechnologies.modifications.bandwidth.BandwidthUtil;
import exoticatechnologies.util.StringUtils;
import lombok.Setter;

import java.awt.*;

public class ShipModificationUIPanelPlugin extends TabbedCustomUIPanelPlugin {
    public static final int PICKER_INDEX = 0;
    public static final int BANDWIDTH_INDEX = 1;
    public static final int UPGRADES_INDEX = 2;
    public static final int EXOTICS_INDEX = 3;

    private static final float PICKER_TEXT_WIDTH = 45;
    private static final float BANDWIDTH_TEXT_WIDTH = 64;
    private static final float UPGRADES_TEXT_WIDTH = 64;
    private static final float EXOTICS_TEXT_WIDTH = 45;
    private static final float UNSELECTED_PANEL_HEIGHT = 64;
    private static final float SELECTED_PANEL_HEIGHT = UNSELECTED_PANEL_HEIGHT * 3.5f;

    @Setter protected float upperXOffset = 203;
    protected final ETPrototypeUI.ShipModificationDialogDelegate delegate;
    protected final FleetMemberAPI fm;
    protected final MarketAPI market;
    protected final ShipAPI.HullSize hullSize;
    protected final ShipModifications mods;

    @Setter
    protected LabelAPI bandwidthLabel;


    public ShipModificationUIPanelPlugin(ETPrototypeUI.ShipModificationDialogDelegate delegate, TooltipMakerAPI tooltip, float defaultSwitcherPanelWidth, FleetMemberAPI fm, ShipModifications mods, MarketAPI market) {
        super(tooltip, defaultSwitcherPanelWidth);
        this.delegate = delegate;
        this.fm = fm;
        this.mods = mods;
        this.hullSize = fm.getHullSpec().getHullSize();
        this.market = market;
    }

    @Override
    protected int getMaxPanelIndex() {
        return EXOTICS_INDEX;
    }

    @Override
    protected boolean shouldMakeSwitcher() {
        return true;
    }

    @Override
    protected boolean canSwitch() {
        return true;
    }

    @Override
    protected String getSwitcherLabelText(int newPanelIndex) {
        if (newPanelIndex == PICKER_INDEX) {
            return StringUtils.getString("ShipListDialog", "OpenModOptions");
        } else if (newPanelIndex == BANDWIDTH_INDEX) {
            return StringUtils.getString("BandwidthDialog", "OpenBandwidthOptions");
        } else if (newPanelIndex == UPGRADES_INDEX) {
            return StringUtils.getString("UpgradesDialog", "OpenUpgradeOptions");
        } else if (newPanelIndex == EXOTICS_INDEX) {
            return StringUtils.getString("ExoticsDialog", "OpenExoticOptions");
        }
        throw new RuntimeException("Unexpected panel index");
    }

    @Override
    protected float getSwitcherLabelWidth(int newPanelIndex) {
        if (newPanelIndex == PICKER_INDEX) {
            return PICKER_TEXT_WIDTH;
        } else if (newPanelIndex == BANDWIDTH_INDEX) {
            return BANDWIDTH_TEXT_WIDTH;
        } else if (newPanelIndex == UPGRADES_INDEX) {
            return UPGRADES_TEXT_WIDTH;
        } else if (newPanelIndex == EXOTICS_INDEX) {
            return EXOTICS_TEXT_WIDTH;
        }
        throw new RuntimeException("Unexpected panel index");
    }

    protected float getSwitcherPanelHeight(int newPanelIndex) {
        if (newPanelIndex == PICKER_INDEX
                || newPanelIndex == BANDWIDTH_INDEX) {
            return UNSELECTED_PANEL_HEIGHT;
        }
        return SELECTED_PANEL_HEIGHT;
    }

    @Override
    protected CustomPanelAPI createNewPanel(int newPanelIndex, float panelWidth, float panelHeight) {
        if (newPanelIndex == PICKER_INDEX) {
            return createPickerPanel(panelWidth, panelHeight);
        } else if (newPanelIndex == BANDWIDTH_INDEX) {
            return createBandwidthPanel(panelWidth, panelHeight);
        } else if (newPanelIndex == UPGRADES_INDEX) {
            return createUpgradesPanel(panelWidth, panelHeight);
        } else if (newPanelIndex == EXOTICS_INDEX) {
            return createExoticsPanel(panelWidth, panelHeight);
        }
        throw new RuntimeException("Unexpected panel index");
    }

    @Override
    protected void switcherButtonClicked() {
        super.switcherButtonClicked();
        delegate.selectedPanelEvent(this);
    }

    @Override
    protected float getSwitcherButtonHeight(int newPanelIndex) {
        return 22;
    }

    public void reloadBandwidth() {
        float bandwidth = mods.getBandwidthWithExotics(fm);
        String bandwidthString = BandwidthUtil.getFormattedBandwidthWithName(bandwidth);
        Color bandwidthColor = Bandwidth.getBandwidthColor(bandwidth);

        bandwidthLabel.setText(StringUtils.getTranslation("FleetScanner", "ShipBandwidthShort")
                .format("bandwidth", bandwidthString, bandwidthColor)
                .toStringNoFormats());
        bandwidthLabel.setHighlightColor(bandwidthColor);
        bandwidthLabel.setHighlight(bandwidthString);
    }

    protected CustomPanelAPI createPickerPanel(float panelWidth, float panelHeight) {
        CustomPanelAPI pickmePanel = myPanel.createCustomPanel(panelWidth, panelHeight, null);

        TooltipMakerAPI bandwidthTooltip = pickmePanel.createUIElement(panelWidth, panelHeight, false);

        StringUtils.getTranslation("ShipListDialog", "SelectPanelText")
                .format("numUpgrades", mods.getUpgradeMap().size())
                .format("numExotics", mods.getExoticSet().size())
                .addToTooltip(bandwidthTooltip)
                .getPosition().inTL(3, switcherPanelHeight / 4f - 3);

        pickmePanel.addUIElement(bandwidthTooltip).inTL(getSwitcherButtonXOffset(PICKER_INDEX) + getSwitcherButtonWidth(PICKER_INDEX) + 3, 3);

        return pickmePanel;
    }

    protected CustomPanelAPI createBandwidthPanel(float panelWidth, float panelHeight) {
        BandwidthUIPlugin plugin = new BandwidthUIPlugin(this, fm, mods, market);
        CustomPanelAPI bandwidthPanel = myPanel.createCustomPanel(panelWidth, panelHeight, plugin);

        float shipBandwidth = mods.getBandwidth(fm);
        TooltipMakerAPI bandwidthTooltip = bandwidthPanel.createUIElement(panelWidth, panelHeight, false);

        float upgradeBandwidthMult = BandwidthHandler.getMarketBandwidthMult(market);
        String bonusBandwidth = BandwidthUtil.getRoundedBandwidth(Bandwidth.BANDWIDTH_STEP * upgradeBandwidthMult);

        float bandwidthUpgradePrice = BandwidthHandler.getBandwidthUpgradePrice(fm, shipBandwidth, upgradeBandwidthMult);
        String needCredits = Misc.getFormat().format(bandwidthUpgradePrice);

        float playerCredits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
        boolean enabled = false;
        if (mods.getBandwidth(fm) >= Bandwidth.MAX_BANDWIDTH) {
            StringUtils.getTranslation("ShipListDialog", "BandwidthUpgradePeak")
                    .addToTooltip(bandwidthTooltip);
        } else if (playerCredits >= bandwidthUpgradePrice) {
            enabled = true;
            StringUtils.getTranslation("ShipListDialog", "BandwidthUpgradeCost")
                    .format("bonusBandwidth", bonusBandwidth)
                    .format("credits", needCredits)
                    .addToTooltip(bandwidthTooltip);
        } else {
            StringUtils.getTranslation("ShipListDialog", "BandwidthUpgradeCostCannotAfford")
                    .format("credits", needCredits)
                    .addToTooltip(bandwidthTooltip);
        }

        UIComponentAPI costLabel = bandwidthTooltip.getPrev();

        //manually set size of label. for some reason it gets set to 1000 pixels by default.
        costLabel.getPosition().setSize(bandwidthTooltip.computeStringWidth(((LabelAPI) costLabel).getText()), 22).inLMid(3f).setYAlignOffset(-5f);

        ButtonAPI purchaseButton = bandwidthTooltip.addButton(StringUtils.getString("ShipListDialog", "BandwidthPurchase"), "purchaseBandwidth", 72, 22, 0f);
        purchaseButton.setEnabled(enabled);
        purchaseButton.getPosition().rightOfMid(costLabel, 6f).setYAlignOffset(3f);
        plugin.setUpgradeButton(purchaseButton);

        bandwidthPanel.addUIElement(bandwidthTooltip).inTL(getSwitcherPanelUpperOffset(BANDWIDTH_INDEX) + 3, 3);

        return bandwidthPanel;
    }

    protected CustomPanelAPI createUpgradesPanel(float panelWidth, float panelHeight) {
        UpgradeUIPlugin plugin = new UpgradeUIPlugin(this, fm, mods, market, panelWidth, panelHeight);
        CustomPanelAPI iconPanel = myPanel.createCustomPanel(panelWidth, panelHeight, plugin);
        plugin.setPanel(iconPanel);
        plugin.initialize();

        return iconPanel;
    }

    protected CustomPanelAPI createExoticsPanel(float panelWidth, float panelHeight) {
        ExoticUIPlugin plugin = new ExoticUIPlugin(this, fm, mods, market, panelWidth, panelHeight);
        CustomPanelAPI iconPanel = myPanel.createCustomPanel(panelWidth, panelHeight, plugin);
        plugin.setPanel(iconPanel);
        plugin.initialize();
        return iconPanel;
    }


    @Override
    protected float getSwitcherButtonXOffset(int newPanelIndex) {
        return upperXOffset + getSwitcherButtonWidth(newPanelIndex);
    }

    public boolean isSelected() {
        return currentPanelIndex > PICKER_INDEX;
    }
}
