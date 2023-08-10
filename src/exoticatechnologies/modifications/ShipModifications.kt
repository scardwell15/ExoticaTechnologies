package exoticatechnologies.modifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.ETModSettings
import exoticatechnologies.config.FactionConfigLoader
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.modifications.exotics.*
import exoticatechnologies.modifications.upgrades.ETUpgrades
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import exoticatechnologies.ui.UIUtils
import exoticatechnologies.util.StringUtils
import org.apache.log4j.Logger
import org.json.JSONException
import org.json.JSONObject
import org.lazywizard.lazylib.ext.json.optFloat
import java.awt.Color

class ShipModifications(var bandwidth: Float, var upgrades: ETUpgrades, var exotics: ETExotics) {
    constructor() : this(-1f, ETUpgrades(), ETExotics())

    @Throws(JSONException::class)
    constructor(obj: JSONObject) : this() {
        bandwidth = obj.optFloat(BANDWIDTH_KEY, -1f)

        if (obj.has(UPGRADES_KEY)) {
            val upgObj = obj.getJSONObject(UPGRADES_KEY)
            upgrades.parseJson(upgObj)
        }

        if (obj.has(EXOTICS_KEY)) {
            val exoObj = obj.get(EXOTICS_KEY)
            exotics.parseJson(exoObj)
        }
    }

    companion object {
        @JvmStatic
        private val log = Logger.getLogger(Companion::class.java)

        val UPGRADES_KEY = "upgrades"
        val EXOTICS_KEY = "exotics"
        val BANDWIDTH_KEY = "baseBandwidth"
    }

    fun shouldApplyHullmod(): Boolean {
        return (upgrades.hasUpgrades()
                || exotics.hasAnyExotic())
    }

    fun getValue(): Float {
        return bandwidth + exotics.list.size + upgrades.totalLevels
    }

    //bandwidth
    /**
     * Use this only if bandwidth has already been generated. The Exotica dialog WILL generate bandwidth.
     * @return
     */
    fun getBaseBandwidth(): Float {
        return bandwidth.coerceAtLeast(0f)
    }

    /**
     * Will generate bandwidth if not already generated.
     * @param fm
     * @return
     */
    fun getBaseBandwidth(fm: FleetMemberAPI): Float {
        if (bandwidth < 0f) {
            log.info(String.format("Bandwidth was below zero for fm [%s]", fm.id))
            bandwidth = ShipModFactory.generateBandwidth(fm)
        }
        return Math.max(bandwidth, 0f)
    }

    fun getBandwidthWithExotics(fm: FleetMemberAPI): Float {
        if (bandwidth < 0f) {
            log.info(String.format("Bandwidth with exotics was below zero for fm [%s]", fm.id))
            bandwidth = ShipModFactory.generateBandwidth(fm)
        }
        var returnedBandwidth = bandwidth
        for (exotic in ExoticsHandler.EXOTIC_LIST) {
            if (this.hasExotic(exotic)) {
                returnedBandwidth += exotic.getExtraBandwidth(fm, this, this.getExoticData(exotic)!!)
            }
        }
        return returnedBandwidth
    }

    fun canUpgradeBandwidth(fm: FleetMemberAPI): Boolean {
        var maxBandwidth = Bandwidth.MAX_BANDWIDTH
        for (exotic in ExoticsHandler.EXOTIC_LIST) {
            if (this.hasExotic(exotic)) {
                maxBandwidth += exotic.getExtraBandwidthPurchaseable(fm, this, this.getExoticData(exotic)!!)
            }
        }
        return maxBandwidth > getBaseBandwidth(fm)
    }

    fun getUsedBandwidth(): Float {
        var usedBandwidth = 0f
        for (upgrade in UpgradesHandler.UPGRADES_LIST) {
            usedBandwidth += upgrade.bandwidthUsage * this.getUpgrade(upgrade)
        }
        return usedBandwidth
    }

    fun getUsableBandwidth(member: FleetMemberAPI): Float {
        return getBandwidthWithExotics(member) - getUsedBandwidth()
    }

    //exotics
    fun getExoticSet(): Set<ExoticData> {
        val exoticSet: MutableSet<ExoticData> = HashSet()
        for (exotic in exotics.exoticData) {
            exoticSet.add(exotic.value)
        }
        return exoticSet
    }

    fun getExoticIdSet(): Set<Exotic> {
        val exoticSet: MutableSet<Exotic> = HashSet()
        for (exotic in exotics.exoticData) {
            exoticSet.add(exotic.value.exotic)
        }
        return exoticSet
    }

    fun hasExotic(key: String): Boolean {
        return exotics.hasExotic(key)
    }

    fun hasExotic(exotic: Exotic): Boolean {
        return hasExotic(exotic.key)
    }

    fun hasExotics(): Boolean {
        return exotics.hasAnyExotic()
    }

    fun putExotic(exoticData: ExoticData) {
        exotics.putExotic(exoticData)
    }

    fun removeExotic(exotic: Exotic) {
        exotics.removeExotic(exotic)
    }

    fun getExoticData(exotic: Exotic): ExoticData? {
        return exotics.getData(exotic)
    }

    fun getMaxExotics(member: FleetMemberAPI): Int {
        if (member.fleetData != null && member.fleetData.fleet != null) {
            val factionConfig = FactionConfigLoader.getFactionConfig(member.fleetData.fleet.faction.id)
            return factionConfig.getMaxExotics(member)
        }
        return ETModSettings.MAX_EXOTICS
    }

    //upgrades

    fun getUpgradeMap(): Map<Upgrade, Int> {
        val upgradeMap: MutableMap<Upgrade, Int> = HashMap()
        for ((key, value) in upgrades.map) {
            upgradeMap[Upgrade.get(key)] = value
        }
        return upgradeMap
    }

    fun putUpgrade(upgrade: Upgrade) {
        upgrades.putUpgrade(upgrade)
    }

    fun putUpgrade(upgrade: Upgrade, level: Int) {
        upgrades.putUpgrade(upgrade, level)
    }

    fun getUpgrade(key: String): Int {
        return upgrades.getUpgrade(key)
    }

    fun getUpgrade(upgrade: Upgrade): Int {
        return getUpgrade(upgrade.key)
    }

    fun removeUpgrade(upgrade: Upgrade) {
        upgrades.removeUpgrade(upgrade)
    }

    fun hasUpgrade(upgrade: Upgrade): Boolean {
        return getUpgrade(upgrade) > 0
    }

    fun hasUpgrades(): Boolean {
        return upgrades.hasUpgrades()
    }

    fun hasBandwidthForUpgrade(member: FleetMemberAPI, upgrade: Upgrade, level: Int): Boolean {
        val upgradeBandwidth = (level - this.getUpgrade(upgrade)) * upgrade.bandwidthUsage
        return getUsedBandwidth() + upgradeBandwidth <= getBandwidthWithExotics(member)
    }

    fun isMaxLevel(member: FleetMemberAPI, upgrade: Upgrade): Boolean {
        return this.getUpgrade(upgrade) >= upgrade.maxLevel
    }

    fun getTags(): List<String> {
        val tags: MutableList<String> = mutableListOf()
        tags.addAll(exotics.tags)
        tags.addAll(upgrades.tags)
        return tags
    }

    fun getModsThatConflict(tags: List<String>): List<Modification> {
        if (tags.isEmpty()) {
            return mutableListOf()
        }

        val mods: MutableList<Modification> = mutableListOf()
        mods.addAll(exotics.getConflicts(tags))
        mods.addAll(upgrades.getConflicts(tags))
        return mods
    }

    override fun toString(): String {
        return "ShipModifications{" +
                "bandwidth=" + bandwidth +
                ", modules=" + exotics +
                ", upgrades=" + upgrades +
                '}'
    }

    private val tooltipColor = Misc.getTextColor()
    private val infoColor = Misc.getPositiveHighlightColor()
    fun populateTooltip(
        member: FleetMemberAPI,
        stats: MutableShipStatsAPI,
        mainTooltip: TooltipMakerAPI,
        width: Float,
        height: Float = 350f,
        expandUpgrades: Boolean,
        expandExotics: Boolean,
        noScroller: Boolean = false
    ) {
        val bandwidth: Float = this.getBandwidthWithExotics(member)
        val bandwidthString = BandwidthUtil.getFormattedBandwidthWithName(bandwidth)

        mainTooltip.addPara("The ship has %s bandwidth.", 0f, Bandwidth.getColor(bandwidth), bandwidthString)

        val customPanelAPI: CustomPanelAPI = Global.getSettings().createCustom(width, height, null)
        val scrollTooltip = customPanelAPI.createUIElement(width, height, !noScroller)

        var tooltip: TooltipMakerAPI = scrollTooltip

        var scrollerParentPanel: CustomPanelAPI? = null
        if (!noScroller) {
            val innerPanel = customPanelAPI.createCustomPanel(width, 0f, null)
            scrollerParentPanel = innerPanel
            tooltip = innerPanel.createUIElement(width, 0f, false)
        }

        var addedExoticSection = false
        if (!expandUpgrades) {
            try {
                addedExoticSection = addExoticSection(tooltip, width, member, expandExotics)
            } catch (th: Throwable) {
                log.info("Caught exotic description exception", th)
                tooltip.addPara("Caught an error! See starsector.log", Color.RED, 0f)
            }
        }


        if (addedExoticSection) {
            tooltip.addSpacer(12f)
        }

        var addedUpgradeSection = false
        if (!expandExotics) {
            try {
                addedUpgradeSection = addUpgradeSection(tooltip, stats, member, expandUpgrades)
            } catch (th: Throwable) {
                log.info("Caught upgrade description exception", th)
                tooltip.addPara("Caught an error! See starsector.log", Color.RED, 0f)
            }
        }

        scrollerParentPanel?.apply {
            tooltip.addSpacer(6f)
            position.setSize(position.width, tooltip.heightSoFar)

            addUIElement(tooltip)
            scrollTooltip.addCustom(this, 0f)
        }

        customPanelAPI.addUIElement(scrollTooltip).inTL(-8f, 0f)
        mainTooltip.addCustom(customPanelAPI, 0f)
        mainTooltip.setForceProcessInput(true)
    }

    private fun addUpgradeSection(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        expandUpgrades: Boolean
    ): Boolean {
        var addedUpgradeSection = false
        for (upgrade in UpgradesHandler.UPGRADES_LIST) {
            if (this.getUpgrade(upgrade) < 1) continue
            if (!addedUpgradeSection) {
                addedUpgradeSection = true
                tooltip.addSectionHeading(
                    StringUtils.getString("FleetScanner", "UpgradeHeader"),
                    Alignment.MID,
                    6f
                )
            }

            upgrade.modifyToolTip(tooltip, stats, member, this, expandUpgrades)
            tooltip.setParaFontDefault()
            tooltip.setParaFontColor(tooltipColor)
        }

        return addedUpgradeSection
    }

    private fun addExoticSection(
        tooltip: TooltipMakerAPI,
        width: Float,
        member: FleetMemberAPI,
        expandExotics: Boolean
    ): Boolean {
        var addedExoticSection = false
        var lastThing: UIComponentAPI? = null
        for (exotic in ExoticsHandler.EXOTIC_LIST) {
            if (!this.hasExotic(exotic.key)) continue
            if (!addedExoticSection) {
                addedExoticSection = true
                tooltip.addSectionHeading(
                    StringUtils.getString("FleetScanner", "ExoticHeader"),
                    Alignment.MID,
                    6f
                )
                lastThing = tooltip.prev
            }

            val exoticData = this.getExoticData(exotic)!!

            val innerPanel = Global.getSettings().createCustom(width - 4f, 64f, null)

            val iconTooltip = innerPanel.createUIElement(64f, 64f, false)
            exoticData.addExoticIcon(iconTooltip)
            innerPanel.addUIElement(iconTooltip)

            val textTooltip = innerPanel.createUIElement(width - 68f, 64f, false)
            textTooltip.addTitle(exoticData.getNameTranslation().toStringNoFormats(), exotic.color)
            val title = textTooltip.prev
            exotic.modifyToolTip(textTooltip, title, member, this, exoticData, expandExotics)
            innerPanel.addUIElement(textTooltip).rightOfMid(iconTooltip, 4f)

            UIUtils.autoResize(textTooltip)
            innerPanel.position.setSize(width - 4f, textTooltip.position.height.coerceAtLeast(64f))

            tooltip.addCustom(innerPanel, 4f).position.belowLeft(lastThing, 8f)
            lastThing = innerPanel

            tooltip.setParaFontDefault()
            tooltip.setParaFontColor(tooltipColor)
        }
        return addedExoticSection
    }

    fun populateTooltip(
        member: FleetMemberAPI,
        mainTooltip: TooltipMakerAPI,
        width: Float,
        height: Float = 350f,
        expandUpgrades: Boolean,
        expandExotics: Boolean,
        noScroller: Boolean = false
    ) {
        populateTooltip(member, member.stats, mainTooltip, width, height, expandUpgrades, expandExotics, noScroller)
    }

    fun toJson(member: FleetMemberAPI): JSONObject {
        val obj = JSONObject()
        obj.put(BANDWIDTH_KEY, getBaseBandwidth(member).toDouble())

        if (hasUpgrades()) {
            obj.put(UPGRADES_KEY, upgrades.toJson())
        }

        if (hasExotics()) {
            obj.put(EXOTICS_KEY, exotics.toJson())
        }

        return obj
    }
}