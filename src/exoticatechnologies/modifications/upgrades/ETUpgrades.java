package exoticatechnologies.modifications.upgrades;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.util.MagicSettings;
import exoticatechnologies.ETModSettings;
import lombok.extern.log4j.Log4j;
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Log4j
public class ETUpgrades {
    private final Map<String, Integer> upgrades;

    public ETUpgrades() {
        this.upgrades = new HashMap<>();
    }

    public ETUpgrades(Map<String, Integer> upgrades) {
        this.upgrades = upgrades;
    }

    public float getHullSizeFactor(ShipAPI.HullSize hullSize) {
        return ETModSettings.getHullSizeFactors().get(hullSize);
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

    public void removeUpgrade(Upgrade upgrade) {
        this.upgrades.remove(upgrade.getKey());
    }

    @Override
    public String toString() {
        return "ETUpgrades{" +
                "upgrades=" + upgrades +
                '}';
    }
}
