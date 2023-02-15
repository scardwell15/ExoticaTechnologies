package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.ExoticData;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DaemonCore extends HullmodExotic {
    private static final String ITEM = "tahlan_daemoncore";

    public DaemonCore(String key, JSONObject settingsObj) {
        super(key, settingsObj, "et_daemoncore", "DaemonCore", new Color(150,20,20));
    }

    @Override
    public boolean shouldShow(FleetMemberAPI member, ShipModifications mods, MarketAPI market) {
        return canAfford(member.getFleetData().getFleet(), market)
                || Utilities.hasExoticChip(member.getFleetData().getFleet().getCargo(), this.getKey())
                || Utilities.hasExoticChip(Misc.getStorageCargo(market), this.getKey());
    }

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return Utilities.hasItem(fleet.getCargo(), ITEM)
                || (Misc.getStorageCargo(market) != null && Utilities.hasItem(Misc.getStorageCargo(market), ITEM));
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm, MarketAPI market) {
        if (Utilities.hasItem(fleet.getCargo(), ITEM)) {
            Utilities.takeItemQuantity(fleet.getCargo(), ITEM, 1);
        } else {
            Utilities.takeItemQuantity(Misc.getStorageCargo(market), ITEM, 1);
        }
        return true;
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications mods, boolean expand) {
        if (expand) {
            StringUtils.getTranslation(this.getKey(), "longDescription")
                    .addToTooltip(tooltip, title);
        }
    }

    @Override
    public Map<String, Float> getResourceCostMap(FleetMemberAPI fm, ShipModifications mods, MarketAPI market) {
        Map<String, Float> resourceCosts = new HashMap<>();
        resourceCosts.put(ITEM, 1f);
        return resourceCosts;
    }


    @Override
    public void applyExoticToStats(String id, FleetMemberAPI fm, MutableShipStatsAPI stats, ExoticData data) {
        onInstall(fm);
    }

    @Override
    public void applyExoticToShip(String id, FleetMemberAPI fm, ShipAPI ship, ExoticData data) {
        if (!ship.hasListenerOfClass(DaemonCoreDamageTakenListener.class)) {
            ship.addListener(new DaemonCoreDamageTakenListener());
        }

        if (!ship.hasListenerOfClass(DaemonCoreDamageDealtListener.class)) {
            ship.addListener(new DaemonCoreDamageDealtListener());
        }
    }

    /**
     * extra bandwidth added directly to ship.
     *
     * @param fm
     * @param mods
     * @param data
     * @return
     */
    public float getExtraBandwidth(FleetMemberAPI fm, ShipModifications mods, ExoticData data) {
        return 60f;
    }

    public class DaemonCoreDamageTakenListener implements DamageTakenModifier {
        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            damage.getModifier().modifyMult(DaemonCore.this.getBuffId(), 1.2f);
            return null;
        }
    }

    public class DaemonCoreDamageDealtListener implements DamageDealtModifier {
        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            damage.getModifier().modifyMult(DaemonCore.this.getBuffId(), 1.2f);
            return null;
        }
    }
}
