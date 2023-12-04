package exoticatechnologies;

import com.fs.starfarer.api.Global;
import org.magiclib.util.MagicSettings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ETModSettings {
    public static final String RANDOM_BANDWIDTH = "useRandomBandwidth";
    public static final String STARTING_BANDWIDTH = "baseBandwidth";
    public static final String SHIPS_KEEP_UPGRADES_ON_DEATH = "shipsKeepUpgradesOnDeath";
    public static final String MARKET_EXOTIC_SCALE = "marketExoticScale";
    public static final String MARKET_UPGRADE_SCALE = "marketUpgradeScale";
    public static final String INDUSTRY_PRODUCTION_BANDWIDTH_MULT = "industryBandwidthGenerationMultipliers";
    public static final String MAX_EXOTICS_KEY = "maxExotics";

    public static int MAX_EXOTICS = 2;

    private static JSONObject modSettings = null;
    private static Map<String, Float> productionBandwidthMults = new HashMap<>();

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
        throw new IllegalArgumentException("The value [%s] in the Exotica Technologies config is not a number.");
    }

    public static float getFloat(String key) {
        Object value = get(key);
        if(value instanceof Number) {
            return ((Number) value).floatValue();
        }
        throw new IllegalArgumentException("The value [%s] in the Exotica Technologies config is not a number.");
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

    public static Map<String, Float> getProductionBandwidthMults() {
        return productionBandwidthMults;
    }

    public static void loadModSettings() {
        try {
            modSettings = Global.getSettings().loadJSON("data/config/settings.json", "exoticatechnologies");
            modSettings.put(MARKET_EXOTIC_SCALE, MagicSettings.getFloat("exoticatechnologies", MARKET_EXOTIC_SCALE));
            modSettings.put(MARKET_UPGRADE_SCALE, MagicSettings.getFloat("exoticatechnologies", MARKET_UPGRADE_SCALE));

            MAX_EXOTICS  = MagicSettings.getInteger("exoticatechnologies", MAX_EXOTICS_KEY);

            productionBandwidthMults = MagicSettings.getFloatMap("industryBandwidthGenerationMultipliers", INDUSTRY_PRODUCTION_BANDWIDTH_MULT);
        } catch (JSONException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
