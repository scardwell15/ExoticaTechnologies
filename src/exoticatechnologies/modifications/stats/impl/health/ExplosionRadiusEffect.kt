package exoticatechnologies.modifications.stats.impl.health

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.impl.campaign.ids.Stats
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class ExplosionRadiusEffect : UpgradeMutableStatEffect() {
    override var hullmodShowsFinalValue: Boolean = false
    override val key: String
        get() = "explosionRadius"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.dynamic.getStat(Stats.EXPLOSION_DAMAGE_MULT)
    }
}