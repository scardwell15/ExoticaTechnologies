package exoticatechnologies.modifications.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.Modification
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.toList
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.ui.impl.shop.exotics.methods.ExoticMethod
import exoticatechnologies.util.StringUtils
import org.json.JSONObject
import java.util.*

abstract class Exotic(key: String, settings: JSONObject) : Modification(key, settings) {
    var loreDescription: String? = null
    open val textDescription: String?
        get() = "$loreDescription\n\n$description"

    public override var icon: String = settings.optString("icon", key)
    val buffId: String
        get() = "ET_$key"
    var allowedMethods: List<String>? = null
    var blockedMethods: List<String>? = null

    init {
        if (settings.has("allowedMethods")) {
            allowedMethods = settings.optJSONArray("allowedMethods").toList()
        }

        if (settings.has("blockedMethods")) {
            blockedMethods = settings.optJSONArray("blockedMethods").toList()
        }
    }

    protected fun isNPC(fm: FleetMemberAPI): Boolean {
        return fm.fleetData == null || fm.fleetData.fleet == null || fm.fleetData.fleet != Global.getSector().playerFleet
    }

    open fun onInstall(member: FleetMemberAPI) {}
    open fun onDestroy(member: FleetMemberAPI) {}
    open fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI?): Boolean {
        return false
    }

    open fun getGenerationChanceMult(member: FleetMemberAPI): Float {
        return 1f
    }

    open fun countsTowardsExoticLimit(member: FleetMemberAPI): Boolean {
        return true
    }

    open fun removeItemsFromFleet(fleet: CampaignFleetAPI, member: FleetMemberAPI, market: MarketAPI?): Boolean {
        return false
    }

    fun printDescriptionToTooltip(fm: FleetMemberAPI, tooltip: TooltipMakerAPI) {
        StringUtils.getTranslation(key, "description")
            .addToTooltip(tooltip)
    }

    fun printStatInfoToTooltip(fm: FleetMemberAPI, tooltip: TooltipMakerAPI) {
        StringUtils.getTranslation(key, "longDescription")
            .addToTooltip(tooltip)
    }

    fun printDescriptionToTooltip(tooltip: TooltipMakerAPI, member: FleetMemberAPI) {
        StringUtils.getTranslation(key, "description")
            .addToTooltip(tooltip)
    }

    fun printStatInfoToTooltip(tooltip: TooltipMakerAPI, member: FleetMemberAPI) {
        StringUtils.getTranslation(key, "longDescription")
            .addToTooltip(tooltip)
    }

    open fun getResourceCostMap(
        fm: FleetMemberAPI,
        mods: ShipModifications,
        market: MarketAPI?
    ): MutableMap<String, Float> {
        return HashMap()
    }

    /**
     * extra bandwidth allowed to be installed.
     *
     * @param member
     * @param mods
     * @param exoticData
     * @return
     */
    fun getExtraBandwidthPurchaseable(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData?): Float {
        return 0f
    }

    /**
     * extra bandwidth added directly to ship.
     *
     * @param member
     * @param mods
     * @param exoticData
     * @return
     */
    open fun getExtraBandwidth(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData?): Float {
        return 0f
    }

    open fun applyExoticToStats(
        id: String,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
    }

    open fun applyToShip(
        id: String,
        member: FleetMemberAPI,
        ship: ShipAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
    }

    open fun applyToFighters(member: FleetMemberAPI, ship: ShipAPI, fighter: ShipAPI, mods: ShipModifications) {
    }

    open fun advanceInCombatUnpaused(
        ship: ShipAPI,
        amount: Float,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
    }

    open fun advanceInCombatAlways(
        ship: ShipAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
    }

    open fun advanceInCampaign(
        member: FleetMemberAPI,
        mods: ShipModifications,
        amount: Float,
        exoticData: ExoticData
    ) {
    }

    open fun getSalvageChance(chanceMult: Float): Float {
        return 0.2f * chanceMult
    }

    open fun canUseMethod(member: FleetMemberAPI?, mods: ShipModifications, method: ExoticMethod): Boolean {
        if (blockedMethods?.contains(method.key) == true) {
            return false
        }

        if (allowedMethods?.contains(method.key) == false) {
            return false
        }

        return true
    }

    open fun canUseExoticType(type: ExoticType): Boolean {
        return true
    }

    open fun getPositiveMult(member: FleetMemberAPI, mods: ShipModifications, exoticType: ExoticType?): Float {
        return exoticType?.getPositiveMult(member, mods) ?: 1f
    }

    fun getPositiveMult(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData?): Float {
        return getPositiveMult(member, mods, exoticData?.type)
    }

    open fun getNegativeMult(member: FleetMemberAPI, mods: ShipModifications, exoticType: ExoticType?): Float {
        return exoticType?.getNegativeMult(member, mods) ?: 1f
    }

    fun getNegativeMult(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData?): Float {
        return getNegativeMult(member, mods, exoticData?.type)
    }

    fun getNewSpecialItemData(exoticType: ExoticType): SpecialItemData {
        return SpecialItemData(ITEM, String.format("%s,%s", key, exoticType.nameKey))
    }

    val newSpecialItemData: SpecialItemData
        get() = SpecialItemData(ITEM, key)

    abstract fun modifyToolTip(
        tooltip: TooltipMakerAPI,
        title: UIComponentAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData,
        expand: Boolean
    )

    companion object {
        const val ITEM = "et_exotic"
        operator fun get(exoticKey: String?): Exotic? {
            return ExoticsHandler.EXOTICS[exoticKey]
        }
    }
}