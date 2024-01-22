package exoticatechnologies.campaign.market

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import org.magiclib.kotlin.elapsedDaysSinceGameStart
import java.util.*
import kotlin.collections.HashMap

object MarketManager {
    val random = Random()

    fun initialize() {
        random.setSeed(Global.getSector().seedString.hashCode().toLong())
        if (!Global.getSector().persistentData.containsKey("exoticaMarketData")) {
            Global.getSector().persistentData["exoticaMarketData"] = HashMap<String, MarketData>()
        }
    }

    fun clearData() {
        getData().clear()
    }
    private fun getData(): HashMap<String, MarketData> {
        return Global.getSector().persistentData["exoticaMarketData"] as HashMap<String, MarketData>
    }

    fun getDataForMarket(market: MarketAPI): MarketData {
        var marketData = getData()[market.id]
        if (marketData != null && Global.getSector().clock.elapsedDaysSinceGameStart() > marketData.generatedTimestamp + 30f) {
            marketData = null //refresh market data
        }

        if (marketData == null) {
            marketData = MarketData(market)
            getData()[market.id] = marketData
        }
        return marketData
    }
}