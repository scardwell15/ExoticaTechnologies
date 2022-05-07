package extrasystemreloaded.systems.exotics;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.util.MagicSettings;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Log4j
public class ESExotics {
    private static Random random = new Random();
    private static float CHANCE_OF_EXOTICS = 0.1f;

    private final List<String> exotics;

    public ESExotics() {
        this.exotics = new ArrayList<>();
    }

    public ESExotics(List<String> exotics) {
        if (exotics == null) {
            this.exotics = new ArrayList<>();
        } else {
            this.exotics = exotics;
        }
    }

    public boolean hasExotic(Exotic upgrade) {
        return this.hasExotic(upgrade.getKey());
    }

    public boolean hasExotic(String key) {
        if (this.exotics.contains(key)) {
            return true;
        }
        return false;
    }

    public void putExotic(Exotic upggrade) {
        this.putExotic(upggrade.getKey());
    }

    public void putExotic(String key) {
        this.exotics.add(key);
    }

    public void removeExotic(String key) {
        this.exotics.remove(key);
    }

    public void removeExotic(Exotic exotic) {
        this.exotics.remove(exotic.getKey());
    }

    public boolean hasAnyExotic() {
        return !this.exotics.isEmpty();
    }

    public void generate(long seed, String faction, ShipVariantAPI var) {
        Map<String, Float> factionExoticChances = MagicSettings.getFloatMap("extrasystemsreloaded", "factionExoticChances");
        Map<String, Float> factionPerExoticMult = MagicSettings.getFloatMap("extrasystemsreloaded", "factionPerExoticMult");
        List<String> factionAllowedExotics = MagicSettings.getList("extrasystemsreloaded", faction + "_ExoticWhitelist");
        if (factionAllowedExotics == null) {
            factionAllowedExotics = MagicSettings.getList("extrasystemsreloaded", "rngExoticWhitelist");
        }

        if (factionAllowedExotics.isEmpty()) {
            return;
        }

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
                        this.putExotic(exotic);
                    }
                }
            }
        }
    }

    public void generate(long seed, String faction, FleetMemberAPI fm) {
        Map<String, Float> factionExoticChances = MagicSettings.getFloatMap("extrasystemsreloaded", "factionExoticChances");
        Map<String, Float> factionPerExoticMult = MagicSettings.getFloatMap("extrasystemsreloaded", "factionPerExoticMult");

        List<String> factionAllowedExotics = MagicSettings.getList("extrasystemsreloaded", faction + "_ExoticWhitelist");
        if (factionAllowedExotics == null) {
            factionAllowedExotics = MagicSettings.getList("extrasystemsreloaded", "rngExoticWhitelist");
        }

        if (factionAllowedExotics.isEmpty()) {
            return;
        }


        if (factionAllowedExotics.isEmpty()) {
            return;
        }

        float exoticChance = CHANCE_OF_EXOTICS;
        if (factionExoticChances.containsKey(faction)) {
            exoticChance = factionExoticChances.get(faction);
        }

        if (random.nextFloat() < exoticChance) {
            log.info("generating exotic");
            float perExoticMult = 1.0f;
            if (factionPerExoticMult.containsKey(faction)) {
                perExoticMult = factionPerExoticMult.get(faction);
            }

            for (Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
                if (!factionAllowedExotics.contains(exotic.getKey())) {
                    log.info("not in faction exotics");
                    continue;
                } else {
                    log.info("in faction exotics");
                }

                if (exotic.canApply(fm)) {
                    if (random.nextFloat() < exotic.getSpawnChance(perExoticMult)) {
                        this.putExotic(exotic);
                        log.info("put exotic");
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ESExotics{" +
                "exotics=" + exotics +
                '}';
    }
}
