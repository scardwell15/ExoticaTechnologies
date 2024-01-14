package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.FleetMemberUtils
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.json.JSONObject
import java.awt.Color
import java.util.*
import kotlin.math.ceil

class PlasmaFluxCatalyst(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color(0x00BBFF)

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
            val maxCaps = getMaxCaps(member, mods, exoticData)
            val maxVents = getMaxVents(member, mods, exoticData)
            StringUtils.getTranslation(key, "longDescription")
                .format("effectLevel", getPositiveMult(member, mods, exoticData) * 100f)
                .format("capacitorLimit", ceil((maxCaps / 3f).toDouble()))
                .format("ventLimit", ceil((maxVents / 3f).toDouble()))
                .format("crDecrease", 1)
                .addToTooltip(tooltip, title)
        }
    }

    fun getMaxCaps(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Int {
        return ((FleetMemberUtils.getFleetCommander(member)?.stats?.maxCapacitorsBonus?.computeEffective(MAX_FLUX_EQUIPMENT[member.hullSpec.hullSize]!!.toFloat())
            ?: MAX_FLUX_EQUIPMENT[member.hullSpec.hullSize]!!)
            .toFloat() / getNegativeMult(member, mods, exoticData)).toInt()
    }

    fun getMaxVents(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Int {
        return ((FleetMemberUtils.getFleetCommander(member)?.stats?.maxVentsBonus?.computeEffective(MAX_FLUX_EQUIPMENT[member.hullSpec.hullSize]!!.toFloat())
        ?: MAX_FLUX_EQUIPMENT[member.hullSpec.hullSize]!!)
        .toFloat() / getNegativeMult(member, mods, exoticData)).toInt()
    }

    override fun applyExoticToStats(
        id: String,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        if (FleetMemberUtils.getFleetCommander(member) == null) {
            return
        }
        if (isNPC(member)) {
            return
        }

        val numCapsStats = stats.variant.numFluxCapacitors
        val numVentsStats = stats.variant.numFluxVents
        val maxCaps = getMaxCaps(member, mods, exoticData)
        val maxVents = getMaxVents(member, mods, exoticData)

        var crReduction = 0
        if (numCapsStats > ceil((maxCaps / 3f).toDouble())) {
            crReduction += (numCapsStats - ceil((maxCaps / 3f).toDouble())).toInt()
        }
        if (numVentsStats > ceil((maxVents / 3f).toDouble())) {
            crReduction += (numVentsStats - ceil((maxVents / 3f).toDouble())).toInt()
        }
        if (crReduction > 0) {
            stats.maxCombatReadiness.modifyFlat(name, -crReduction / 100f, name)
        }
    }

    override fun applyToShip(
        id: String,
        member: FleetMemberAPI,
        ship: ShipAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        val numCaps = ship.variant.numFluxCapacitors
        val numVents = ship.variant.numFluxVents
        ship.mutableStats.fluxCapacity.modifyFlat(buffId,numCaps * 200 * getPositiveMult(member, mods, exoticData))
        ship.mutableStats.fluxDissipation.modifyFlat(buffId, numVents * 10 * getPositiveMult(member, mods, exoticData))
    }

    companion object {
        private const val ITEM = "et_plasmacatalyst"
        private val MAX_FLUX_EQUIPMENT: MutableMap<HullSize, Int> = EnumMap(HullSize::class.java)

        init {
            MAX_FLUX_EQUIPMENT[HullSize.FIGHTER] = 10
            MAX_FLUX_EQUIPMENT[HullSize.FRIGATE] = 10
            MAX_FLUX_EQUIPMENT[HullSize.DESTROYER] = 20
            MAX_FLUX_EQUIPMENT[HullSize.CRUISER] = 30
            MAX_FLUX_EQUIPMENT[HullSize.CAPITAL_SHIP] = 50
        }
    }
}