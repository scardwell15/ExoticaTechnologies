package exoticatechnologies.campaign.market

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import exoticatechnologies.ETModSettings
import exoticatechnologies.config.FactionConfigLoader
import exoticatechnologies.modifications.exotics.ExoticsGenerator
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.modifications.upgrades.UpgradesGenerator
import org.lazywizard.lazylib.campaign.CampaignUtils
import org.magiclib.kotlin.elapsedDaysSinceGameStart
import org.magiclib.util.MagicCampaign
import kotlin.math.roundToInt

class MarketData(market: MarketAPI) {
    val marketId: String = market.id
    var factionId: String = market.factionId

    val market: MarketAPI
        get() = Global.getSector().economy.getMarket(marketId)
    val faction: FactionAPI
        get() = Global.getSector().getFaction(factionId)
    val generatedTimestamp: Float = Global.getSector().clock.elapsedDaysSinceGameStart()

    var cargo: CargoAPI = Global.getFactory().createCargo(true)

    init {
        val config = FactionConfigLoader.getFactionConfig(factionId)

        val exoticAmount: Int = (MarketManager.random.nextFloat() * 2 * ETModSettings.getFloat(ETModSettings.MARKET_EXOTIC_SCALE)).roundToInt() + 1

        for (i in 1..exoticAmount) {
            val exoticPicker = ExoticsGenerator.getExoticPicker(MarketManager.random, config.allowedExotics)
            val exotic = exoticPicker.pick()
            var type = ExoticType.NORMAL

            if (config.exoticTypeChance >= MarketManager.random.nextFloat()) {
                val typePicker = ExoticsGenerator.getTypePicker(MarketManager.random, exotic, config.allowedExoticTypes)
                type = typePicker.pick()
            }

            val exoticItemData = exotic.getNewSpecialItemData(type)
            cargo.addSpecial(exoticItemData, 1f)
        }

        val upgradeAmount: Int = (MarketManager.random.nextFloat() * 7 * ETModSettings.getFloat(ETModSettings.MARKET_UPGRADE_SCALE)).roundToInt() + 5

        for (i in 1..upgradeAmount) {
            val upgradePicker = UpgradesGenerator.getPicker(MarketManager.random, config.allowedUpgrades)
            val upgrade = upgradePicker.pick()
            var level = 1

            while (MarketManager.random.nextFloat() <= 0.4f && upgrade.maxLevel > level) {
                level++
            }

            val upgradeItemData = upgrade.getNewSpecialItemData(level)
            cargo.addSpecial(upgradeItemData, 1f)
        }
    }
}