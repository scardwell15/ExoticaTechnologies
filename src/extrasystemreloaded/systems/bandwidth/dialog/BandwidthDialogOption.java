package extrasystemreloaded.systems.bandwidth.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import extrasystemreloaded.ESModSettings;
import extrasystemreloaded.Es_ModPlugin;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.dialog.DialogOption;
import extrasystemreloaded.hullmods.ExtraSystemHM;
import extrasystemreloaded.systems.bandwidth.Bandwidth;
import extrasystemreloaded.systems.bandwidth.BandwidthHandler;
import extrasystemreloaded.systems.bandwidth.BandwidthUtil;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BandwidthDialogOption extends DialogOption {
    static final BandwidthDialogOption PURCHASE = new BandwidthDialogOption(Option.PURCHASE);
    public static final float BANDWIDTH_STEP = 5f;

    @Getter private final Option option;

    @Override
    public String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        return StringUtils.getString(this.option.getParent(), this.option.getKey());
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin) {
        switch(option) {
            case PURCHASE:
                TextPanelAPI textPanel = dialog.getTextPanel();
                OptionPanelAPI options = dialog.getOptionPanel();
                FleetMemberAPI fm = plugin.getShip();
                ExtraSystems es = ExtraSystems.getForFleetMember(fm);
                MarketAPI market = plugin.getMarket();

                float shipBandwidth = es.getBandwidth(fm);
                float upgradeBandwidthMult = BandwidthHandler.getMarketBandwidthMult(market);
                float estimatedOverhaulCost = BandwidthHandler.getBandwidthUpgradePrice(fm, shipBandwidth, upgradeBandwidthMult);
                float bonusBandwidth = BANDWIDTH_STEP * upgradeBandwidthMult;
                float newBandwidth = Math.round(shipBandwidth + bonusBandwidth);
                newBandwidth = Math.min(newBandwidth, ESModSettings.getFloat(ESModSettings.MAX_BANDWIDTH));

                es.putBandwidth(newBandwidth);
                es.save(fm);
                ExtraSystemHM.addToFleetMember(fm);

                if(!Es_ModPlugin.isDebugUpgradeCosts()) {
                    Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(estimatedOverhaulCost);
                }

                plugin.redrawResourcesPanel();

                String newBandwidthString = BandwidthUtil.getFormattedBandwidthWithName(newBandwidth);
                StringUtils.getTranslation("BandwidthDialog","UpgradedBandwidthForShip")
                        .format("bandwidth", newBandwidthString)
                        .addToTextPanel(textPanel, Misc.getPositiveHighlightColor(), Bandwidth.getBandwidthColor(newBandwidth));

                if(es.canUpgradeBandwidth(fm)) {
                    newBandwidth = es.getBandwidth(fm);
                    upgradeBandwidthMult = BandwidthHandler.getMarketBandwidthMult(market);
                    bonusBandwidth = Math.min(
                            BANDWIDTH_STEP * upgradeBandwidthMult,
                            ESModSettings.getFloat(ESModSettings.MAX_BANDWIDTH) - newBandwidth);
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
