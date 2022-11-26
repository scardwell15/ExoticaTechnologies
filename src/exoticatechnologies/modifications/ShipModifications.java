package exoticatechnologies.modifications;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.modifications.exotics.ETExotics;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticsGenerator;
import exoticatechnologies.modifications.exotics.ExoticsHandler;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.modifications.upgrades.ETUpgrades;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradesGenerator;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import lombok.extern.log4j.Log4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

@Log4j
public class ShipModifications {
    ShipModifications() {
        this.upgrades = new ETUpgrades();
        this.exotics = new ETExotics();
    }

    ShipModifications(float bandwidth, ETUpgrades upgrades, ETExotics exotics) {
        this.bandwidth = bandwidth;
        this.upgrades = upgrades;
        this.exotics = exotics;
    }

    public boolean shouldApplyHullmod() {
        return this.upgrades.hasUpgrades()
                || this.exotics.hasAnyExotic();
    }

    /**
     * for ships without a fleet member.
     * @param var
     * @param faction
     */
    public void generate(ShipVariantAPI var, String faction) {
        if (this.bandwidth == -1) {
            this.bandwidth = Bandwidth.generate().getRandomInRange();
        }

        this.exotics = ExoticsGenerator.generate(var, faction, this.getBaseBandwidth());
        this.upgrades = UpgradesGenerator.generate(var, faction, this.getBaseBandwidth());
    }

    /**
     * for a fleet member
     * @param fm
     * @param faction
     */
    public void generate(FleetMemberAPI fm, String faction) {
        if (this.bandwidth == -1) {
            this.bandwidth = ShipModFactory.generateBandwidth(fm, faction);
        }

        if (fm.getFleetData() != null && fm.getFleetData().getFleet().isPlayerFleet()) {
            return;
        }

        this.exotics = ExoticsGenerator.generate(fm, faction, this.getBaseBandwidth(fm));
        this.upgrades = UpgradesGenerator.generate(fm, faction, this.getBandwidthWithExotics(fm));
    }

    public float getValue() {
        return bandwidth + this.exotics.getList().size() + this.upgrades.getTotalLevels();
    }

    //bandwidth
    private float bandwidth = -1f;

    public void setBandwidth(float bandwidth) {
        this.bandwidth = bandwidth;
    }

    /**
     * Use this only if bandwidth has already been generated. The Exotica dialog WILL generate bandwidth.
     * @return
     */
    public float getBaseBandwidth() {
        return Math.max(this.bandwidth, 0f);
    }

    /**
     * Will generate bandwidth if not already generated.
     * @param fm
     * @return
     */
    public float getBaseBandwidth(FleetMemberAPI fm) {
        if(bandwidth < 0f) {
            log.info(String.format("Bandwidth was below zero for fm [%s]", fm.getId()));
            bandwidth = ShipModFactory.generateBandwidth(fm);
        }

        return Math.max(this.bandwidth, 0f);
    }

    public float getBandwidthWithExotics(FleetMemberAPI fm) {
        if(bandwidth < 0f) {
            log.info(String.format("Bandwidth with exotics was below zero for fm [%s]", fm.getId()));
            bandwidth = ShipModFactory.generateBandwidth(fm);
        }

        float returnedBandwidth = bandwidth;

        for(Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if(this.hasExotic(exotic)) {
                returnedBandwidth += exotic.getExtraBandwidth(fm, this);
            }
        }

        return returnedBandwidth;
    }

    public boolean canUpgradeBandwidth(FleetMemberAPI fm) {
        float maxBandwidth = Bandwidth.MAX_BANDWIDTH;
        for(Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if(this.hasExotic(exotic)) {
                maxBandwidth += exotic.getExtraBandwidthPurchaseable(fm, this);
            }
        }
        return maxBandwidth > getBaseBandwidth(fm);
    }

    public float getUsedBandwidth() {
        float usedBandwidth = 0f;
        for(Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            usedBandwidth += upgrade.getBandwidthUsage() * this.getUpgrade(upgrade);
        }

        return usedBandwidth;
    }

    //exotics
    protected ETExotics exotics = null;

    public ETExotics getExotics() {
        return exotics;
    }

    public Set<Exotic> getExoticSet() {
        Set<Exotic> exoticSet = new HashSet<>();
        for (String exotic : exotics.getList()) {
            exoticSet.add(Exotic.get(exotic));
        }

        return exoticSet;
    }

    public boolean hasExotic(String key) {
        return exotics.hasExotic(key);
    }

    public boolean hasExotic(Exotic exotic) {
        return hasExotic(exotic.getKey());
    }

    public boolean hasExotics() {
        return exotics.hasAnyExotic();
    }

    public void putExotic(Exotic exotic) {
        exotics.putExotic(exotic);
    }

    public void removeExotic(Exotic exotic) {
        exotics.removeExotic(exotic);
    }

    //upgrades
    protected ETUpgrades upgrades = null;
    public ETUpgrades getUpgrades() {
        return upgrades;
    }

    public Map<Upgrade, Integer> getUpgradeMap() {
        Map<Upgrade, Integer> upgradeMap = new HashMap<>();
        for (Map.Entry<String, Integer> upgrade : upgrades.getMap().entrySet()) {
            upgradeMap.put(Upgrade.get(upgrade.getKey()), upgrade.getValue());
        }

        return upgradeMap;
    }

    public void putUpgrade(Upgrade upgrade) {
        upgrades.putUpgrade(upgrade);
    }

    public void putUpgrade(Upgrade upgrade, int level) {
        upgrades.putUpgrade(upgrade, level);
    }

    public int getUpgrade(String key) {
        return upgrades.getUpgrade(key);
    }

    public int getUpgrade(Upgrade upgrade) {
        return getUpgrade(upgrade.getKey());
    }

    public void removeUpgrade(Upgrade upgrade) {
        upgrades.removeUpgrade(upgrade);
    }

    public boolean hasUpgrade(Upgrade upgrade){
        return getUpgrade(upgrade) > 0;
    }

    public boolean hasUpgrades() {
        return this.upgrades.hasUpgrades();
    }

    public boolean hasBandwidthForUpgrade(FleetMemberAPI member, Upgrade upgrade, int level) {
        float upgradeBandwidth = (level - this.getUpgrade(upgrade)) * upgrade.getBandwidthUsage();
        return getUsedBandwidth() + upgradeBandwidth <= getBandwidthWithExotics(member);
    }

    public float getHullSizeFactor(ShipAPI.HullSize hullSize) {
        return this.upgrades.getHullSizeFactor(hullSize);
    }

    public boolean isMaxLevel(FleetMemberAPI member, Upgrade upgrade) {
        return this.getUpgrade(upgrade) >= upgrade.getMaxLevel(member.getHullSpec().getHullSize());
    }

    @Override
    public String toString() {
        return "ShipModifications{" +
                "bandwidth=" + bandwidth +
                ", modules=" + exotics +
                ", upgrades=" + upgrades +
                '}';
    }
}
