package exoticatechnologies.combat

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

object ExoticaCombatUtils {
    fun getAverageWeaponRange(ship: ShipAPI, includePD: Boolean): Float {
        return ship.allWeapons
            .filter { it.type != WeaponAPI.WeaponType.MISSILE }
            .filter { !(includePD || it.hasAIHint(WeaponAPI.AIHints.PD)) }
            .map { it.range }
            .ifEmpty { mutableListOf(0f) }
            .sum ()
    }

    fun getMaxWeaponRange(ship: ShipAPI, includePD: Boolean): Float {
        return ship.allWeapons
            .filter { it.type != WeaponAPI.WeaponType.MISSILE }
            .filter { !(includePD || it.hasAIHint(WeaponAPI.AIHints.PD)) }
            .ifEmpty { mutableListOf() }
            .maxOfOrNull { it.range } ?: 0f
    }
}