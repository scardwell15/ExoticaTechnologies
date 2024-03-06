package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.combat.ExoticaCombatUtils
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.StringUtils
import org.json.JSONObject
import org.magiclib.subsystems.MagicSubsystem
import org.magiclib.subsystems.MagicSubsystemsManager
import java.awt.Color
import kotlin.math.abs

class FullMetalSalvo(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color(0xD99836)

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
                .format("projSpeedBoost", DAMAGE_BUFF)
                .format("damageBoost", DAMAGE_BUFF * 0.33f)
                .formatFloat("boostTime", BUFF_DURATION * getPositiveMult(member, mods, exoticData))
                .formatFloat("cooldown", COOLDOWN * getNegativeMult(member, mods, exoticData))
                .format("firerateMalus", abs(RATE_OF_FIRE_DEBUFF))
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
        stats.ballisticRoFMult.modifyMult(buffId, 1 + RATE_OF_FIRE_DEBUFF / 100f)
        stats.energyRoFMult.modifyMult(buffId, 1 + RATE_OF_FIRE_DEBUFF / 100f)
    }

    fun gigaProjectiles(source: ShipAPI) {
        for (proj in Global.getCombatEngine().projectiles) {
            if (proj.source == source && proj.elapsed <= Global.getCombatEngine().elapsedInLastFrame) {
                proj.damage.modifier.modifyMult(buffId, 1 + DAMAGE_BUFF / 100f * 0.33f)
            }
        }
    }

    override fun applyToShip(
        id: String,
        member: FleetMemberAPI,
        ship: ShipAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        MagicSubsystemsManager.addSubsystemToShip(ship, SalvoActivator(ship, member, mods, exoticData))
    }

    inner class SalvoActivator(
        ship: ShipAPI,
        val member: FleetMemberAPI,
        val mods: ShipModifications,
        val exoticData: ExoticData
    ) :
        MagicSubsystem(ship) {
        override fun getDisplayText(): String {
            return Global.getSettings().getString(exoticData.key, "systemText")
        }

        override fun getBaseActiveDuration(): Float {
            return BUFF_DURATION * getPositiveMult(member, mods, exoticData)
        }

        override fun getBaseCooldownDuration(): Float {
            return COOLDOWN.toFloat()
        }

        override fun advance(amount: Float, isPaused: Boolean) {
            if (isPaused) return

            if (state == State.ACTIVE) {
                gigaProjectiles(ship)
            }
        }

        override fun onStateSwitched(oldState: State) {
            if (state == State.ACTIVE) {
                ship.mutableStats.ballisticProjectileSpeedMult.modifyMult(this.toString(), 1 + DAMAGE_BUFF / 100f)
                ship.mutableStats.energyProjectileSpeedMult.modifyMult(this.toString(), 1 + DAMAGE_BUFF / 100f)
                ship.mutableStats.missileMaxSpeedBonus.modifyMult(this.toString(), 1 + DAMAGE_BUFF / 100f)

                ship.addAfterimage(
                    Color(255, 125, 0, 150),
                    0f,
                    0f,
                    0f,
                    0f,
                    6f,
                    0f,
                    this.activeDuration,
                    0.25f,
                    true,
                    false,
                    true
                )
            } else {
                ship.mutableStats.ballisticProjectileSpeedMult.unmodify(this.toString())
                ship.mutableStats.energyProjectileSpeedMult.unmodify(this.toString())
                ship.mutableStats.missileMaxSpeedBonus.unmodify(this.toString())
            }
        }

        override fun shouldActivateAI(amount: Float): Boolean {
            val target = ship.shipTarget
            if (target != null) {
                var score = 0f
                score += (target.currFlux / target.maxFlux) * 12f

                if (target.fluxTracker.isOverloadedOrVenting) {
                    score += 10f
                }

                var dist = Misc.getDistance(ship.location, target.location)
                if (dist > ExoticaCombatUtils.getMaxWeaponRange(ship, false)) {
                    return false
                }

                var avgRange = ExoticaCombatUtils.getAverageWeaponRange(ship, false)
                score += (avgRange / dist).coerceAtMost(6f)

                if (score > 10f) {
                    return true
                }
            }
            return false
        }
    }

    companion object {
        private const val DAMAGE_BUFF = 100f
        private const val RATE_OF_FIRE_DEBUFF = -33f
        private const val COOLDOWN = 8
        private const val BUFF_DURATION = 2
    }
}