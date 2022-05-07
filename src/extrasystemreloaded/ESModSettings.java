package extrasystemreloaded;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ESModSettings {
    public static String RANDOM_BANDWIDTH = "useRandomBandwidth";
    public static String STARTING_BANDWIDTH = "baseBandwidth";
    public static String MAX_BANDWIDTH = "maxBandwidth";

    public static String SHIPS_KEEP_UPGRADES_ON_DEATH = "shipsKeepUpgradesOnDeath";
    public static String UPGRADE_ALWAYS_SUCCEED = "upgradeAlwaysSucceed";
    public static String UPGRADE_FAILURE_CHANCE = "upgradeFailureMinFactor";
    public static String HULL_COST_BASE_FACTOR = "hullCostBaseFactor";
    public static String HULL_COST_DIMINISHING_MAXIMUM = "hullCostDiminishingMaximum";
    public static String UPGRADE_COST_MIN_FACTOR = "upgradeCostMinFactor";
    public static String UPGRADE_COST_MAX_FACTOR = "upgradeCostMaxFactor";
    public static String UPGRADE_COST_DIVIDING_RATIO = "upgradeCostDividingRatio";

    public static String MAX_UPGRADES_FRIGATE = "frigateMaxUpgrades";
    public static String MAX_UPGRADES_DESTROYER = "destroyerMaxUpgrades";
    public static String MAX_UPGRADES_CRUISER = "cruiserMaxUpgrades";
    public static String MAX_UPGRADES_CAPITAL = "capitalMaxUpgrades";

    public static String SCALING_CURVES = "scalingCurves";

    private static JSONObject modSettings = null;

    private static final Map<ShipAPI.HullSize, Integer> HULLSIZE_TO_MAXLEVEL = new HashMap<>();
    private static final Map<ShipAPI.HullSize, Float> HULLSIZE_FACTOR = new HashMap<>();

    public static Object get(String key) {
        try {
            return getModSettings().get(key);
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean getBoolean(String key) {
        return (boolean) get(key);
    }

    public static int getInt(String key) {
        Object value = get(key);
        if(value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("The value [%s] in the Extra Systems Reloaded config is not a number.");
    }

    public static float getFloat(String key) {
        Object value = get(key);
        if(value instanceof Number) {
            return ((Number) value).floatValue();
        }
        throw new IllegalArgumentException("The value [%s] in the Extra Systems Reloaded config is not a number.");
    }

    public static JSONObject getObject(String key) {
        return (JSONObject) get(key);
    }

    public static JSONArray getArray(String key) {
        return (JSONArray) get(key);
    }

    public static JSONObject getModSettings() {
        if(modSettings == null) {
            loadModSettings();
        }

        return modSettings;
    }

    public static Map<ShipAPI.HullSize, Integer> getHullSizeToMaxLevel() {
        if (HULLSIZE_TO_MAXLEVEL == null) {
            loadModSettings();
        }

        return HULLSIZE_TO_MAXLEVEL;
    }

    public static Map<ShipAPI.HullSize, Float> getHullSizeFactors() {
        if (HULLSIZE_FACTOR == null) {
            loadModSettings();
        }

        return HULLSIZE_FACTOR;
    }

    public static void loadModSettings() {
        try {
            modSettings = Global.getSettings().loadJSON("data/config/settings.json", "extra_system_reloaded");
        } catch (JSONException | IOException ex) {
            throw new RuntimeException(ex);
        }

        //fill the helper maps
        int frigateMaxUpgrades = getInt("frigateMaxUpgrades");
        int destroyerMaxUpgrades = getInt("destroyerMaxUpgrades");
        int cruiserMaxUpgrades = getInt("cruiserMaxUpgrades");
        int capitalMaxUpgrades = getInt("capitalMaxUpgrades");

        HULLSIZE_TO_MAXLEVEL.put(ShipAPI.HullSize.FIGHTER, frigateMaxUpgrades);
        HULLSIZE_TO_MAXLEVEL.put(ShipAPI.HullSize.DEFAULT, frigateMaxUpgrades);
        HULLSIZE_TO_MAXLEVEL.put(ShipAPI.HullSize.FRIGATE, frigateMaxUpgrades);
        HULLSIZE_TO_MAXLEVEL.put(ShipAPI.HullSize.DESTROYER, destroyerMaxUpgrades);
        HULLSIZE_TO_MAXLEVEL.put(ShipAPI.HullSize.CRUISER, cruiserMaxUpgrades);
        HULLSIZE_TO_MAXLEVEL.put(ShipAPI.HullSize.CAPITAL_SHIP, capitalMaxUpgrades);
        calculateHullSizeFactors();
    }

    public static void calculateHullSizeFactors() {
        float lowestMax = Integer.MAX_VALUE;
        for(ShipAPI.HullSize hullSize : HULLSIZE_TO_MAXLEVEL.keySet()) {
            if(HULLSIZE_TO_MAXLEVEL.get(hullSize) < lowestMax) {
                lowestMax = HULLSIZE_TO_MAXLEVEL.get(hullSize);
            }
        }

        for(ShipAPI.HullSize hullSize : HULLSIZE_TO_MAXLEVEL.keySet()) {
            HULLSIZE_FACTOR.put(hullSize, lowestMax / HULLSIZE_TO_MAXLEVEL.get(hullSize));
        }
    }
}
