package exoticatechnologies.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize
import com.fs.starfarer.api.combat.listeners.FighterOPCostModifier
import com.fs.starfarer.api.combat.listeners.WeaponOPCostModifier
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.loading.FighterWingSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.exotics.impl.AlphaSubcore
import exoticatechnologies.util.StringUtils

class AlphaSubcoreHM : BaseHullMod() {
    override fun applyEffectsBeforeShipCreation(hullSize: HullSize, stats: MutableShipStatsAPI, id: String) {
        if(stats.variant.hullMods.any { AlphaSubcore.BLOCKED_HULLMODS.contains(it) }) {
            return
        }

        stats.addListener(OPCostListener())
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

    class OPCostListener : WeaponOPCostModifier,FighterOPCostModifier {
        override fun getWeaponOPCost(stats: MutableShipStatsAPI, weapon: WeaponSpecAPI, currCost: Int): Int {
            return currCost - (WEAPON_REDUCTIONS[weapon.size] ?: 0)
        }

        override fun getFighterOPCost(stats: MutableShipStatsAPI, fighter: FighterWingSpecAPI, currCost: Int): Int {
            if (fighter.isBomber)
                return currCost - BOMBER_REDUCTION
            return currCost - FIGHTER_REDUCTION
        }
    }

    companion object {
        val WEAPON_REDUCTIONS = mutableMapOf(
            WeaponSize.SMALL to 1,
            WeaponSize.MEDIUM to 2,
            WeaponSize.LARGE to 4
        )

        const val BOMBER_REDUCTION = 2
        const val FIGHTER_REDUCTION = 2

        const val COST_REDUCTION_LG = 4
        const val COST_REDUCTION_MED = 2
        const val COST_REDUCTION_SM = 1
        const val COST_REDUCTION_FIGHTER = 2
        const val COST_REDUCTION_BOMBER = 2
    }
}