package exoticatechnologies.modifications.stats.impl.weapons

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.stats.UpgradeStatBonusEffect
import exoticatechnologies.modifications.upgrades.Upgrade

class WeaponFluxCostEffect : UpgradeStatBonusEffect() {
    override var negativeIsBuff: Boolean = true

    override val key: String
        get() = "weaponFluxCost"

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.ballisticWeaponFluxCostMod
    }

    override fun applyToStats(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ) {
        applyEffectToStat(stats.ballisticWeaponFluxCostMod, member, mods, mod)
        applyEffectToStat(stats.energyWeaponFluxCostMod, member, mods, mod)
        applyEffectToStat(stats.missileWeaponFluxCostMod, member, mods, mod)
    }
}