package exoticatechnologies.campaign.listeners;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.ETModSettings;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.util.FleetMemberUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.util.*;

@Log4j
public class CampaignEventListener extends BaseCampaignEventListener implements EveryFrameScript, EconomyTickListener {
    private static final boolean debug = false;
    @Getter
    private static final List<CampaignFleetAPI> activeFleets = new ArrayList<>();
    private final IntervalUtil interval = new IntervalUtil(2f, 2f);
    private final IntervalUtil cleaningInterval = new IntervalUtil(15f, 15f);
    @Getter private static boolean appliedData = false;

    @Getter @Setter
    private static boolean mergeCheck = false;

    private static final List<String> submarketIdsToCheckForSpecialItems = new ArrayList<>();

    static {
        submarketIdsToCheckForSpecialItems.add(Submarkets.SUBMARKET_BLACK);
        submarketIdsToCheckForSpecialItems.add(Submarkets.SUBMARKET_OPEN);
        submarketIdsToCheckForSpecialItems.add(Submarkets.GENERIC_MILITARY);
    }

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
            dlog("Defender fleet is in dialog. Generating modifications.");

            String newId = interactionTarget.getId();
            defenderFleet.setId(newId);
            applyExtraSystemsToFleet(defenderFleet);
            return;
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
            return;
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
            ShipRecoverySpecial.PerShipData shipData = data.ship;

            ShipVariantAPI var = shipData.getVariant();
            if (var == null) return;

            FleetMemberAPI fm = Global.getFactory().createFleetMember(FleetMemberType.SHIP, shipData.getVariant());

            if (shipData.fleetMemberId == null) {
                shipData.fleetMemberId = fm.getId();
            } else {
                fm.setId(shipData.fleetMemberId);
            }

            if (shipData.shipName != null) {
                fm.setShipName(shipData.shipName);
            }

            //just in case?
            fm.updateStats();


            long seed = shipData.fleetMemberId.hashCode();
            ShipModFactory.getRandom().setSeed(seed);

            //note: saving here isn't really an issue because the cleanup script searches for fleet members with this ID.
            //it will never find one.
            ShipModifications mods = ShipModFactory.generateRandom(fm);
            ETModPlugin.saveData(shipData.fleetMemberId, mods);

            Global.getSector().addTransientScript(new DerelictsEFScript(shipData.fleetMemberId, mods));
            return;
        }

        if (Entities.DEBRIS_FIELD_SHARED.equals(interactionTarget.getCustomEntityType())
                && interactionTarget.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPECIAL_DATA)
                && interactionTarget.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA) instanceof ShipRecoverySpecial.ShipRecoverySpecialData) {
            ShipRecoverySpecial.ShipRecoverySpecialData data = (ShipRecoverySpecial.ShipRecoverySpecialData) interactionTarget.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA);

            if (data.ships != null
                    && !data.ships.isEmpty()) {

                Map<String, ShipModifications> derelictVariantMap = new LinkedHashMap<>();

                for (ShipRecoverySpecial.PerShipData shipData : data.ships) {

                    ShipVariantAPI var = shipData.getVariant();
                    if (var == null) continue;

                    FleetMemberAPI fm = Global.getFactory().createFleetMember(FleetMemberType.SHIP, var);
                    if (shipData.fleetMemberId != null) {
                        fm.setId(shipData.fleetMemberId);
                    } else {
                        shipData.fleetMemberId = fm.getId();
                    }

                    if (shipData.shipName != null) {
                        fm.setShipName(shipData.shipName);
                    }

                    long seed = shipData.fleetMemberId.hashCode();
                    ShipModFactory.getRandom().setSeed(seed);

                    ShipModifications mods = ShipModFactory.generateRandom(fm);
                    //note: saving here isn't really an issue because the cleanup script searches for fleet members with this ID.
                    //it will never find one.
                    ETModPlugin.saveData(shipData.fleetMemberId, mods);

                    derelictVariantMap.put(shipData.fleetMemberId, mods);
                }

                Global.getSector().addTransientScript(new DerelictsEFScript(derelictVariantMap));
            }
            return;
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
        EngagementResultForFleetAPI playerResult = result.didPlayerWin()
                ? result.getWinnerResult()
                : result.getLoserResult();

        if (!ETModSettings.getBoolean(ETModSettings.SHIPS_KEEP_UPGRADES_ON_DEATH)) {
            List<FleetMemberAPI> fms = new ArrayList<>();
            fms.addAll(playerResult.getDisabled());
            fms.addAll(playerResult.getDestroyed());

            for (FleetMemberAPI fm : fms) {
                ShipModifications mods = ETModPlugin.getData(fm.getId());
                if (mods != null) {
                    ExoticaTechHM.removeFromFleetMember(fm);
                    ETModPlugin.removeData(fm.getId());
                }
            }
        }

        EngagementResultForFleetAPI otherResult = result.didPlayerWin()
                ? result.getLoserResult()
                : result.getWinnerResult();

        List<FleetMemberAPI> fms = new ArrayList<>();
        fms.addAll(otherResult.getDisabled());
        fms.addAll(otherResult.getDestroyed());

        Pair<Map<Upgrade, Map<Integer, Integer>>, Map<Exotic, Integer>> potentialDrops = getDrops(fms);
        result.getBattle().getPrimary(result.getBattle().getNonPlayerSide()).getMemoryWithoutUpdate().set("$exotica_drops", potentialDrops, 0);
    }

    private static Pair<Map<Upgrade, Map<Integer, Integer>>, Map<Exotic, Integer>> getDrops(List<FleetMemberAPI> fms) {
        Map<Upgrade, Map<Integer, Integer>> upgradesMap = new HashMap<>();
        Map<Exotic, Integer> exotics = new HashMap<>();

        for (FleetMemberAPI fm : fms) {
            ShipModifications mods = ETModPlugin.getData(fm.getId());
            if (mods != null) {
                for (Map.Entry<Upgrade, Integer> upgData : mods.getUpgradeMap().entrySet()) {
                    Upgrade upgrade = upgData.getKey();
                    int level = upgData.getValue();

                    if (!upgradesMap.containsKey(upgrade)) {
                        upgradesMap.put(upgrade, new HashMap<Integer, Integer>());
                    }

                    Map<Integer, Integer> perUpgradeMap = upgradesMap.get(upgrade);
                    if (!perUpgradeMap.containsKey(level)) {
                        perUpgradeMap.put(level, 1);
                    } else {
                        perUpgradeMap.put(level, perUpgradeMap.get(level) + 1);
                    }
                }

                for (Exotic exotic : mods.getExoticSet()) {
                    if (!exotic.canDropFromFleets()) {
                        continue;
                    }

                    if (!exotics.containsKey(exotic)) {
                        exotics.put(exotic, 1);
                    } else {
                        exotics.put(exotic, exotics.get(exotic) + 1);
                    }
                }
            }
        }

        return new Pair<>(upgradesMap, exotics);
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
                    ExoticaTechHM.addToFleetMember(fm);
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

        if (mergeCheck && !Global.getSector().getCampaignUI().isShowingMenu()) {
            mergeCheck = false;
            Utilities.mergeChipsIntoCrate(Global.getSector().getPlayerFleet().getCargo());
        }

        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.REFIT) {
            FleetMemberUtils.moduleMap.clear();
        }

        boolean checkFleets = false;

        cleaningInterval.advance(v);
        if (checkFleets || cleaningInterval.intervalElapsed()) {
            cleaningInterval.setElapsed(0f);

            checkNearbyFleets();

            //disgustingly get a ships of the fmIds
            String[] fmIds = ETModPlugin.getShipModificationMap().keySet().toArray(new String[0]);
            for (String fmId : fmIds) {
                if (findFM(fmId) == null) {

                    if (!isInFleet(fmId, Global.getSector().getPlayerFleet())) {
                        ETModPlugin.removeData(fmId);
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

    @Override
    public void reportEconomyTick(int iterIndex) {

    }

    @Override
    public void reportEconomyMonthEnd() {
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            for (String submarketId : submarketIdsToCheckForSpecialItems) {
                if (market.hasSubmarket(submarketId)) {
                    SubmarketAPI submarket = market.getSubmarket(submarketId);
                    CargoAPI cargo = submarket.getCargoNullOk();

                    if (cargo == null) continue;
                    for (CargoStackAPI stack : cargo.getStacksCopy()) {
                        if (shouldRemoveStackFromSubmarket(stack)) {
                            cargo.removeStack(stack);
                        }
                    }
                }
            }
        }
    }

    private static boolean shouldRemoveStackFromSubmarket(CargoStackAPI stack) {
        if (stack.isSpecialStack() && stack.getSpecialDataIfSpecial() != null) {
            String specialId = stack.getSpecialDataIfSpecial().getId();
            if (specialId.startsWith("et_")) {
                return true;
            }
        }
        return false;
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
        List<CampaignFleetAPI> fleets = Global.getSector().getCurrentLocation().getFleets();
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

    private static void checkNearbyFleets() {
        boolean removedFleet = false;
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        for (Iterator<CampaignFleetAPI> it = activeFleets.iterator(); it.hasNext(); ) {
            CampaignFleetAPI fleet = it.next();
            if (fleet.equals(playerFleet)) continue;

            if (!fleet.isAlive()
                    || !Objects.equals(fleet.getContainingLocation(), playerFleet.getContainingLocation())) {
                removedFleet = true;
            }

            if (removedFleet) {
                dlog(String.format("Fleet %s was not found in player location", fleet.getNameWithFaction()));
                removeExtraSystemsFromFleet(fleet);
                it.remove();
            }
        }

    }

    private static void removeExtraSystemsFromFleet(CampaignFleetAPI fleet) {
        if (fleet.getFleetData() == null || fleet.getFleetData().getMembersListCopy() == null) {
            dlog("Fleet data was null");
            return;
        }

        dlog(String.format("Removing mods for fleet %s", fleet.getNameWithFaction()));

        if (fleet.equals(Global.getSector().getPlayerFleet())) {
            dlog("This fleet is the player fleet.");
            return;
        }

        for (FleetMemberAPI fm : fleet.getFleetData().getMembersListCopy()) {
            if (!isInFleet(fm.getId(), Global.getSector().getPlayerFleet())) {
                dlog(String.format("Removed mods for member %s", fm.getId()));
                ETModPlugin.removeData(fm.getId());
            }
        }
    }

    public static void applyExtraSystemsToFleet(CampaignFleetAPI fleet) {
        int hash = fleet.getId().hashCode();
        ShipModFactory.getRandom().setSeed(hash);

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

        for (FleetMemberAPI fleetMember : fleet.getFleetData().getMembersListCopy()) {
            if (fleetMember.isFighterWing()) continue;

            if (fm.equals(fleetMember)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInFleet(String fmId, CampaignFleetAPI fleet) {
        if (fleet == null) {
            return false;
        }

        for (FleetMemberAPI fleetMember : fleet.getFleetData().getMembersListCopy()) {
            if (fleetMember.isFighterWing()) continue;

            if (fmId.equals(fleetMember.getId())) {
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

            for (int i = 0; i < args.length; i++) {
                values[i] = String.valueOf(args[i]);
            }

            log.info(String.format(format, (Object[]) values));
        }
    }
}