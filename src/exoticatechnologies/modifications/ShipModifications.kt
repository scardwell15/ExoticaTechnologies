package exoticatechnologies.modifications

import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.exotics.ETExotics
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticsGenerator
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.modifications.upgrades.ETUpgrades
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradesGenerator
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import org.apache.log4j.Logger

class ShipModifications(private var bandwidth: Float, var upgrades: ETUpgrades, var exotics: ETExotics) {
    constructor() : this(-1f, ETUpgrades(), ETExotics())

    companion object {
        @JvmStatic
        private val log = Logger.getLogger(Companion::class.java)
    }

    fun shouldApplyHullmod(): Boolean {
        return (upgrades.hasUpgrades()
                || exotics.hasAnyExotic())
    }

    /**
     * for a fleet member
     * @param fm
     * @param faction
     */
    fun generate(fm: FleetMemberAPI, faction: String?) {
        if (bandwidth == -1f) {
            bandwidth = ShipModFactory.generateBandwidth(fm, faction)
        }
        if (fm.fleetData != null && fm.fleetData.fleet.isPlayerFleet) {
            return
        }
        exotics = ExoticsGenerator.generate(fm, faction, this)
        upgrades = UpgradesGenerator.generate(fm, faction!!, this)
    }

    fun getValue(): Float {
        return bandwidth + exotics.list.size + upgrades.totalLevels
    }

    //bandwidth

    fun setBandwidth(bandwidth: Float) {
        this.bandwidth = bandwidth
    }

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
                returnedBandwidth += exotic.getExtraBandwidth(fm, this)
            }
        }
        return returnedBandwidth
    }

    fun canUpgradeBandwidth(fm: FleetMemberAPI): Boolean {
        var maxBandwidth = Bandwidth.MAX_BANDWIDTH
        for (exotic in ExoticsHandler.EXOTIC_LIST) {
            if (this.hasExotic(exotic)) {
                maxBandwidth += exotic.getExtraBandwidthPurchaseable(fm, this)
            }
        }
        return maxBandwidth > getBaseBandwidth(fm)
    }

    fun getUsedBandwidth(): Float {
        var usedBandwidth = 0f
        for (upgrade in UpgradesHandler.UPGRADES_LIST) {
            usedBandwidth += upgrade.getBandwidthUsage() * this.getUpgrade(upgrade)
        }
        return usedBandwidth
    }

    //exotics
    fun getExoticSet(): Set<Exotic> {
        val exoticSet: MutableSet<Exotic> = HashSet()
        for (exotic in exotics.list) {
            exoticSet.add(Exotic.get(exotic))
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

    fun putExotic(exotic: Exotic) {
        exotics.putExotic(exotic)
    }

    fun removeExotic(exotic: Exotic) {
        exotics.removeExotic(exotic)
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
        val upgradeBandwidth = (level - this.getUpgrade(upgrade)) * upgrade.getBandwidthUsage()
        return getUsedBandwidth() + upgradeBandwidth <= getBandwidthWithExotics(member)
    }

    fun getHullSizeFactor(hullSize: HullSize): Float {
        return upgrades.getHullSizeFactor(hullSize)
    }

    fun isMaxLevel(member: FleetMemberAPI, upgrade: Upgrade): Boolean {
        return this.getUpgrade(upgrade) >= upgrade.getMaxLevel(member.hullSpec.hullSize)
    }

    fun getTags(): List<String> {
        val tags: MutableList<String> = mutableListOf()
        tags.addAll(exotics.tags)
        tags.addAll(upgrades.tags)
        return tags
    }

    fun getModsThatConflict(tag: String): List<Modification> {
        if (tag.isEmpty()) {
            return mutableListOf()
        }

        val mods: MutableList<Modification> = mutableListOf()
        mods.addAll(exotics.getConflicts(tag))
        mods.addAll(upgrades.getConflicts(tag))
        return mods
    }

    override fun toString(): String {
        return "ShipModifications{" +
                "bandwidth=" + bandwidth +
                ", modules=" + exotics +
                ", upgrades=" + upgrades +
                '}'
    }
}