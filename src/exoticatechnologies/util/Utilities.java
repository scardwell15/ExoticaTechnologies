package exoticatechnologies.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import exoticatechnologies.cargo.CrateGlobalData;
import exoticatechnologies.cargo.CrateItemPlugin;
import exoticatechnologies.cargo.CrateSpecialData;
import exoticatechnologies.modifications.ModSpecialItemPlugin;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin;
import exoticatechnologies.modifications.exotics.GenericExoticItemPlugin;
import exoticatechnologies.modifications.exotics.types.ExoticType;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Utilities {

    private Utilities() {
    }

    public static final String STORY_POINTS = "STORYPOINTS";
    public static final List<String> RESOURCES_LIST = new ArrayList<String>() {{
        add("supplies");
        add("volatiles");
        add("organics");
        add("hand_weapons");
        add("metals");
        add("rare_metals");
        add("heavy_machinery");
    }};

    public static Color colorFromJSONArray(JSONArray arr) throws JSONException {
        return new Color(arr.getInt(0), arr.getInt(1), arr.getInt(2), arr.optInt(3, 255));
    }

    public static boolean isInside(float arg, float a, float b) {
        return (arg >= a && arg < b);
    }

    public static int getSModCount(ShipVariantAPI var) {
        if (var.getSMods() != null) {
            return var.getSMods().size();
        }
        return 0;
    }

    public static int getSModCount(FleetMemberAPI fm) {
        if (fm.getVariant() != null) {
            return getSModCount(fm.getVariant());
        }
        return 0;
    }

    public static int getSModCount(CampaignFleetAPI fleet) {
        int smods = 0;
        for (FleetMemberAPI fm : fleet.getMembersWithFightersCopy()) {
            if (fm.isFighterWing()) continue;
            smods += getSModCount(fm);
        }

        return smods;
    }

    public static String getItemName(String id) {
        CommoditySpecAPI commodity = Global.getSettings().getCommoditySpec(id);
        if (commodity != null) {
            return commodity.getName();
        }

        SpecialItemSpecAPI specialSpec = Global.getSettings().getSpecialItemSpec(id);
        if (specialSpec != null) {
            return specialSpec.getName();
        }

        return id;
    }

    public static float getItemPrice(String id) {
        CommoditySpecAPI commodity = Global.getSettings().getCommoditySpec(id);
        if (commodity != null) {
            return commodity.getBasePrice();
        }

        SpecialItemSpecAPI specialSpec = Global.getSettings().getSpecialItemSpec(id);
        if (specialSpec != null) {
            return specialSpec.getBasePrice();
        }

        return 0f;
    }


    public static CargoStackAPI getStack(CargoAPI cargo, String id) {
        CommoditySpecAPI commodity = Global.getSettings().getCommoditySpec(id);
        if (commodity != null) {
            for(CargoStackAPI stack : cargo.getStacksCopy()) {
                if(stack.isCommodityStack() && stack.getCommodityId().equals(id)) {
                    return stack;
                }
            }
        }

        SpecialItemSpecAPI specialSpec = Global.getSettings().getSpecialItemSpec(id);
        if (specialSpec != null) {
            for(CargoStackAPI stack : cargo.getStacksCopy()) {
                if(stack.isSpecialStack() && stack.getSpecialDataIfSpecial().getId().equals(id)) {
                    return stack;
                }
            }
        }
        return null;
    }


    public static float getItemQuantity(CargoAPI cargo, String id) {
        if (cargo == null) return 0f;

        CommoditySpecAPI commodity = Global.getSettings().getCommoditySpec(id);
        if (commodity != null) {
            return cargo.getCommodityQuantity(id);
        }

        SpecialItemSpecAPI specialSpec = Global.getSettings().getSpecialItemSpec(id);
        if (specialSpec != null) {
            float quantity = 0f;
            for(CargoStackAPI stack : cargo.getStacksCopy()) {
                if(stack.isSpecialStack() && stack.getSpecialDataIfSpecial().getId().equals(id)) {
                    quantity += stack.getSize();
                }
            }
            return quantity;
        }

        return 0f;
    }

    public static boolean hasItem(CargoAPI cargo, String id) {
        return getItemQuantity(cargo, id) > 0f;
    }

    public static float takeItemQuantity(CargoAPI cargo, String id, float quantity) {
        float takenItems = 0f;

        CommoditySpecAPI commodity = Global.getSettings().getCommoditySpec(id);
        if (commodity != null) {
            takenItems = Math.min(cargo.getCommodityQuantity(id), quantity);
            cargo.removeCommodity(id, quantity);
        }

        SpecialItemSpecAPI specialSpec = Global.getSettings().getSpecialItemSpec(id);
        if (specialSpec != null) {
            for(CargoStackAPI stack : cargo.getStacksCopy()) {
                if(stack.isSpecialStack() && stack.getSpecialDataIfSpecial().getId().equals(id)) {

                    takenItems = Math.min(quantity, stack.getSize());
                    stack.subtract(takenItems);

                    if(stack.getSize() <= 0) {
                        cargo.removeStack(stack);
                    }

                    if(quantity <= 0) {
                        break;
                    }
                }
            }
        }

        quantity -= takenItems;
        return quantity;
    }

    public static void takeResources(CampaignFleetAPI fleet, MarketAPI market, Map<String, Integer> resources) {
        Map<String, Integer> remainingResources = new HashMap<>(resources);

        if (market != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE) != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo() != null) {

            CargoAPI storageCargo = market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();

            for (Map.Entry<String, Integer> upgradeCost : resources.entrySet()) {
                int remaining = (int) Utilities.takeItemQuantity(storageCargo, upgradeCost.getKey(), upgradeCost.getValue());
                remainingResources.put(upgradeCost.getKey(), remaining);
            }
        }

        CargoAPI fleetCargo = fleet.getCargo();
        for (Map.Entry<String, Integer> upgradeCost : remainingResources.entrySet()) {

            if (upgradeCost.getValue() <= 0) {
                continue;
            }

            int remaining = (int) Utilities.takeItemQuantity(fleetCargo, upgradeCost.getKey(), upgradeCost.getValue());
            remainingResources.put(upgradeCost.getKey(), remaining);
        }
    }

    public static Integer getTotalQuantity(CampaignFleetAPI fleet, MarketAPI market, String resource) {
        int quantity = (int) getItemQuantity(fleet.getCargo(), resource);

        if (market != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE) != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo() != null) {
            quantity += getItemQuantity(market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo(), resource);
        }

        return quantity;
    }

    public static Map<String, Integer> getTotalResources(CampaignFleetAPI fleet, MarketAPI market, Set<String> resources) {
        Map<String, Integer> finalStacks = new HashMap<>();

        for(String resourceId : resources) {
            finalStacks.put(resourceId, getTotalQuantity(fleet, market, resourceId));
        }

        return finalStacks;
    }

    public static void addItem(CampaignFleetAPI fleet, String id, int quantity) {
        CargoAPI storage = fleet.getCargo();

        if (storage != null) {
            CargoStackAPI stack = Utilities.getStack(storage, id);

            if(stack != null) {
                //add to existing stack
                stack.add(quantity);
            } else {
                //create new stack
                addStack(storage, id, quantity);
            }
        }
    }

    public static void addStack(CargoAPI cargo, String id, int quantity) {
        CommoditySpecAPI commodity = Global.getSettings().getCommoditySpec(id);
        if (commodity != null) {
            cargo.addCommodity(id, quantity);
        }

        SpecialItemSpecAPI specialSpec = Global.getSettings().getSpecialItemSpec(id);
        if (specialSpec != null) {
            cargo.addSpecial(new SpecialItemData(id, null), quantity);
        }
    }

    public static boolean hasChip(CargoAPI cargo, String id) {
        if (cargo == null) return false;

        return hasExoticChip(cargo, id) || hasUpgradeChip(cargo, id);
    }

    public static CargoStackAPI getExoticChip(CargoAPI cargo, String id) {
        if (cargo == null) return null;

        for(CargoStackAPI stack : cargo.getStacksCopy()) {
            if(stack.isSpecialStack()) {
                if (stack.getPlugin() instanceof GenericExoticItemPlugin) {
                    GenericExoticItemPlugin exoticPlugin = (GenericExoticItemPlugin) stack.getPlugin();
                    if (exoticPlugin.getModId().equals(id)) {
                        return stack;
                    }
                } else if (stack.getPlugin() instanceof CrateItemPlugin) {
                    CrateItemPlugin plugin = (CrateItemPlugin) stack.getPlugin();
                    CargoStackAPI crateStack = getExoticChip(plugin.getCargo(), id);
                    if (crateStack != null) {
                        return crateStack;
                    }
                }
            }
        }

        return null;
    }

    public static CargoStackAPI getExoticChip(CargoAPI cargo, String id, String type) {
        if (cargo == null) return null;

        for(CargoStackAPI stack : cargo.getStacksCopy()) {
            if(stack.isSpecialStack()) {
                if (stack.getPlugin() instanceof GenericExoticItemPlugin) {
                    GenericExoticItemPlugin exoticPlugin = (GenericExoticItemPlugin) stack.getPlugin();
                    if (exoticPlugin.getModId().equals(id) && exoticPlugin.getExoticData() != null && exoticPlugin.getExoticData().getType().getNameKey().equals(type)) {
                        return stack;
                    }
                } else if (stack.getPlugin() instanceof CrateItemPlugin) {
                    CrateItemPlugin plugin = (CrateItemPlugin) stack.getPlugin();
                    CargoStackAPI crateStack = getExoticChip(plugin.getCargo(), id, type);
                    if (crateStack != null) {
                        return crateStack;
                    }
                }
            }
        }

        return null;
    }

    public static boolean hasExoticChip(CargoAPI cargo, String id) {
        return getExoticChip(cargo, id) != null;
    }

    public static void takeItem(CargoStackAPI stack) {
        stack.subtract(1);
        if (stack.getSize() == 0) {
            stack.getCargo().removeStack(stack);
        }
    }

    public static int countChips(CargoAPI cargo, String id) {
        if (cargo == null) return 0;

        int count = 0;
        for(CargoStackAPI stack : cargo.getStacksCopy()) {
            if(stack.isSpecialStack()) {
                if (stack.getPlugin() instanceof CrateItemPlugin) {
                    CrateItemPlugin plugin = (CrateItemPlugin) stack.getPlugin();
                    count += countChips(plugin.getCargo(), id);
                } else if (stack.getPlugin() instanceof ModSpecialItemPlugin) {
                    ModSpecialItemPlugin upgradeItem = (ModSpecialItemPlugin) stack.getPlugin();
                    if (id.equals(upgradeItem.getModId())) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    public static Set<ExoticType> getTypesInCargo(CargoAPI cargo, String id) {
        if (cargo == null) return new LinkedHashSet<>();

        Set<ExoticType> types = new LinkedHashSet<>();
        for(CargoStackAPI stack : cargo.getStacksCopy()) {
            if(stack.isSpecialStack()) {
                if (stack.getPlugin() instanceof CrateItemPlugin) {
                    CrateItemPlugin plugin = (CrateItemPlugin) stack.getPlugin();
                    types.addAll(getTypesInCargo(plugin.getCargo(), id));
                } else if (stack.getPlugin() instanceof ExoticSpecialItemPlugin) {
                    ExoticSpecialItemPlugin upgradeItem = (ExoticSpecialItemPlugin) stack.getPlugin();
                    if (id.equals(upgradeItem.getModId())) {
                        types.add(upgradeItem.getExoticData().getType());
                    }
                }
            }
        }

        return types;
    }

    public static CargoStackAPI getUpgradeChip(CargoAPI cargo, String id, int level) {
        if (cargo == null) return null;

        for(CargoStackAPI stack : cargo.getStacksCopy()) {
            if(stack.isSpecialStack()) {
                if (stack.getPlugin() instanceof CrateItemPlugin) {
                    CrateItemPlugin plugin = (CrateItemPlugin) stack.getPlugin();
                    CargoStackAPI crateStack = getUpgradeChip(plugin.getCargo(), id, level);
                    if (crateStack != null) {
                        stack = crateStack;
                    }
                }

                if (stack.getPlugin() instanceof UpgradeSpecialItemPlugin) {
                    UpgradeSpecialItemPlugin upgradeItem = (UpgradeSpecialItemPlugin) stack.getPlugin();
                    if (id.equals(upgradeItem.getModId()) && upgradeItem.getUpgradeLevel() == level) {
                        return stack;
                    }
                }
            }
        }

        return null;
    }

    public static CargoStackAPI getUpgradeChip(CargoAPI cargo, String id) {
        if (cargo == null) return null;

        CargoStackAPI winner = null;
        int winningLevel = 0;

        for(CargoStackAPI stack : cargo.getStacksCopy()) {
            if(stack.isSpecialStack()) {
                if (stack.getPlugin() instanceof CrateItemPlugin) {
                    CrateItemPlugin plugin = (CrateItemPlugin) stack.getPlugin();
                    CargoStackAPI crateStack = getUpgradeChip(plugin.getCargo(), id);
                    if (crateStack != null) {
                        stack = crateStack;
                    }
                }

                if (stack.getPlugin() instanceof UpgradeSpecialItemPlugin) {
                    UpgradeSpecialItemPlugin upgradeItem = (UpgradeSpecialItemPlugin) stack.getPlugin();
                    if (id.equals(upgradeItem.getModId()) && (winner == null || upgradeItem.getUpgradeLevel() > winningLevel)) {
                        winningLevel = upgradeItem.getUpgradeLevel();
                        winner = stack;
                    }
                }
            }
        }

        return winner;
    }

    /**
     * finds the highest level chip that can be installed for a specific upgrade. this takes into account bandwidth,
     * so if the only chips that can be found are too high level due to bandwidth then none will be returned.
     * @param cargo cargo
     * @param fm the ship
     * @param mods ship mods
     * @param upgrade the upgrade
     * @return the chip, or null if no chip can be found that can be installed, or if there is no chip for that upgrade.
     */
    public static CargoStackAPI getUpgradeChip(CargoAPI cargo, FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade) {
        if (cargo == null) return null;

        float shipBandwidth = mods.getBandwidthWithExotics(fm);
        float usedBandwidth = mods.getUsedBandwidth();

        CargoStackAPI winner = null;
        int winningLevel = 0;
        String id = upgrade.getKey();

        for(CargoStackAPI stack : cargo.getStacksCopy()) {
            if(stack.isSpecialStack()) {
                if (stack.getPlugin() instanceof CrateItemPlugin) {
                    CrateItemPlugin plugin = (CrateItemPlugin) stack.getPlugin();
                    CargoStackAPI crateStack = getUpgradeChip(plugin.getCargo(), fm, mods, upgrade);
                    if (crateStack != null) {
                        stack = crateStack;
                    }
                }

                if(stack.getPlugin() instanceof UpgradeSpecialItemPlugin) {
                    UpgradeSpecialItemPlugin upgradeItem = (UpgradeSpecialItemPlugin) stack.getPlugin();

                    if (id.equals(upgradeItem.getModId())) {
                        int level = upgradeItem.getUpgradeLevel();
                        if (level > mods.getUpgrade(upgrade)) {
                            float upgradeBandwidth = (level - mods.getUpgrade(upgrade)) * upgrade.getBandwidthUsage();
                            if ((usedBandwidth + upgradeBandwidth <= shipBandwidth) && (winner == null || upgradeItem.getUpgradeLevel() > winningLevel)) {
                                winningLevel = upgradeItem.getUpgradeLevel();
                                winner = stack;
                            }
                        }
                    }
                }
            }
        }

        return winner;
    }

    public static boolean hasUpgradeChip(CargoAPI cargo, String id) {
        return getUpgradeChip(cargo, id) != null;
    }

    public static CargoStackAPI getSpecialStack(CargoAPI cargo, String id, String params) {
        if (cargo == null) return null;

        for(CargoStackAPI stack : cargo.getStacksCopy()) {
            if(stack.isSpecialStack()) {
                if (stack.getPlugin() instanceof CrateItemPlugin) {
                    CrateItemPlugin plugin = (CrateItemPlugin) stack.getPlugin();
                    stack = getSpecialStack(plugin.getCargo(), id, params);
                    if (stack != null) {
                        return stack;
                    }
                } else {
                    SpecialItemData data = stack.getSpecialDataIfSpecial();
                    if (params != null && params.isEmpty()) {
                        params = null;
                    }
                    if (data.getId().equals(id) && Objects.equals(data.getData(), params)) {
                        return stack;
                    }
                }
            }
        }

        return null;
    }

    public static CargoStackAPI getSpecialStackWithData(CargoAPI cargo, String data) {
        if (cargo == null) return null;

        for(CargoStackAPI stack : cargo.getStacksCopy()) {
            if(stack.isSpecialStack()) {
                if (stack.getPlugin() instanceof CrateItemPlugin) {
                    CrateItemPlugin plugin = (CrateItemPlugin) stack.getPlugin();
                    stack = getSpecialStackWithData(plugin.getCargo(), data);
                    if (stack != null) {
                        return stack;
                    }
                } else {
                    SpecialItemData itemData = stack.getSpecialDataIfSpecial();
                    if (Objects.equals(itemData.getData(), data)) {
                        return stack;
                    }
                }
            }
        }

        return null;
    }


    public static void mergeChipsIntoCrate(CargoAPI cargo) {
        if (cargo == null) return;

        CrateGlobalData crateData = CrateGlobalData.getInstance();
        boolean hasCrate = false;

        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            if (stack.isSpecialStack()) {
                String specialId = stack.getSpecialDataIfSpecial().getId();
                if (specialId.equals("et_upgrade") || specialId.equals("et_exotic")) {
                    ModSpecialItemPlugin plugin = (ModSpecialItemPlugin) stack.getPlugin();
                    if (!plugin.getIgnoreCrate()) {
                        crateData.getCargo().addFromStack(stack);
                        cargo.removeStack(stack);
                    }
                } else if (stack.getSpecialDataIfSpecial() instanceof CrateSpecialData) {
                    CrateSpecialData data = (CrateSpecialData) stack.getSpecialDataIfSpecial();
                    if (data != null && data.getCargo() != null && !data.getCargo().isEmpty()) {
                        CrateGlobalData.addCargo(data.getCargo());
                        data.getCargo().removeAll(CrateGlobalData.getInstance().getCargo());
                    }
                    hasCrate = true;
                }
            }
        }

        if (!hasCrate) {
            CargoStackAPI stack = Global.getFactory().createCargoStack(CargoAPI.CargoItemType.SPECIAL, new CrateSpecialData(), cargo);
            stack.setSize(1);
            cargo.addFromStack(stack);
        }
    }

    public static String formatSpecialItem(SpecialItemData data) {
        return formatSpecialItem(data.getId(), data.getData());
    }

    public static String formatSpecialItem(String id, Object... data) {
        StringBuilder paramBuilder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            paramBuilder.append(data[i]);
            if (i < data.length - 1) {
                paramBuilder.append(",");
            }
        }

        return String.format("$%s(%s)", id, paramBuilder);
    }

    public static String getSpecialItemId(String specialKey) {
        return specialKey.substring(1, specialKey.indexOf("("));
    }

    public static String getSpecialItemParams(String specialKey) {
        return specialKey.substring(specialKey.indexOf("(") + 1, specialKey.lastIndexOf(")"));
    }

    public static boolean isSpecialItemId(String key) {
        return key.startsWith("$");
    }

    public static boolean isResourceString(String key) {
        return key.startsWith("&");
    }
}
