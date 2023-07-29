package exoticatechnologies.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.exotics.impl.AlphaSubcore
import exoticatechnologies.util.StringUtils

class AlphaSubcoreHM : BaseHullMod() {
    override fun applyEffectsBeforeShipCreation(hullSize: HullSize, stats: MutableShipStatsAPI, id: String) {
        if(stats.variant.hullMods
            .any { AlphaSubcore.BLOCKED_HULLMODS.contains(it) }
            ) {
            return
        }

        stats.dynamic.getMod(Stats.ALL_FIGHTER_COST_MOD).modifyFlat(id, -COST_REDUCTION_FIGHTER.toFloat())
        stats.dynamic.getMod(Stats.BOMBER_COST_MOD).modifyFlat(id, -COST_REDUCTION_BOMBER.toFloat())
        stats.dynamic.getMod(Stats.FIGHTER_COST_MOD).modifyFlat(id, -COST_REDUCTION_FIGHTER.toFloat())
        stats.dynamic.getMod(Stats.INTERCEPTOR_COST_MOD).modifyFlat(id, -COST_REDUCTION_FIGHTER.toFloat())
        stats.dynamic.getMod(Stats.SUPPORT_COST_MOD).modifyFlat(id, -COST_REDUCTION_FIGHTER.toFloat())
        stats.dynamic.getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION_LG.toFloat())
        stats.dynamic.getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_LG.toFloat())
        stats.dynamic.getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION_LG.toFloat())
        stats.dynamic.getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION_MED.toFloat())
        stats.dynamic.getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_MED.toFloat())
        stats.dynamic.getMod(Stats.MEDIUM_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION_MED.toFloat())
        stats.dynamic.getMod(Stats.SMALL_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION_SM.toFloat())
        stats.dynamic.getMod(Stats.SMALL_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_SM.toFloat())
        stats.dynamic.getMod(Stats.SMALL_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION_SM.toFloat())
    }

    override fun addPostDescriptionSection(
        tooltip: TooltipMakerAPI?,
        hullSize: HullSize?,
        ship: ShipAPI?,
        width: Float,
        isForModSpec: Boolean
    ) {
        if(ship?.variant?.hullMods?.any { AlphaSubcore.BLOCKED_HULLMODS.contains(it) } == true) {
            StringUtils.getTranslation("AlphaSubcore", "conflictDetected")
                .addToTooltip(tooltip)
        }

        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)
    }

    override fun getDescriptionParam(index: Int, hullSize: HullSize): String? {
        var i = 0
        if (index == i++) return COST_REDUCTION_SM.toString()
        if (index == i++) return COST_REDUCTION_MED.toString()
        if (index == i++) return COST_REDUCTION_LG.toString()
        if (index == i++) return COST_REDUCTION_FIGHTER.toString()
        return if (index == i++) COST_REDUCTION_BOMBER.toString() else null
    }

    override fun affectsOPCosts(): Boolean {
        return true
    }

    companion object {
        const val COST_REDUCTION_LG = 4
        const val COST_REDUCTION_MED = 2
        const val COST_REDUCTION_SM = 1
        const val COST_REDUCTION_FIGHTER = 2
        const val COST_REDUCTION_BOMBER = 2
    }
}