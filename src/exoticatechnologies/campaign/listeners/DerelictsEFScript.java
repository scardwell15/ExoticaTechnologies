package exoticatechnologies.campaign.listeners;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModifications;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j
public class DerelictsEFScript implements EveryFrameScript {
    private boolean done = false;

    @Getter //map links derelict variant IDs to their generated modifications
    Map<String, ShipModifications> derelictVariantMap = new LinkedHashMap<>();
    private List<FleetMemberAPI> fleetSnapshot = new ArrayList<>();

    public DerelictsEFScript(Map<String, ShipModifications> derelictVariantMap) {
        this.derelictVariantMap.putAll(derelictVariantMap);
        fleetSnapshot.addAll(Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy());
    }

    public DerelictsEFScript(String derelictVariantId, ShipModifications es) {
        derelictVariantMap.put(derelictVariantId, es);
        fleetSnapshot.addAll(Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy());
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {

        if (!Global.getSector().getCampaignUI().isShowingDialog()) {
            done = true;

            //get new fleet members
            List<FleetMemberAPI> newFleetMembers = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
            newFleetMembers.removeAll(fleetSnapshot);

            //link derelicts to new members
            if (!newFleetMembers.isEmpty()) {
                linkDerelictsToNewMembers(newFleetMembers);
            }
        }
    }

    private void linkDerelictsToNewMembers(List<FleetMemberAPI> newMembers) {
        for (FleetMemberAPI fm : newMembers) {
            ShipModifications mods = null;

            String fmIdHash = String.valueOf(fm.getId().hashCode());
            if (derelictVariantMap.containsKey(fmIdHash)) {
                mods = derelictVariantMap.get(fmIdHash);
            } else {
                ShipVariantAPI var = fm.getVariant();
                if (var == null) continue;

                for (int i = 0; i < derelictVariantMap.size(); i++) {
                    String varHash = String.valueOf(var.hashCode() + i);
                    String hullVarHash = String.valueOf(var.getHullVariantId().hashCode() + i);
                    if (derelictVariantMap.containsKey(hullVarHash)) {
                        mods = derelictVariantMap.get(hullVarHash);
                        break;
                    } else if (derelictVariantMap.containsKey(varHash)) {
                        mods = derelictVariantMap.get(varHash);
                        break;
                    } else {
                        varHash = String.valueOf(var.hashCode() + fm.getShipName().hashCode() + i);
                        hullVarHash = String.valueOf(var.getHullVariantId().hashCode() + fm.getShipName().hashCode() + i);
                        if (derelictVariantMap.containsKey(hullVarHash)) {
                            mods = derelictVariantMap.get(hullVarHash);
                            break;
                        } else if (derelictVariantMap.containsKey(varHash)) {
                            mods = derelictVariantMap.get(varHash);
                            break;
                        }
                    }
                }
            }

            if (mods != null) {
                mods.save(fm);
                ExoticaTechHM.addToFleetMember(fm);
            }
        }
    }
}
