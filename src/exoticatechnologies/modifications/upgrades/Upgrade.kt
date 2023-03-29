package exoticatechnologies.modifications.upgrades

import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.ETModSettings
import exoticatechnologies.modifications.Modification
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.stats.UpgradeModEffect
import exoticatechnologies.modifications.stats.impl.UpgradeModEffectDict.Companion.getStatsFromJSONArray
import exoticatechnologies.ui.impl.shop.upgrades.methods.UpgradeMethod
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.json.JSONObject
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

class Upgrade(key: String?, settings: JSONObject) : Modification(key!!, settings) {
    val resourceRatios: MutableMap<String, Float> = LinkedHashMap()
    val upgradeEffects: MutableList<UpgradeModEffect> = ArrayList()
    var bandwidthUsage: Float
    var spawnChance: Float
    var salvageChance: Float
    var showInStoreIfNotInstalled: Boolean
    var chipFirstInstall: Boolean
    var chipOnlyInstall: Boolean

    init {
        color = Utilities.colorFromJSONArray(settings.getJSONArray("color"))
        description = StringUtils.getString(key, "description")
        bandwidthUsage = settings.getDouble("bandwidthPerLevel").toFloat()
        upgradeEffects.addAll(getStatsFromJSONArray(settings.getJSONArray("stats")))
        spawnChance = settings.optDouble("spawnChance", 1.0).toFloat()
        salvageChance = settings.optDouble("salvageChance", spawnChance.toDouble()).toFloat()
        showInStoreIfNotInstalled = settings.optBoolean("showInStoreIfNotInstalled", true)
        chipFirstInstall = settings.optBoolean("chipFirstInstall")
        chipOnlyInstall = settings.optBoolean("chipOnlyInstall")
        val settingRatios = settings.getJSONObject("resourceRatios")
        for (resource in Utilities.RESOURCES_LIST) {
            var ratio = 0f
            if (settingRatios.has(resource)) {
                //class cast exception indicates an improperly configured config
                ratio = (settingRatios.getDouble(resource) as Number).toFloat()
            }
            resourceRatios[resource] = ratio
        }
    }

    override fun shouldShow(member: FleetMemberAPI, mods: ShipModifications, market: MarketAPI): Boolean {
        if (mods.getUpgrade(this) > 0) {
            return true
        }
        if (!showInStoreIfNotInstalled) {
            if (member.fleetData.fleet.isPlayerFleet) {
                if (Utilities.hasUpgradeChip(member.fleetData.fleet.cargo, key)) {
                    return super.shouldShow(member, mods, market)
                }
            }
            return false
        }
        return super.shouldShow(member, mods, market)
    }

    fun canUseUpgradeMethod(member: FleetMemberAPI?, mods: ShipModifications, method: UpgradeMethod): Boolean {
        if (method.key == "recover") {
            return true
        }
        if (chipOnlyInstall) {
            if (method.key != "chip") {
                return false
            }
        }
        return !(chipFirstInstall && !mods.hasUpgrade(this) && method.key != "chip")
    }

    public override val icon: String
        get() = "graphics/icons/upgrades/" + key.lowercase(Locale.getDefault()) + ".png"
    private val maxLevel: Int
        private get() = -1

    fun getMaxLevel(hullSize: HullSize?): Int {
        return if (maxLevel != -1) maxLevel else ETModSettings.getHullSizeToMaxLevel()[hullSize]!!
    }

    /**
     * note: overrides should be done to the HullSize method
     *
     * @param fm
     * @return
     */
    fun getMaxLevel(fm: FleetMemberAPI): Int {
        return getMaxLevel(fm.hullSpec.hullSize)
    }

    fun applyUpgradeToStats(stats: MutableShipStatsAPI?, fm: FleetMemberAPI?, mods: ShipModifications?) {
        for (effect in upgradeEffects) {
            if (!effect.appliesToFighters) {
                effect.applyToStats(stats!!, fm!!, mods!!, this)
            }
        }
    }

    fun applyToShip(member: FleetMemberAPI?, ship: ShipAPI?, mods: ShipModifications?) {
        for (effect in upgradeEffects) {
            if (!effect.appliesToFighters) {
                effect.applyToShip(ship!!, member!!, mods!!, this)
            }
        }
    }

    fun applyToFighters(member: FleetMemberAPI?, ship: ShipAPI?, fighter: ShipAPI?, mods: ShipModifications?) {
        for (effect in upgradeEffects) {
            if (effect.appliesToFighters) {
                effect.applyToFighter(ship!!, fighter!!, member!!, mods!!, this)
            }
        }
    }

    fun advanceInCombatUnpaused(ship: ShipAPI?, amount: Float, member: FleetMemberAPI?, mods: ShipModifications?) {
        for (effect in upgradeEffects) {
            effect.advanceInCombatUnpaused(ship!!, amount, member!!, mods!!, this)
        }
    }

    fun advanceInCombatAlways(ship: ShipAPI?, member: FleetMemberAPI?, mods: ShipModifications?) {
        for (effect in upgradeEffects) {
            effect.advanceInCombatAlways(ship!!, member!!, mods!!, this)
        }
    }

    fun advanceInCampaign(member: FleetMemberAPI?, mods: ShipModifications?, amount: Float?) {
        for (effect in upgradeEffects) {
            effect.advanceInCampaign(member!!, mods!!, this, amount!!)
        }
    }

    fun modifyToolTip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI?,
        member: FleetMemberAPI?,
        mods: ShipModifications,
        expand: Boolean
    ) {
        val imageText = tooltip.beginImageWithText(icon, 64f)
        imageText.addPara("$name (%s)", 0f, color, mods.getUpgrade(this).toString())
        if (expand) {
            for (effect in upgradeEffects) {
                effect.printToTooltip(imageText, stats!!, member!!, mods, this)
            }
        }
        tooltip.addImageWithText(5f)
    }

    fun modifyInShop(tooltip: TooltipMakerAPI?, member: FleetMemberAPI, mods: ShipModifications?) {
        val levelToEffectMap: MutableMap<Int, MutableList<UpgradeModEffect>> = HashMap()
        for (effect in upgradeEffects) {
            val startingLevel = effect.startingLevel
            val levelList: MutableList<UpgradeModEffect> = levelToEffectMap.getOrPut(startingLevel){ mutableListOf() }
            levelList.add(effect)
        }
        for (startingLevel in 0 until getMaxLevel(member)) {
            if (levelToEffectMap.containsKey(startingLevel)) {
                val levelList: List<UpgradeModEffect> = levelToEffectMap[startingLevel]!!
                if (startingLevel > 1) {
                    StringUtils.getTranslation("UpgradesDialog", "UpgradeDrawbackAfterLevel")
                        .format("level", startingLevel)
                        .addToTooltip(tooltip).position.setYAlignOffset(-10f)
                }
                for (effect in levelList) {
                    effect.printToShop(tooltip!!, member, mods!!, this)
                }
            }
        }
    }

    fun getNewSpecialItemData(level: Int): SpecialItemData {
        return SpecialItemData(ITEM, String.format("%s,%s", key, level))
    }

    fun getResourceCosts(shipSelected: FleetMemberAPI, level: Int): Map<String, Int> {
        val max = getMaxLevel(shipSelected.hullSpec.hullSize)
        var hullBaseValue = shipSelected.hullSpec.baseValue
        hullBaseValue = if (hullBaseValue > 450000) {
            225000f
        } else {
            (hullBaseValue - 1.0 / 900000.0 * hullBaseValue.toDouble().pow(2.0)).toFloat()
        }
        hullBaseValue *= 0.01f
        val upgradeCostRatioByLevel = 0.25f + 0.75f * (level.toFloat() / max.toFloat())
        val upgradeCostByHull = hullBaseValue * upgradeCostRatioByLevel
        val resourceCosts: MutableMap<String, Int> = HashMap()
        for ((itemKey, ratio) in resourceRatios) {
            val commodityCost = Utilities.getItemPrice(itemKey).roundToInt()
            val finalCost = (ratio * upgradeCostByHull / commodityCost).roundToInt()
            resourceCosts[itemKey] = finalCost
        }
        return resourceCosts
    }

    override fun toString(): String {
        return "Upgrade{name=$name}"
    }

    companion object {
        const val ITEM = "et_upgrade"
        operator fun get(upgradeKey: String?): Upgrade {
            return UpgradesHandler.UPGRADES[upgradeKey]!!
        }
    }
}