package exoticatechnologies.modifications.upgrades

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.Modification
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.toList
import exoticatechnologies.modifications.stats.UpgradeModEffect
import exoticatechnologies.modifications.stats.impl.UpgradeModEffectDict.Companion.getStatsFromJSONArray
import exoticatechnologies.ui.impl.shop.upgrades.methods.UpgradeMethod
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.json.JSONObject
import java.awt.Color
import kotlin.math.pow
import kotlin.math.roundToInt

open class Upgrade(key: String, settings: JSONObject) : Modification(key, settings) {
    val resourceRatios: MutableMap<String, Float> = LinkedHashMap()
    val upgradeEffects: MutableList<UpgradeModEffect> = ArrayList()
    var bandwidthUsage: Float
    var spawnChance: Float
    var salvageChance: Float
    var showInStoreIfNotInstalled: Boolean
    var chipFirstInstall: Boolean
    var chipOnlyInstall: Boolean
    var italicsText: String? = null
    open var maxLevel: Int = 10
    public override var icon: String = settings.optString("icon", key)
    var iconPath: String = Global.getSettings().getSpriteName("upgrades", icon)
    var allowedMethods: List<String>? = null
    var blockedMethods: List<String>? = null

    init {
        color = Utilities.colorFromJSONArray(settings.getJSONArray("color"))
        description = StringUtils.getString(key, "description")

        if (settings.has("italicsKey")) {
            italicsText = settings.getString("italicsKey")
        }

        bandwidthUsage = settings.getDouble("bandwidthPerLevel").toFloat()

        if (settings.has("stats")) {
            upgradeEffects.addAll(getStatsFromJSONArray(settings.getJSONArray("stats")))
        }

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

        if (settings.has("allowedMethods")) {
            allowedMethods = settings.optJSONArray("allowedMethods").toList()
        }

        if (settings.has("blockedMethods")) {
            blockedMethods = settings.optJSONArray("blockedMethods").toList()
        }
    }

    override fun shouldShow(member: FleetMemberAPI, mods: ShipModifications, market: MarketAPI?): Boolean {
        if (mods.getUpgrade(this) > 0) {
            return true
        }
        if (!showInStoreIfNotInstalled) {
            if (Global.getSector().playerFleet.isPlayerFleet) {
                if (Utilities.hasUpgradeChip(Global.getSector().playerFleet.cargo, key)) {
                    return super.shouldShow(member, mods, market)
                }
            }
            return false
        }
        return super.shouldShow(member, mods, market)
    }

    fun canUseUpgradeMethod(member: FleetMemberAPI?, mods: ShipModifications, method: UpgradeMethod): Boolean {
        if (blockedMethods?.contains(method.key) == true) {
            return false
        }
        
        if (allowedMethods?.contains(method.key) == false) {
            return false
        }

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


    open fun applyUpgradeToStats(stats: MutableShipStatsAPI, fm: FleetMemberAPI, mods: ShipModifications, level: Int) {
        for (effect in upgradeEffects) {
            if (!effect.appliesToFighters) {
                effect.applyToStats(stats, fm, mods, this)
            }
        }
    }

    open fun applyToShip(member: FleetMemberAPI, ship: ShipAPI, mods: ShipModifications) {
        for (effect in upgradeEffects) {
            if (!effect.appliesToFighters) {
                effect.applyToShip(ship, member, mods, this)
            }
        }
    }

    open fun applyToFighters(member: FleetMemberAPI, ship: ShipAPI, fighter: ShipAPI, mods: ShipModifications) {
        for (effect in upgradeEffects) {
            if (effect.appliesToFighters) {
                effect.applyToFighter(ship, fighter, member, mods, this)
            }
        }
    }

    open fun advanceInCombatUnpaused(ship: ShipAPI, amount: Float, member: FleetMemberAPI, mods: ShipModifications) {
        for (effect in upgradeEffects) {
            effect.advanceInCombatUnpaused(ship, amount, member, mods, this)
        }
    }

    open fun advanceInCombatAlways(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications) {
        for (effect in upgradeEffects) {
            effect.advanceInCombatAlways(ship, member, mods, this)
        }
    }

    open fun advanceInCampaign(member: FleetMemberAPI, mods: ShipModifications, amount: Float) {
        for (effect in upgradeEffects) {
            effect.advanceInCampaign(member, mods, this, amount)
        }
    }

    open fun modifyToolTip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        expand: Boolean
    ): TooltipMakerAPI {
        val imageText = tooltip.beginImageWithText(iconPath, 64f)
        imageText.addPara("$name (%s)", 0f, color, mods.getUpgrade(this).toString())
        if (expand) {
            for (effect in upgradeEffects) {
                effect.printToTooltip(imageText, stats, member, mods, this)
            }
        }
        tooltip.addImageWithText(5f)

        return imageText
    }

    open fun showDescriptionInShop(tooltip: TooltipMakerAPI, member: FleetMemberAPI, mods: ShipModifications) {
        if (italicsText != null) {
            val italicsLabel = StringUtils.getTranslation(key, italicsText)
                .addToTooltip(tooltip)
            italicsLabel.italicize()
            italicsLabel.setColor(Color.GRAY)
        }

        StringUtils.getTranslation(key, "description")
            .addToTooltip(tooltip)
    }

    open fun showStatsInShop(tooltip: TooltipMakerAPI, member: FleetMemberAPI, mods: ShipModifications) {
        val levelToEffectMap: MutableMap<Int, MutableList<UpgradeModEffect>> = HashMap()
        for (effect in upgradeEffects) {
            val startingLevel = effect.startingLevel
            val levelList: MutableList<UpgradeModEffect> = levelToEffectMap.getOrPut(startingLevel) { mutableListOf() }
            levelList.add(effect)
        }
        for (startingLevel in 0 until maxLevel) {
            if (levelToEffectMap.containsKey(startingLevel)) {
                val levelList: List<UpgradeModEffect> = levelToEffectMap[startingLevel]!!
                if (startingLevel > 1) {
                    StringUtils.getTranslation("UpgradesDialog", "UpgradeDrawbackAfterLevel")
                        .format("level", startingLevel)
                        .addToTooltip(tooltip).position.setYAlignOffset(-10f)
                }
                for (effect in levelList) {
                    effect.printToShop(tooltip, member, mods, this)
                }
            }
        }
    }

    fun getNewSpecialItemData(level: Int): SpecialItemData {
        return SpecialItemData(ITEM, String.format("%s,%s", key, level))
    }

    fun getResourceCosts(shipSelected: FleetMemberAPI, level: Int): Map<String, Int> {
        var hullBaseValue = shipSelected.hullSpec.baseValue
        hullBaseValue = if (hullBaseValue > 450000) {
            225000f
        } else {
            (hullBaseValue - hullBaseValue.pow(2f) / 900000f)
        }
        hullBaseValue *= 0.01f

        val upgradeCostRatioByLevel = 0.25f + 0.75f * (level.toFloat() / maxLevel.toFloat())
        val upgradeCostByHull = hullBaseValue * upgradeCostRatioByLevel
        val resourceCosts: MutableMap<String, Int> = HashMap()
        for ((itemKey, ratio) in resourceRatios) {
            val commodityCost = Utilities.getItemPrice(itemKey).roundToInt()
            val finalCost = (ratio * upgradeCostByHull / commodityCost).roundToInt()
            resourceCosts[itemKey] = finalCost
        }
        return resourceCosts
    }

    fun getResourceCosts(level: Int): Map<String, Int> {
        val hullBaseValue = 30000f * 0.01f
        val upgradeCostRatioByLevel = 0.25f + 0.75f * (level.toFloat() / maxLevel.toFloat())
        val upgradeCostByHull = hullBaseValue * upgradeCostRatioByLevel
        val resourceCosts: MutableMap<String, Int> = HashMap()
        for ((itemKey, ratio) in resourceRatios) {
            val commodityCost = Utilities.getItemPrice(itemKey).roundToInt()
            val finalCost = (ratio * upgradeCostByHull / commodityCost).roundToInt()
            resourceCosts[itemKey] = finalCost
        }
        return resourceCosts
    }

    fun getCreditCostForResources(level: Int): Int {
        return getCreditCostForResources(getResourceCosts(level))
    }

    /**
     * Sums up the floats in the map.
     *
     * @param resourceCosts
     * @return The sum.
     */
    fun getCreditCostForResources(resourceCosts: Map<String, Int>): Int {
        var creditCost = 0f
        for ((key, value) in resourceCosts) {
            creditCost += Utilities.getItemPrice(key) * value
        }
        return creditCost.toInt()
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