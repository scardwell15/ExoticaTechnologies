package extrasystemreloaded.campaign.listeners;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import extrasystemreloaded.Es_ModPlugin;
import extrasystemreloaded.hullmods.ExtraSystemHM;
import extrasystemreloaded.util.ExtraSystems;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j
public class ESDerelictsEFScript implements EveryFrameScript {
    private boolean done = false;

    @Getter //map links derelict variant IDs to their generated systems
    Map<String, ExtraSystems> derelictVariantMap = new LinkedHashMap<>();
    private List<FleetMemberAPI> fleetSnapshot = new ArrayList<>();

    public ESDerelictsEFScript(Map<String, ExtraSystems> derelictVariantMap) {
        this.derelictVariantMap.putAll(derelictVariantMap);
        fleetSnapshot.addAll(Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy());
    }

    public ESDerelictsEFScript(String derelictVariantId, ExtraSystems es) {
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

            log.info("started linking derelicts");

            //get new fleet members
            List<FleetMemberAPI> newFleetMembers = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
            newFleetMembers.removeAll(fleetSnapshot);

            //link derelicts to new members
            if (!newFleetMembers.isEmpty()) {
                linkDerelictsToNewMembers(newFleetMembers);
            }

            log.info("finished linking derelicts");
        }
    }

    private void linkDerelictsToNewMembers(List<FleetMemberAPI> newMembers) {
        for (FleetMemberAPI fm : newMembers) {
            if (derelictVariantMap.containsKey(fm.getVariant().getHullVariantId())) {
                ExtraSystems es = derelictVariantMap.get(fm.getVariant().getHullVariantId());
                es.save(fm);
                ExtraSystemHM.addToFleetMember(fm);

                log.info(String.format("linked [%s] to [%s]", fm.getVariant().getHullVariantId(), fm.getShipName()));
            }
        }
    }
}
