package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
import org.json.JSONException;

import java.awt.*;
import java.util.Map;

public class EqualizerCore extends Exotic {
    private static final String ITEM = "et_equalizercore";
    private static final Color[] tooltipColors = {Color.orange.darker(), ExoticaTechHM.infoColor};

    private static float RECOIL_REDUCTION = -25f;
    private static float TURN_RATE_BUFF = 50f;

    private static int RANGE_LIMIT_BOTTOM = 550;
    private static int RANGE_BOTTOM_BUFF = 200;
    private static int RANGE_LIMIT_TOP = 800;
    private static int RANGE_TOP_BUFF = -150;

    @Getter private final Color mainColor = Color.orange.darker();

    @Override
    public void loadConfig() throws JSONException {
        RECOIL_REDUCTION = (float) exoticSettings.getDouble("recoilReduction");
        TURN_RATE_BUFF = (float) exoticSettings.getDouble("weaponTurnRateIncrease");

        RANGE_LIMIT_BOTTOM = (int) exoticSettings.getInt("rangeBottomBounds");
        RANGE_BOTTOM_BUFF = (int) exoticSettings.getInt("rangeBottomBuff");
        RANGE_LIMIT_TOP = (int) exoticSettings.getInt("rangeTopBounds");
        RANGE_TOP_BUFF = (int) exoticSettings.getInt("rangeTopBuff");
    }

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return Utilities.hasItem(fleet.getCargo(), ITEM);
    }

    public String getUnableToApplyTooltip(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        return "You need an Equalizer Core to install this.";
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        Utilities.takeItemQuantity(fleet.getCargo(), ITEM, 1);

        return true;
    }

    @Override
    public boolean restoreItemsToFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        Utilities.addItem(fleet, ITEM, 1);
        return true;
    }

    @Override
    public void modifyResourcesPanel(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm) {
        resourceCosts.put(ITEM, 1f);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        if (systems.hasExotic(this.getKey())) {
            if(expand) {
                StringUtils.getTranslation(this.getKey(), "longDescription")
                        .format("exoticName", this.getName())
                        .format("recoilReduction", RECOIL_REDUCTION)
                        .format("weaponTurnBonus", TURN_RATE_BUFF)
                        .format("lowRangeThreshold", RANGE_LIMIT_BOTTOM)
                        .format("rangeBonus", RANGE_BOTTOM_BUFF)
                        .format("highRangeThreshold", RANGE_LIMIT_TOP)
                        .format("rangeMalus", RANGE_TOP_BUFF)
                        .addToTooltip(tooltip, tooltipColors);
            } else {
                tooltip.addPara(this.getName(), tooltipColors[0], 5);
            }
        }
    }

    @Override
    public void applyExoticToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, float bandwidth, String id) {
        stats.getAutofireAimAccuracy().modifyPercent(this.getBuffId(), 1000f);
        stats.getMaxRecoilMult().modifyMult(this.getBuffId(), Math.abs(RECOIL_REDUCTION) / 100f);
        stats.getRecoilDecayMult().modifyMult(this.getBuffId(), Math.abs(RECOIL_REDUCTION) / 100f);
        stats.getRecoilPerShotMult().modifyMult(this.getBuffId(), Math.abs(RECOIL_REDUCTION) / 100f);

        stats.getWeaponTurnRateBonus().modifyPercent(this.getBuffId(), TURN_RATE_BUFF);
        stats.getBeamWeaponTurnRateBonus().modifyPercent(this.getBuffId(), TURN_RATE_BUFF);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount, float bandwidth) {
        if (!ship.hasListenerOfClass(ESR_EqualizerCoreListener.class)) {
            ship.addListener(new ESR_EqualizerCoreListener());
        }
    }

    // Our range listener
    private class ESR_EqualizerCoreListener implements WeaponBaseRangeModifier {

        @Override
        public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
            return 0f;
        }

        @Override
        public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
            return 1f;
        }

        @Override
        public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            if (weapon.getType() == WeaponAPI.WeaponType.MISSILE) {
                return 0f;
            }

            float baseRangeMod = 0;
            if(weapon.getSpec().getMaxRange() >= RANGE_LIMIT_TOP) {
                baseRangeMod = RANGE_TOP_BUFF;
            } else if (weapon.getSpec().getMaxRange() <= RANGE_LIMIT_BOTTOM) {
                baseRangeMod = RANGE_BOTTOM_BUFF;
            }

            return baseRangeMod;
        }
    }
}