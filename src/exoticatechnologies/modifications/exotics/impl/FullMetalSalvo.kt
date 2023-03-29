package exoticatechnologies.modifications.exotics.impl

import activators.ActivatorManager
import activators.CombatActivator
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.entities.Missile
import exoticatechnologies.combat.ExoticaCombatUtils
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.reflect.ReflectionUtil
import org.json.JSONObject
import java.awt.Color
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
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
                .format("damageBoost", DAMAGE_BUFF)
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
                if (proj is Missile) {
                    try {
                        val missileSpeed =
                            ReflectionUtil.getObjectFieldWrapper(proj, "maxSpeed", Float::class.javaPrimitiveType)
                        missileSpeed.value = missileSpeed.value * (1 + DAMAGE_BUFF / 100f)
                        val engineStatsField =
                            ReflectionUtil.getObjectFieldWrapper(proj, "engineStats", Any::class.java)
                        val engineStats = engineStatsField.value
                        val speedStat = MethodHandles.lookup().findVirtual(
                            engineStats.javaClass, "getMaxSpeed", MethodType.methodType(
                                MutableStat::class.java
                            )
                        ).invoke(engineStats) as MutableStat
                        speedStat.modifyMult(buffId, 1 + DAMAGE_BUFF / 100f)
                        val accelStat = MethodHandles.lookup().findVirtual(
                            engineStats.javaClass, "getAcceleration", MethodType.methodType(
                                MutableStat::class.java
                            )
                        ).invoke(engineStats) as MutableStat
                        accelStat.modifyMult(buffId, 1 + DAMAGE_BUFF / 100f)
                    } catch (ex: Throwable) {
                        throw RuntimeException(ex)
                    }
                } else {
                    proj.damage.modifier.modifyMult(buffId, 1 + DAMAGE_BUFF / 100f)
                    proj.velocity.scale(1 + DAMAGE_BUFF / 100f)
                }
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
        val activator = SalvoActivator(member, mods, exoticData)
        ActivatorManager.addActivator(ship, activator)
    }

    inner class SalvoActivator(val member: FleetMemberAPI, val mods: ShipModifications, val exoticData: ExoticData) :
        CombatActivator() {
        override fun getDisplayText(): String {
            return Global.getSettings().getString(this@FullMetalSalvo.key, "systemText")
        }

        override fun getActiveDuration(): Float {
            return BUFF_DURATION * getPositiveMult(member, mods, exoticData)
        }

        override fun getCooldownDuration(): Float {
            return COOLDOWN.toFloat()
        }

        override fun advance(ship: ShipAPI, state: State, amount: Float) {
            if (state == State.ACTIVE) {
                gigaProjectiles(ship)
            }
        }

        override fun onStateSwitched(ship: ShipAPI, state: State) {
            if (state == State.ACTIVE) {
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
            }
        }

        override fun shouldActivateAI(ship: ShipAPI): Boolean {
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