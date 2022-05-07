package extrasystemreloaded.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.util.MagicSettings;
import extrasystemreloaded.ESModSettings;
import extrasystemreloaded.Es_ModPlugin;
import extrasystemreloaded.systems.exotics.Exotic;
import extrasystemreloaded.systems.exotics.ExoticsHandler;
import extrasystemreloaded.systems.exotics.ESExotics;
import extrasystemreloaded.systems.bandwidth.Bandwidth;
import extrasystemreloaded.systems.upgrades.ESUpgrades;
import extrasystemreloaded.systems.upgrades.Upgrade;
import extrasystemreloaded.systems.upgrades.UpgradesHandler;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Log4j
public class ExtraSystems {
    private static Random random = new Random();
    //per fleet member!
    private static float CHANCE_OF_UPGRADES = 0.4f;

    protected transient FleetMemberAPI fm;

    private static String getFaction(FleetMemberAPI fm) {
        if (fm.getHullId().contains("ziggurat")) {
            return "omega";
        }
        if(fm.getFleetData() == null
                || fm.getFleetData().getFleet() == null
                || fm.getFleetData().getFleet().getFaction() == null) {
            return null;
        }
        return fm.getFleetData().getFleet().getFaction().getId();
    }

    public static ExtraSystems getForFleetMember(FleetMemberAPI fm) {
        boolean ziggurat = fm.getHullId().contains("ziggurat");
        if(Es_ModPlugin.hasData(fm.getId())) {
            return Es_ModPlugin.getData(fm.getId());
        } else if (ziggurat) {
            ExtraSystems es = generateRandom(fm);
            es.save(fm);
            return es;
        } else {
            ExtraSystems es = new ExtraSystems(fm);
            es.save(fm);
            return es;
        }
    }

    public static ExtraSystems generateRandom(FleetMemberAPI fm) {
        if(Es_ModPlugin.hasData(fm.getId())) {
            return Es_ModPlugin.getData(fm.getId());
        }

        ExtraSystems es = new ExtraSystems(fm);

        String faction = getFaction(fm);

        long seed = fm.getId().hashCode();

        //notes: ziggurat is one-of-a-kind in that it is completely regenerated in a special dialog after its battle.
        //to make sure it still generates the same upgrades, we use its hull ID as seed.
        boolean ziggurat = fm.getHullId().contains("ziggurat");
        if (ziggurat) {
            seed = fm.getHullSpec().getBaseHullId().hashCode() + Global.getSector().getSeedString().hashCode();
        }

        es.generate(seed, faction);
        es.save(fm);

        return es;
    }

    public ExtraSystems(long bandwidthSeed) {
        this.upgrades = new ESUpgrades();
        this.modules = new ESExotics();
        this.bandwidth = Bandwidth.generate(bandwidthSeed).getRandomInRange();
    }

    public ExtraSystems(FleetMemberAPI fm) {
        this.fm = fm;
        this.upgrades = new ESUpgrades();
        this.modules = new ESExotics();
        this.bandwidth = generateBandwidth(fm);
    }

    public boolean shouldApplyHullmod() {
        return this.upgrades.hasUpgrades()
                || this.modules.hasAnyExotic();
    }

    public void save(FleetMemberAPI fm) {
        Es_ModPlugin.saveData(fm.getId(), this);
    }

    /**
     * for ships without a fleet member.
     * @param seed
     * @param var
     * @param faction
     */
    public void generate(long seed, ShipVariantAPI var, String faction) {
        random.setSeed(seed);

        if (faction == null) {
            faction = "";
        }

        this.modules.generate(seed, faction, var);
        this.upgrades.generate(seed, faction, var, this.getBandwidth());

        log.info(this.toString());
    }

    /**
     * for using the fleet member assigned to the ES object by the constructor.
     * @param seed
     * @param faction
     */
    public void generate(long seed, String faction) {

        log.info(String.format("ExtraSystems seed: [%s]", seed));

        random.setSeed(seed);

        if (faction == null) {
            faction = "";
        }

        this.modules.generate(seed, faction, fm);
        this.upgrades.generate(seed, faction, fm, this.getBandwidth());

        log.info(this.toString());
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
            bandwidth = generateBandwidth(fm);
        }

        float returnedBandwidth = bandwidth;

        for(Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if(this.hasExotic(exotic)) {
                returnedBandwidth += exotic.getExtraBandwidth(fm, this);
            }
        }

        return returnedBandwidth;
    }

    public static float generateBandwidth(FleetMemberAPI fm) {

        if (ESModSettings.getBoolean(ESModSettings.RANDOM_BANDWIDTH)) {

            long seed = fm.getId().hashCode();
            if (fm.getHullId().contains("ziggurat")) {
                seed = fm.getHullSpec().getBaseHullId().hashCode() + Global.getSector().getSeedString().hashCode();
            }

            if (fm.getFleetData() != null) {
                String faction = getFaction(fm);

                log.info(String.format("Fleet Member has fleet data with faction ID [%s]", faction));

                Map<String, Float> factionBandwidthMult = MagicSettings.getFloatMap("extrasystemsreloaded", "factionBandwidthMult");

                float mult = 1.0f;
                if(factionBandwidthMult.containsKey(faction)) {
                    mult = factionBandwidthMult.get(faction);
                }

                log.info(String.format("Fleet Member has bandwidth mult [%s]", mult));

                return Bandwidth.generate(seed, mult).getRandomInRange();
            }

            return Bandwidth.generate(seed).getRandomInRange();
        }
        return ESModSettings.getFloat(ESModSettings.STARTING_BANDWIDTH);
    }

    public boolean canUpgradeBandwidth(FleetMemberAPI fm) {
        float maxBandwidth = ESModSettings.getFloat(ESModSettings.MAX_BANDWIDTH);
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
    protected ESExotics modules;

    protected ESExotics getESModules() {
        return modules;
    }

    public boolean hasExotic(String key) {
        return modules.hasExotic(key);
    }

    public boolean hasExotic(Exotic exotic) {
        return hasExotic(exotic.getKey());
    }

    public boolean hasExotics() {
        return modules.hasAnyExotic();
    }

    public void putExotic(Exotic exotic) {
        modules.putExotic(exotic);
    }

    public void removeExotic(Exotic exotic) {
        modules.removeExotic(exotic);
    }

    //upgrades
    private ESUpgrades upgrades;
    protected ESUpgrades getESUpgrades() {
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
        return "ExtraSystems{" +
                "bandwidth=" + bandwidth +
                ", modules=" + modules +
                ", upgrades=" + upgrades +
                '}';
    }
}
