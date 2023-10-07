package exoticatechnologies.modifications.upgrades.impl

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.util.StringUtils
import org.json.JSONObject
import java.awt.Color

class Kingslayer(key: String, settings: JSONObject): Upgrade(key, settings) {
    override var canDropFromCombat: Boolean = false

    override fun applyUpgradeToStats(
        stats: MutableShipStatsAPI,
        fm: FleetMemberAPI,
        mods: ShipModifications,
        level: Int
    ) {
        stats.damageToCapital.modifyPercent(key, getDamageToCapitals(fm, level))
        stats.damageToCruisers.modifyPercent(key, getDamageToCruisers(fm, level))
    }

    private fun getDamageToCruisers(member: FleetMemberAPI, level: Int): Float {
        return HULLSIZE_DAMAGE_MAP[member.hullSpec.hullSize]!! * 1f * level
    }

    private fun getDamageToCapitals(member: FleetMemberAPI, level: Int): Float {
        return HULLSIZE_DAMAGE_MAP[member.hullSpec.hullSize]!! * 2f * level
    }

    override fun showDescriptionInShop(tooltip: TooltipMakerAPI, member: FleetMemberAPI, mods: ShipModifications) {
        val italicsLabel = StringUtils.getTranslation(key, "italics")
            .addToTooltip(tooltip)
        italicsLabel.italicize()
        italicsLabel.setColor(Color.GRAY)

        StringUtils.getTranslation(key, "description")
            .addToTooltip(tooltip)
    }

    override fun modifyToolTip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        expand: Boolean
    ): TooltipMakerAPI {
        val imageText = tooltip.beginImageWithText(iconPath, 64f)
        imageText.addPara("$name (%s)", 0f, color, mods.getUpgrade(this).toString())
        if (expand) {
            val italicsLabel = StringUtils.getTranslation(key, "italics")
                .addToTooltip(imageText)
            italicsLabel.italicize()
            italicsLabel.setColor(Color.GRAY)
            StringUtils.getTranslation(key, "description")
                .addToTooltip(imageText)
        }
        tooltip.addImageWithText(5f)

        return imageText
    }

    override fun showStatsInShop(tooltip: TooltipMakerAPI, member: FleetMemberAPI, mods: ShipModifications) {
        StringUtils.getTranslation(key, "tooltip")
            .format("damageToCruisers", getDamageToCruisers(member, mods.getUpgrade(this)))
            .format("damageToCapitals", getDamageToCapitals(member, mods.getUpgrade(this)))
            .addToTooltip(tooltip)
    }


    companion object {
        private val HULLSIZE_DAMAGE_MAP = mutableMapOf(ShipAPI.HullSize.FRIGATE to 0.05f, ShipAPI.HullSize.DESTROYER to 0.025f)
    }
}