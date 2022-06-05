package exoticatechnologies.campaign.listeners;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
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
import exoticatechnologies.ETModSettings;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.FleetMemberUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.util.*;

@Log4j
public class CampaignEventListener extends BaseCampaignEventListener implements EveryFrameScript {
    private static boolean debug = false;
    @Getter
    private static List<CampaignFleetAPI> activeFleets = new ArrayList<>();

    @Getter private static boolean appliedData = false;
    private IntervalUtil interval = new IntervalUtil(2f, 2f);
    private IntervalUtil cleaningInterval = new IntervalUtil(15f, 15f);

    public CampaignEventListener(boolean permaRegister) {
        super(permaRegister);
    }

    /**
     * Apply ship modifications to everything in the next frame.
     */
    public void invalidateShipModifications() {
        appliedData = false;
    }

    @Override
    public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
        SectorEntityToken interactionTarget = dialog.getInteractionTarget();
        if (interactionTarget == null) {
            return;
        }

        CampaignFleetAPI defenderFleet = interactionTarget.getMemoryWithoutUpdate().getFleet("$defenderFleet");
        if (defenderFleet != null) {

            dlog("Defender fleet is in dialog.");

            if (activeFleets.contains(defenderFleet)) {
                return;
            }

            dlog("Generating modifications for fleet.");

            activeFleets.add(defenderFleet);
            applyExtraSystemsToFleet(defenderFleet);
        }

        if (interactionTarget instanceof CampaignFleetAPI) {
            dlog("Target fleet is in dialog.");

            CampaignFleetAPI fleet = (CampaignFleetAPI) interactionTarget;
            if (activeFleets.contains(fleet)) {
                return;
            }

            dlog("Generating modifications for fleet.");

            activeFleets.add(fleet);

            applyExtraSystemsToFleet(fleet);

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

            ShipVariantAPI var = plugin.getData().ship.getVariant();

            if (var == null) return;

            long seed = interactionTarget.getId().hashCode();
            if (data.ship.fleetMemberId != null) {
                seed = data.ship.fleetMemberId.hashCode();
            }


            //note: saving here isn't really an issue because the cleanup script searches for fleet members with this ID.
            //it will never find one.
            ShipModifications mods = ShipModFactory.generateRandom(var, seed, null);
            ETModPlugin.saveData(interactionTarget.getId(), mods);

            Global.getSector().addTransientScript(new DerelictsEFScript(var.getHullVariantId(), mods));
        }

        if (Entities.DEBRIS_FIELD_SHARED.equals(interactionTarget.getCustomEntityType())
                && interactionTarget.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPECIAL_DATA)
                && interactionTarget.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA) instanceof ShipRecoverySpecial.ShipRecoverySpecialData) {
            ShipRecoverySpecial.ShipRecoverySpecialData data = (ShipRecoverySpecial.ShipRecoverySpecialData) interactionTarget.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA);

            if (data.ships != null
                    && !data.ships.isEmpty()) {

                Map<String, ShipModifications> derelictVariantMap = new LinkedHashMap<>();

                int i = 0;
                for (ShipRecoverySpecial.PerShipData shipData : data.ships) {

                    ShipVariantAPI var = shipData.getVariant();

                    if (var == null) continue;

                    long hash = var.hashCode();
                    if (var.getHullVariantId() != null) {
                        hash = var.getHullVariantId().hashCode();
                    }

                    long seed = hash + (i++);
                    if (shipData.fleetMemberId != null) {
                        seed = shipData.fleetMemberId.hashCode();
                    }

                    ShipModifications mods = ShipModFactory.generateRandom(var, seed, null);
                    //note: saving here isn't really an issue because the cleanup script searches for fleet members with this ID.
                    //it will never find one.
                    ETModPlugin.saveData(interactionTarget.getId() + String.valueOf(i), mods);

                    derelictVariantMap.put(var.getHullVariantId(), mods);
                }

                Global.getSector().addTransientScript(new DerelictsEFScript(derelictVariantMap));
            }
        }
    }

    @Override
    public void reportFleetSpawned(CampaignFleetAPI fleet) {
        if (fleet.isPlayerFleet()) {
            return;
        }

        if (Global.getSector().getCampaignUI().isShowingDialog()) {
            dlog("Fleet spawned in dialog.");
            if (activeFleets.contains(fleet)) {
                return;
            }

            activeFleets.add(fleet);

            applyExtraSystemsToFleet(fleet);
        }
    }

    @Override
    public void reportFleetDespawned(CampaignFleetAPI fleet,
                                     FleetDespawnReason reason, Object param) {

        dlog(String.format("Fleet %s has despawned.", fleet.getNameWithFaction()));
        activeFleets.remove(fleet);
        removeExtraSystemsFromFleet(fleet);
    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {
        if (ETModSettings.getBoolean(ETModSettings.SHIPS_KEEP_UPGRADES_ON_DEATH)) return;

        EngagementResultForFleetAPI playerResult = result.didPlayerWin()
                ? result.getWinnerResult()
                : result.getLoserResult();

        List<FleetMemberAPI> fms = new ArrayList<>();
        fms.addAll(playerResult.getDisabled());
        fms.addAll(playerResult.getDestroyed());

        for (FleetMemberAPI fm : fms) {
            dlog("Removed FM [%s] because it died in combat", fm.getId());
            ExoticaTechHM.removeFromFleetMember(fm);
            ETModPlugin.removeData(fm.getId());
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
        if (!appliedData) {
            appliedData = true;
            for (String fmId : ETModPlugin.getShipModificationMap().keySet()) {
                FleetMemberAPI fm = CampaignEventListener.findFM(fmId);

                if (fm != null) {
                    dlog("found fm [%s] for id [%s]", fm.getShipName(), fmId);
                    ExoticaTechHM.addToFleetMember(fm);
                } else {
                    dlog("could not find for id [%s]", fmId);
                }
            }

            for (FleetMemberAPI fm : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
                String fmId = fm.getId();
                if (ETModPlugin.hasData(fmId)) {
                    ExoticaTechHM.addToFleetMember(fm);
                } else {
                    if (fm.getHullId().contains("ziggurat") && ETModPlugin.getZigguratDuplicateId() != null) {
                        fmId = ETModPlugin.getZigguratDuplicateId();
                        if (ETModPlugin.hasData(fmId)) {
                            ExoticaTechHM.addToFleetMember(fm);
                        }
                    }
                }
            }
        }

        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.REFIT) {
            FleetMemberUtils.moduleMap.clear();
        }

        boolean checkFleets = false;

        cleaningInterval.advance(v);
        if (checkFleets || cleaningInterval.intervalElapsed()) {
            cleaningInterval.setElapsed(0f);

            checkNearbyFleets();

            //disgustingly get a list of the fmIds
            List<String> fmIds = Arrays.asList(ETModPlugin.getShipModificationMap().keySet().toArray(new String[0]));
            for (String fmId : fmIds) {
                if (findFM(fmId) == null) {

                    dlog(String.format("Fleet member %s was not found in the current location or market storage", fmId));
                    if (!isInFleet(fmId, Global.getSector().getPlayerFleet())) {
                        ETModPlugin.removeData(fmId);
                    } else {
                        dlog("The member was found in the player fleet, though.");
                    }
                }
            }
        }

        interval.advance(v);
        if (checkFleets || interval.intervalElapsed()) {
            interval.setElapsed(0f);

            dlog(String.format("Extra system instances active: [%s]", ETModPlugin.getShipModificationMap().size()));
            dlog(String.format("Fleet instances active: [%s]", activeFleets.size()));
        }
    }

    public static FleetMemberAPI findFM(String fmId) {
        if (Global.getSector().getPlayerFleet() != null) {
            for (FleetMemberAPI playerFm : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
                if (playerFm.getId().equals(fmId)) {
                    return playerFm;
                }
            }
        }

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
            for (SubmarketAPI submarket : market.getSubmarketsCopy()) {
                if (submarket.getCargoNullOk() != null) {
                    CargoAPI storage = submarket.getCargoNullOk();
                    if (storage.getMothballedShips() != null
                            && storage.getMothballedShips().getMembersListWithFightersCopy() != null) {

                        for (FleetMemberAPI fm : storage.getMothballedShips().getMembersListWithFightersCopy()) {
                            if (fm.getId().equals(fmId)) {
                                return fm;
                            }
                        }
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
            if (fleet.equals(playerFleet)) continue;

            if (fleet == null
                    || !fleet.isAlive()
                    || !Objects.equals(fleet.getContainingLocation(),
                    playerFleet.getContainingLocation())) {
                removedFleet = true;
            }

            if (removedFleet) {
                if (fleet != null) {
                    dlog(String.format("Fleet %s was not found in player location", fleet.getNameWithFaction()));
                    removeExtraSystemsFromFleet(fleet);
                } else {
                    dlog("A null fleet was removed");
                }
                it.remove();
            }
        }

        return removedFleet;
    }

    private static void removeExtraSystemsFromFleet(CampaignFleetAPI fleet) {
        if (fleet.getFleetData() == null || fleet.getFleetData().getMembersListCopy() == null) {
            dlog("Fleet data was null");
            return;
        }

        dlog(String.format("Removing mods for fleet %s", fleet.getNameWithFaction()));

        if (fleet.equals(Global.getSector().getPlayerFleet())) {
            dlog(String.format("This fleet is the player fleet."));
            return;
        }

        for (Iterator<FleetMemberAPI> it = fleet.getFleetData().getMembersListCopy().iterator(); it.hasNext(); ) {
            FleetMemberAPI fm = it.next();
            if (!isInFleet(fm.getId(), Global.getSector().getPlayerFleet())) {
                dlog(String.format("Removed mods for member %s", fm.getId()));
                ETModPlugin.removeData(fm.getId());
            }
        }
    }

    private static void applyExtraSystemsToFleet(CampaignFleetAPI fleet) {
        for (FleetMemberAPI fm : fleet.getFleetData().getMembersListCopy()) {
            if (fm.isFighterWing()) continue;

            //generate random extra system
            ShipModifications mods = ShipModFactory.generateRandom(fm);
            mods.save(fm);
            ExoticaTechHM.addToFleetMember(fm);

            dlog(String.format("Added modifications to member %s", fm.getShipName()));
        }
    }

    private static boolean isInFleet(FleetMemberAPI fm, CampaignFleetAPI fleet) {
        if (fm == null || fleet == null) {
            return false;
        }

        for(FleetMemberAPI fleetMember : fleet.getFleetData().getMembersListCopy()) {
            if (fleetMember.isFighterWing()) continue;

            if(fm.equals(fleetMember)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInFleet(String fmId, CampaignFleetAPI fleet) {
        if (fleet == null) {
            return false;
        }

        for(FleetMemberAPI fleetMember : fleet.getFleetData().getMembersListCopy()) {
            if (fleetMember.isFighterWing()) continue;

            if(fmId.equals(fleetMember.getId())) {
                return true;
            }
        }
        return false;
    }

    private static void dlog(String format, Object... args) {
        if (!debug) return;

        if (args == null || args.length == 0) {
            log.info(format);
        } else {
            String[] values = new String[args.length];

            for(int i = 0; i < args.length; i++) {
                values[i] = String.valueOf(args[i]);
            }

            log.info(String.format(format, args));
        }
    }
}