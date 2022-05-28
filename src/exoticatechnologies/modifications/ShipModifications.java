package exoticatechnologies.modifications;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.ETModSettings;
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

import java.util.Random;

@Log4j
public class ShipModifications {
    private static Random random = new Random();
    //per fleet member!
    private static float CHANCE_OF_UPGRADES = 0.4f;

    protected transient FleetMemberAPI fm;

    ShipModifications(long bandwidthSeed) {
        this.upgrades = new ETUpgrades();
        this.exotics = new ETExotics();
        this.bandwidth = Bandwidth.generate(bandwidthSeed).getRandomInRange();
    }

    ShipModifications(FleetMemberAPI fm) {
        this.fm = fm;
        this.upgrades = new ETUpgrades();
        this.exotics = new ETExotics();
        this.bandwidth = ShipModFactory.generateBandwidth(fm);
    }

    public boolean shouldApplyHullmod() {
        return this.upgrades.hasUpgrades()
                || this.exotics.hasAnyExotic();
    }

    public void save(FleetMemberAPI fm) {
        ETModPlugin.saveData(fm.getId(), this);
    }

    /**
     * for ships without a fleet member.
     * @param seed
     * @param var
     * @param faction
     */
    public void generate(long seed, ShipVariantAPI var, String faction) {
        this.exotics = ExoticsGenerator.generate(var, seed, faction, this.getBandwidth());
        this.upgrades = UpgradesGenerator.generate(var, seed, faction, this.getBandwidth());
    }

    /**
     * for using the fleet member assigned to the object by the constructor.
     * @param seed
     * @param faction
     */
    public void generate(long seed, String faction) {
        this.exotics = ExoticsGenerator.generate(fm, seed, faction, this.getBandwidth());
        this.upgrades = UpgradesGenerator.generate(fm, seed, faction, this.getBandwidth());
    }

    //bandwidth
    private float bandwidth = -1f;

    public void putBandwidth(float bandwidth) {
        this.bandwidth = bandwidth;
    }

    public float getBandwidth() {
        return Math.max(this.bandwidth, 0f);
    }

    public float getBandwidth(FleetMemberAPI fm) {
        if(bandwidth < 0f) {
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
        float maxBandwidth = ETModSettings.getFloat(ETModSettings.MAX_BANDWIDTH);
        for(Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if(this.hasExotic(exotic)) {
                maxBandwidth += exotic.getExtraBandwidthPurchaseable(fm, this);
            }
        }
        return maxBandwidth > getBandwidth(fm);
    }

    public float getUsedBandwidth() {
        float usedBandwidth = 0f;
        for(Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            usedBandwidth += upgrade.getBandwidthUsage() * this.getUpgrade(upgrade);
        }

        return usedBandwidth;
    }

    //exotics
    protected ETExotics exotics;

    protected ETExotics getExotics() {
        return exotics;
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

        //hard-coded. todo - add remove method on exotics
        if (exotic.getKey().equals("AlphaSubcore")) {
            fm.getVariant().removeMod("AlphaSubcoreHM");
        }

        exotics.removeExotic(exotic);
    }

    //upgrades
    private ETUpgrades upgrades;
    protected ETUpgrades getUpgrades() {
        return upgrades;
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

    public float getHullSizeFactor(ShipAPI.HullSize hullSize) {
        return this.upgrades.getHullSizeFactor(hullSize);
    }

    public boolean isMaxLevel(FleetMemberAPI shipSelected, Upgrade upgrade) {
        return this.getUpgrade(upgrade) >= upgrade.getMaxLevel(shipSelected.getHullSpec().getHullSize());
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
