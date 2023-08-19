package exoticatechnologies.modifications.exotics

import com.fs.starfarer.api.Global
import exoticatechnologies.modifications.exotics.impl.HullmodExotic
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import exoticatechnologies.ui.impl.shop.ShopManager.Companion.addMenu
import exoticatechnologies.ui.impl.shop.exotics.ExoticShopUIPlugin
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import lombok.extern.log4j.Log4j
import org.apache.log4j.Logger
import org.json.JSONException
import org.json.JSONObject
import org.magiclib.util.MagicSettings
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

@Log4j
object ExoticsHandler {
    private val log = Logger.getLogger(ExoticsHandler.javaClass)

    @JvmField
    val EXOTICS: MutableMap<String, Exotic> = HashMap()

    @JvmField
    val EXOTIC_LIST: MutableList<Exotic> = ArrayList()

    @JvmField
    val EXOTICS_BY_HINT: MutableMap<String, MutableSet<Exotic>> = LinkedHashMap()

    @JvmStatic
    fun initialize() {
        populateExotics()
        addMenu(ExoticShopUIPlugin())
    }

    fun populateExotics() {
        try {
            val settings = Global.getSettings().getMergedJSONForMod("data/config/exotics.json", "exoticatechnologies")
            val augIterator = settings.keys()
            while (augIterator.hasNext()) {
                val exoticKey = augIterator.next() as String
                if (EXOTICS.containsKey(exoticKey)) return
                val exoticSettings = settings.getJSONObject(exoticKey)
                var exotic: Exotic? = null
                if (exoticSettings.has("exoticClass")) {
                    val clzz = Global.getSettings().scriptClassLoader.loadClass(exoticSettings.getString("exoticClass"))

                    //magic to get around reflection block
                    exotic = MethodHandles.lookup().findConstructor(
                        clzz,
                        MethodType.methodType(Void.TYPE, String::class.java, JSONObject::class.java)
                    )
                        .invoke(exoticKey, exoticSettings) as Exotic
                    if (exotic.shouldLoad()) {
                        exotic!!.loreDescription = StringUtils.getString(exoticKey, "description")
                        exotic.description = StringUtils.getString(exoticKey, "tooltip")
                    } else {
                        exotic = null
                    }
                } else if (exoticSettings.has("exoticHullmod")) {
                    val colorArr = exoticSettings.optJSONArray("exoticColor")
                    val mainColor = Utilities.colorFromJSONArray(colorArr)
                    val hullmodId = exoticSettings.getString("exoticHullmod")
                    val exoticStatDescriptionStringKey = exoticSettings.getString("exoticStatDescriptionStringKey")
                    exotic =
                        HullmodExotic(exoticKey, exoticSettings, hullmodId, exoticStatDescriptionStringKey, mainColor)
                    exotic.loreDescription = StringUtils.getString(exoticKey, "description")
                    exotic.description = StringUtils.getString(exoticKey, "tooltip")
                }
                if (exotic != null) {
                    addExotic(exotic)
                    log.info(String.format("loaded exotic [%s]", exotic.name))
                }
            }
        } catch (ex: Throwable) {
            throw RuntimeException(ex)
        }
    }

    fun addExotic(exotic: Exotic) {
        EXOTICS[exotic.key] = exotic
        EXOTIC_LIST.add(exotic)

        exotic.hints.forEach {
            EXOTICS_BY_HINT.getOrPut(it) { LinkedHashSet() }
                .add(exotic)
        }

        log.info(String.format("initialized exotic [%s]", exotic.name))
    }

    //whitelist for faction
    fun getWhitelistForFaction(faction: String): List<String> {
        var factionAllowedExotics = MagicSettings.getList("exoticatechnologies", "rngExoticWhitelist")
        try {
            if (MagicSettings.modSettings.getJSONObject("exoticatechnologies").has(faction + "_ExoticWhitelist")) {
                factionAllowedExotics = MagicSettings.getList("exoticatechnologies", faction + "_ExoticWhitelist")
            }
        } catch (ex: JSONException) {
            log.info("ET modSettings object doesn't exist. Is this a bug in MagicLib, or did you remove it?")
            log.info("The actual exception follows.", ex)
        }
        return factionAllowedExotics
    }
}