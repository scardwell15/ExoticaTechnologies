package exoticatechnologies.config

import com.fs.starfarer.api.Global
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import org.lazywizard.lazylib.ext.json.getFloat
import org.magiclib.util.MagicSettings

object VariantConfigLoader {
    fun loadConfigs() {
        val exoticaSettings = MagicSettings.modSettings.getJSONObject("exoticatechnologies")
        if (exoticaSettings.has("variantWeights")) {
            val variantWeights = exoticaSettings.getJSONObject("variantWeights")
            variantWeights.keys().forEach { variantId ->
                variantId as String
                if (Global.getSettings().getVariant(variantId) != null) {
                    val weights = variantWeights.getJSONObject(variantId)

                    weights.optJSONObject("upgrades")?.let { upgradeWeights ->
                        upgradeWeights.keys().forEach { upgradeKey ->
                            upgradeKey as String
                            UpgradesHandler.UPGRADES[upgradeKey]?.variantScales?.put(variantId, upgradeWeights.getFloat(upgradeKey))
                        }
                    }
                    weights.optJSONObject("exotics")?.let { exoticWeights ->
                        exoticWeights.keys().forEach { exoticKey ->
                            exoticKey as String
                            ExoticsHandler.EXOTICS[exoticKey]?.variantScales?.put(variantId, exoticWeights.getFloat(exoticKey))
                        }
                    }
                }
            }
        }
    }
}