package exoticatechnologies.modifications.exotics;

import com.fs.starfarer.api.Global;
import data.scripts.util.MagicSettings;
import exoticatechnologies.dialog.modifications.SystemOptionsHandler;
import exoticatechnologies.modifications.exotics.dialog.ExoticsPickerState;
import exoticatechnologies.util.StringUtils;
import lombok.extern.log4j.Log4j;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

@Log4j
public class ExoticsHandler {
    private static int EXOTIC_OPTION_ORDER = 2;
    public static final Map<String, Exotic> EXOTICS = new HashMap<>();
    public static final List<Exotic> EXOTIC_LIST = new ArrayList<>();
    public static final ExoticsPickerState EXOTICS_PICKER_DIALOG = new ExoticsPickerState();

    public static void initialize() {
        SystemOptionsHandler.addOption(EXOTICS_PICKER_DIALOG);
        ExoticsHandler.populateExotics();
    }

    public static void populateExotics() {
        try {
            JSONObject settings = Global.getSettings().getMergedJSONForMod("data/config/exotics.json", "extra_system_reloaded");

            Iterator augIterator = settings.keys();
            while(augIterator.hasNext()) {
                String augKey = (String) augIterator.next();

                if(EXOTICS.containsKey(augKey)) return;

                JSONObject augObj = (JSONObject) settings.getJSONObject(augKey);

                Class<?> clzz = Global.getSettings().getScriptClassLoader().loadClass(augObj.getString("exoticClass"));
                Exotic exotic = (Exotic) clzz.newInstance();

                if(exotic.shouldLoad()) {
                    exotic.setKey(augKey);
                    exotic.setName(StringUtils.getString(augKey, "name"));
                    exotic.setDescription(StringUtils.getString(augKey, "description"));
                    exotic.setTooltip(StringUtils.getString(augKey, "tooltip"));
                    exotic.setConfig(augObj);

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
