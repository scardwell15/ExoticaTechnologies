package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.IntervalUtil
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.StringUtils
import org.json.JSONObject
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class HyperspecLPC(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color: Color = Color(255, 230, 0)

    override fun modifyToolTip(
        tooltip: TooltipMakerAPI,
        title: UIComponentAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData,
        expand: Boolean
    ) {
        if (expand) {
            StringUtils.getTranslation(key, "longDescription")
                .formatFloat("fighterDamageIncrease", getDamageBonus(member, mods, exoticData))
                .formatFloat("fighterSpeedIncrease", getSpeedBonus(member, mods, exoticData))
                .formatFloat("fighterArmorIncrease", getArmorBonus(member, mods, exoticData))
                .formatFloat("fighterFluxIncrease", getFluxBonus(member, mods, exoticData))
                .formatFloat("fighterHullIncrease", getHullBonus(member, mods, exoticData))
                .formatFloat("replacementTimeIncrease", getReplacementTimeMalus(member, mods, exoticData))
                .addToTooltip(tooltip, title)
        }
    }

    override fun applyExoticToStats(
        id: String,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        val fighterBays = member.hullSpec.fighterBays
        stats.numFighterBays.modifyFlat(buffId, -(fighterBays - 1f))
        stats.fighterRefitTimeMult.modifyPercent(buffId, getReplacementTimeMalus(member, mods, exoticData))
    }

    override fun applyToShip(
        id: String,
        member: FleetMemberAPI,
        ship: ShipAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        ship.mutableStats.numFighterBays.modifyFlat(buffId, -ship.mutableStats.numFighterBays.modifiedValue)
    }

    fun getImageInterval(ship: ShipAPI): IntervalUtil {
        if (ship.customData.containsKey(buffId)) {
            return ship.customData[buffId] as IntervalUtil
        }

        val time = 0.4f
        val interval = IntervalUtil(time, time)
        ship.setCustomData(buffId, interval)
        return interval
    }

    override fun advanceInCombatUnpaused(
        ship: ShipAPI,
        amount: Float,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        val interval: IntervalUtil = getImageInterval(ship)
        interval.advance(amount)

        if (interval.intervalElapsed()) {
            for (wing in ship.allWings) {
                for (fighter in wing.wingMembers) {
                    val loc = fighter.location
                    val vel = Vector2f(fighter.velocity)
                    vel.scale(-(ship.velocity.length() / ship.maxSpeed) * 0.5f)
                    fighter.addAfterimage(Color(255, 0, 0), 0f, 0f, vel.x, vel.y, 1f, 0.2f, 0f, 0.2f, true, true, false)
                }
            }
        }
    }

    override fun applyToFighters(member: FleetMemberAPI, ship: ShipAPI, fighter: ShipAPI, mods: ShipModifications) {
        val exoticData = mods.getExoticData(this)!!

        fighter.mutableStats.ballisticWeaponDamageMult.modifyPercent(buffId, getDamageBonus(member, mods, exoticData))
        fighter.mutableStats.energyWeaponDamageMult.modifyPercent(buffId, getDamageBonus(member, mods, exoticData))

        fighter.mutableStats.maxSpeed.modifyPercent(buffId, getSpeedBonus(member, mods, exoticData))
        fighter.mutableStats.acceleration.modifyPercent(buffId, getSpeedBonus(member, mods, exoticData))
        fighter.mutableStats.deceleration.modifyPercent(buffId, getSpeedBonus(member, mods, exoticData))
        fighter.mutableStats.turnAcceleration.modifyPercent(buffId, getSpeedBonus(member, mods, exoticData))

        fighter.mutableStats.hullBonus.modifyPercent(buffId, getHullBonus(member, mods, exoticData))
        fighter.mutableStats.armorBonus.modifyPercent(buffId, getArmorBonus(member, mods, exoticData))
        fighter.mutableStats.fluxCapacity.modifyPercent(buffId, getFluxBonus(member, mods, exoticData))
        fighter.mutableStats.fluxDissipation.modifyPercent(buffId, getFluxBonus(member, mods, exoticData))
    }

    fun getBaysRemoved(member: FleetMemberAPI): Int {
        return (member.hullSpec.fighterBays - 1)
    }

    fun getDamageBonus(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return (FIGHTER_DAMAGE_PER_BAY * getBaysRemoved(member)).coerceAtMost(FIGHTER_DAMAGE_MAX)
    }

    fun getSpeedBonus(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return (FIGHTER_SPEED_PER_BAY * getBaysRemoved(member)).coerceAtMost(FIGHTER_SPEED_MAX) * exoticData.type.getPositiveMult(member, mods)
    }

    fun getArmorBonus(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return (FIGHTER_ARMOR_PER_BAY * getBaysRemoved(member)).coerceAtMost(FIGHTER_ARMOR_MAX)
    }

    fun getFluxBonus(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return (FIGHTER_FLUX_PER_BAY * getBaysRemoved(member)).coerceAtMost(FIGHTER_FLUX_MAX) * exoticData.type.getPositiveMult(member, mods)
    }

    fun getReplacementTimeMalus(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return (REPLACEMENT_TIME_PER_BAY * getBaysRemoved(member)).coerceAtMost(REPLACEMENT_TIME_MAX) * exoticData.type.getNegativeMult(member, mods)
    }

    fun getHullBonus(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return (FIGHTER_HULL_PER_BAY * getBaysRemoved(member)).coerceAtMost(FIGHTER_HULL_MAX) * exoticData.type.getPositiveMult(member, mods)
    }

    companion object {
        private const val FIGHTER_DAMAGE_MAX = 200f
        private const val FIGHTER_ARMOR_MAX = 250f
        private const val FIGHTER_FLUX_MAX = 250f
        private const val FIGHTER_SPEED_MAX = 100f
        private const val FIGHTER_HULL_MAX = 300f
        private const val REPLACEMENT_TIME_MAX = 60f

        private const val FIGHTER_DAMAGE_PER_BAY = 60f

        private const val FIGHTER_ARMOR_PER_BAY = 80f

        private const val FIGHTER_FLUX_PER_BAY = 80f

        private const val FIGHTER_SPEED_PER_BAY = 50f

        private const val FIGHTER_HULL_PER_BAY = 100f

        private const val REPLACEMENT_TIME_PER_BAY = 10f
    }
}