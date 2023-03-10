package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.json.JSONObject
import java.awt.Color

class TierIIIDriveSystem(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color(0xFFA2E4)
    override fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI): Boolean {
        return Utilities.hasItem(fleet.cargo, ITEM)
    }

    override fun removeItemsFromFleet(fleet: CampaignFleetAPI, member: FleetMemberAPI, market: MarketAPI): Boolean {
        Utilities.takeItemQuantity(fleet.cargo, ITEM, 1f)
        return true
    }

    fun getCargoToFuelPercent(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return CARGO_TO_FUEL_PERCENT + (100 - CARGO_TO_FUEL_PERCENT) * (getNegativeMult(member, mods, exoticData) - 1f)
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
                .format("cargoToFuelPercent", getCargoToFuelPercent(member, mods, exoticData))
                .format("burnBonusFuelReq", BURN_BONUS_FUEL_REQ / getPositiveMult(member, mods, exoticData))
                .format("burnBonus", BURN_BONUS)
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
        val addedFuelCap = (member.cargoCapacity * getCargoToFuelPercent(member, mods, exoticData) / 100f).toInt()
        stats.cargoMod.modifyMult(buffId, 1 - getCargoToFuelPercent(member, mods, exoticData) / 100f)
        stats.fuelMod.modifyFlat(buffId, addedFuelCap.toFloat())
    }

    override fun advanceInCampaign(member: FleetMemberAPI, mods: ShipModifications, amount: Float, exoticData: ExoticData) {
        if (member.fleetData != null && member.fleetData.fleet != null) {
            checkBuff(member.fleetData.fleet, member, mods, exoticData)
        }
    }

    override fun onDestroy(member: FleetMemberAPI) {
        if (member.fleetData != null && member.fleetData.fleet != null) {
            removeBuff(member.fleetData.fleet.stats)
        }
    }

    private fun checkBuff(
        fleet: CampaignFleetAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        val fleetStats = fleet.stats
        if (fleet.cargo.freeFuelSpace < fleet.cargo.maxFuel * (1 - CARGO_TO_FUEL_PERCENT / 100f / getPositiveMult(member, mods, exoticData))) {
            if (fleetStats.fleetwideMaxBurnMod.getFlatBonus(buffId) == null) {
                fleetStats.fleetwideMaxBurnMod.modifyFlat(buffId, BURN_BONUS, name)
            }
        } else {
            removeBuff(fleetStats)
        }
    }

    private fun removeBuff(fleetStats: MutableFleetStatsAPI) {
        if (fleetStats.fleetwideMaxBurnMod.getFlatBonus(buffId) != null) {
            fleetStats.fleetwideMaxBurnMod.unmodify(buffId)
        }
    }

    companion object {
        private const val CARGO_TO_FUEL_PERCENT = 75f
        private const val BURN_BONUS_FUEL_REQ = 66f
        private const val BURN_BONUS = 2f
    }
}