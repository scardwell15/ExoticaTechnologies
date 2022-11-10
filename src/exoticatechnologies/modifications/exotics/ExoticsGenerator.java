package exoticatechnologies.modifications.exotics;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.util.MagicSettings;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.util.Utilities;
import lombok.extern.log4j.Log4j;
import org.json.JSONException;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Log4j
public class ExoticsGenerator {
    //per fleet member!
    private static final float CHANCE_OF_EXOTICS = 0.1f;

    public static ETExotics generate(ShipVariantAPI var, String faction, float bandwidth) {
        Map<String, Float> factionExoticChances = MagicSettings.getFloatMap("exoticatechnologies", "factionExoticChances");
        Map<String, Float> factionPerExoticMult = MagicSettings.getFloatMap("exoticatechnologies", "factionPerExoticMult");

        List<String> factionAllowedExotics = getAllowedForFaction(faction);
        if (factionAllowedExotics.isEmpty()) {
            return null;
        }

        ETExotics exotics = new ETExotics();

        float exoticChance = CHANCE_OF_EXOTICS;
        if (factionExoticChances.containsKey(faction)) {
            exoticChance = factionExoticChances.get(faction);
        }

        exoticChance *= (1 + Utilities.getSModCount(var));


        Random random = ShipModFactory.getRandom();
        float rolledChance = random.nextFloat();
        if (rolledChance < exoticChance) {
            float perExoticMult = 1.0f;
            if (factionPerExoticMult.containsKey(faction)) {
                perExoticMult = factionPerExoticMult.get(faction);
            }
            perExoticMult *= (1 + Utilities.getSModCount(var) * 0.5f);

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

    public static ETExotics generate(FleetMemberAPI fm, String faction, float bandwidth) {
        Map<String, Float> factionExoticChances = MagicSettings.getFloatMap("exoticatechnologies", "factionExoticChances");
        Map<String, Float> factionPerExoticMult = MagicSettings.getFloatMap("exoticatechnologies", "factionPerExoticMult");

        List<String> factionAllowedExotics = getAllowedForFaction(faction);
        if (factionAllowedExotics.isEmpty()) {
            return null;
        }

        ETExotics exotics = new ETExotics();

        float exoticChance = CHANCE_OF_EXOTICS;
        if (faction != null && factionExoticChances.containsKey(faction)) {
            exoticChance = factionExoticChances.get(faction);
        }

        int smodCount = Utilities.getSModCount(fm);
        exoticChance *= (1 + smodCount);


        Random random = ShipModFactory.getRandom();
        float rolledChance = random.nextFloat();
        if (rolledChance < exoticChance) {
            float perExoticMult = 1.0f;
            if (faction != null && factionPerExoticMult.containsKey(faction)) {
                perExoticMult = factionPerExoticMult.get(faction);
            }
            perExoticMult *= (1 + smodCount * 0.5f);

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
