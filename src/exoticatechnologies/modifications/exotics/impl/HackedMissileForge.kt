package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.json.JSONObject
import java.awt.Color

class HackedMissileForge(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color(0xFF8902)
    override fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI?): Boolean {
        return (Utilities.hasItem(fleet.cargo, ITEM)
                && fleet.cargo.credits.get() >= COST_CREDITS)
    }

    override fun removeItemsFromFleet(fleet: CampaignFleetAPI, member: FleetMemberAPI, market: MarketAPI?): Boolean {
        fleet.cargo.credits.subtract(150000f)
        Utilities.takeItemQuantity(fleet.cargo, ITEM, 1f)
        return true
    }

    override fun getSalvageChance(chanceMult: Float): Float {
        return 0.025f * chanceMult
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
            StringUtils.getTranslation(key, "longDescription")
                .format("damageDecrease", DAMAGE_DECREASE * getNegativeMult(member, mods, exoticData))
                .formatFloat("reloadTime", getReloadTime(member, mods, exoticData))
                .addToTooltip(tooltip, title)
        }
    }

    private fun getReloadTime(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        //return SECONDS_PER_RELOAD / (1f + ((getPositiveMult(member, mods, exoticData) - 1f) * 0.75f))
        return SECONDS_PER_RELOAD / getPositiveMult(member, mods, exoticData)
    }


    override fun applyExoticToStats(
        id: String,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        stats.missileWeaponDamageMult.modifyPercent(
            buffId,
            -DAMAGE_DECREASE * getNegativeMult(member, mods, exoticData)
        )
    }

    override fun applyToShip(
        id: String,
        member: FleetMemberAPI,
        ship: ShipAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        for (weapon in ship.allWeapons) {
            if (shouldAffectWeapon(weapon)) {
                val maxAmmo = weapon.ammoTracker.maxAmmo
                weapon.ammoTracker.reloadSize = maxAmmo.toFloat()
                weapon.ammoTracker.ammoPerSecond = weapon.ammoTracker.reloadSize / getReloadTime(member, mods, exoticData)
                weapon.ammoTracker.resetAmmo()
            }
        }
    }

    private fun shouldAffectWeapon(weapon: WeaponAPI): Boolean {
        return weapon.slot != null
                && (weapon.spec.mountType == WeaponAPI.WeaponType.MISSILE || weapon.type == WeaponAPI.WeaponType.MISSILE || weapon.spec.type == WeaponAPI.WeaponType.MISSILE || weapon.slot.weaponType == WeaponAPI.WeaponType.MISSILE)
                && !weapon.spec.hasTag(Tags.NO_RELOAD)
                && weapon.spec.maxAmmo > 1
                && weapon.ammoTracker != null
                && weapon.ammoTracker.usesAmmo()
                && weapon.ammoTracker.ammoPerSecond == 0f
    }

    companion object {
        private const val ITEM = "et_hangarforge"
        private const val COST_CREDITS = 150000f
        private const val SECONDS_PER_RELOAD = 60f
        private const val DAMAGE_DECREASE = 40f
        private val blacklistedWeapons: Set<String> = HashSet()
    }
}