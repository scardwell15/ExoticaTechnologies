package extrasystemreloaded.systems.bandwidth.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Misc;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.dialog.modifications.SystemState;
import extrasystemreloaded.systems.bandwidth.Bandwidth;
import extrasystemreloaded.systems.bandwidth.BandwidthHandler;
import extrasystemreloaded.systems.bandwidth.BandwidthUtil;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import org.lwjgl.input.Keyboard;

import java.util.Map;

public class BandwidthState extends SystemState {
    @Override
    public void modifyInteractionPanel(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        FleetMemberAPI fm = plugin.getShip();
        ExtraSystems es = ExtraSystems.getForFleetMember(fm);
        MarketAPI market = plugin.getMarket();

        float shipBandwidth = es.getBandwidth(fm);

        String shipBandwidthText = BandwidthUtil.getFormattedBandwidthWithName(shipBandwidth);
        String usedBandwidthText = BandwidthUtil.getFormattedBandwidth(es.getUsedBandwidth());
        StringUtils.getTranslation("BandwidthDialog", "BandwidthForShip")
                .format("bandwidth", shipBandwidthText)
                .format("bandwidthUsedByUpgrades", usedBandwidthText)
                .addToTextPanel(textPanel, Bandwidth.getBandwidthColor(shipBandwidth), Misc.getHighlightColor());

        String bandwidthBonusFromMarket = (int) (BandwidthHandler.getMarketBandwidthMult(market) * 100f) + "%";

        StringUtils.getTranslation("BandwidthDialog", "BandwidthBonusFromMarket")
                .format("bonus", bandwidthBonusFromMarket)
                .addToTextPanel(textPanel);
    }

    @Override
    public void modifyResourcesPanel(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin, Map<String, Float> resourceCosts) {
        FleetMemberAPI fm = plugin.getShip();
        ExtraSystems es = ExtraSystems.getForFleetMember(fm);
        float shipBandwidth = es.getBandwidth(fm);
        float upgradeBandwidthMult = BandwidthHandler.getMarketBandwidthMult(plugin.getMarket());
        float estimatedOverhaulCost = BandwidthHandler.getBandwidthUpgradePrice(fm, shipBandwidth, upgradeBandwidthMult);

        resourceCosts.put(Commodities.CREDITS, (float) Math.ceil(estimatedOverhaulCost));
    }

    @Override
    public String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        return StringUtils.getString("BandwidthDialog", "OpenBandwidthOptions");
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        FleetMemberAPI fm = plugin.getShip();
        ExtraSystems es = ExtraSystems.getForFleetMember(fm);

        float shipBandwidth = es.getBandwidth(fm);
        float upgradeBandwidthMult = BandwidthHandler.getMarketBandwidthMult(plugin.getMarket());
        float estimatedOverhaulCost = BandwidthHandler.getBandwidthUpgradePrice(fm, shipBandwidth, upgradeBandwidthMult);

        String shipBandwidthText = BandwidthUtil.getFormattedBandwidthWithName(shipBandwidth);
        String usedBandwidthText = BandwidthUtil.getFormattedBandwidth(es.getUsedBandwidth());
        StringUtils.getTranslation("BandwidthDialog", "BandwidthForShip")
                .format("bandwidth", shipBandwidthText)
                .format("bandwidthUsedByUpgrades", usedBandwidthText)
                .addToTextPanel(textPanel, Bandwidth.getBandwidthColor(shipBandwidth), Misc.getHighlightColor());

        String bonusBandwidth = BandwidthUtil.getRoundedBandwidth(BandwidthDialogOption.BANDWIDTH_STEP * upgradeBandwidthMult);
        StringUtils.getTranslation("BandwidthDialog","BandwidthUpgradeForShip")
                .format("bonusBandwidth", bonusBandwidth)
                .addToTextPanel(textPanel);

        String needCredits = Misc.getFormat().format(estimatedOverhaulCost);
        StringUtils.getTranslation("BandwidthDialog","CostCreditsToUpgrade")
                .format("credits", needCredits)
                .addToTextPanel(textPanel);

        OptionPanelAPI options = dialog.getOptionPanel();
        BandwidthDialogOption.PURCHASE.addToOptions(options, plugin, fm, es);

        if (!BandwidthHandler.isAbleToPayForBandwidthUpgrade(Global.getSector().getPlayerFleet(), estimatedOverhaulCost)) {
            options.setTooltip(BandwidthDialogOption.PURCHASE, StringUtils.getString("BandwidthDialog","ConfirmInsufficientCredits"));
            options.setEnabled(BandwidthDialogOption.PURCHASE, false);
        } else if (!BandwidthHandler.canUpgrade(es, fm)) {
            options.setTooltip(BandwidthDialogOption.PURCHASE, StringUtils.getString("BandwidthDialog","ConfirmBandwidthTooHigh"));
            options.setEnabled(BandwidthDialogOption.PURCHASE, false);
        }

        ESInteractionDialogPlugin.SYSTEM_PICKER.addToOptions(options, plugin, fm, es, Keyboard.KEY_ESCAPE);
    }

    @Override
    public void addToOptions(OptionPanelAPI options, ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es, String tooltip, int hotkey) {
        super.addToOptions(options, plugin, fm, es, tooltip, hotkey);

        if(!es.canUpgradeBandwidth(fm)) {
            options.setEnabled(this, false);
            options.setTooltip(this, StringUtils.getString("BandwidthDialog", "ConfirmBandwidthTooHigh"));
        }
    }
}
