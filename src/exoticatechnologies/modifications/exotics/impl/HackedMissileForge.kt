package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.IntervalUtil
import data.scripts.util.MagicUI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.json.JSONObject
import java.awt.Color

class HackedMissileForge(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color(0xFF8902)
    override fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI): Boolean {
        return (Utilities.hasItem(fleet.cargo, ITEM)
                && fleet.cargo.credits.get() >= COST_CREDITS)
    }

    override fun removeItemsFromFleet(fleet: CampaignFleetAPI, member: FleetMemberAPI, market: MarketAPI): Boolean {
        fleet.cargo.credits.subtract(150000f)
        Utilities.takeItemQuantity(fleet.cargo, ITEM, 1f)
        return true
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

    private fun getReloadId(ship: ShipAPI): String {
        return String.format("%s%s_reload", buffId, ship.id)
    }

    private fun getReloadInterval(ship: ShipAPI): IntervalUtil? {
        val `val` = Global.getCombatEngine().customData[getReloadId(ship)]
        return if (`val` != null) {
            `val` as IntervalUtil?
        } else null
    }

    private fun createReloadInterval(
        ship: ShipAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ): IntervalUtil {
        val time = getReloadTime(member, mods, exoticData)
        val interval = IntervalUtil(time, time)
        Global.getCombatEngine().customData[getReloadId(ship)] = interval
        return interval
    }

    override fun advanceInCombatAlways(ship: ShipAPI, member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData) {
        val reloadInterval = getReloadInterval(ship)
        if (reloadInterval != null) {
            MagicUI.drawInterfaceStatusBar(
                ship,
                reloadInterval.elapsed / reloadInterval.intervalDuration,
                RenderUtils.getAliveUIColor(),
                RenderUtils.getAliveUIColor(),
                0f,
                StringUtils.getString(key, "statusBarText"),
                -1
            )
        }
    }

    override fun advanceInCombatUnpaused(
        ship: ShipAPI,
        amount: Float,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        var reloadInterval = getReloadInterval(ship)
        if (reloadInterval == null) {
            reloadInterval = createReloadInterval(ship, member, mods, exoticData)
        }
        if (Global.getCombatEngine().isPaused) {
            return
        }

        reloadInterval.advance(amount)
        if (reloadInterval.intervalElapsed()) {
            var addedAmmo = false
            for (weapon in ship.allWeapons) {
                if (shouldAffectWeapon(weapon)) {
                    val ammo = weapon.ammoTracker.ammo
                    val maxAmmo = weapon.ammoTracker.maxAmmo
                    if (ammo < maxAmmo) {
                        weapon.ammoTracker.ammo = maxAmmo
                        addedAmmo = true
                    }
                }
            }

            if (addedAmmo) {
                Global.getCombatEngine().addFloatingText(
                    ship.location,
                    StringUtils.getString(key, "statusReloaded"),
                    8f,
                    Color.WHITE,
                    ship,
                    2f,
                    2f
                )
            } else {
                reloadInterval.setInterval(10f, 10f)
            }
        }
    }

    private fun shouldAffectWeapon(weapon: WeaponAPI): Boolean {
        return weapon.ammoTracker != null && weapon.ammoTracker.usesAmmo() && weapon.ammoTracker.maxAmmo > 1 && weapon.ammoTracker.ammoPerSecond == 0f && weapon.type == WeaponAPI.WeaponType.MISSILE
    }

    companion object {
        private const val ITEM = "et_hangarforge"
        private const val COST_CREDITS = 150000f
        private const val SECONDS_PER_RELOAD = 60f
        private const val DAMAGE_DECREASE = 25f
        private val blacklistedWeapons: Set<String> = HashSet()
    }
}