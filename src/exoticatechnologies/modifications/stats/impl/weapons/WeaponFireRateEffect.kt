package exoticatechnologies.modifications.stats.impl.weapons

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect
import exoticatechnologies.modifications.upgrades.Upgrade

class WeaponFireRateEffect : UpgradeMutableStatEffect() {
    override var hullmodShowsFinalValue: Boolean = false

    override val key: String
        get() = "weaponFireRate"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.ballisticRoFMult
    }

    override fun applyToStats(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ) {
        applyEffectToStat(stats.ballisticRoFMult, member, mods, mod)
        applyEffectToStat(stats.energyRoFMult, member, mods, mod)
        applyEffectToStat(stats.missileRoFMult, member, mods, mod)
    }
}