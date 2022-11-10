package exoticatechnologies.modifications.exotics;

import com.fs.starfarer.api.Global;
import data.scripts.util.MagicSettings;
import exoticatechnologies.modifications.exotics.impl.HullmodExotic;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.extern.log4j.Log4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.List;

@Log4j
public class ExoticsHandler {
    private static final int EXOTIC_OPTION_ORDER = 2;
    public static final Map<String, Exotic> EXOTICS = new HashMap<>();
    public static final List<Exotic> EXOTIC_LIST = new ArrayList<>();

    public static void initialize() {
        ExoticsHandler.populateExotics();
    }

    public static void populateExotics() {
        try {
            JSONObject settings = Global.getSettings().getMergedJSONForMod("data/config/exotics.json", "exoticatechnologies");

            Iterator augIterator = settings.keys();
            while(augIterator.hasNext()) {
                String exoticKey = (String) augIterator.next();

                if(EXOTICS.containsKey(exoticKey)) return;

                JSONObject exoticSettings = settings.getJSONObject(exoticKey);
                Exotic exotic = null;

                if (exoticSettings.has("exoticClass")) {
                    Class<?> clzz = Global.getSettings().getScriptClassLoader().loadClass(exoticSettings.getString("exoticClass"));

                    //magic to get around reflection block
                    exotic = (Exotic) MethodHandles.lookup().findConstructor(clzz, MethodType.methodType(void.class, String.class, JSONObject.class))
                            .invoke(exoticKey, exoticSettings);

                    if (exotic.shouldLoad()) {
                        exotic.setDescription(StringUtils.getString(exoticKey, "description"));
                        exotic.setTooltip(StringUtils.getString(exoticKey, "tooltip"));
                    } else {
                        exotic = null;
                    }
                } else if (exoticSettings.has("exoticHullmod")) {
                    JSONArray colorArr = exoticSettings.optJSONArray("exoticColor");
                    Color mainColor = Utilities.colorFromJSONArray(colorArr);

                    String hullmodId = exoticSettings.getString("exoticHullmod");
                    String exoticStatDescriptionStringKey = exoticSettings.getString("exoticStatDescriptionStringKey");

                    exotic = new HullmodExotic(exoticKey, exoticSettings, hullmodId, exoticStatDescriptionStringKey, mainColor);

                    exotic.setDescription(StringUtils.getString(exoticKey, "description"));
                    exotic.setTooltip(StringUtils.getString(exoticKey, "tooltip"));
                }

                if (exotic != null) {
                    ExoticsHandler.addExotic(exotic);

                    log.info(String.format("loaded exotic [%s]", exotic.getName()));
                }
            }
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void addExotic(Exotic exotic) {
        EXOTICS.put(exotic.getKey(), exotic);
        EXOTIC_LIST.add(exotic);

        log.info(String.format("initialized exotic [%s]", exotic.getName()));
    }

    //whitelist for faction
    public static List<String> getWhitelistForFaction(String faction) {
        List<String> factionAllowedExotics = MagicSettings.getList("exoticatechnologies", "rngExoticWhitelist");
        try {
            if(MagicSettings.modSettings.getJSONObject("exoticatechnologies").has(faction + "_ExoticWhitelist")) {
                factionAllowedExotics = MagicSettings.getList("exoticatechnologies", faction + "_ExoticWhitelist");
            }
        } catch (JSONException ex) {
            log.info("ET modSettings object doesn't exist. Is this a bug in MagicLib, or did you remove it?");
            log.info("The actual exception follows.", ex);
        }
        return factionAllowedExotics;
    }
}
