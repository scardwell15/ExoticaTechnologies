package exoticatechnologies.modifications.exotics;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.util.MagicSettings;
import lombok.extern.log4j.Log4j;
import org.json.JSONException;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Log4j
public class ExoticsGenerator {
    //per fleet member!
    private static float CHANCE_OF_EXOTICS = 0.1f;
    private static Random random = new Random();

    public static ETExotics generate(ShipVariantAPI var, long seed, String faction, float bandwidth) {
        Map<String, Float> factionExoticChances = MagicSettings.getFloatMap("exoticatechnologies", "factionExoticChances");
        Map<String, Float> factionPerExoticMult = MagicSettings.getFloatMap("exoticatechnologies", "factionPerExoticMult");

        List<String> factionAllowedExotics = getAllowedForFaction(faction);
        if (factionAllowedExotics.isEmpty()) {
            return null;
        }

        ETExotics exotics = new ETExotics();

        random.setSeed(seed);

        float exoticChance = CHANCE_OF_EXOTICS;
        if (factionExoticChances.containsKey(faction)) {
            exoticChance = factionExoticChances.get(faction);
        }

        if (random.nextFloat() < exoticChance) {
            float perExoticMult = 1.0f;
            if (factionPerExoticMult.containsKey(faction)) {
                perExoticMult = factionPerExoticMult.get(faction);
            }

            for (Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
                if (!factionAllowedExotics.contains(exotic.getKey())) {
                    continue;
                }

                if (exotic.canApply(var)) {
                    if (random.nextFloat() < exotic.getSpawnChance(perExoticMult)) {
                        exotics.putExotic(exotic);
                    }
                }
            }
        }

        return exotics;
    }

    public static ETExotics generate(FleetMemberAPI fm, long seed, String faction, float bandwidth) {
        Map<String, Float> factionExoticChances = MagicSettings.getFloatMap("exoticatechnologies", "factionExoticChances");
        Map<String, Float> factionPerExoticMult = MagicSettings.getFloatMap("exoticatechnologies", "factionPerExoticMult");

        List<String> factionAllowedExotics = getAllowedForFaction(faction);
        if (factionAllowedExotics.isEmpty()) {
            return null;
        }

        ETExotics exotics = new ETExotics();

        random.setSeed(seed);

        float exoticChance = CHANCE_OF_EXOTICS;
        if (factionExoticChances.containsKey(faction)) {
            exoticChance = factionExoticChances.get(faction);
        }

        if (random.nextFloat() < exoticChance) {
            float perExoticMult = 1.0f;
            if (factionPerExoticMult.containsKey(faction)) {
                perExoticMult = factionPerExoticMult.get(faction);
            }

            for (Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
                if (!factionAllowedExotics.contains(exotic.getKey())) {
                    continue;
                }

                if (exotic.canApply(fm)) {
                    if (random.nextFloat() < exotic.getSpawnChance(perExoticMult)) {
                        exotics.putExotic(exotic);
                    }
                }
            }
        }

        return exotics;
    }

    private static List<String> getAllowedForFaction(String faction) {
        List<String> factionAllowedUpgrades = MagicSettings.getList("exoticatechnologies", "rngExoticWhitelist");

        if (faction != null) {
            try {
                if(MagicSettings.modSettings.getJSONObject("exoticatechnologies").has(faction + "_ExoticWhitelist")) {
                    factionAllowedUpgrades = MagicSettings.getList("exoticatechnologies", faction + "_ExoticWhitelist");
                }
            } catch (JSONException ex) {
                log.info("ET modSettings object doesn't exist. Is this a bug in MagicLib, or did you remove it?");
                log.info("The actual exception follows.", ex);
            }
        }

        return factionAllowedUpgrades;
    }
}
