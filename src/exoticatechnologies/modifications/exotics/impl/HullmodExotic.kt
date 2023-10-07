package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.refit.checkRefitVariant
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.getRefitVariant
import org.json.JSONObject
import java.awt.Color

open class HullmodExotic(
    key: String,
    settingsObj: JSONObject,
    private val hullmodId: String,
    private val statDescriptionKey: String,
    override var color: Color
) : Exotic(key, settingsObj) {
    override fun onInstall(member: FleetMemberAPI) {
        installOnVariant(member.variant)
        installOnVariant(member.checkRefitVariant())
    }

    fun installOnVariant(variant: ShipVariantAPI?) {
        variant?.let {
            variant.addPermaMod(hullmodId)
        }
    }

    override fun onDestroy(member: FleetMemberAPI) {
        removeFromVariant(member.variant)
        removeFromVariant(member.checkRefitVariant())

        val check = member.checkRefitVariant().hasHullMod(hullmodId)
        println("$check has mod still")
    }

    private fun removeFromVariant(variant: ShipVariantAPI?) {
        variant?.let {
            variant.removePermaMod(hullmodId)
            variant.removeMod(hullmodId)
            variant.removePermaMod(hullmodId)
            variant.addSuppressedMod(hullmodId)
            variant.removeMod(hullmodId)
            variant.removePermaMod(hullmodId)
            variant.removeMod(hullmodId)
            variant.removeSuppressedMod(hullmodId)
            variant.removePermaMod(hullmodId)
            variant.removeMod(hullmodId)
            variant.removePermaMod(hullmodId)
        }
    }



    override fun applyExoticToStats(
        id: String,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        onInstall(member)
    }

    override fun applyToShip(
        id: String,
        member: FleetMemberAPI,
        ship: ShipAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        onInstall(member)
    }

    override fun modifyToolTip(
        tooltip: TooltipMakerAPI,
        title: UIComponentAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData,
        expand: Boolean
    ) {
        if (expand) {
            StringUtils.getTranslation(key, statDescriptionKey)
                .addToTooltip(tooltip, title)
        }
    }
}