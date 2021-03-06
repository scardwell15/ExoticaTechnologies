package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PlasmaFluxCatalyst extends Exotic {
    private static final String ITEM = "et_plasmacatalyst";
    private static final Color[] tooltipColors = {new Color(0x00BBFF), ExoticaTechHM.infoColor};

    private static Map<ShipAPI.HullSize, Integer> MAX_FLUX_EQUIPMENT = new HashMap() {{
        put(ShipAPI.HullSize.FIGHTER, 10);
        put(ShipAPI.HullSize.FRIGATE, 10);
        put(ShipAPI.HullSize.DESTROYER, 20);
        put(ShipAPI.HullSize.CRUISER, 30);
        put(ShipAPI.HullSize.CAPITAL_SHIP, 50);
    }};

    @Getter private final Color mainColor = new Color(0x00BBFF);

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return Utilities.hasItem(fleet.getCargo(), ITEM);
    }

    public String getUnableToApplyTooltip(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        return StringUtils.getTranslation(this.getKey(), "needItem")
                .format("itemName", Global.getSettings().getSpecialItemSpec(ITEM).getName())
                .toString();
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        Utilities.takeItemQuantity(fleet.getCargo(), ITEM, 1);

        return true;
    }

    @Override
    public boolean restoreItemsToFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        Utilities.addItem(fleet, ITEM, 1);
        return true;
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        if (systems.hasExotic(this.getKey())) {
            if(expand) {
                int maxCaps = (int) fm.getFleetCommander().getStats().getMaxCapacitorsBonus().computeEffective(MAX_FLUX_EQUIPMENT.get(fm.getHullSpec().getHullSize()));
                int maxVents = (int) fm.getFleetCommander().getStats().getMaxVentsBonus().computeEffective(MAX_FLUX_EQUIPMENT.get(fm.getHullSpec().getHullSize()));

                StringUtils.getTranslation(this.getKey(), "longDescription")
                        .format("exoticName", this.getName())
                        .format("capacitorLimit", Math.ceil(maxCaps / 3))
                        .format("ventLimit", Math.ceil(maxVents / 3))
                        .format("crDecrease", 1)
                        .addToTooltip(tooltip, tooltipColors);
            } else {
                tooltip.addPara(this.getName(), tooltipColors[0], 5);
            }
        }
    }

    @Override
    public void modifyResourcesPanel(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm) {
        resourceCosts.put(ITEM, 1f);
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
        if(numCapsStats > Math.ceil(maxCaps / 3)) {
            crReduction += numCapsStats - Math.ceil(maxCaps / 3);
        }

        if(numVentsStats > Math.ceil(maxVents / 3)) {
            crReduction += numVentsStats - Math.ceil(maxVents / 3);
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
