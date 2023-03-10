package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
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
import org.lazywizard.lazylib.VectorUtils
import java.awt.Color
import kotlin.math.abs

class DriveFluxVent(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color(0x9D62C4)
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
                .format("ventBonus", VENT_SPEED_INCREASE * getPositiveMult(member, mods, exoticData))
                .format("speedThreshold", FLUX_LEVEL_REQUIRED)
                .format("speedBonus", FORWARD_SPEED_INCREASE * getPositiveMult(member, mods, exoticData))
                .format("speedBonusTime", SPEED_BUFF_TIME)
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
        stats.ventRateMult.modifyPercent(buffId, VENT_SPEED_INCREASE * getPositiveMult(member, mods, exoticData))
    }

    override fun advanceInCombatAlways(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) {
        val state = getVentState(ship, member, mods, exoticData)
        state.advanceAlways(ship)
    }

    override fun advanceInCombatUnpaused(
        ship: ShipAPI,
        amount: Float,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        val state = getVentState(ship, member, mods, exoticData)
        state.advance(ship, amount)
    }

    private fun getVentState(ship: ShipAPI?, member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): VentState {
        var state = ship!!.customData[STATE_KEY] as VentState?
        if (state == null) {
            state = ReadyState(member, mods, exoticData)
            ship.setCustomData(STATE_KEY, state)
        }
        return state
    }

    private val statusBarText: String
        get() = StringUtils.getString(key, "statusBarText")

    private abstract inner class VentState(
        val member: FleetMemberAPI,
        val mods: ShipModifications,
        val exoticData: ExoticData
    ) : StateWithNext(STATE_KEY) {
        abstract fun advanceAlways(ship: ShipAPI?)
    }

    private inner class ReadyState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : VentState(member, mods, exoticData) {
        override fun initShip(ship: ShipAPI) {
            ship.mutableStats.acceleration.unmodify(buffId)
            ship.mutableStats.deceleration.unmodify(buffId)
            ship.mutableStats.maxSpeed.unmodify(buffId)
        }

        override fun advanceShip(ship: ShipAPI, amount: Float) {
            if (ship.fluxTracker.isVenting) {
                if (ship.currFlux > ship.maxFlux * FLUX_LEVEL_REQUIRED / 100f) {
                    setNextState(ship)
                }
            }
        }

        private fun isReady(ship: ShipAPI?): Boolean {
            return ship!!.currFlux > ship.maxFlux * FLUX_LEVEL_REQUIRED / 100f
        }

        override fun advanceAlways(ship: ShipAPI?) {
            var renderColor = RenderUtils.getAliveUIColor()
            if (isReady(ship)) {
                renderColor = READY_STATE_COLOR
            }
            MagicUI.drawInterfaceStatusBar(ship, 1f, renderColor, renderColor, 0f, statusBarText, -1)
        }

        override fun getDuration(): Float {
            return 0f
        }

        override fun getNextState(): StateWithNext {
            return BuffedState(member, mods, exoticData)
        }
    }

    private inner class BuffedState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : VentState(member, mods, exoticData) {
        override fun initShip(ship: ShipAPI) {
            ship.engineController.fadeToOtherColor(buffId, Color(255, 75, 255), null, 1f, 0.75f)
        }

        override fun advanceShip(ship: ShipAPI, amount: Float) {
            if (ship.fluxTracker.isVenting) {
                interval.elapsed = 0f
            }
            ship.engineController.fadeToOtherColor(buffId, Color(255, 75, 255), null, getProgressRatio(), 0.75f)
            val velocityDir = VectorUtils.getFacing(ship.velocity) - ship.facing
            if (abs(velocityDir) < 15f) {
                ship.mutableStats.acceleration.modifyPercent(buffId, 50f)
                ship.mutableStats.deceleration.modifyPercent(buffId, -50f)
                ship.mutableStats.maxSpeed.modifyPercent(buffId, FORWARD_SPEED_INCREASE * getPositiveMult(member, mods, exoticData))
            } else {
                ship.mutableStats.acceleration.unmodify(buffId)
                ship.mutableStats.deceleration.unmodify(buffId)
                ship.mutableStats.maxSpeed.unmodify(buffId)
            }
        }

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
            return SPEED_BUFF_TIME
        }

        override fun intervalExpired(ship: ShipAPI): Boolean {
            setNextState(ship)
            return true
        }

        override fun getNextState(): StateWithNext {
            return ReadyState(member, mods, exoticData)
        }
    }

    companion object {
        private const val ITEM = "et_drivevent"
        private const val VENT_SPEED_INCREASE = 30f
        private const val FORWARD_SPEED_INCREASE = 50
        private const val FLUX_LEVEL_REQUIRED = 40f
        private const val SPEED_BUFF_TIME = 4f
        private const val STATE_KEY = "et_drivefluxvent_state"
        private val READY_STATE_COLOR = Color(170, 140, 220)
    }
}