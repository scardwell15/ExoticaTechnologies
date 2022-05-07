package extrasystemreloaded.systems.upgrades;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.util.MagicSettings;
import extrasystemreloaded.ESModSettings;
import lombok.extern.log4j.Log4j;
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Log4j
public class ESUpgrades {
    private static Random random = new Random();
    //per fleet member!
    private static float CHANCE_OF_UPGRADES = 0.4f;

    private final Map<String, Integer> upgrades;

    public ESUpgrades() {
        this.upgrades = new HashMap<>();
    }

    public ESUpgrades(Map<String, Integer> upgrades) {
        this.upgrades = upgrades;
    }

    public float getHullSizeFactor(ShipAPI.HullSize hullSize) {
        return ESModSettings.getHullSizeFactors().get(hullSize);
    }

    public int getUpgrade(Upgrade upgrade) {
        return this.getUpgrade(upgrade.getKey());
    }

    public int getUpgrade(String key) {
        if (this.upgrades.containsKey(key)) {
            return this.upgrades.get(key);
        }
        return 0;
    }

    public void putUpgrade(Upgrade upggrade) {
        this.putUpgrade(upggrade.getKey());
    }

    public void putUpgrade(Upgrade upggrade, int level) {
        this.putUpgrade(upggrade.getKey(), level);
    }

    public void addUpgrades(Upgrade upggrade, int levels) {
        this.putUpgrade(upggrade.getKey(), this.getUpgrade(upggrade.getKey()) + levels);
    }

    public void putUpgrade(String key) {
        this.putUpgrade(key, getUpgrade(key) + 1);
    }

    public void putUpgrade(String key, int level) {
        this.upgrades.put(key, level);
    }

    public boolean hasUpgrades() {
        return !this.upgrades.isEmpty();
    }

    public void generate(long seed, String faction, ShipVariantAPI var, float bandwidth) {
        Map<String, Float> factionUpgradeChances = MagicSettings.getFloatMap("extrasystemsreloaded", "factionUpgradeChances");
        Map<String, Float> factionPerUpgradeMult = MagicSettings.getFloatMap("extrasystemsreloaded", "factionPerUpgradeMult");
        List<String> factionAllowedUpgrades = MagicSettings.getList("extrasystemsreloaded", faction + "_UpgradeWhitelist");
        if (factionAllowedUpgrades == null) {
            factionAllowedUpgrades = MagicSettings.getList("extrasystemsreloaded", "rngUpgradeWhitelist");
        }

        if (factionAllowedUpgrades.isEmpty()) {
            return;
        }

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
                        || !(upgrade.getMaxLevel(var.getHullSize()) > this.getUpgrade(upgrade)
                        && upgrade.canApply(var))) {

                    String upgradeKey = factionAllowedUpgrades.get(random.nextInt(factionAllowedUpgrades.size()));

                    if (!UpgradesHandler.UPGRADES.containsKey(upgradeKey)) continue;
                    upgrade = UpgradesHandler.UPGRADES.get(upgradeKey);
                }

                if ((bandwidth - upgrade.getBandwidthUsage()) < 0f) {
                    break;
                }

                this.addUpgrades(upgrade, 1);
                bandwidth = bandwidth - upgrade.getBandwidthUsage();
            }
        }
    }

    public void generate(long seed, String faction, FleetMemberAPI fm, float bandwidth) {
        Map<String, Float> factionUpgradeChances = MagicSettings.getFloatMap("extrasystemsreloaded", "factionUpgradeChances");
        Map<String, Float> factionPerUpgradeMult = MagicSettings.getFloatMap("extrasystemsreloaded", "factionPerUpgradeMult");

        List<String> factionAllowedUpgrades = MagicSettings.getList("extrasystemsreloaded", "rngUpgradeWhitelist");
        try {
            if(MagicSettings.modSettings.getJSONObject("extrasystemsreloaded").has(faction + "_UpgradeWhitelist")) {
                factionAllowedUpgrades = MagicSettings.getList("extrasystemsreloaded", faction + "_UpgradeWhitelist");
            }
        } catch (JSONException ex) {
            log.info("ESR modSettings object doesn't exist. Is this a bug in MagicLib, or did you remove it?");
            log.info("The actual exception follows.", ex);
        }

        if (factionAllowedUpgrades.isEmpty()) {
            return;
        }

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
                        || !(upgrade.getMaxLevel(fm.getHullSpec().getHullSize()) > this.getUpgrade(upgrade)
                            && upgrade.canApply(fm))) {

                    String upgradeKey = factionAllowedUpgrades.get(random.nextInt(factionAllowedUpgrades.size()));

                    if (!UpgradesHandler.UPGRADES.containsKey(upgradeKey)) continue;
                    upgrade = UpgradesHandler.UPGRADES.get(upgradeKey);
                }

                if ((bandwidth - upgrade.getBandwidthUsage()) < 0f) {
                    break;
                }

                this.addUpgrades(upgrade, 1);
                bandwidth = bandwidth - upgrade.getBandwidthUsage();
            }
        }
    }

    @Override
    public String toString() {
        return "ESUpgrades{" +
                "upgrades=" + upgrades +
                '}';
    }
}
