package exoticatechnologies.ui.java;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.modifications.bandwidth.BandwidthHandler;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@RequiredArgsConstructor
public class BandwidthUIPlugin implements CustomUIPanelPlugin {
    @Setter
    private ButtonAPI upgradeButton;
    private final FleetMemberAPI fm;
    private final ShipModifications mods;
    private final MarketAPI market;

    @Override
    public void advance(float amount) {
        if (upgradeButton.isChecked()) {
            doBandwidthPurchase();
        }
    }

    @Override
    public void positionChanged(PositionAPI position) {
    }

    @Override
    public void renderBelow(float alphaMult) {
    }

    @Override
    public void render(float alphaMult) {
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
    }

    public void doBandwidthPurchase() {
        float shipBandwidth = mods.getBaseBandwidth(fm);
        float upgradeBandwidthMult = BandwidthHandler.getMarketBandwidthMult(market);
        float estimatedOverhaulCost = BandwidthHandler.getBandwidthUpgradePrice(fm, shipBandwidth, upgradeBandwidthMult);
        float bonusBandwidth = Bandwidth.BANDWIDTH_STEP * upgradeBandwidthMult;
        float newBandwidth = Math.round(shipBandwidth + bonusBandwidth);
        newBandwidth = Math.min(newBandwidth, Bandwidth.MAX_BANDWIDTH);

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 0.75f);

        mods.setBandwidth(newBandwidth);
        mods.save(fm);
        ExoticaTechHM.addToFleetMember(fm);

        if (!ETModPlugin.isDebugUpgradeCosts()) {
            Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(estimatedOverhaulCost);
        }
    }
}
