package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
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
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class ReactiveDamperField(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color(200, 60, 20)
    override fun shouldShow(member: FleetMemberAPI, mods: ShipModifications, market: MarketAPI?): Boolean {
        return (Utilities.hasExoticChip(Global.getSector().playerFleet.cargo, key)
                || Utilities.hasExoticChip(Misc.getStorageCargo(market), key))
    }

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
                .formatFloat("damperDuration", DAMPER_DURATION * getPositiveMult(member, mods, exoticData))
                .format("damperReduction", DAMPER_REDUCTION)
                .format("triggeringDamage", TRIGGERING_DAMAGE)
                .formatFloat("damperCooldown", DAMPER_COOLDOWN * getNegativeMult(member, mods, exoticData))
                .format("armorDamageTaken", PASSIVE_DAMAGE_TAKEN)
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
        stats.armorDamageTakenMult.modifyPercent(buffId, PASSIVE_DAMAGE_TAKEN)
    }

    override fun advanceInCombatAlways(
        ship: ShipAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        val state = getDamperState(ship, member, mods, exoticData)
        state.advanceAlways(ship)
    }

    override fun advanceInCombatUnpaused(
        ship: ShipAPI,
        amount: Float,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        val state = getDamperState(ship, member, mods, exoticData)
        state.advance(ship, amount)
    }

    private fun getDamperState(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): DamperState {
        var state = ship.customData[DAMPER_STATE_ID] as DamperState?
        if (state == null) {
            state = ReadyState(member, mods, exoticData)
            ship.setCustomData(DAMPER_STATE_ID, state)
        }
        return state
    }

    private val statusBarText: String
        get() = StringUtils.getString(key, "statusBarText")

    private abstract inner class DamperState(
        val member: FleetMemberAPI,
        val mods: ShipModifications,
        val exoticData: ExoticData
    ) : StateWithNext(DAMPER_STATE_ID) {
        open fun advanceAlways(ship: ShipAPI?) {}
    }

    private inner class ReadyState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : DamperState(member, mods, exoticData) {
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

        override fun advanceShip(ship: ShipAPI, amount: Float) {
            if (!ship.hasListenerOfClass(ET_ReactiveDamperFieldListener::class.java)) {
                ship.addListener(ET_ReactiveDamperFieldListener(ship, member, mods, exoticData))
            }
        }

        override fun intervalExpired(ship: ShipAPI): Boolean {
            ship.addAfterimage(Color(255, 0, 0, 120), 0f, 0f, 0f, 0f, 15f, 0f, 0.1f, 0.75f, true, false, false)
            return false
        }

        override fun getDuration(): Float {
            return 2f
        }

        override fun getNextState(): DamperState {
            return ActiveState(member, mods, exoticData)
        }
    }

    private inner class ActiveState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : DamperState(member, mods, exoticData) {
        override fun initShip(ship: ShipAPI) {
            ship.addAfterimage(Color(255, 0, 0, 120), 0f, 0f, 0f, 0f, 0f, 0f, 0.75f, 0.33f, true, true, true)
            ship.addAfterimage(Color(255, 0, 0, 200), 0f, 0f, 0f, 0f, 15f, 0f, 0.75f, 0.33f, true, false, false)
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

        override fun advanceShip(ship: ShipAPI, amount: Float) {}
        override fun intervalExpired(ship: ShipAPI): Boolean {
            setNextState(ship)
            return true
        }

        override fun getDuration(): Float {
            return DAMPER_DURATION * getPositiveMult(member, mods, exoticData)
        }

        override fun getNextState(): DamperState {
            return CooldownState(member, mods, exoticData)
        }
    }

    private inner class CooldownState(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) : DamperState(member, mods, exoticData) {
        override fun advanceAlways(ship: ShipAPI?) {
            MagicUI.drawInterfaceStatusBar(
                ship,
                getProgressRatio(),
                RenderUtils.getAliveUIColor(),
                RenderUtils.getAliveUIColor(),
                0f,
                statusBarText,
                -1
            )
        }

        override fun advanceShip(ship: ShipAPI, amount: Float) {}
        override fun intervalExpired(ship: ShipAPI): Boolean {
            setNextState(ship)
            return true
        }

        override fun getDuration(): Float {
            return DAMPER_COOLDOWN * getNegativeMult(member, mods, exoticData)
        }

        override fun getNextState(): DamperState {
            return ReadyState(member, mods, exoticData)
        }
    }

    private inner class ET_ReactiveDamperFieldListener(
        val ship: ShipAPI,
        val member: FleetMemberAPI,
        val mods: ShipModifications,
        val exoticData: ExoticData
    ) :
        DamageTakenModifier {
        override fun modifyDamageTaken(
            param: Any?,
            target: CombatEntityAPI,
            damage: DamageAPI,
            point: Vector2f,
            shieldHit: Boolean
        ): String? {
            if (ship == target && !shieldHit) {
                var state = getDamperState(ship, member, mods, exoticData)
                if (state is ReadyState) {
                    val totalDamage = damage.damage
                    if (totalDamage > TRIGGERING_DAMAGE) {
                        state.setNextState(ship)
                        state = getDamperState(ship, member, mods, exoticData)
                    }
                }
                if (state is ActiveState) {
                    damage.modifier.modifyMult(buffId, 1 - DAMPER_REDUCTION)
                    return buffId
                }
            }
            return null
        }
    }

    companion object {
        private const val TRIGGERING_DAMAGE = 100f
        private const val DAMPER_DURATION = 1f
        private const val DAMPER_COOLDOWN = 10f
        private const val DAMPER_REDUCTION = 90f
        private const val PASSIVE_DAMAGE_TAKEN = 15f
        private const val DAMPER_STATE_ID = "et_damperState"
    }
}