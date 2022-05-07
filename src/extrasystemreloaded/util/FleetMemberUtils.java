package extrasystemreloaded.util;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import extrasystemreloaded.campaign.listeners.ESCampaignEventListener;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FleetMemberUtils {
    public static final Map<String, FleetMemberAPI> moduleMap = new HashMap<>();
    private static final Logger log = Logger.getLogger(FleetMemberUtils.class);

    private FleetMemberUtils() {
    }

    public static FleetMemberAPI findMemberFromShip(ShipAPI ship) {
        String id = ship.getVariant().getHullVariantId();

        if(moduleMap.containsKey(id)) {
            return moduleMap.get(id);
        }

        if(ship.getParentStation() != null) {
            return findMemberFromShip(ship.getParentStation());
        }

        if(ship.getFleetMember() != null) {
            return ship.getFleetMember();
        }

        return findMemberForStats(ship.getMutableStats());
    }

    public static FleetMemberAPI findMemberForStats(MutableShipStatsAPI stats) {
        String id = stats.getVariant().getHullVariantId();

        if(moduleMap.containsKey(id)) {
            return moduleMap.get(id);
        }

        if (stats.getFleetMember() != null) {
            return stats.getFleetMember();
        }

        if (stats.getEntity() instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            if (ship.getFleetMember() != null) {
                return ship.getFleetMember();
            }
        }

        //note: this looks awful, but it actually doesn't go into this loop all that often.
        for (CampaignFleetAPI fleet : ESCampaignEventListener.getActiveFleets()) {
            FleetMemberAPI fm = searchFleetForStats(fleet, stats);
            if(fm != null) {
                return fm;
            }
        }

        return null;
    }

    private static FleetMemberAPI searchFleetForStats(CampaignFleetAPI fleet, MutableShipStatsAPI stats) {
        if(fleet == null) {
            return null;
        }

        for(FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if(member.isFighterWing()) continue;

            if(member.getStats() == stats) {
                return member;
            } else if (stats.getEntity() != null && member.getStats().getEntity() == stats.getEntity()) {
                return member;
            } else if (stats.getFleetMember() != null && (stats.getFleetMember() == member || member.getStats().getFleetMember() == stats.getFleetMember())) {
                return member;
            } else if (stats.getVariant() != null && member.getVariant() == stats.getVariant()) {
                return member;
            } else if (member.getVariant().getStatsForOpCosts() != null) {
                if (member.getVariant().getStatsForOpCosts() == stats) {
                    return member;
                } else if (stats.getEntity() != null && member.getVariant().getStatsForOpCosts().getEntity() == stats.getEntity()) {
                    return member;
                } else if (stats.getFleetMember() != null && member.getVariant().getStatsForOpCosts().getFleetMember() == stats.getFleetMember()) {
                    return member;
                } else if (stats.getVariant() != null && member.getVariant().getStatsForOpCosts().getVariant() == stats.getVariant()) {
                    return member;
                }
            }

            ShipVariantAPI shipVariant = member.getVariant();
            Iterator<String> moduleIterator = shipVariant.getStationModules().keySet().iterator();
            while (moduleIterator.hasNext()) {
                String moduleVariantId = moduleIterator.next();
                ShipVariantAPI moduleVariant = shipVariant.getModuleVariant(moduleVariantId);

                if (moduleVariant.getStatsForOpCosts() != null) {
                    if (moduleVariant.getStatsForOpCosts() == stats) {
                        return member;
                    } else if (stats.getEntity() != null && stats.getEntity() == moduleVariant.getStatsForOpCosts().getEntity()) {
                        return member;
                    } else if (stats.getFleetMember() != null && moduleVariant.getStatsForOpCosts().getFleetMember() == stats.getFleetMember()) {
                        return member;
                    }
                }
            }
        }

        return null;
    }
}
