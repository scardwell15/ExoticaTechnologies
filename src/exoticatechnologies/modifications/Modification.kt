package exoticatechnologies.modifications

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import org.json.JSONObject
import java.awt.Color

abstract class Modification(val key: String, val settings: JSONObject) {
    var name: String = settings.getString("name")
    open var color: Color = Color.white
    protected abstract val icon: String

    open fun shouldLoad(): Boolean {
        return true
    }

    open fun shouldShow(member: FleetMemberAPI, mods: ShipModifications, market: MarketAPI): Boolean {
        return true
    }

    open fun shouldShow(): Boolean {
        return true
    }

    open fun canApply(member: FleetMemberAPI): Boolean {
        return canApply(member.variant)
    }

    open fun canApply(variant: ShipVariantAPI): Boolean {
        return true
    }
}