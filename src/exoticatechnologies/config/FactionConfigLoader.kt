package exoticatechnologies.config

import data.scripts.util.MagicSettings
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradesHandler

class FactionConfigLoader {
    companion object {
        const val SETTINGS_PATH = "data/config/exoticaFactionConfig/%s.json"
        private var inst = FactionConfigLoader()

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
        fun getDefaultFactionUpgrades(): Map<Upgrade, Float> {
            return inst.defaultFactionUpgrades
        }

        @JvmStatic
        fun load() {
            inst.loadFactionConfigs()
        }
    }

    private var factionMap: MutableMap<String, FactionConfig> = mutableMapOf()
    private var defaultFactionExotics: MutableMap<Exotic, Float> = mutableMapOf()
    private var defaultFactionUpgrades: MutableMap<Upgrade, Float> = mutableMapOf()

    private fun loadFactionConfigs() {
        factionMap.clear()
        defaultFactionExotics.clear()
        defaultFactionUpgrades.clear()

        MagicSettings.getList("exoticatechnologies", "rngExoticWhitelist")
            .mapNotNull { ExoticsHandler.EXOTICS[it] }
            .forEach { defaultFactionExotics[it] = it.getSalvageChance(1f) }

        MagicSettings.getList("exoticatechnologies", "rngUpgradeWhitelist")
            .mapNotNull { UpgradesHandler.UPGRADES[it] }
            .forEach { defaultFactionUpgrades[it] = it.getSpawnChance() }

        MagicSettings.getList("exoticatechnologies", "factionsWithConfigs")
            .map { FactionConfig(it) }
            .forEach {
                factionMap[it.factionId] = it
            }

    }
}