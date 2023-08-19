package exoticatechnologies.modifications.stats.impl.weapons

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.combat.StatBonus
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect
import exoticatechnologies.modifications.stats.UpgradeStatBonusEffect
import exoticatechnologies.modifications.upgrades.Upgrade

class WeaponMagazinesEffect : UpgradeStatBonusEffect() {
    override var hullmodShowsFinalValue: Boolean = false

    override val key: String
        get() = "weaponMagazines"

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.ballisticAmmoBonus
    }

    override fun applyToStats(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ) {
        applyEffectToStat(stats.ballisticAmmoBonus, member, mods, mod)
        applyEffectToStat(stats.energyAmmoBonus, member, mods, mod)
    }
}