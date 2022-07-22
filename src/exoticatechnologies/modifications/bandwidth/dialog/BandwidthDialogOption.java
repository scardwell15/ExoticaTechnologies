package exoticatechnologies.modifications.bandwidth.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.dialog.DialogOption;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.modifications.bandwidth.BandwidthHandler;
import exoticatechnologies.modifications.bandwidth.BandwidthUtil;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BandwidthDialogOption extends DialogOption {
    static final BandwidthDialogOption PURCHASE = new BandwidthDialogOption(Option.PURCHASE);
    public static final float BANDWIDTH_STEP = 5f;

    @Getter private final Option option;

    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        return StringUtils.getString(this.option.getParent(), this.option.getKey());
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {
        switch(option) {
            case PURCHASE:
                TextPanelAPI textPanel = dialog.getTextPanel();
                OptionPanelAPI options = dialog.getOptionPanel();
                FleetMemberAPI fm = plugin.getShip();
                ShipModifications mods = ShipModFactory.getForFleetMember(fm);
                MarketAPI market = plugin.getMarket();

                float shipBandwidth = mods.getBandwidth(fm);
                float upgradeBandwidthMult = BandwidthHandler.getMarketBandwidthMult(market);
                float estimatedOverhaulCost = BandwidthHandler.getBandwidthUpgradePrice(fm, shipBandwidth, upgradeBandwidthMult);
                float bonusBandwidth = BANDWIDTH_STEP * upgradeBandwidthMult;
                float newBandwidth = Math.round(shipBandwidth + bonusBandwidth);
                newBandwidth = Math.min(newBandwidth, Bandwidth.MAX_BANDWIDTH);

                mods.putBandwidth(newBandwidth);
                mods.save(fm);
                ExoticaTechHM.addToFleetMember(fm);

                if(!ETModPlugin.isDebugUpgradeCosts()) {
                    Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(estimatedOverhaulCost);
                }

                plugin.redrawResourcesPanel();

                String newBandwidthString = BandwidthUtil.getFormattedBandwidthWithName(newBandwidth);
                StringUtils.getTranslation("BandwidthDialog","UpgradedBandwidthForShip")
                        .format("bandwidth", newBandwidthString)
                        .addToTextPanel(textPanel, Misc.getPositiveHighlightColor(), Bandwidth.getBandwidthColor(newBandwidth));

                if(mods.canUpgradeBandwidth(fm)) {
                    newBandwidth = mods.getBandwidth(fm);
                    upgradeBandwidthMult = BandwidthHandler.getMarketBandwidthMult(market);
                    bonusBandwidth = Math.min(
                            BANDWIDTH_STEP * upgradeBandwidthMult,
                            Bandwidth.MAX_BANDWIDTH - newBandwidth);
                    estimatedOverhaulCost = BandwidthHandler.getBandwidthUpgradePrice(fm, newBandwidth, upgradeBandwidthMult);

                    String needCredits = Misc.getFormat().format(estimatedOverhaulCost);
                    String bonusBandwidthString = BandwidthUtil.getRoundedBandwidth(bonusBandwidth);

                    StringUtils.getTranslation("BandwidthDialog","AnotherBandwidthUpgradeForShip")
                            .format("credits", needCredits)
                            .format("bonusBandwidth", bonusBandwidthString)
                            .addToTextPanel(textPanel);

                    if (!BandwidthHandler.isAbleToPayForBandwidthUpgrade(Global.getSector().getPlayerFleet(), estimatedOverhaulCost)) {
                        options.setEnabled(this, false);
                        options.setTooltip(this, StringUtils.getString("BandwidthDialog","ConfirmInsufficientCredits"));
                    }
                } else {
                    textPanel.addParagraph(
                            StringUtils.getString("BandwidthDialog","UpgradedToMaxBandwidthShip"));

                    options.setTooltip(this, StringUtils.getString("BandwidthDialog","ConfirmBandwidthTooHigh"));
                    options.setEnabled(this, false);
                }
                break;
        }
    }

    @RequiredArgsConstructor
    public enum Option {
        PURCHASE("BandwidthDialog", "ConfirmPurchaseUpgrade");
        @Getter private final String parent;
        @Getter private final String key;
    }
}
