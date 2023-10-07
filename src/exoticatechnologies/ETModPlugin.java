package exoticatechnologies;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.campaign.listeners.CampaignEventListener;
import exoticatechnologies.campaign.listeners.SalvageListener;
import exoticatechnologies.campaign.market.MarketManager;
import exoticatechnologies.cargo.CrateGlobalData;
import exoticatechnologies.cargo.CrateSpecialData;
import exoticatechnologies.config.FactionConfigLoader;
import exoticatechnologies.config.VariantConfigLoader;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.integration.indevo.IndEvoUtil;
import exoticatechnologies.modifications.ShipModLoader;
import exoticatechnologies.modifications.exotics.ExoticsHandler;
import exoticatechnologies.modifications.stats.impl.logistics.CrewSalaryEffect;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import exoticatechnologies.refit.RefitButtonAdder;
import exoticatechnologies.ui.impl.shop.ShopManager;
import exoticatechnologies.ui.impl.shop.overview.OverviewPanelUIPlugin;
import exoticatechnologies.util.FleetMemberUtils;
import exoticatechnologies.util.Utilities;
import lombok.extern.log4j.Log4j;
import org.lazywizard.console.Console;
import org.lazywizard.lazylib.MathUtils;

@Log4j
public class ETModPlugin extends BaseModPlugin {
    private static boolean debugUpgradeCosts = false;
    private static CampaignEventListener campaignListener = null;
    private static SalvageListener salvageListener = null;

    @Override
    public void onApplicationLoad() {
        ETModSettings.loadModSettings();

        UpgradesHandler.initialize();
        ExoticsHandler.initialize();
        FactionConfigLoader.load();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        FleetMemberUtils.moduleMap.clear();
        ShopManager.getShopMenuUIPlugins().clear();

        ETModSettings.loadModSettings();

        ShopManager.addMenu(new OverviewPanelUIPlugin());
        UpgradesHandler.initialize();
        ExoticsHandler.initialize();
        VariantConfigLoader.INSTANCE.loadConfigs();
        MarketManager.INSTANCE.initialize();

        if (Global.getSettings().getModManager().isModEnabled("IndEvo")) {
            IndEvoUtil.loadIntegration();
        }

        addListeners();

        Utilities.mergeChipsIntoCrate(Global.getSector().getPlayerFleet().getCargo());
    }

    public static String getSectorSeedString() {
        if (!Global.getSector().getPersistentData().containsKey("ET_seedString")) {
            Global.getSector().getPersistentData().put("ET_seedString", String.valueOf(MathUtils.getRandomNumberInRange(1000, 10000)).hashCode());
        }
        return String.valueOf(Global.getSector().getPersistentData().get("ET_seedString"));
    }

    @Override
    public void beforeGameSave() {
        CampaignEventListener.Companion.getActiveFleets().clear();
        removeListeners();
        ETModPlugin.removeHullmodsFromAutoFitGoalVariants();
    }

    @Override
    public void afterGameSave() {
        addListeners();
    }

    private static void addListeners() {
        campaignListener = new CampaignEventListener(false);
        Global.getSector().getListenerManager().addListener(salvageListener = new SalvageListener(), true);
        Global.getSector().addTransientScript(campaignListener);
        Global.getSector().addTransientScript(new RefitButtonAdder());
        Global.getSector().addListener(campaignListener);
    }

    public static void removeListeners() {
        Global.getSector().removeTransientScript(campaignListener);
        Global.getSector().removeListener(campaignListener);
        Global.getSector().getListenerManager().removeListener(salvageListener);

        if (Global.getSector().getListenerManager().hasListener(CrewSalaryEffect.SalaryListener.class)) {
            Global.getSector().getListenerManager().removeListener(CrewSalaryEffect.SalaryListener.class);
        }
    }

    public static void removeHullmodsFromAutoFitGoalVariants() {
        try {
            for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
                for (ShipVariantAPI v : Global.getSector().getAutofitVariants().getTargetVariants(spec.getHullId())) {
                    if (v != null) ExoticaTechHM.removeHullModFromVariant(v);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeDataFromFleetsAndMarkets() {
        try {
            for (LocationAPI loc : Global.getSector().getAllLocations()) {
                for (CampaignFleetAPI campaignFleetAPI : loc.getFleets()) {
                    for (FleetMemberAPI member : campaignFleetAPI.getFleetData().getMembersListCopy()) {
                        ExoticaTechHM.removeHullModFromVariant(member.getVariant());
                        ShipModLoader.remove(member, member.getVariant());
                        Console.showMessage("Cleared ET data from: " + member.getShipName());
                    }
                }

                for (SectorEntityToken token : loc.getAllEntities()) {
                    if (token.getMarket() != null) {
                        for (SubmarketAPI submarket : token.getMarket().getSubmarketsCopy()) {
                            if (submarket.getCargoNullOk() != null) {
                                removeAllCargo(submarket.getCargo());
                            }
                        }
                    }

                    try {
                        if (token.getCargo() != null) {
                            removeAllCargo(token.getCargo());
                        }
                    } finally {
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        removeHullmodsFromAutoFitGoalVariants();
    }

    public static void removeAllCargo(CargoAPI cargo) {
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            if (stack.isSpecialStack() && stack.getSpecialItemSpecIfSpecial().hasTag("exotica")) {
                stack.getCargo().removeStack(stack);
            }
        }

        if (cargo.getFleetData() != null) {
            for (FleetMemberAPI member : cargo.getFleetData().getMembersListCopy()) {
                if (member.getVariant() != null) {
                    ExoticaTechHM.removeHullModFromVariant(member.getVariant());
                }
                ShipModLoader.remove(member, member.getVariant());
            }
        }

        if (cargo.getMothballedShips() != null) {
            for (FleetMemberAPI member : cargo.getMothballedShips().getMembersListCopy()) {
                if (member.getVariant() != null) {
                    ExoticaTechHM.removeHullModFromVariant(member.getVariant());
                }
                ShipModLoader.remove(member, member.getVariant());
            }
        }
    }

    public static void setDebugUpgradeCosts(boolean set) {
        debugUpgradeCosts = set;
    }

    public static boolean isDebugUpgradeCosts() {
        return debugUpgradeCosts;
    }
}
