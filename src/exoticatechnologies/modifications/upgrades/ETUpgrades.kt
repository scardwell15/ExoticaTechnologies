package exoticatechnologies.modifications.upgrades

import lombok.extern.log4j.Log4j
import org.json.JSONException
import org.json.JSONObject

@Log4j
class ETUpgrades {
    private val upgrades: MutableMap<String, Int>

    constructor() {
        upgrades = HashMap()
    }

    constructor(upgrades: MutableMap<String, Int>) {
        this.upgrades = upgrades
    }

    val map: Map<String, Int>
        get() = upgrades

    fun getUpgrade(upgrade: Upgrade): Int {
        return this.getUpgrade(upgrade.key)
    }

    fun getUpgrade(key: String): Int {
        return if (upgrades.containsKey(key)) {
            upgrades[key]!!
        } else 0
    }

    fun putUpgrade(upgrade: Upgrade) {
        this.putUpgrade(upgrade.key)
    }

    fun putUpgrade(upgrade: Upgrade, level: Int) {
        this.putUpgrade(upgrade.key, level)
    }

    fun addUpgrades(upgrade: Upgrade, levels: Int) {
        this.putUpgrade(upgrade.key, this.getUpgrade(upgrade.key) + levels)
    }

    @JvmOverloads
    fun putUpgrade(key: String, level: Int = getUpgrade(key) + 1) {
        upgrades[key] = level
    }

    fun hasUpgrades(): Boolean {
        return upgrades.isNotEmpty()
    }

    fun removeUpgrade(upgrade: Upgrade) {
        upgrades.remove(upgrade.key)
    }

    val totalLevels: Int
        get() {
            var size = 0
            for (value in upgrades.values) {
                size += value
            }
            return size
        }
    val tags: List<String>
        get() {
            val tagSet: MutableSet<String> = HashSet()
            for (key in upgrades.keys) {
                val upgrade = UpgradesHandler.UPGRADES[key]
                tagSet.addAll(upgrade!!.tags)
            }
            return ArrayList(tagSet)
        }

    fun getConflicts(tags: List<String>): List<Upgrade> {
        val conflicts: MutableList<Upgrade> = ArrayList()
        for (key in upgrades.keys) {
            val upgrade = UpgradesHandler.UPGRADES[key]!!
            upgrade.tags
                .firstOrNull { tags.contains(it) }
                ?.let {
                    conflicts.add(upgrade)
                }
        }
        return conflicts
    }

    override fun toString(): String {
        return "ETUpgrades{" +
                "upgrades=" + upgrades +
                '}'
    }

    fun toJson(): JSONObject {
        return JSONObject(map)
    }

    @Throws(JSONException::class)
    fun parseJson(obj: JSONObject) {
        val it = obj.keys()
        while (it.hasNext()) {
            val key = it.next().toString()
            putUpgrade(key, obj.getInt(key))
        }
    }
}