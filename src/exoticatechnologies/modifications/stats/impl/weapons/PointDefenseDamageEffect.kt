package exoticatechnologies.modifications.stats.impl.weapons

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier
import com.fs.starfarer.api.combat.listeners.DamageListener
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import com.fs.starfarer.api.combat.listeners.WeaponOPCostModifier
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.stats.UpgradeModEffect
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect
import exoticatechnologies.modifications.upgrades.Upgrade
import org.lwjgl.util.vector.Vector2f

class PointDefenseDamageEffect : UpgradeModEffect() {
    override var hullmodShowsFinalValue: Boolean = false

    override val key: String
        get() = "pointDefenseDamage"

    override fun getEffectiveValue(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): Float {
        return getCurrentEffect(member, mods, mod)
    }

    override fun applyToShip(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, mod: Upgrade) {
        ship.allWeapons
            .filter { it.hasAIHint(WeaponAPI.AIHints.PD) }
            .forEach {
                it.damage.multiplier *= mods.getUpgrade(mod) * getCurrentEffect(member, mods, mod)
             }
    }
}