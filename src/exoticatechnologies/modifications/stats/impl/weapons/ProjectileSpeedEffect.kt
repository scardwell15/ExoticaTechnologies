package exoticatechnologies.modifications.stats.impl.weapons

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class ProjectileSpeedEffect : UpgradeMutableStatEffect() {
    override val key: String
        get() = "projectileSpeed"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.projectileSpeedMult
    }
}