package exoticatechnologies.modifications.stats

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.Modification
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.util.StringUtils

abstract class ModEffect<T : Modification> {
    abstract val key: String
    open val translationParent: String = "ModEffects"
    open val translationKey: String
        get() = key

    open fun getName(): String {
        return StringUtils.getString(translationParent, translationKey)
    }

    open fun applyToStats(stats: MutableShipStatsAPI, member: FleetMemberAPI, mods: ShipModifications, mod: T) {

    }

    open fun applyToShip(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, mod: T) {

    }

    open fun applyToFighter(ship: ShipAPI, fighter: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, mod: T) {
        applyToStats(fighter.mutableStats, member, mods, mod)
    }

    open fun advanceInCombatUnpaused(ship: ShipAPI, amount: Float, member: FleetMemberAPI, mods: ShipModifications, mod: T) {

    }

    open fun advanceInCombatAlways(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, mod: T) {

    }

    open fun advanceInCampaign(member: FleetMemberAPI, mods: ShipModifications, mod: T, amount: Float) {

    }

    abstract fun printToTooltip(tooltip: TooltipMakerAPI, stats: MutableShipStatsAPI, member: FleetMemberAPI, mods: ShipModifications, mod: T): LabelAPI?
    abstract fun printToShop(tooltip: TooltipMakerAPI, member: FleetMemberAPI, mods: ShipModifications, mod: T): LabelAPI?
}