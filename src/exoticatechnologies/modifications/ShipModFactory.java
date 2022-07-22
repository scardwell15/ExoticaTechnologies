package exoticatechnologies.modifications;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.util.MagicSettings;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.ETModSettings;
import exoticatechnologies.campaign.listeners.CampaignEventListener;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.util.Utilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.util.Map;
import java.util.Random;

@Log4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShipModFactory {
    @Getter
    private static Random random = new Random();

    public static ShipModifications getForFleetMember(FleetMemberAPI fm) {
        if (fm.getHullId().contains("ziggurat")) {
            if (ETModPlugin.getZigguratDuplicateId() != null && ETModPlugin.hasData(ETModPlugin.getZigguratDuplicateId())) {
                return ETModPlugin.getData(ETModPlugin.getZigguratDuplicateId());
            } else {
                ShipModifications mods = generateRandom(fm);

                if (CampaignEventListener.isAppliedData()) {
                    mods.save(fm);
                }

                return mods;
            }
        } else if (ETModPlugin.hasData(fm.getId())) {
            return ETModPlugin.getData(fm.getId());
        } else {
            ShipModifications mods = new ShipModifications(fm);
            mods.putBandwidth(ShipModFactory.generateBandwidth(fm));

            if (CampaignEventListener.isAppliedData()) {
                mods.save(fm);
            }

            return mods;
        }
    }

    private static String getFaction(FleetMemberAPI fm) {
        if (fm.getHullId().contains("ziggurat")) {
            return "omega";
        }

        if (fm.getFleetData() == null
                || fm.getFleetData().getFleet() == null
                || fm.getFleetData().getFleet().getFaction() == null) {
            return null;
        }

        return fm.getFleetData().getFleet().getFaction().getId();
    }

    public static ShipModifications generateRandom(ShipVariantAPI var, String faction) {
        ShipModifications mods = new ShipModifications();

        mods.generate(var, faction);

        return mods;
    }

    public static ShipModifications generateRandom(FleetMemberAPI fm) {
        if (ETModPlugin.hasData(fm.getId())) {
            return ETModPlugin.getData(fm.getId());
        }

        ShipModifications mods = new ShipModifications(fm);

        String faction = getFaction(fm);

        long seed = fm.getFleetData().getFleet().getId().hashCode();

        //notes: ziggurat is one-of-a-kind in that it is completely regenerated in a special dialog after its battle.
        //to make sure it still generates the same upgrades, we use its hull ID as seed.
        boolean ziggurat = fm.getHullId().contains("ziggurat");
        if (ziggurat) {
            seed = fm.getHullId().hashCode() + ETModPlugin.getSectorSeedString().hashCode();
        }

        mods.generate(fm, faction);

        if (CampaignEventListener.isAppliedData()) {
            mods.save(fm);
        }

        return mods;
    }

    public static float generateBandwidth(FleetMemberAPI fm, String faction) {
        if (!ETModSettings.getBoolean(ETModSettings.RANDOM_BANDWIDTH)) {
            return ETModSettings.getFloat(ETModSettings.STARTING_BANDWIDTH);
        }

        String manufacturer = fm.getHullSpec().getManufacturer();

        Map<String, Float> factionBandwidthMult = MagicSettings.getFloatMap("exoticatechnologies", "factionBandwidthMult");
        Map<String, Float> manufacturerBandwidthMult = MagicSettings.getFloatMap("exoticatechnologies", "manufacturerBandwidthMult");

        float mult = 1.0f;
        if (factionBandwidthMult != null && factionBandwidthMult.containsKey(faction)) {
            mult = factionBandwidthMult.get(faction);
        }

        if (manufacturerBandwidthMult != null && manufacturerBandwidthMult.containsKey(manufacturer)) {
            mult = manufacturerBandwidthMult.get(manufacturer);
        }

        mult += (Utilities.getSModCount(fm));

        return Bandwidth.generate(mult).getRandomInRange();
    }

    public static float generateBandwidth(FleetMemberAPI fm) {
        if (!ETModSettings.getBoolean(ETModSettings.RANDOM_BANDWIDTH)) {
            return ETModSettings.getFloat(ETModSettings.STARTING_BANDWIDTH);
        }

        log.info(String.format("Generating bandwidth for fm ID [%s]", fm.getId()));

        if (fm.getFleetData() != null) {
            String faction = getFaction(fm);

            return generateBandwidth(fm, faction);
        }

        return Bandwidth.generate().getRandomInRange();
    }

    public static float getRandomNumberInRange(float min, float max) {
        return random.nextFloat() * (max - min) + min;
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            return min == max ? min : random.nextInt(min - max + 1) + max;
        } else {
            return random.nextInt(max - min + 1) + min;
        }
    }
}
