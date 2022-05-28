package exoticatechnologies.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.impl.AlphaSubcore;
import exoticatechnologies.util.FleetMemberUtils;

public class AlphaSubcoreHM extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.ALL_FIGHTER_COST_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_FIGHTER);
        stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_BOMBER);
        stats.getDynamic().getMod(Stats.FIGHTER_COST_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_FIGHTER);
        stats.getDynamic().getMod(Stats.INTERCEPTOR_COST_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_FIGHTER);
        stats.getDynamic().getMod(Stats.SUPPORT_COST_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_FIGHTER);

        stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_LG);
        stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_LG);
        stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_LG);
        stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_MED);
        stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_MED);
        stats.getDynamic().getMod(Stats.MEDIUM_MISSILE_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_MED);
        stats.getDynamic().getMod(Stats.SMALL_BALLISTIC_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_SM);
        stats.getDynamic().getMod(Stats.SMALL_ENERGY_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_SM);
        stats.getDynamic().getMod(Stats.SMALL_MISSILE_MOD).modifyFlat(id, -AlphaSubcore.COST_REDUCTION_SM);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        int i = 0;
        if (index == i++)
            return "" + (int) AlphaSubcore.COST_REDUCTION_SM + "";
        if (index == i++)
            return "" + (int) AlphaSubcore.COST_REDUCTION_MED + "";
        if (index == i++)
            return "" + (int) AlphaSubcore.COST_REDUCTION_LG + "";
        if (index == i++)
            return "" + (int) AlphaSubcore.COST_REDUCTION_FIGHTER + "";
        if (index == i++)
            return "" + (int) AlphaSubcore.COST_REDUCTION_BOMBER + "";
        return null;
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }
}
