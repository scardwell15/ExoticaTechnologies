package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.combat.entities.Missile
import data.scripts.util.MagicUI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.reflect.ReflectionUtil
import exoticatechnologies.util.states.StateWithNext
import org.json.JSONObject
import org.lwjgl.input.Mouse
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

    override fun advanceInCombatAlways(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) {
        val state = getSalvoState(ship, member, mods, exoticData)
        state.advanceAlways(ship)
    }

    override fun advanceInCombatUnpaused(
        ship: ShipAPI,
        amount: Float,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        val state = getSalvoState(ship, member, mods, exoticData)
        if (Global.getCombatEngine().isPaused) {
            return
        }
        state.advance(ship, amount)
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

    private fun getSalvoState(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): SalvoState {
        var state = ship.customData[STATE_KEY] as SalvoState?
        if (state == null) {
            state = ReadyState(member, mods, exoticData)
            ship.setCustomData(STATE_KEY, state)
        }
        return state
    }

    private val salvoStatusBarText: String
        get() = StringUtils.getString(key, "statusBarText")

    private abstract inner class SalvoState(
        val member: FleetMemberAPI,
        val mods: ShipModifications,
        val exoticData: ExoticData
    ) : StateWithNext(STATE_KEY) {
        abstract fun advanceAlways(ship: ShipAPI?)
    }

    private inner class ReadyState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : SalvoState(member, mods, exoticData) {
        override fun advanceShip(ship: ShipAPI, amount: Float) {
            if (canSpool(ship)) {
                for (weapon in ship.allWeapons) {
                    if (weapon.isFiring && (ship.shipAI == null || shouldSpoolAI(weapon))) {
                        setNextState(ship)
                        ship.addAfterimage(
                            Color(255, 0, 0, 150),
                            0f,
                            0f,
                            0f,
                            0f,
                            0f,
                            0.1f,
                            1.75f,
                            0.25f,
                            true,
                            true,
                            true
                        )
                        break
                    }
                }
            }
        }

        override fun advanceAlways(ship: ShipAPI?) {
            MagicUI.drawInterfaceStatusBar(
                ship,
                1f,
                RenderUtils.getAliveUIColor(),
                RenderUtils.getAliveUIColor(),
                0f,
                salvoStatusBarText,
                -1
            )
        }

        override fun getDuration(): Float {
            return 0f
        }

        override fun getNextState(): StateWithNext {
            return BuffedState(member, mods, exoticData)
        }
    }

    private inner class BuffedState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : SalvoState(member, mods, exoticData) {
        override fun advanceShip(ship: ShipAPI, amount: Float) {
            gigaProjectiles(ship)
        }

        override fun advanceAlways(ship: ShipAPI?) {
            MagicUI.drawInterfaceStatusBar(
                ship,
                1f - getProgressRatio(),
                RenderUtils.getAliveUIColor(),
                RenderUtils.getAliveUIColor(),
                0f,
                salvoStatusBarText,
                -1
            )
        }

        override fun getDuration(): Float {
            return BUFF_DURATION * getPositiveMult(member, mods, exoticData)
        }

        override fun intervalExpired(ship: ShipAPI): Boolean {
            setNextState(ship)
            return true
        }

        override fun getNextState(): StateWithNext {
            return CooldownState(member, mods, exoticData)
        }
    }

    private inner class CooldownState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : SalvoState(member, mods, exoticData) {
        override fun advanceShip(ship: ShipAPI, amount: Float) {}
        override fun advanceAlways(ship: ShipAPI?) {
            val ratio = getProgressRatio()
            val progressBarColor =
                RenderUtils.mergeColors(RenderUtils.getEnemyUIColor(), RenderUtils.getAliveUIColor(), ratio)
            MagicUI.drawInterfaceStatusBar(
                ship,
                ratio,
                progressBarColor,
                RenderUtils.getAliveUIColor(),
                0f,
                salvoStatusBarText,
                -1
            )
        }

        override fun intervalExpired(ship: ShipAPI): Boolean {
            setNextState(ship)
            return true
        }

        override fun getDuration(): Float {
            return COOLDOWN * getNegativeMult(member, mods, exoticData)
        }

        override fun getNextState(): StateWithNext {
            return ReadyState(member, mods, exoticData)
        }
    }

    companion object {
        private const val DAMAGE_BUFF = 100f
        private const val RATE_OF_FIRE_DEBUFF = -33f
        private const val COOLDOWN = 8
        private const val BUFF_DURATION = 2
        private fun shouldSpoolAI(weapon: WeaponAPI): Boolean {
            return if (weapon.slot.weaponType == WeaponAPI.WeaponType.MISSILE) false
                    else !weapon.hasAIHint(WeaponAPI.AIHints.PD) && !weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY)
        }

        private fun canSpool(ship: ShipAPI): Boolean {
            return ship.shipAI != null || Mouse.isButtonDown(0)
        }

        private const val STATE_KEY = "et_salvo_state"
    }
}