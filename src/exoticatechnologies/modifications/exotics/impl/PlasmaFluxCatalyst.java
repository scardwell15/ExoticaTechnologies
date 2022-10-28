package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PlasmaFluxCatalyst extends Exotic {
    private static final String ITEM = "et_plasmacatalyst";

    private static final Map<ShipAPI.HullSize, Integer> MAX_FLUX_EQUIPMENT = new HashMap<>();

    static {
        MAX_FLUX_EQUIPMENT.put(ShipAPI.HullSize.FIGHTER, 10);
        MAX_FLUX_EQUIPMENT.put(ShipAPI.HullSize.FRIGATE, 10);
        MAX_FLUX_EQUIPMENT.put(ShipAPI.HullSize.DESTROYER, 20);
        MAX_FLUX_EQUIPMENT.put(ShipAPI.HullSize.CRUISER, 30);
        MAX_FLUX_EQUIPMENT.put(ShipAPI.HullSize.CAPITAL_SHIP, 50);
    }

    @Getter private final Color mainColor = new Color(0x00BBFF);

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return Utilities.hasItem(fleet.getCargo(), ITEM);
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        Utilities.takeItemQuantity(fleet.getCargo(), ITEM, 1);

        return true;
    }

    @Override
    public Map<String, Float> getResourceCostMap(FleetMemberAPI fm, ShipModifications mods, MarketAPI market) {
        Map<String, Float> resourceCosts = new HashMap<>();
        resourceCosts.put(Utilities.formatSpecialItem(ITEM), 1f);
        return resourceCosts;
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        if (expand) {
            int maxCaps = (int) fm.getFleetCommander().getStats().getMaxCapacitorsBonus().computeEffective(MAX_FLUX_EQUIPMENT.get(fm.getHullSpec().getHullSize()));
            int maxVents = (int) fm.getFleetCommander().getStats().getMaxVentsBonus().computeEffective(MAX_FLUX_EQUIPMENT.get(fm.getHullSpec().getHullSize()));

            StringUtils.getTranslation(this.getKey(), "longDescription")
                    .format("capacitorLimit", Math.ceil(maxCaps / 3f))
                    .format("ventLimit", Math.ceil(maxVents / 3f))
                    .format("crDecrease", 1)
                    .addToTooltip(tooltip, title);
        }
    }

    @Override
    public void applyExoticToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, float bandwidth, String id) {
        if (fm.getFleetCommander() == null) {
            return;
        }

        if (isNPC(fm)) {
            return;
        }

        int numCapsStats = stats.getVariant().getNumFluxCapacitors();
        int numVentsStats = stats.getVariant().getNumFluxVents();

        int maxCaps = (int) fm.getFleetCommander().getStats().getMaxCapacitorsBonus().computeEffective(MAX_FLUX_EQUIPMENT.get(fm.getHullSpec().getHullSize()));
        int maxVents = (int) fm.getFleetCommander().getStats().getMaxVentsBonus().computeEffective(MAX_FLUX_EQUIPMENT.get(fm.getHullSpec().getHullSize()));

        int crReduction = 0;
        if(numCapsStats > Math.ceil(maxCaps / 3f)) {
            crReduction += numCapsStats - Math.ceil(maxCaps / 3f);
        }

        if(numVentsStats > Math.ceil(maxVents / 3f)) {
            crReduction += numVentsStats - Math.ceil(maxVents / 3f);
        }

        if(crReduction > 0) {
            stats.getMaxCombatReadiness().modifyFlat(this.getName(), -crReduction / 100f, this.getName());
        }
    }

    @Override
    public void applyExoticToShip(FleetMemberAPI fm, ShipAPI ship, float bandwidth, String id) {
        int numCaps = ship.getVariant().getNumFluxCapacitors();
        int numVents = ship.getVariant().getNumFluxVents();

        ship.getMutableStats().getFluxCapacity().modifyFlat(this.getBuffId(), numCaps * 200);
        ship.getMutableStats().getFluxDissipation().modifyFlat(this.getBuffId(), numVents * 10);
    }
}
