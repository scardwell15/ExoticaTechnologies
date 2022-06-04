package exoticatechnologies.modifications.bandwidth.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.dialog.modifications.SystemState;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.modifications.bandwidth.BandwidthHandler;
import exoticatechnologies.modifications.bandwidth.BandwidthUtil;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import org.lwjgl.input.Keyboard;

import java.util.Map;

public class BandwidthState extends SystemState {
    @Override
    public void modifyInteractionPanel(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        FleetMemberAPI fm = plugin.getShip();
        ShipModifications mods = ShipModFactory.getForFleetMember(fm);
        MarketAPI market = plugin.getMarket();

        float shipBandwidth = mods.getBandwidthWithExotics(fm);

        String shipBandwidthText = BandwidthUtil.getFormattedBandwidthWithName(shipBandwidth);
        String usedBandwidthText = BandwidthUtil.getFormattedBandwidth(mods.getUsedBandwidth());
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
    public void modifyResourcesPanel(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts) {
        FleetMemberAPI fm = plugin.getShip();
        ShipModifications mods = ShipModFactory.getForFleetMember(fm);
        float shipBandwidth = mods.getBandwidth(fm);
        float upgradeBandwidthMult = BandwidthHandler.getMarketBandwidthMult(plugin.getMarket());
        float estimatedOverhaulCost = BandwidthHandler.getBandwidthUpgradePrice(fm, shipBandwidth, upgradeBandwidthMult);

        resourceCosts.put(Commodities.CREDITS, (float) Math.ceil(estimatedOverhaulCost));
    }

    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        return StringUtils.getString("BandwidthDialog", "OpenBandwidthOptions");
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        FleetMemberAPI fm = plugin.getShip();
        ShipModifications mods = ShipModFactory.getForFleetMember(fm);

        float shipBandwidth = mods.getBandwidth(fm);
        float upgradeBandwidthMult = BandwidthHandler.getMarketBandwidthMult(plugin.getMarket());
        float estimatedOverhaulCost = BandwidthHandler.getBandwidthUpgradePrice(fm, shipBandwidth, upgradeBandwidthMult);

        String shipBandwidthText = BandwidthUtil.getFormattedBandwidthWithName(mods.getBandwidthWithExotics(fm));
        String usedBandwidthText = BandwidthUtil.getFormattedBandwidth(mods.getUsedBandwidth());
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
        BandwidthDialogOption.PURCHASE.addToOptions(options, plugin, fm, mods);

        if (!BandwidthHandler.isAbleToPayForBandwidthUpgrade(Global.getSector().getPlayerFleet(), estimatedOverhaulCost)) {
            options.setTooltip(BandwidthDialogOption.PURCHASE, StringUtils.getString("BandwidthDialog","ConfirmInsufficientCredits"));
            options.setEnabled(BandwidthDialogOption.PURCHASE, false);
        } else if (!BandwidthHandler.canUpgrade(mods, fm)) {
            options.setTooltip(BandwidthDialogOption.PURCHASE, StringUtils.getString("BandwidthDialog","ConfirmBandwidthTooHigh"));
            options.setEnabled(BandwidthDialogOption.PURCHASE, false);
        }

        ETInteractionDialogPlugin.SYSTEM_PICKER.addToOptions(options, plugin, fm, mods, Keyboard.KEY_ESCAPE);
    }

    @Override
    public void addToOptions(OptionPanelAPI options, ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods, String tooltip, int hotkey) {
        super.addToOptions(options, plugin, fm, mods, tooltip, hotkey);

        if(!mods.canUpgradeBandwidth(fm)) {
            options.setEnabled(this, false);
            options.setTooltip(this, StringUtils.getString("BandwidthDialog", "ConfirmBandwidthTooHigh"));
        }
    }
}
