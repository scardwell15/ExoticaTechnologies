package exoticatechnologies.modifications.upgrades;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.util.MagicSettings;
import lombok.extern.log4j.Log4j;
import org.json.JSONException;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Log4j
public class UpgradesGenerator {
    //per fleet member!
    private static float CHANCE_OF_UPGRADES = 0.4f;
    private static Random random = new Random();

    public static ETUpgrades generate(ShipVariantAPI var, long seed, String faction, float bandwidth) {
        Map<String, Float> factionUpgradeChances = MagicSettings.getFloatMap("exoticatechnologies", "factionUpgradeChances");
        Map<String, Float> factionPerUpgradeMult = MagicSettings.getFloatMap("exoticatechnologies", "factionPerUpgradeMult");

        List<String> factionAllowedUpgrades = getAllowedForFaction(faction);
        if (factionAllowedUpgrades.isEmpty()) {
            return null;
        }

        ETUpgrades upgrades = new ETUpgrades();

        random.setSeed(seed);

        float upgradeChance = CHANCE_OF_UPGRADES;
        if (factionUpgradeChances.containsKey(faction)) {
            upgradeChance = factionUpgradeChances.get(faction);
        }

        if (random.nextFloat() < upgradeChance) {
            float perUpgradeMult = 1.0f;
            if (factionPerUpgradeMult.containsKey(faction)) {
                perUpgradeMult = factionPerUpgradeMult.get(faction);
            }

            while (random.nextFloat() < (bandwidth / 100f * perUpgradeMult)) {
                Upgrade upgrade = null;

                while (upgrade == null
                        || !(upgrade.getMaxLevel(var.getHullSize()) > upgrades.getUpgrade(upgrade)
                        && upgrade.canApply(var))) {

                    String upgradeKey = factionAllowedUpgrades.get(random.nextInt(factionAllowedUpgrades.size()));

                    if (!UpgradesHandler.UPGRADES.containsKey(upgradeKey)) continue;
                    upgrade = UpgradesHandler.UPGRADES.get(upgradeKey);
                }

                if ((bandwidth - upgrade.getBandwidthUsage()) < 0f) {
                    break;
                }

                upgrades.addUpgrades(upgrade, 1);
                bandwidth = bandwidth - upgrade.getBandwidthUsage();
            }
        }

        return upgrades;
    }

    public static ETUpgrades generate(FleetMemberAPI fm, long seed, String faction, float bandwidth) {
        Map<String, Float> factionUpgradeChances = MagicSettings.getFloatMap("exoticatechnologies", "factionUpgradeChances");
        Map<String, Float> factionPerUpgradeMult = MagicSettings.getFloatMap("exoticatechnologies", "factionPerUpgradeMult");

        List<String> factionAllowedUpgrades = getAllowedForFaction(faction);
        if (factionAllowedUpgrades.isEmpty()) {
            return null;
        }

        ETUpgrades upgrades = new ETUpgrades();

        random.setSeed(seed);

        float upgradeChance = CHANCE_OF_UPGRADES;
        if (factionUpgradeChances.containsKey(faction)) {
            upgradeChance = factionUpgradeChances.get(faction);
        }

        if (random.nextFloat() < upgradeChance) {
            float perUpgradeMult = 1.0f;
            if (factionPerUpgradeMult.containsKey(faction)) {
                perUpgradeMult = factionPerUpgradeMult.get(faction);
            }

            while (random.nextFloat() < (bandwidth / 100f * perUpgradeMult)) {
                Upgrade upgrade = null;

                while (upgrade == null
                        || !(upgrade.getMaxLevel(fm.getHullSpec().getHullSize()) > upgrades.getUpgrade(upgrade)
                        && upgrade.canApply(fm))) {

                    String upgradeKey = factionAllowedUpgrades.get(random.nextInt(factionAllowedUpgrades.size()));

                    if (!UpgradesHandler.UPGRADES.containsKey(upgradeKey)) continue;
                    upgrade = UpgradesHandler.UPGRADES.get(upgradeKey);
                }

                if ((bandwidth - upgrade.getBandwidthUsage()) < 0f) {
                    break;
                }

                if (random.nextFloat() <= upgrade.getSpawnChance()) {
                    upgrades.addUpgrades(upgrade, 1);
                }

                bandwidth = bandwidth - upgrade.getBandwidthUsage();
            }
        }

        return upgrades;
    }

    private static List<String> getAllowedForFaction(String faction) {
        List<String> factionAllowedUpgrades = MagicSettings.getList("exoticatechnologies", "rngUpgradeWhitelist");

        if (faction != null) {
            try {
                if(MagicSettings.modSettings.getJSONObject("exoticatechnologies").has(faction + "_UpgradeWhitelist")) {
                    factionAllowedUpgrades = MagicSettings.getList("exoticatechnologies", faction + "_UpgradeWhitelist");
                }
            } catch (JSONException ex) {
                log.info("ET modSettings object doesn't exist. Is this a bug in MagicLib, or did you remove it?");
                log.info("The actual exception follows.", ex);
            }
        }

        return factionAllowedUpgrades;
    }
}
