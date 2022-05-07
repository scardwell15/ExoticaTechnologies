package extrasystemreloaded.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import extrasystemreloaded.Es_ModPlugin;
import extrasystemreloaded.systems.exotics.Exotic;
import extrasystemreloaded.systems.exotics.ExoticsHandler;
import extrasystemreloaded.systems.bandwidth.Bandwidth;
import extrasystemreloaded.systems.bandwidth.BandwidthUtil;
import extrasystemreloaded.systems.upgrades.Upgrade;
import extrasystemreloaded.systems.upgrades.UpgradesHandler;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.FleetMemberUtils;
import extrasystemreloaded.util.StringUtils;
import lombok.extern.log4j.Log4j;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Log4j
public class ExtraSystemHM extends BaseHullMod {
    private static Color hullmodColor = new Color(94, 206, 226);
    private static Color tooltipColor = Misc.getTextColor();
    public static Color infoColor = Misc.getPositiveHighlightColor();

    public static void addToFleetMember(FleetMemberAPI fm) {
        if (fm.getVariant() == null) {
            return;
        }

        ExtraSystems levels = ExtraSystems.getForFleetMember(fm);
        ShipVariantAPI shipVariant = fm.getVariant();

        if(shipVariant.hasHullMod("extrasystemsHM")) {
            shipVariant.removePermaMod("extrasystemsHM");
        }

        if (levels.shouldApplyHullmod()) {

            if(shipVariant.isStockVariant() || shipVariant.getSource() != VariantSource.REFIT) {
                shipVariant = shipVariant.clone();
                shipVariant.setOriginalVariant(null);
                shipVariant.setSource(VariantSource.REFIT);
                fm.setVariant(shipVariant, false, false);
            }

            shipVariant.addPermaMod("extrasystemsHM");

            List<String> slots = shipVariant.getModuleSlots();

            Iterator<String> moduleIterator = shipVariant.getStationModules().keySet().iterator();
            while(moduleIterator.hasNext()) {
                String moduleVariantId = moduleIterator.next();
                ShipVariantAPI moduleVariant = shipVariant.getModuleVariant(moduleVariantId);

                if (moduleVariant != null) {
                    if(moduleVariant.isStockVariant() || shipVariant.getSource() != VariantSource.REFIT) {
                        moduleVariant = moduleVariant.clone();
                        moduleVariant.setOriginalVariant(null);
                        moduleVariant.setSource(VariantSource.REFIT);
                        shipVariant.setModuleVariant(moduleVariantId, moduleVariant);
                    }

                    moduleVariant.addPermaMod("extrasystemsHM");
                }
            }

            fm.updateStats();
        }
    }

    public static void removeFromFleetMember(FleetMemberAPI fm) {

    }

    @Override
    public boolean affectsOPCosts() {
        return false;
    }

    @Override
    public Color getNameColor() {
        return hullmodColor;
    }

    public ExtraSystems getExtraSystems(MutableShipStatsAPI stats) {
        FleetMemberAPI fm = FleetMemberUtils.findMemberForStats(stats);
        if(fm == null) return null;

        return getExtraSystems(fm);
    }

    public ExtraSystems getExtraSystems(FleetMemberAPI fm) {
        return ExtraSystems.getForFleetMember(fm);
    }

    @Override
    public void advanceInCampaign(FleetMemberAPI fm, float amount) {
        if (!Es_ModPlugin.hasData(fm.getId())) {
            fm.getVariant().removePermaMod("extrasystemsHM");
            return;
        }

        ExtraSystems extraSystems = this.getExtraSystems(fm);
        if(extraSystems == null) return;

        for(Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            int level = extraSystems.getUpgrade(upgrade);
            if(level <= 0) continue;
            upgrade.advanceInCampaign(fm, level, upgrade.getMaxLevel(fm));
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        FleetMemberAPI fm = FleetMemberUtils.findMemberFromShip(ship);
        if(fm == null) return;

        ExtraSystems extraSystems = this.getExtraSystems(fm);
        if(extraSystems == null) return;

        ShipAPI.HullSize hullSize = ship.getHullSize();
        float bandwidth = extraSystems.getBandwidth(fm);
        for(Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            int level = extraSystems.getUpgrade(upgrade);
            if(level <= 0) continue;
            upgrade.advanceInCombat(ship, amount, level, bandwidth, extraSystems.getHullSizeFactor(hullSize));
        }

        for(Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if(!extraSystems.hasExotic(exotic)) continue;
            exotic.advanceInCombat(ship, amount, bandwidth);
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        FleetMemberAPI fm = FleetMemberUtils.findMemberForStats(stats);
        if(fm == null) {
            return;
        }

        try {
            if (!stats.getVariant().getStationModules().isEmpty()) {
                FleetMemberUtils.moduleMap.clear();

                for (Map.Entry<String, String> e : stats.getVariant().getStationModules().entrySet()) {
                    ShipVariantAPI module = stats.getVariant().getModuleVariant(e.getKey());

                    FleetMemberUtils.moduleMap.put(module.getHullVariantId(), fm);
                }
            }
        } catch (Exception e) {
            log.info("Failed to get modules", e);
        }

        ExtraSystems extraSystems = this.getExtraSystems(fm);

        if (extraSystems == null) {
            fm.getVariant().removePermaMod("extrasystemsHM");
            return;
        }

        float bandwidth = extraSystems.getBandwidth(fm);

        for(Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if(!extraSystems.hasExotic(exotic)) continue;

            exotic.applyExoticToStats(fm, stats, bandwidth, id);
        }

        for(Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            int level = extraSystems.getUpgrade(upgrade);
            if(level <= 0) continue;

            upgrade.applyUpgradeToStats(fm, stats, level, upgrade.getMaxLevel(fm));
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        FleetMemberAPI fm = FleetMemberUtils.findMemberFromShip(ship);
        if(fm == null) return;

        ExtraSystems extraSystems = this.getExtraSystems(fm);
        if(extraSystems == null) return;

        float bandwidth = extraSystems.getBandwidth(fm);

        for(Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if(!extraSystems.hasExotic(exotic)) continue;
            exotic.applyExoticToShip(fm, ship, bandwidth, id);
        }
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        FleetMemberAPI fm = FleetMemberUtils.findMemberFromShip(ship);
        if(fm == null) return "SHIP NOT FOUND";
        if(fm.getShipName() == null) {
            return "SHIP MODULE";
        }
        return fm.getShipName();
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI hullmodTooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        FleetMemberAPI fm = FleetMemberUtils.findMemberFromShip(ship);
        if(fm == null) return;
        if(fm.getShipName() == null) {
            hullmodTooltip.addPara("Ship modules do not support tooltips.", 0);
            return;
        }

        ExtraSystems extraSystems = this.getExtraSystems(fm);
        if (extraSystems == null) return;
        float bandwidth = extraSystems.getBandwidth(fm);
        String bandwidthString = BandwidthUtil.getFormattedBandwidthWithName(bandwidth);

        hullmodTooltip.addPara("The ship has %s bandwidth.", 0, Bandwidth.getBandwidthColor(bandwidth),  bandwidthString);

        boolean expand = Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"));

        CustomPanelAPI customPanelAPI = null;
        TooltipMakerAPI tooltip = hullmodTooltip;

        if(expand) {
            customPanelAPI = Global.getSettings().createCustom(width, 500f, null);
            tooltip = customPanelAPI.createUIElement(width, 500f, true);
        }

        boolean addedExoticSection = false;
        try {
            for (Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
                if (!extraSystems.hasExotic(exotic.getKey())) continue;

                if (!addedExoticSection) {
                    addedExoticSection = true;
                    tooltip.addSectionHeading(StringUtils.getString("FleetScanner", "ExoticHeader"), Alignment.MID, 6);
                }
                exotic.modifyToolTip(tooltip, fm, extraSystems, expand);
                tooltip.setParaFontDefault();
                tooltip.setParaFontColor(tooltipColor);
            }
        } catch (Throwable th) {
            log.info("Caught exotic description exception", th);
            tooltip.addPara("Caught an error! See starsector.log", Color.RED, 0);
        }

        boolean addedUpgradeSection = false;
        try {
            for (Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
                if (extraSystems.getUpgrade(upgrade) < 1) continue;

                if (!addedUpgradeSection) {
                    addedUpgradeSection = true;
                    tooltip.addSectionHeading(StringUtils.getString("FleetScanner", "UpgradeHeader"), Alignment.MID, 6);
                }
                upgrade.modifyToolTip(tooltip, fm, extraSystems, expand);
                tooltip.setParaFontDefault();
                tooltip.setParaFontColor(tooltipColor);
            }
        } catch (Throwable th) {
            log.info("Caught upgrade description exception", th);
            tooltip.addPara("Caught an error! See starsector.log", Color.RED, 0);
        }

        if(expand) {
            customPanelAPI.addUIElement(tooltip).inTL(-5f, 0);
            hullmodTooltip.addCustom(customPanelAPI, 0f);
            hullmodTooltip.setForceProcessInput(true);
        }

        if (expand) {
            hullmodTooltip.addPara("Press F1 to show less information.", 10, infoColor, "F1");
        } else {
            hullmodTooltip.addPara("Hold F1 to show more information.", 10, infoColor, "F1");
        }
    }

    public static void removeESHullModsFromVariant(ShipVariantAPI v) {
        v.removePermaMod("extrasystemsHM");

        List<String> slots = v.getModuleSlots();
        if (slots == null || slots.isEmpty()) return;

        for(int i = 0; i < slots.size(); ++i) {
            ShipVariantAPI module = v.getModuleVariant(slots.get(i));
            if (module != null) {
                removeESHullModsFromVariant(module);
            }
        }
    }
}