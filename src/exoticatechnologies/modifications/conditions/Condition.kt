package exoticatechnologies.modifications.conditions

import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.ETExotics
import exoticatechnologies.modifications.upgrades.ETUpgrades
import org.json.JSONArray
import org.json.JSONObject

interface Condition {
    val key: String
    var cannotApplyReason: String?
    var weight: Float
    var weightOnly: Boolean

    fun setup(condition: JSONObject)
    fun compare(member: FleetMemberAPI, mods: ShipModifications?, chipKey: String?): Boolean
    fun calculateWeight(member: FleetMemberAPI, mods: ShipModifications?): Float
}

fun JSONArray.toList(): List<String> {
    val list: MutableList<String> = mutableListOf()
    for (i in 0 until this.length()) {
        list.add(this.get(i).toString())
    }
    return list
}

