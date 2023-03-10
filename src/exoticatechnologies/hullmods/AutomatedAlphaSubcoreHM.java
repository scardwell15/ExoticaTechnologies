package exoticatechnologies.hullmods;

import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class AutomatedAlphaSubcoreHM extends BaseHullMod {

    private static final String ET_AUTOMATED_POINTS_MULT = "et_autoPointsMultApplied";

    public static final int COST_REDUCTION_LG = 2;
    public static final int COST_REDUCTION_MED = 1;
    public static final int COST_REDUCTION_SM = 1;
    public static final int COST_REDUCTION_FIGHTER = 2;
    public static final int COST_REDUCTION_BOMBER = 2;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.ALL_FIGHTER_COST_MOD).modifyFlat(id, -COST_REDUCTION_FIGHTER);
        stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyFlat(id, -COST_REDUCTION_BOMBER);
        stats.getDynamic().getMod(Stats.FIGHTER_COST_MOD).modifyFlat(id, -COST_REDUCTION_FIGHTER);
        stats.getDynamic().getMod(Stats.INTERCEPTOR_COST_MOD).modifyFlat(id, -COST_REDUCTION_FIGHTER);
        stats.getDynamic().getMod(Stats.SUPPORT_COST_MOD).modifyFlat(id, -COST_REDUCTION_FIGHTER);

        stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION_LG);
        stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_LG);
        stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION_LG);
        stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION_MED);
        stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_MED);
        stats.getDynamic().getMod(Stats.MEDIUM_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION_MED);
        stats.getDynamic().getMod(Stats.SMALL_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION_SM);
        stats.getDynamic().getMod(Stats.SMALL_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_SM);
        stats.getDynamic().getMod(Stats.SMALL_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION_SM);

        if (stats.getFleetMember() != null && stats.getFleetMember().getCaptain() != null) {
            PersonAPI captain = stats.getFleetMember().getCaptain();
            if (!captain.getMemoryWithoutUpdate().contains(ET_AUTOMATED_POINTS_MULT)) {
                float mult;
                if (captain.getMemoryWithoutUpdate().contains(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT)) {
                    mult = captain.getMemoryWithoutUpdate().getFloat(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT);
                } else {
                    mult = 1f;
                }
                mult *= 0.5f;

                captain.getMemoryWithoutUpdate().set(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT, mult);
                captain.getMemoryWithoutUpdate().set(ET_AUTOMATED_POINTS_MULT, true);
            }
        }
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        int i = 0;
        if (index == i++)
            return String.valueOf(COST_REDUCTION_SM);
        if (index == i++)
            return String.valueOf(COST_REDUCTION_MED);
        if (index == i++)
            return String.valueOf(COST_REDUCTION_LG);
        if (index == i++)
            return String.valueOf(COST_REDUCTION_FIGHTER);
        if (index == i++)
            return String.valueOf(COST_REDUCTION_BOMBER);
        return null;
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }
}

