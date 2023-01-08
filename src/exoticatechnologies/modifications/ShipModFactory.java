package exoticatechnologies.modifications;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import data.scripts.util.MagicSettings;
import exoticatechnologies.ETModSettings;
import exoticatechnologies.config.FactionConfig;
import exoticatechnologies.config.FactionConfigLoader;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.util.Utilities;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

@Log4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShipModFactory {
    private static final Random random = new Random();

    public static ShipModifications generateForFleetMember(FleetMemberAPI fm) {
        ShipModifications mods = ShipModLoader.get(fm);
        if (mods != null) {
            return mods;
        }

        ShipModFactory.getRandom().setSeed(fm.getId().hashCode());
        mods = new ShipModifications();
        mods.setBandwidth(ShipModFactory.generateBandwidth(fm));
        ShipModLoader.set(fm, mods);

        return mods;
    }

    private static String getFaction(FleetMemberAPI fm) {
        if (fm.getHullId().contains("ziggurat")) {
            return "omega";
        }

        if (fm.getFleetData() == null
                || fm.getFleetData().getFleet() == null) {
            return null;
        }

        try {
            if (fm.getFleetData().getFleet().getMemoryWithoutUpdate().contains("$faction")) {
                return (String) fm.getFleetData().getFleet().getMemoryWithoutUpdate().get("$faction");
            }
        } catch (Throwable th) {
            return null;
        }

        if (fm.getFleetData().getFleet().getFaction() == null) {
            return null;
        }

        return fm.getFleetData().getFleet().getFaction().getId();
    }

    public static ShipModifications generateRandom(FleetMemberAPI fm) {
        ShipModifications mods = ShipModLoader.get(fm);
        if (mods != null) {
            return mods;
        }

        mods = new ShipModifications();

        if (fm.getFleetData() == null || fm.getFleetData().getFleet() == null) {
            return mods;
        }

        String faction = getFaction(fm);

        mods.generate(fm, faction);
        ShipModLoader.set(fm, mods);

        return mods;
    }

    public static float generateBandwidth(FleetMemberAPI fm, String faction) {
        if (!ETModSettings.getBoolean(ETModSettings.RANDOM_BANDWIDTH)) {
            return ETModSettings.getFloat(ETModSettings.STARTING_BANDWIDTH);
        }

        if (Objects.equals(faction, Factions.OMEGA)) {
            return 350f;
        }

        String manufacturer = fm.getHullSpec().getManufacturer();

        float mult = 1.0f;

        Map<String, Float> manufacturerBandwidthMult = MagicSettings.getFloatMap("exoticatechnologies", "manufacturerBandwidthMult");
        if (manufacturerBandwidthMult.containsKey(manufacturer)) {
            mult = manufacturerBandwidthMult.get(manufacturer);
        }

        FactionConfig factionConfig = FactionConfigLoader.getFactionConfig(faction);
        if (factionConfig.getBandwidthMult() != 1.0) {
            mult = (float) factionConfig.getBandwidthMult();
        }

        mult += (Utilities.getSModCount(fm));

        return Bandwidth.generate(mult).getRandomInRange();
    }

    public static float generateBandwidth(FleetMemberAPI fm) {
        if (!ETModSettings.getBoolean(ETModSettings.RANDOM_BANDWIDTH)) {
            return ETModSettings.getFloat(ETModSettings.STARTING_BANDWIDTH);
        }

        log.info(String.format("Generating bandwidth for fm ID [%s]", fm.getId()));

        String faction = getFaction(fm);
        if (faction != null) {
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

    public static Random getRandom() {
        return random;
    }
}
