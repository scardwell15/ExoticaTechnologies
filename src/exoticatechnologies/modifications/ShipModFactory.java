package exoticatechnologies.modifications;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.util.MagicSettings;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.ETModSettings;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.Map;

@Log4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShipModFactory {
    public static ShipModifications getForFleetMember(FleetMemberAPI fm) {
        boolean ziggurat = fm.getHullId().contains("ziggurat");
        if (ETModPlugin.hasData(fm.getId())) {
            return ETModPlugin.getData(fm.getId());
        } else if (ziggurat) {
            ShipModifications mods = generateRandom(fm);
            mods.save(fm);
            return mods;
        } else {
            ShipModifications mods = new ShipModifications(fm);
            mods.save(fm);
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

    public static ShipModifications generateRandom(ShipVariantAPI var, Long seed, String faction) {
        if (seed == null) {
            seed = (long) var.getHullVariantId().hashCode();
        }

        ShipModifications mods = new ShipModifications(seed);

        mods.generate(seed, var, faction);

        return mods;
    }

    public static ShipModifications generateRandom(FleetMemberAPI fm) {
        if (ETModPlugin.hasData(fm.getId())) {
            return ETModPlugin.getData(fm.getId());
        }

        ShipModifications mods = new ShipModifications(fm);

        String faction = getFaction(fm);

        long seed = fm.getId().hashCode();

        //notes: ziggurat is one-of-a-kind in that it is completely regenerated in a special dialog after its battle.
        //to make sure it still generates the same upgrades, we use its hull ID as seed.
        boolean ziggurat = fm.getHullId().contains("ziggurat");
        if (ziggurat) {
            seed = fm.getHullSpec().getBaseHullId().hashCode() + Global.getSector().getSeedString().hashCode();
        }

        mods.generate(seed, faction);
        mods.save(fm);

        return mods;
    }

    public static float generateBandwidth(FleetMemberAPI fm) {

        if (ETModSettings.getBoolean(ETModSettings.RANDOM_BANDWIDTH)) {

            long seed = fm.getId().hashCode();
            if (fm.getHullId().contains("ziggurat")) {
                seed = fm.getHullSpec().getBaseHullId().hashCode() + Global.getSector().getSeedString().hashCode();
            }

            if (fm.getFleetData() != null) {
                String faction = getFaction(fm);
                String manufacturer = fm.getHullSpec().getManufacturer();

                Map<String, Float> factionBandwidthMult = MagicSettings.getFloatMap("exoticatechnologies", "factionBandwidthMult");
                Map<String, Float> manufacturerBandwidthMult = MagicSettings.getFloatMap("exoticatechnologies", "manufacturerBandwidthMult");

                float mult = 1.0f;
                if (factionBandwidthMult.containsKey(faction)) {
                    mult = factionBandwidthMult.get(faction);
                }

                if (manufacturerBandwidthMult.containsKey(manufacturer)) {
                    mult = manufacturerBandwidthMult.get(faction);
                }

                return Bandwidth.generate(seed, mult).getRandomInRange();
            }

            return Bandwidth.generate(seed).getRandomInRange();
        }
        return ETModSettings.getFloat(ETModSettings.STARTING_BANDWIDTH);
    }
}
