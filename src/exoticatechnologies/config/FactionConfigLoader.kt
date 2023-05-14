package exoticatechnologies.config

import exoticatechnologies.ETModSettings
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import org.magiclib.util.MagicSettings

class FactionConfigLoader {
    companion object {
        const val SETTINGS_PATH = "data/config/exoticaFactionConfig/%s.json"
        private var inst = FactionConfigLoader()

        @JvmStatic
        val useTheMethodThatMakesHartleyverseStronger = false

        @JvmStatic
        fun getFactionConfig(factionId: String): FactionConfig {
            var factionConfig = inst.factionMap[factionId]
            if (factionConfig == null) {
                factionConfig = FactionConfig(factionId, false)
                inst.factionMap[factionId] = factionConfig
            }
            return factionConfig
        }

        @JvmStatic
        fun getDefaultFactionExotics(): Map<Exotic, Float> {
            return inst.defaultFactionExotics
        }

        @JvmStatic
        fun getDefaultFactionExoticTypes(): Map<ExoticType, Float> {
            return inst.defaultFactionExoticTypes
        }

        @JvmStatic
        fun getDefaultFactionMaxExotics(): Int {
            return inst.defaultMaxExotics
        }

        @JvmStatic
        fun getDefaultFactionUpgrades(): Map<Upgrade, Float> {
            return inst.defaultFactionUpgrades
        }

        fun getDefaultFactionExoticTypeChance(): Float {
            return inst.defaultFactionExoticTypeChance
        }

        @JvmStatic
        fun load() {
            inst.loadFactionConfigs()
        }
    }

    private var factionMap: MutableMap<String, FactionConfig> = mutableMapOf()
    private var defaultFactionExotics: MutableMap<Exotic, Float> = mutableMapOf()
    private var defaultFactionExoticTypes: MutableMap<ExoticType, Float> = mutableMapOf()
    private var defaultFactionUpgrades: MutableMap<Upgrade, Float> = mutableMapOf()
    private var defaultFactionExoticTypeChance = 0.05f
    private var defaultMaxExotics = ETModSettings.MAX_EXOTICS

    private fun loadFactionConfigs() {
        factionMap.clear()
        defaultFactionExotics.clear()
        defaultFactionExoticTypes.clear()
        defaultFactionUpgrades.clear()

        MagicSettings.getList("exoticatechnologies", "rngExoticWhitelist")
            .mapNotNull { ExoticsHandler.EXOTICS[it] }
            .forEach { defaultFactionExotics[it] = it.getSalvageChance(1f) }

        MagicSettings.getFloatMap("exoticatechnologies", "rngExoticTypeWhitelist")
            .mapNotNull { ExoticType.valueOf(it.key) to it.value }
            .forEach { defaultFactionExoticTypes[it.first] = it.second }

        MagicSettings.getList("exoticatechnologies", "rngUpgradeWhitelist")
            .mapNotNull { UpgradesHandler.UPGRADES[it] }
            .forEach { defaultFactionUpgrades[it] = it.spawnChance }

        MagicSettings.getList("exoticatechnologies", "factionsWithConfigs")
            .map { FactionConfig(it) }
            .forEach {
                factionMap[it.factionId] = it
            }

    }
}