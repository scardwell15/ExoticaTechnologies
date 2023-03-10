package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.FighterLaunchBayAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.IntervalUtil
import data.scripts.util.MagicUI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.json.JSONObject
import java.awt.Color

class HangarForge(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color.GREEN
    override fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI): Boolean {
        return Utilities.hasItem(fleet.cargo, ITEM)
    }

    override fun canApply(member: FleetMemberAPI, mods: ShipModifications?): Boolean {
        if (member.stats != null) {
            if (member.stats.numFighterBays.modifiedInt > 0) {
                return canApplyToVariant(member.variant)
            }
        }
        return false
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
                .format("rateDecreaseBuff", RATE_DECREASE_MODIFIER)
                .addToTooltip(tooltip, title)
        }
    }

    override fun applyExoticToStats(
        id: String,
        stats: MutableShipStatsAPI,
        fm: FleetMemberAPI,
        mods: ShipModifications,
        data: ExoticData
    ) {
        stats.dynamic.getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT)
            .modifyMult(buffId, 1f + RATE_DECREASE_MODIFIER / 100f)
    }

    override fun advanceInCombatAlways(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) {
        val replacements = getFreeReplacements(ship)
        val replacementInterval = getReplacementInterval(ship)
        if (replacementInterval != null) {
            MagicUI.drawInterfaceStatusBar(
                ship,
                replacementInterval.elapsed / replacementInterval.intervalDuration,
                RenderUtils.getAliveUIColor(),
                RenderUtils.getAliveUIColor(),
                0f,
                StringUtils.getString(key, "statusBarText"),
                replacements
            )
        }
    }

    override fun advanceInCombatUnpaused(
        ship: ShipAPI,
        amount: Float,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        var replacements = getFreeReplacements(ship)
        if (Global.getCombatEngine().isPaused) {
            return
        }
        if (replacements < calculateMaxReplacements(ship)) {
            advanceReplacementInterval(ship, amount)
        }
        if (replacements > 0) {
            for (bay in ship.launchBaysCopy) {
                if (getDeadFighters(bay) > 0 && bay.fastReplacements == 0) {
                    bay.fastReplacements = 1
                    bay.makeCurrentIntervalFast()
                    addFreeReplacements(ship, -1)
                    replacements--
                    if (replacements == 0) break
                }
            }
        }
    }

    fun maintainStatus(ship: ShipAPI, id: String?, translation: String?) {
        if (Global.getCombatEngine().playerShip != null && Global.getCombatEngine().playerShip == ship) {
            Global.getCombatEngine().maintainStatusForPlayerShip(
                id,
                "graphics/icons/hullsys/reserve_deployment.png",
                name,
                translation,
                false
            )
        }
    }

    override fun canUseExoticType(type: ExoticType): Boolean {
        return false
    }

    companion object {
        private const val ITEM = "et_hangarforge"
        private const val REPLACEMENT_COUNT_ID = "et_fighterReplacements"
        private const val REPLACEMENT_INTERVAL_ID = "et_fighterReplacementInterval"
        private const val RATE_DECREASE_MODIFIER = 25f
        fun getDeadFighters(bay: FighterLaunchBayAPI): Int {
            var dead = 0
            if (bay.wing != null && bay.wing.wingMembers != null) {
                for (ship in bay.wing.wingMembers) {
                    if (!ship.isAlive) {
                        dead++
                    }
                }
            }
            return dead
        }

        fun advanceReplacementInterval(ship: ShipAPI, amount: Float) {
            val replacementInterval = getReplacementInterval(ship)
            replacementInterval!!.advance(amount)
            if (replacementInterval.intervalElapsed()) {
                addFreeReplacements(ship, calculateAddedReplacements(ship))
            }
        }

        private fun getReplacementInterval(ship: ShipAPI): IntervalUtil? {
            if (!ship.customData.containsKey(REPLACEMENT_INTERVAL_ID)) {
                ship.setCustomData(REPLACEMENT_INTERVAL_ID, IntervalUtil(60f, 60f))
            }
            return ship.customData[REPLACEMENT_INTERVAL_ID] as IntervalUtil?
        }

        private fun getFreeReplacements(ship: ShipAPI): Int {
            if (!ship.customData.containsKey(REPLACEMENT_COUNT_ID)) {
                val maxReplacements = calculateMaxReplacements(ship)
                setFreeReplacements(ship, maxReplacements)
            }
            return ship.customData[REPLACEMENT_COUNT_ID] as Int
        }

        private fun setFreeReplacements(ship: ShipAPI, value: Int) {
            ship.setCustomData(REPLACEMENT_COUNT_ID, value)
        }

        private fun addFreeReplacements(ship: ShipAPI, value: Int) {
            setFreeReplacements(ship, getFreeReplacements(ship) + value)
        }

        private fun calculateAddedReplacements(ship: ShipAPI): Int {
            return Math.ceil((calculateMaxReplacements(ship) / 2f).toDouble()).toInt()
        }

        private fun calculateMaxReplacements(ship: ShipAPI): Int {
            if (ship.variant == null || ship.variant.wings == null) {
                return 1
            }
            var wingCount = 0
            for (wingId in ship.variant.wings) {
                val spec = Global.getSettings().getFighterWingSpec(wingId)
                wingCount += spec?.numFighters ?: 1
            }
            return wingCount
        }
    }
}