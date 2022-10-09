package exoticatechnologies.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import exoticatechnologies.modifications.exotics.impl.AlphaSubcore;

public class AlphaSubcoreHM extends BaseHullMod {
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return "";
    }
}
