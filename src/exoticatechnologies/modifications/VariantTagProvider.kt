package exoticatechnologies.modifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.exotics.ETExotics
import exoticatechnologies.modifications.upgrades.ETUpgrades
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.WeakHashMap

open class VariantTagProvider : ShipModLoader.Provider {
    companion object {
        @JvmStatic
        var inst: VariantTagProvider = VariantTagProvider()
    }

    var currGets: Int = 0
    val maxGetsPerMember: Int = 10

    val cache: MutableMap<FleetMemberAPI, ShipModifications> = WeakHashMap()
    val EXOTICA_INDICATOR = "$\$EXOTICA$$"
    val UPGRADES_KEY = "upgrades"
    val EXOTICS_KEY = "exotics"
    val BANDWIDTH_KEY = "baseBandwidth"

    override fun get(member: FleetMemberAPI): ShipModifications? {
        val members: Int = Global.getSector()?.playerFleet?.numMembersFast ?: 0
        if (currGets++ >= maxGetsPerMember * members) {
            cache.clear()
            currGets = 0
        }

        val cacheMods: ShipModifications? = cache.keys
            .filter { it == member }
            .firstNotNullOfOrNull { cache[it] }

        if (cacheMods != null) {
            return cacheMods
        }

        val variant: ShipVariantAPI = member.variant
            ?: return null

        getFromVariant(variant)?.let {
            if (Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT || Global.getSector().campaignUI.currentCoreTab == CoreUITabId.FLEET) {
                return it
            } else {
                cache[member] = it
            }
            return it
        }
        return null
    }

    override fun set(member: FleetMemberAPI, mods: ShipModifications) {
        remove(member)
        member.variant.addTag(EXOTICA_INDICATOR + convertToJson(member, mods))
        cache[member] = mods
    }

    override fun remove(member: FleetMemberAPI) {
        removeFromTags(member.variant)
    }

    private fun removeFromTags(variant: ShipVariantAPI) {
        variant.tags.removeAll { it.startsWith(EXOTICA_INDICATOR) }
    }

    fun getFromVariant(variant: ShipVariantAPI): ShipModifications? {
        val exoticaTag: String? = variant.tags.firstOrNull { it.startsWith(EXOTICA_INDICATOR) }

        if (exoticaTag != null) {
            val jsonStr = exoticaTag.replace(EXOTICA_INDICATOR, "")
            return convertFromJson(jsonStr)
        }

        return null
    }

    fun convertFromJson(json: String): ShipModifications {
        try {
            val modsObj = JSONObject(json)

            val upgrades = ETUpgrades()
            if (modsObj.has(UPGRADES_KEY)) {
                val upgObj = modsObj.getJSONObject(UPGRADES_KEY)

                upgObj.keys().forEach {
                    upgrades.putUpgrade(it as String, upgObj.getInt(it))
                }
            }

            val exotics = ETExotics()
            if (modsObj.has(EXOTICS_KEY)) {
                val exoObj = modsObj.getJSONArray(EXOTICS_KEY)
                for (i in 0 until exoObj.length()) {
                    exotics.putExotic(exoObj.getString(i))
                }
            }

            return ShipModifications(
                modsObj.optDouble(BANDWIDTH_KEY, -1.0).toFloat(),
                upgrades,
                exotics
            )
        } catch (ex: JSONException) {
            throw RuntimeException("Failed to load Exotica modifications object from variant tags.", ex)
        }
    }

    fun convertToJson(member:FleetMemberAPI, mods: ShipModifications): String {
        try {
            val savedObj = JSONObject()
            savedObj.put(BANDWIDTH_KEY, mods.getBaseBandwidth(member).toDouble())
            if (mods.hasUpgrades()) {
                savedObj.put(UPGRADES_KEY, JSONObject(mods.upgrades.map))
            }

            if (mods.hasExotics()) {
                savedObj.put(EXOTICS_KEY, JSONArray(mods.exotics.list))
            }

            return savedObj.toString()
        } catch (ex: JSONException) {
            throw RuntimeException("Failed to save Exotica object.")
        }
    }
}