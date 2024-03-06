package exoticatechnologies.modifications.upgrades.impl

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.util.StringUtils
import org.json.JSONObject
import org.magiclib.subsystems.MagicSubsystem
import org.magiclib.subsystems.MagicSubsystemsManager
import org.magiclib.subsystems.drones.MagicDroneSubsystem

class WaspDefenseDrones(key: String, settings: JSONObject) : Upgrade(key, settings) {
    override var maxLevel: Int = 1

    override fun applyToShip(member: FleetMemberAPI, ship: ShipAPI, mods: ShipModifications) {
        MagicSubsystemsManager.addSubsystemToShip(ship, WaspDroneActivator(ship))
    }

    override fun shouldAffectModule(ship: ShipAPI?, module: ShipAPI?): Boolean {
        return false
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
            StringUtils.getTranslation("WaspDefenseDrones", "tooltip")
                .format("drones", WaspDroneActivator.maxDronesMap[member.hullSpec.hullSize] ?: 1)
                .addToTooltip(imageText)
        }
        tooltip.addImageWithText(5f)

        return imageText
    }

    override fun showStatsInShop(tooltip: TooltipMakerAPI, member: FleetMemberAPI, mods: ShipModifications) {
        StringUtils.getTranslation("WaspDefenseDrones", "tooltip")
            .format("drones", WaspDroneActivator.maxDronesMap[member.hullSpec.hullSize] ?: 1)
            .addToTooltip(tooltip)
    }

    class WaspDroneActivator(ship: ShipAPI) : MagicDroneSubsystem(ship) {
        companion object {
            val maxDronesMap: Map<ShipAPI.HullSize, Int> = mapOf(
                ShipAPI.HullSize.FRIGATE to 4,
                ShipAPI.HullSize.DESTROYER to 4,
                ShipAPI.HullSize.CRUISER to 5,
                ShipAPI.HullSize.CAPITAL_SHIP to 6
            )
        }

        override fun canAssignKey(): Boolean {
            return false
        }

        override fun getBaseActiveDuration(): Float {
            return 0f
        }

        override fun getBaseCooldownDuration(): Float {
            return 0f
        }

        override fun shouldActivateAI(amount: Float): Boolean {
            return canActivate()
        }

        override fun getBaseChargeRechargeDuration(): Float {
            return 20f
        }

        override fun canActivate(): Boolean {
            return false
        }

        override fun getDisplayText(): String {
            return "Wasp Drones"
        }

        override fun getStateText(): String {
            return ""
        }

        override fun getBarFill(): Float {
            var fill = 0f
            if (charges < maxCharges) {
                fill = chargeInterval.elapsed / chargeInterval.intervalDuration
            }
            return fill
        }

        override fun getMaxCharges(): Int {
            return maxDronesMap[ship.hullSize] ?: 2
        }

        override fun getMaxDeployedDrones(): Int {
            return maxDronesMap[ship.hullSize] ?: 1
        }

        override fun usesChargesOnActivate(): Boolean {
            return false
        }

        override fun getDroneVariant(): String {
            return "wasp_single_wing"
        }
    }
}