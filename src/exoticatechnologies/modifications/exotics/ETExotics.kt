package exoticatechnologies.modifications.exotics

import com.fs.starfarer.api.fleet.FleetMemberAPI
import lombok.extern.log4j.Log4j
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@Log4j
class ETExotics {
    var exotics: List<String>? = null
    var exoticData: MutableMap<String, ExoticData> = HashMap()

    constructor() {
        fixExoticsList()
    }

    constructor(exotics: List<String>) {
        for (key in exotics) {
            this.putExotic(key)
        }
    }

    val list: Collection<String>
        get() {
            fixExoticsList()
            return ArrayList(exoticData.keys)
        }

    fun getData(exotic: Exotic): ExoticData? {
        return exoticData[exotic.key]
    }

    fun getCount(member: FleetMemberAPI): Int {
        return exoticData
            .filterValues { it.exotic.countsTowardsExoticLimit(member) }
            .count()
    }

    fun hasExotic(exotic: Exotic): Boolean {
        return this.hasExotic(exotic.key)
    }

    fun hasExotic(key: String): Boolean {
        fixExoticsList()
        return exoticData.containsKey(key)
    }

    fun putExotic(exoticData: ExoticData) {
        this.exoticData[exoticData.key] = exoticData
    }

    fun putExotic(key: String?) {
        putExotic(ExoticData(key!!))
    }

    fun removeExotic(key: String) {
        exoticData.remove(key)
    }

    fun removeExotic(exotic: Exotic) {
        exoticData.remove(exotic.key)
    }

    fun hasAnyExotic(): Boolean {
        fixExoticsList()
        return exoticData.isNotEmpty()
    }

    val tags: List<String>
        get() {
            val tagSet: MutableSet<String> = HashSet()
            for (data in exoticData.values) {
                val exotic = data.exotic
                tagSet.addAll(exotic.tags)
            }
            return ArrayList(tagSet)
        }

    fun getConflicts(tags: List<String>): List<Exotic> {
        val exotics: MutableList<Exotic> = ArrayList()
        for (data in exoticData.values) {
            val exotic = data.exotic
            exotic.tags
                .firstOrNull { tags.contains(it) }
                ?.let {
                    exotics.add(exotic)
                }
        }
        return exotics
    }

    fun fixExoticsList() {
        if (exotics != null) {
            for (exoticKey in exotics!!) {
                exoticData.getOrPut(exoticKey){ ExoticData(exoticKey) }
            }
            exotics = null
        }
    }

    override fun toString(): String {
        return "ETExotics{" +
                "exoticData=" + exoticData +
                '}'
    }

    fun toJson(): JSONObject? {
        val obj = JSONObject()
        exoticData.values.forEach {
            obj.put(it.key, it.toJson())
        }
        return obj
    }

    @Throws(JSONException::class)
    fun parseJson(obj: Any) {
        if (obj is JSONArray) {
            parseJsonArray(obj)
        } else if (obj is JSONObject) {
            parseJsonObj(obj)
        }
    }

    private fun parseJsonObj(obj: JSONObject) {
        for (key in obj.keys()) {
            val exoObj = obj.getJSONObject(key.toString())
            putExotic(ExoticData(exoObj))
        }
    }

    private fun parseJsonArray(obj: JSONArray) {
        for (i in 0 until obj.length()) {
            putExotic(obj.getString(i))
        }
    }
}