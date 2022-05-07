package extrasystemreloaded.campaign.listeners;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import extrasystemreloaded.ESModSettings;
import extrasystemreloaded.Es_ModPlugin;
import extrasystemreloaded.hullmods.ExtraSystemHM;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.FleetMemberUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.util.*;

@Log4j
public class ESCampaignEventListener extends BaseCampaignEventListener implements EveryFrameScript {
    @Getter
    private static List<CampaignFleetAPI> activeFleets = new ArrayList<>();

    private IntervalUtil interval = new IntervalUtil(2f, 2f);
    private IntervalUtil esCleaningInterval = new IntervalUtil(15f, 15f);

    public ESCampaignEventListener(boolean permaRegister) {
        super(permaRegister);
    }

    @Override
    public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
        SectorEntityToken interactionTarget = dialog.getInteractionTarget();
        if (interactionTarget == null) {
            return;
        }

        CampaignFleetAPI defenderFleet = interactionTarget.getMemoryWithoutUpdate().getFleet("$defenderFleet");
        if (defenderFleet != null) {
            log.info("Defender fleet is in dialog.");

            if (activeFleets.contains(defenderFleet)) {
                return;
            }

            log.info("Generating systems for fleet.");

            activeFleets.add(defenderFleet);
            Es_ModPlugin.applyExtraSystemsToFleet(defenderFleet);
        }

        if (interactionTarget instanceof CampaignFleetAPI) {
            log.info("Target fleet is in dialog.");

            CampaignFleetAPI fleet = (CampaignFleetAPI) interactionTarget;
            if (activeFleets.contains(fleet)) {
                return;
            }

            log.info("Generating systems for fleet.");

            activeFleets.add(fleet);

            Es_ModPlugin.applyExtraSystemsToFleet(fleet);

            FireAll.fire(null, dialog, dialog.getPlugin().getMemoryMap(), "GeneratedESForFleet");
        }

        if (interactionTarget.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
            //generate for interactionTarget.getId()
            if (Misc.getSalvageSpecial(interactionTarget) instanceof ShipRecoverySpecial.ShipRecoverySpecialData) {
                return;
            }

            if (interactionTarget.hasTag(Tags.UNRECOVERABLE)) {
                return;
            }

            DerelictShipEntityPlugin plugin = (DerelictShipEntityPlugin) interactionTarget.getCustomPlugin();
            DerelictShipEntityPlugin.DerelictShipData data = plugin.getData();

            long seed = interactionTarget.getId().hashCode();
            if (data.ship.fleetMemberId != null) {
                seed = data.ship.fleetMemberId.hashCode();
            }

            ExtraSystems es = new ExtraSystems(seed);


            ShipVariantAPI var = plugin.getData().ship.getVariant();
            es.generate(seed, var, null);

            //note: saving here isn't really an issue because the cleanup script searches for fleet members with this ID.
            //it will never find one.
            Es_ModPlugin.saveData(interactionTarget.getId(), es);

            Global.getSector().addTransientScript(new ESDerelictsEFScript(var.getHullVariantId(), es));
        }

        if (Entities.DEBRIS_FIELD_SHARED.equals(interactionTarget.getCustomEntityType())
                && interactionTarget.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPECIAL_DATA)
                && interactionTarget.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA) instanceof ShipRecoverySpecial.ShipRecoverySpecialData) {
            ShipRecoverySpecial.ShipRecoverySpecialData data = (ShipRecoverySpecial.ShipRecoverySpecialData) interactionTarget.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA);

            if (data.ships != null
                    && !data.ships.isEmpty()) {

                Map<String, ExtraSystems> derelictVariantMap = new LinkedHashMap<>();

                int i = 0;
                for (ShipRecoverySpecial.PerShipData shipData : data.ships) {

                    ShipVariantAPI var = shipData.getVariant();
                    long seed = var.getHullVariantId().hashCode() + (i++);
                    if (shipData.fleetMemberId != null) {
                        seed = shipData.fleetMemberId.hashCode();
                    }

                    ExtraSystems es = new ExtraSystems(seed);
                    es.generate(seed, var, null);
                    //note: saving here isn't really an issue because the cleanup script searches for fleet members with this ID.
                    //it will never find one.
                    Es_ModPlugin.saveData(interactionTarget.getId() + String.valueOf(i), es);

                    derelictVariantMap.put(var.getHullVariantId(), es);
                }

                Global.getSector().addTransientScript(new ESDerelictsEFScript(derelictVariantMap));
            }
        }
    }

    @Override
    public void reportFleetSpawned(CampaignFleetAPI fleet) {
        if (Global.getSector().getCampaignUI().isShowingDialog()) {
            log.info("Fleet spawned in dialog.");
            if (activeFleets.contains(fleet)) {
                return;
            }

            activeFleets.add(fleet);

            Es_ModPlugin.applyExtraSystemsToFleet(fleet);
        }
    }

    @Override
    public void reportFleetDespawned(CampaignFleetAPI fleet,
                                     FleetDespawnReason reason, Object param) {
        activeFleets.remove(fleet);

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        for (FleetMemberAPI fm : fleet.getFleetData().getMembersListCopy()) {
            if (fm.isFighterWing()) continue;

            //fleet member may have joined the player fleet. honestly not sure if this is a case, actually.
            if (playerFleet.getFleetData().getMembersListWithFightersCopy().contains(fm)) continue;

            Es_ModPlugin.removeData(fm.getId());
        }
    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {
        if (ESModSettings.getBoolean(ESModSettings.SHIPS_KEEP_UPGRADES_ON_DEATH)) return;

        EngagementResultForFleetAPI playerResult = result.didPlayerWin()
                ? result.getWinnerResult()
                : result.getLoserResult();

        List<FleetMemberAPI> fms = new ArrayList<>();
        fms.addAll(playerResult.getDisabled());
        fms.addAll(playerResult.getDestroyed());

        for (FleetMemberAPI fm : fms) {
            ExtraSystemHM.removeFromFleetMember(fm);
            Es_ModPlugin.removeData(fm.getId());
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return !FleetMemberUtils.moduleMap.isEmpty();
    }

    @Override
    public void advance(float v) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.REFIT) {
            FleetMemberUtils.moduleMap.clear();
        }

        boolean checkFleets = false;

        interval.advance(v);
        if (checkFleets || interval.intervalElapsed()) {
            interval.setElapsed(0f);

            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (!activeFleets.contains(playerFleet)) {
                activeFleets.add(playerFleet);
            }

            log.info(String.format("Extra system instances active: [%s]", Es_ModPlugin.getShipUpgradeData().size()));
            log.info(String.format("Fleet instances active: [%s]", activeFleets.size()));
        }

        esCleaningInterval.advance(v);
        if (checkFleets || esCleaningInterval.intervalElapsed()) {
            esCleaningInterval.setElapsed(0f);

            checkNearbyFleets();

            //disgustingly get a list of the fmIds
            List<String> fmIds = Arrays.asList(Es_ModPlugin.getShipUpgradeData().keySet().toArray(new String[0]));
            for (String fmId : fmIds) {
                if (findFM(fmId) == null) {
                    Es_ModPlugin.removeData(fmId);
                }
            }
        }
    }

    public static FleetMemberAPI findFM(String fmId) {
        FleetMemberAPI fm = checkNearbyFleetsForFM(fmId);

        if (fm != null) {
            return fm;
        } else {
            return checkStorageMarketsForFM(fmId);
        }
    }

    private static FleetMemberAPI checkNearbyFleetsForFM(String fmId) {
        List<CampaignFleetAPI> fleets = Global.getSector().getCurrentLocation().getEntities(CampaignFleetAPI.class);
        for (CampaignFleetAPI fleet : fleets) {
            for (FleetMemberAPI fm : fleet.getMembersWithFightersCopy()) {
                if (fm.getId().equals(fmId)) {
                    return fm;
                }
            }
        }

        return null;
    }

    private static FleetMemberAPI checkStorageMarketsForFM(String fmId) {
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            CargoAPI storage = Misc.getStorageCargo(market);
            if (storage != null
                    && storage.getMothballedShips() != null
                    && storage.getMothballedShips().getMembersListWithFightersCopy() != null) {

                for (FleetMemberAPI fm : storage.getMothballedShips().getMembersListWithFightersCopy()) {
                    if (fm.getId().equals(fmId)) {
                        return fm;
                    }
                }
            }
        }

        return null;
    }

    private static boolean checkNearbyFleets() {
        boolean removedFleet = false;
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        for (Iterator<CampaignFleetAPI> it = activeFleets.iterator(); it.hasNext(); ) {
            CampaignFleetAPI fleet = it.next();

            if (fleet == null
                    || !fleet.isAlive()
                    || !Objects.equals(fleet.getContainingLocation(),
                    playerFleet.getContainingLocation())) {
                removedFleet = true;
            }

            if (removedFleet) {
                if (fleet != null) {
                    Es_ModPlugin.removeExtraSystemsFromFleet(fleet);
                }
                it.remove();
            }
        }

        return removedFleet;
    }
}
