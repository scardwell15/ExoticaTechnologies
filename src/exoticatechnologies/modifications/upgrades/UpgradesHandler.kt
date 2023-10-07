package exoticatechnologies.modifications.upgrades

import com.fs.starfarer.api.Global
import exoticatechnologies.ui.impl.shop.ShopManager.Companion.addMenu
import exoticatechnologies.ui.impl.shop.upgrades.UpgradeShopUIPlugin
import exoticatechnologies.ui.impl.shop.upgrades.methods.UpgradeMethod
import lombok.extern.log4j.Log4j
import org.apache.log4j.Logger
import org.json.JSONException
import org.json.JSONObject
import org.magiclib.util.MagicSettings
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

@Log4j
object UpgradesHandler {
    private val log = Logger.getLogger(UpgradesHandler.javaClass)

    @JvmField
    val UPGRADES: MutableMap<String, Upgrade> = HashMap()
    @JvmField
    val UPGRADES_LIST: MutableList<Upgrade> = ArrayList()
    @JvmField
    val UPGRADE_METHODS: MutableSet<UpgradeMethod> = LinkedHashSet()
    @JvmField
    val UPGRADES_BY_HINT: MutableMap<String, MutableSet<Upgrade>> = LinkedHashMap()

    fun addUpgradeMethod(method: UpgradeMethod) {
        UPGRADE_METHODS.add(method)
    }

    @JvmStatic
    fun initialize() {
        populateUpgrades()
        populateMethods()
        addMenu(UpgradeShopUIPlugin())
    }

    fun populateUpgrades() {
        UPGRADES.clear()

        val settings = Global.getSettings().getMergedJSONForMod("data/config/upgrades.json", "exoticatechnologies")
        val upgIterator = settings.keys()
        while (upgIterator.hasNext()) {
            val upgKey = upgIterator.next() as String
            if (UPGRADES.containsKey(upgKey)) continue
            var upgrade: Upgrade?
            try {
                val upgradeSettings = settings.getJSONObject(upgKey)
                if (upgradeSettings.has("upgradeClass")) {
                    val clzz =
                        Global.getSettings().scriptClassLoader.loadClass(upgradeSettings.getString("upgradeClass"))

                    //magic to get around reflection block
                    upgrade = MethodHandles.lookup().findConstructor(
                        clzz,
                        MethodType.methodType(Void.TYPE, String::class.java, JSONObject::class.java)
                    )
                        .invoke(upgKey, upgradeSettings) as Upgrade
                    if (!upgrade.shouldLoad()) {
                        upgrade = null
                    }
                } else {
                    upgrade = Upgrade(upgKey, upgradeSettings)
                    if (!upgrade.shouldLoad()) {
                        upgrade = null
                    }
                }
                if (upgrade != null) {
                    addUpgrade(upgrade)
                    log.info(String.format("loaded upgrade [%s]", upgrade.name))
                }
            } catch (ex: JSONException) {
                val logStr = String.format("Upgrade [%s] had an error.", upgKey)
                log.error(logStr)
                throw RuntimeException(logStr, ex)
            }
        }

        UPGRADES_LIST.clear()
        val orderedKeys = MagicSettings.getList("exoticatechnologies", "upgradeOrder")
        for (i in orderedKeys.indices) {
            val key = orderedKeys[i]
            UPGRADES[key]?.let {
                UPGRADES_LIST.add(it)
            }
        }
        for (upgrade in UPGRADES.values) {
            if (!orderedKeys.contains(upgrade.key)) {
                UPGRADES_LIST.add(upgrade)
            }
        }
    }

    fun populateMethods() {
        UPGRADE_METHODS.clear()
        try {
            val settings = Global.getSettings().getMergedSpreadsheetDataForMod("name", "data/config/upgradeMethods.csv", "exoticatechnologies")
            for (i in 0 until settings.length()) {
                val methodData = settings.getJSONObject(i)
                val methodClassPath = methodData.getString("class")
                val clzz =
                    Global.getSettings().scriptClassLoader.loadClass(methodClassPath)
                val method = MethodHandles.lookup().findConstructor(
                    clzz,
                    MethodType.methodType(Void.TYPE)
                )
                    .invoke() as UpgradeMethod

                if (method.shouldLoad()) {
                    UPGRADE_METHODS.add(method)
                }
            }
        } catch (ex: Throwable) {
            throw RuntimeException(ex)
        }
    }

    fun addUpgrade(upgrade: Upgrade) {
        if (UPGRADES.containsKey(upgrade.key)) return
        UPGRADES[upgrade.key] = upgrade

        upgrade.hints.forEach {
            UPGRADES_BY_HINT.getOrPut(it) { LinkedHashSet() }
                .add(upgrade)
        }
    }
}