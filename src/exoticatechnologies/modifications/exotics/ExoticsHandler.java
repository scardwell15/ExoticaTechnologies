package exoticatechnologies.modifications.exotics;

import com.fs.starfarer.api.Global;
import data.scripts.util.MagicSettings;
import exoticatechnologies.modifications.exotics.impl.HullmodExotic;
import exoticatechnologies.util.StringUtils;
import lombok.extern.log4j.Log4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
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
                String augKey = (String) augIterator.next();

                if(EXOTICS.containsKey(augKey)) return;

                JSONObject augObj = (JSONObject) settings.getJSONObject(augKey);
                Exotic exotic = null;

                if (augObj.has("exoticClass")) {
                    Class<?> clzz = Global.getSettings().getScriptClassLoader().loadClass(augObj.getString("exoticClass"));
                    exotic = (Exotic) clzz.newInstance();

                    if (exotic.shouldLoad()) {
                        exotic.setKey(augKey);
                        exotic.setName(StringUtils.getString(augKey, "name"));
                        exotic.setDescription(StringUtils.getString(augKey, "description"));
                        exotic.setTooltip(StringUtils.getString(augKey, "tooltip"));
                        exotic.setConfig(augObj);
                    } else {
                        exotic = null;
                    }
                } else if (augObj.has("exoticHullmod")) {
                    JSONArray colorArr = augObj.optJSONArray("exoticColor");
                    Color mainColor = new Color(colorArr.getInt(0), colorArr.getInt(1), colorArr.getInt(2), colorArr.optInt(3, 255));

                    String hullmodId = augObj.getString("exoticHullmod");
                    String tooltipStringKey = augObj.getString("exoticTooltipStringKey");

                    exotic = new HullmodExotic(hullmodId, tooltipStringKey, mainColor);

                    exotic.setKey(augKey);
                    exotic.setName(StringUtils.getString(augKey, "name"));
                    exotic.setDescription(StringUtils.getString(augKey, "description"));
                    exotic.setTooltip(StringUtils.getString(augKey, "tooltip"));
                    exotic.setConfig(augObj);
                }

                if (exotic != null) {
                    ExoticsHandler.addExotic(exotic);

                    log.info(String.format("loaded exotic [%s]", exotic.getName()));
                }
            }
        } catch (JSONException | IOException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void loadConfigs() {
        try {
            JSONObject settings = Global.getSettings().getMergedJSONForMod("data/config/exotics.json", "exoticatechnologies");

            for (Exotic exotic : EXOTIC_LIST) {
                if (!settings.has(exotic.getKey())) {
                    continue;
                }

                exotic.loadConfig();
            }
        } catch (JSONException | IOException ex) {
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
            log.info("ESR modSettings object doesn't exist. Is this a bug in MagicLib, or did you remove it?");
            log.info("The actual exception follows.", ex);
        }
        return factionAllowedExotics;
    }
}
