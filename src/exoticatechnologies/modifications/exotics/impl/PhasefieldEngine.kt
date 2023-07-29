package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.util.MagicUI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import exoticatechnologies.util.states.StateWithNext
import org.json.JSONObject
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.abs

class PhasefieldEngine(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color(0xA94EFF)
    override fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI?): Boolean {
        return Utilities.hasItem(fleet.cargo, ITEM)
    }

    override fun removeItemsFromFleet(fleet: CampaignFleetAPI, member: FleetMemberAPI, market: MarketAPI?): Boolean {
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
                .format("phaseCostReduction", getPhaseCostReduction(member, mods, exoticData))
                .formatFloat("phaseResetTime", PHASE_RESET_INTERVAL * getNegativeMult(member, mods, exoticData))
                .formatFloat("noDamageTime", INVULNERABLE_INTERVAL)
                .format("zeroFluxCost", Misc.getRounded(PHASE_COST_IF_ZERO))
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
        if (member.hullSpec.shieldSpec.phaseCost == 0f) {
            stats.phaseCloakActivationCostBonus.modifyFlat(buffId + "base", PHASE_COST_IF_ZERO / 100f)
        } else if (member.hullSpec.shieldSpec.phaseCost < 0) {
            stats.phaseCloakActivationCostBonus.modifyMult(buffId + "base", -1f)
        }
        stats.phaseCloakActivationCostBonus.modifyMult(buffId, getPhaseCostReduction(member, mods, exoticData))
    }

    private fun getPhaseCostReduction(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return 1f - abs(PHASE_COST_PERCENT_REDUCTION / 100f * getPositiveMult(member, mods, exoticData)).coerceAtMost(0.9f)
    }

    override fun applyToShip(
        id: String,
        member: FleetMemberAPI,
        ship: ShipAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        ship.mutableStats.phaseCloakActivationCostBonus.unmodify("phase_anchor")
    }

    private fun getInvulverableId(ship: ShipAPI): String {
        return String.format("%s_%s_invulnerable", buffId, ship.id)
    }

    private fun getTimesPhasedId(ship: ShipAPI): String {
        return String.format("%s_%s_timesphased", buffId, ship.id)
    }

    private fun getTimesPhasedInInterval(ship: ShipAPI): Int {
        return (Global.getCombatEngine().customData[getTimesPhasedId(ship)] ?: 0) as Int
    }

    private fun addToTimesPhased(ship: ShipAPI) {
        Global.getCombatEngine().customData[getTimesPhasedId(ship)] = getTimesPhasedInInterval(ship) + 1
    }

    private fun removeTimesPhased(ship: ShipAPI) {
        Global.getCombatEngine().customData.remove(getTimesPhasedId(ship))
    }

    private fun getState(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): FieldState {
        var state = ship.customData[STATE_KEY] as FieldState?
        if (state == null) {
            state = ReadyState(member, mods, exoticData)
            ship.setCustomData(STATE_KEY, state)
        }
        return state
    }

    override fun advanceInCombatAlways(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) {
        if (ship.phaseCloak == null) {
            return
        }
        val state = getState(ship, member, mods, exoticData)
        state.advanceAlways(ship)
    }

    override fun advanceInCombatUnpaused(
        ship: ShipAPI,
        amount: Float,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        if (ship.phaseCloak == null) {
            return
        }
        val state = getState(ship, member, mods, exoticData)
        state.advance(ship, amount)
    }

    // damage listener
    private inner class PhasefieldEngineListener(private val ship: ShipAPI) : DamageTakenModifier {
        override fun modifyDamageTaken(
            param: Any?,
            target: CombatEntityAPI,
            damageAPI: DamageAPI,
            point: Vector2f,
            shieldHit: Boolean
        ): String? {
            if (target === ship) {
                damageAPI.modifier.modifyMult(
                    buffId,
                    0.66f
                )
                return buffId
            }
            return null
        }
    }

    private val statusBarText: String
        get() = StringUtils.getString(key, "statusBarText")

    private abstract inner class FieldState(
        val member: FleetMemberAPI,
        val mods: ShipModifications,
        val exoticData: ExoticData
    ) : StateWithNext(STATE_KEY) {
        abstract fun advanceAlways(ship: ShipAPI)
    }

    private open inner class ReadyState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : FieldState(
        member,
        mods,
        exoticData
    ) {
        override fun advanceShip(ship: ShipAPI, amount: Float) {
            //if phased, set state to phased, set state
            if (ship.phaseCloak.state == ShipSystemAPI.SystemState.IN
                || ship.phaseCloak.state == ShipSystemAPI.SystemState.ACTIVE
            ) {
                setNextState(ship)
            }
        }

        override fun advanceAlways(ship: ShipAPI) {
            MagicUI.drawInterfaceStatusBar(
                ship,
                1f,
                RenderUtils.getAliveUIColor(),
                RenderUtils.getAliveUIColor(),
                0f,
                statusBarText,
                getTimesPhasedInInterval(ship)
            )
        }

        override fun getDuration(): Float {
            return 0f
        }

        override fun getNextState(): StateWithNext {
            return PhasedState(member, mods, exoticData)
        }
    }

    private inner class PhasedState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : FieldState(member, mods, exoticData) {
        override fun initShip(ship: ShipAPI) {
            if (ship.hasListenerOfClass(PhasefieldEngineListener::class.java)) {
                ship.removeListenerOfClass(PhasefieldEngineListener::class.java)
            }
            addToTimesPhased(ship)
            ship.mutableStats.phaseCloakActivationCostBonus.modifyMult(
                buffId,
                Math.pow(2.0, getTimesPhasedInInterval(ship).toDouble()).toFloat()
            )
        }

        override fun advanceShip(ship: ShipAPI, amount: Float) {
            //if unphased, set state to invuln
            if (ship.phaseCloak.state == ShipSystemAPI.SystemState.OUT || ship.phaseCloak.state == ShipSystemAPI.SystemState.COOLDOWN || ship.phaseCloak.state == ShipSystemAPI.SystemState.IDLE) {
                setNextState(ship)
            }
        }

        override fun advanceAlways(ship: ShipAPI) {
            MagicUI.drawInterfaceStatusBar(
                ship,
                1f,
                PHASED_STATE_COLOR,
                PHASED_STATE_COLOR,
                1f,
                statusBarText,
                getTimesPhasedInInterval(ship)
            )
        }

        override fun getDuration(): Float {
            return 0f
        }

        override fun getNextState(): StateWithNext {
            return BuffedState(member, mods, exoticData)
        }
    }

    private inner class BuffedState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : FieldState(member, mods, exoticData) {
        override fun initShip(ship: ShipAPI) {
            if (!ship.hasListenerOfClass(PhasefieldEngineListener::class.java)) {
                val listener = PhasefieldEngineListener(ship)
                ship.addListener(listener)
            }
        }

        override fun advanceShip(ship: ShipAPI, amount: Float) {}
        override fun advanceAlways(ship: ShipAPI) {
            MagicUI.drawInterfaceStatusBar(
                ship,
                1f - getProgressRatio(),
                RenderUtils.getAliveUIColor(),
                RenderUtils.getAliveUIColor(),
                1f,
                statusBarText,
                getTimesPhasedInInterval(ship)
            )
        }

        override fun getDuration(): Float {
            return INVULNERABLE_INTERVAL.toFloat()
        }

        override fun intervalExpired(ship: ShipAPI): Boolean {
            setNextState(ship)
            return true
        }

        override fun getNextState(): StateWithNext {
            return CooldownState(member, mods, exoticData)
        }
    }

    private inner class CooldownState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : ReadyState(member, mods, exoticData) {
        private var endTime = 0f

        override fun initShip(ship: ShipAPI) {
            endTime = Global.getCombatEngine().getTotalElapsedTime(false) + ship.phaseCloak.cooldown
            if (ship.hasListenerOfClass(PhasefieldEngineListener::class.java)) {
                ship.removeListenerOfClass(PhasefieldEngineListener::class.java)
            }
        }

        override fun advanceAlways(ship: ShipAPI) {
            val resetRatio = getProgressRatio()
            val remaining = endTime - Global.getCombatEngine().getTotalElapsedTime(false)
            val cooldownRatio = MathUtils.clamp(remaining / ship.phaseCloak.cooldown, 0f, 1f)
            MagicUI.drawInterfaceStatusBar(
                ship,
                1f - cooldownRatio,
                RenderUtils.getAliveUIColor(),
                RenderUtils.getAliveUIColor(),
                1f - resetRatio,
                statusBarText,
                getTimesPhasedInInterval(ship)
            )
        }

        override fun intervalExpired(ship: ShipAPI): Boolean {
            removeTimesPhased(ship)
            ship.mutableStats.phaseCloakActivationCostBonus.unmodifyMult(buffId)
            setNextState(ship)
            return true
        }

        override fun getDuration(): Float {
            return PHASE_RESET_INTERVAL.toFloat() * getNegativeMult(member, mods, exoticData)
        }

        override fun getNextState(): StateWithNext {
            if (!interval.intervalElapsed()) {
                return super.getNextState()
            }
            return ReadyState(member, mods, exoticData)
        }
    }

    companion object {
        private const val ITEM = "et_phaseengine"
        private const val PHASE_RESET_INTERVAL = 6
        private const val INVULNERABLE_INTERVAL = 3
        private const val PHASE_COST_PERCENT_REDUCTION = -75f
        private const val PHASE_COST_IF_ZERO = 20f
        private const val STATE_KEY = "et_phasefieldengine_state"
        private val PHASED_STATE_COLOR = Color(170, 140, 220)
    }
}