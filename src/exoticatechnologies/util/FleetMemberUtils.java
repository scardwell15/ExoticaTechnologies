package exoticatechnologies.util;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.campaign.listeners.CampaignEventListener;
import org.apache.log4j.Logger;

import java.util.HashMap;
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
        for (CampaignFleetAPI fleet : CampaignEventListener.Companion.getActiveFleets()) {
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

            MutableShipStatsAPI memberStats = member.getStats();
            if(memberStats == stats) {
                return member;
            } else if (stats.getEntity() != null && memberStats.getEntity() == stats.getEntity()) {
                return member;
            } else if (stats.getFleetMember() != null && stats.getFleetMember() == member) {
                return member;
            } else if (stats.getVariant() != null && member.getVariant() == stats.getVariant()) {
                return member;
            } else {
                MutableShipStatsAPI opStats = null;
                try {
                    opStats = member.getVariant().getStatsForOpCosts();
                } catch (Throwable ex) {
                    // do nothing
                }

                if (opStats != null) {
                    if (opStats == stats) {
                        return member;
                    } else if (stats.getEntity() != null && opStats.getEntity() == stats.getEntity()) {
                        return member;
                    } else if (stats.getFleetMember() != null && opStats.getFleetMember() == stats.getFleetMember()) {
                        return member;
                    } else if (stats.getVariant() != null && opStats.getVariant() == stats.getVariant()) {
                        return member;
                    }
                }
            }

            ShipVariantAPI shipVariant = member.getVariant();
            for (String moduleVariantId : shipVariant.getStationModules().keySet()) {
                ShipVariantAPI moduleVariant = shipVariant.getModuleVariant(moduleVariantId);

                MutableShipStatsAPI moduleStats;
                try {
                    moduleStats = moduleVariant.getStatsForOpCosts();
                } catch (Throwable ex) {
                    continue;
                }
                if (moduleStats != null) {
                    if (moduleStats == stats) {
                        return member;
                    } else if (stats.getEntity() != null && stats.getEntity() == moduleStats.getEntity()) {
                        return member;
                    } else if (stats.getFleetMember() != null &&moduleStats.getFleetMember() == stats.getFleetMember()) {
                        return member;
                    }
                }
            }
        }

        return null;
    }
}
