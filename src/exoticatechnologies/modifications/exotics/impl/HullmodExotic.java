package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.util.StringUtils;

import java.awt.*;

public class HullmodExotic extends Exotic {
    private final String hullmodId;
    private final String translationKey;
    private final Color mainColor;

    public HullmodExotic(String hullmodId, String translationKey, Color mainColor) {
        this.hullmodId = hullmodId;
        this.translationKey = translationKey;
        this.mainColor = mainColor;
    }

    @Override
    public Color getMainColor() {
        return mainColor;
    }

    @Override
    public void onInstall(FleetMemberAPI fm) {
        if(fm.getVariant() != null && !fm.getVariant().hasHullMod(hullmodId)) {
            fm.getVariant().addMod(hullmodId);
        }
    }

    @Override
    public void onDestroy(FleetMemberAPI fm) {
        if (fm.getVariant() != null) {
            fm.getVariant().removeMod(hullmodId);
        }
    }

    @Override
    public void applyExoticToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, float bandwidth, String id) {
        onInstall(fm);
    }

    @Override
    public void applyExoticToShip(FleetMemberAPI fm, ShipAPI ship, float bandwidth, String id) {
        onInstall(fm);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        if (expand) {
            StringUtils.getTranslation(this.getKey(), translationKey)
                    .addToTooltip(tooltip, title);
        }
    }
}
