package extrasystemreloaded.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.util.Misc;

import java.util.*;
import java.util.List;

public class Utilities {

    private Utilities() {
    }

    public static final List<String> RESOURCES_LIST = new ArrayList<String>() {{
        add("supplies");
        add("volatiles");
        add("organics");
        add("hand_weapons");
        add("metals");
        add("rare_metals");
        add("heavy_machinery");
    }};

    public static boolean isInside(float arg, float a, float b) {
        return (arg >= a && arg < b);
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
        Map<String, Integer> remainingResources = new HashMap<>();
        remainingResources.putAll(resources);

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
}
