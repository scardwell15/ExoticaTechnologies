package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import data.scripts.util.MagicUI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import exoticatechnologies.util.states.StateWithNext
import org.json.JSONObject
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.abs

class SpooledFeeders(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color(0xD93636)
    override fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI): Boolean {
        return Utilities.hasItem(fleet.cargo, ITEM)
    }

    override fun removeItemsFromFleet(fleet: CampaignFleetAPI, member: FleetMemberAPI, market: MarketAPI): Boolean {
        Utilities.takeItemQuantity(fleet.cargo, ITEM, 1f)
        return true
    }

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
                .format("firerateBoost", RATE_OF_FIRE_BUFF)
                .formatFloat("boostTime", BUFF_DURATION * getPositiveMult(member, mods, exoticData))
                .format("firerateMalus", abs(RATE_OF_FIRE_DEBUFF))
                .formatFloat("malusTime", DEBUFF_DURATION * getNegativeMult(member, mods, exoticData))
                .formatFloat("cooldownTime", COOLDOWN)
                .addToTooltip(tooltip, title)
        }
    }

    private fun shouldSpoolAI(weapon: WeaponAPI): Boolean {
        return if (weapon.slot.weaponType == WeaponAPI.WeaponType.MISSILE) false
        else weapon.hasAIHint(WeaponAPI.AIHints.PD) || weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY)
    }

    private fun canSpool(ship: ShipAPI): Boolean {
        return ship.shipAI != null || Mouse.isButtonDown(0)
    }

    override fun advanceInCombatAlways(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) {
        val state = getSpoolState(ship, member, mods, exoticData)
        state.advanceAlways(ship)
    }

    override fun advanceInCombatUnpaused(
        ship: ShipAPI,
        amount: Float,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        val state = getSpoolState(ship, member, mods, exoticData)
        state.advance(ship, amount)
    }

    private fun getSpoolState(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): SpoolState {
        var state = ship.customData[STATE_KEY] as SpoolState?
        if (state == null) {
            state = ReadyState(member, mods, exoticData)
            ship.setCustomData(STATE_KEY, state)
        }
        return state
    }

    private val statusBarText: String
        get() = StringUtils.getString(key, "statusBarText")

    private abstract inner class SpoolState(
        val member: FleetMemberAPI,
        val mods: ShipModifications,
        val exoticData: ExoticData
    ) : StateWithNext(STATE_KEY) {
        abstract fun advanceAlways(ship: ShipAPI?)
    }

    private inner class ReadyState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : SpoolState(member, mods, exoticData) {
        override fun advanceShip(ship: ShipAPI, amount: Float) {
            if (canSpool(ship)) {
                for (weapon in ship.allWeapons) {
                    if (weapon.isFiring && (ship.shipAI == null || shouldSpoolAI(weapon))) {
                        setNextState(ship)
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
                statusBarText,
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

    private inner class BuffedState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : SpoolState(member, mods, exoticData) {
        override fun initShip(ship: ShipAPI) {
            ship.addAfterimage(Color(255, 0, 0, 150), 0f, 0f, 0f, 0f, 0f, 0.1f, 4.6f, 0.25f, true, true, true)
            ship.mutableStats.ballisticRoFMult.modifyMult(buffId, 1 + RATE_OF_FIRE_BUFF / 100f)
            ship.mutableStats.energyRoFMult.modifyMult(buffId, 1 + RATE_OF_FIRE_BUFF / 100f)
            for (buffWeapon in ship.allWeapons) {
                if (buffWeapon.cooldownRemaining > buffWeapon.cooldown / 2f) {
                    buffWeapon.setRemainingCooldownTo(buffWeapon.cooldown / 2f)
                }
            }
        }

        override fun advanceShip(ship: ShipAPI, amount: Float) {}
        override fun advanceAlways(ship: ShipAPI?) {
            MagicUI.drawInterfaceStatusBar(
                ship,
                1f - getProgressRatio(),
                RenderUtils.getAliveUIColor(),
                RenderUtils.getAliveUIColor(),
                0f,
                statusBarText,
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
            return DebuffState(member, mods, exoticData)
        }
    }

    private inner class DebuffState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : SpoolState(member, mods, exoticData) {
        override fun initShip(ship: ShipAPI) {
            ship.mutableStats.ballisticRoFMult.modifyMult(buffId, 1 + RATE_OF_FIRE_DEBUFF / 100f)
            ship.mutableStats.energyRoFMult.modifyMult(buffId, 1 + RATE_OF_FIRE_DEBUFF / 100f)
        }

        override fun advanceShip(ship: ShipAPI, amount: Float) {}
        override fun advanceAlways(ship: ShipAPI?) {
            val ratio = getProgressRatio()
            val fill = interval.elapsed / (interval.intervalDuration + COOLDOWN)
            val progressBarColor =
                RenderUtils.mergeColors(RenderUtils.getDeadUIColor(), RenderUtils.getEnemyUIColor(), ratio)
            MagicUI.drawInterfaceStatusBar(
                ship,
                fill,
                progressBarColor,
                RenderUtils.getAliveUIColor(),
                0f,
                statusBarText,
                -1
            )
        }

        override fun intervalExpired(ship: ShipAPI): Boolean {
            setNextState(ship)
            ship.mutableStats.ballisticRoFMult.unmodifyMult(buffId)
            ship.mutableStats.energyRoFMult.unmodifyMult(buffId)
            return true
        }

        override fun getDuration(): Float {
            return DEBUFF_DURATION * getNegativeMult(member, mods, exoticData)
        }

        override fun getNextState(): StateWithNext {
            return CooldownState(member, mods, exoticData)
        }
    }

    private inner class CooldownState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : SpoolState(member, mods, exoticData) {
        override fun initShip(ship: ShipAPI) {
            ship.mutableStats.ballisticRoFMult.unmodifyMult(buffId)
            ship.mutableStats.energyRoFMult.unmodifyMult(buffId)
        }

        override fun advanceShip(ship: ShipAPI, amount: Float) {}
        override fun advanceAlways(ship: ShipAPI?) {
            val ratio = getProgressRatio()
            val fill = (interval.elapsed + DEBUFF_DURATION * getNegativeMult(member, mods, exoticData)) / (interval.intervalDuration + DEBUFF_DURATION * getNegativeMult(member, mods, exoticData))

            val progressBarColor =
                RenderUtils.mergeColors(RenderUtils.getEnemyUIColor(), RenderUtils.getAliveUIColor(), ratio)
            MagicUI.drawInterfaceStatusBar(
                ship,
                fill,
                progressBarColor,
                RenderUtils.getAliveUIColor(),
                0f,
                statusBarText,
                -1
            )
        }

        override fun intervalExpired(ship: ShipAPI): Boolean {
            setNextState(ship)
            ship.mutableStats.ballisticRoFMult.unmodifyMult(buffId)
            ship.mutableStats.energyRoFMult.unmodifyMult(buffId)
            return true
        }

        override fun getDuration(): Float {
            return COOLDOWN.toFloat()
        }

        override fun getNextState(): StateWithNext {
            return ReadyState(member, mods, exoticData)
        }
    }

    companion object {
        private const val ITEM = "et_ammospool"
        private const val RATE_OF_FIRE_BUFF = 100f
        private const val RATE_OF_FIRE_DEBUFF = -33f
        private const val COOLDOWN = 12
        private const val BUFF_DURATION = 5
        private const val DEBUFF_DURATION = 4
        private const val STATE_KEY = "et_spool_state"
    }
}