package exoticatechnologies.hullmods;

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
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.ShipModLoader;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticsHandler;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.modifications.bandwidth.BandwidthUtil;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.FleetMemberUtils;
import exoticatechnologies.util.StringUtils;
import lombok.extern.log4j.Log4j;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;
import java.util.Map;

@Log4j
public class ExoticaTechHM extends BaseHullMod {
    private static final Color hullmodColor = new Color(94, 206, 226);
    private static final Color tooltipColor = Misc.getTextColor();
    public static Color infoColor = Misc.getPositiveHighlightColor();

    public static void addToFleetMember(FleetMemberAPI fm) {
        if (fm.getVariant() == null) {
            return;
        }

        ShipModifications mods = ShipModFactory.generateForFleetMember(fm);
        ShipVariantAPI shipVariant = fm.getVariant();

        if(shipVariant.hasHullMod("exoticatech")) {
            shipVariant.removePermaMod("exoticatech");
        }

        if (mods.shouldApplyHullmod()) {

            if(shipVariant.isStockVariant() || shipVariant.getSource() != VariantSource.REFIT) {
                shipVariant = shipVariant.clone();
                shipVariant.setOriginalVariant(null);
                shipVariant.setSource(VariantSource.REFIT);
                fm.setVariant(shipVariant, false, false);
            }

            shipVariant.addPermaMod("exoticatech");

            for (String moduleVariantId : shipVariant.getStationModules().keySet()) {
                ShipVariantAPI moduleVariant = shipVariant.getModuleVariant(moduleVariantId);

                if (moduleVariant != null) {
                    if (moduleVariant.isStockVariant() || shipVariant.getSource() != VariantSource.REFIT) {
                        moduleVariant = moduleVariant.clone();
                        moduleVariant.setOriginalVariant(null);
                        moduleVariant.setSource(VariantSource.REFIT);
                        shipVariant.setModuleVariant(moduleVariantId, moduleVariant);
                    }

                    moduleVariant.addPermaMod("exoticatech");
                }
            }

            fm.updateStats();
        }
    }

    public static void removeFromFleetMember(FleetMemberAPI fm) {
        if (fm.getVariant() == null) {
            return;
        }

        ShipVariantAPI shipVariant = fm.getVariant();
        if(shipVariant.hasHullMod("exoticatech")) {
            shipVariant.removePermaMod("exoticatech");
        }
    }

    @Override
    public boolean affectsOPCosts() {
        return false;
    }

    @Override
    public Color getNameColor() {
        return hullmodColor;
    }


    @Override
    public void advanceInCampaign(FleetMemberAPI fm, float amount) {
        ShipModifications mods = ShipModLoader.get(fm);
        if(mods == null) {
            fm.getVariant().removePermaMod("exoticatech");
            return;
        }

        for(Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            int level = mods.getUpgrade(upgrade);
            if(level <= 0) continue;
            upgrade.advanceInCampaign(fm, mods, amount);
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        FleetMemberAPI member = FleetMemberUtils.findMemberFromShip(ship);
        if(member == null) return;

        ShipModifications mods = ShipModLoader.get(member);
        if(mods == null) return;

        float bandwidth = mods.getBandwidthWithExotics(member);
        for(Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            int level = mods.getUpgrade(upgrade);
            if(level <= 0) continue;
            upgrade.advanceInCombat(ship, amount, member, mods);
        }

        for(Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if(!mods.hasExotic(exotic)) continue;
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

        ShipModifications mods = ShipModLoader.get(fm);

        if (mods == null) {
            fm.getVariant().removePermaMod("exoticatech");
            return;
        }

        float bandwidth = mods.getBandwidthWithExotics(fm);

        for(Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if(!mods.hasExotic(exotic)) continue;

            exotic.applyExoticToStats(fm, stats, bandwidth, id);
        }

        for(Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            int level = mods.getUpgrade(upgrade);
            if(level <= 0) continue;

            upgrade.applyUpgradeToStats(fm, stats, mods);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        FleetMemberAPI member = FleetMemberUtils.findMemberFromShip(ship);
        if(member == null) return;

        ShipModifications mods = ShipModLoader.get(member);
        if(mods == null) return;

        float bandwidth = mods.getBandwidthWithExotics(member);

        for(Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if(!mods.hasExotic(exotic)) continue;
            exotic.applyExoticToShip(member, ship, bandwidth, id);
        }

        for(Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            if(!mods.hasUpgrade(upgrade)) continue;
            upgrade.applyUpgradeToShip(member, ship, mods);
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

        ShipModifications mods = ShipModLoader.get(fm);
        if (mods == null) return;
        float bandwidth = mods.getBandwidthWithExotics(fm);
        String bandwidthString = BandwidthUtil.getFormattedBandwidthWithName(bandwidth);

        hullmodTooltip.addPara("The ship has %s bandwidth.", 0, Bandwidth.getColor(bandwidth),  bandwidthString);

        boolean exoticsExpand = Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"));
        boolean upgradesExpand = Keyboard.isKeyDown(Keyboard.getKeyIndex("F2"));

        CustomPanelAPI customPanelAPI = null;
        TooltipMakerAPI tooltip = hullmodTooltip;

        if(exoticsExpand || upgradesExpand) {
            customPanelAPI = Global.getSettings().createCustom(width, 300f, null);
            TooltipMakerAPI scrollTooltip = customPanelAPI.createUIElement(width, 300f, true);
            CustomPanelAPI innerPanel = customPanelAPI.createCustomPanel(width, 1100f, null);
            tooltip = innerPanel.createUIElement(width, 1100f, false);

            innerPanel.addUIElement(tooltip);
            scrollTooltip.addCustom(innerPanel, 0f);
            customPanelAPI.addUIElement(scrollTooltip).inTL(-5f, 0);
            hullmodTooltip.addCustom(customPanelAPI, 0f);
            hullmodTooltip.setForceProcessInput(true);
        }

        if (!upgradesExpand) {
            UIComponentAPI lastLabel = null;
            boolean addedExoticSection = false;
            try {
                for (Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
                    if (!mods.hasExotic(exotic.getKey())) continue;

                    if (!addedExoticSection) {
                        addedExoticSection = true;
                        tooltip.addSectionHeading(StringUtils.getString("FleetScanner", "ExoticHeader"), Alignment.MID, 6);
                        lastLabel = tooltip.getPrev();
                    }

                    tooltip.addTitle(exotic.getName(), exotic.getColor()).getPosition().belowLeft(lastLabel, 3);
                    UIComponentAPI title = tooltip.getPrev();
                    exotic.modifyToolTip(tooltip, title, fm, mods, exoticsExpand);

                    lastLabel = tooltip.getPrev();
                    tooltip.setParaFontDefault();
                    tooltip.setParaFontColor(tooltipColor);
                }
            } catch (Throwable th) {
                log.info("Caught exotic description exception", th);
                tooltip.addPara("Caught an error! See starsector.log", Color.RED, 0);
            }
        }

        if (!exoticsExpand) {
            boolean addedUpgradeSection = false;
            try {
                for (Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
                    if (mods.getUpgrade(upgrade) < 1) continue;

                    if (!addedUpgradeSection) {
                        addedUpgradeSection = true;
                        tooltip.addSectionHeading(StringUtils.getString("FleetScanner", "UpgradeHeader"), Alignment.MID, 6);
                    }
                    upgrade.modifyToolTip(tooltip, ship.getMutableStats(), fm, mods, upgradesExpand);
                    tooltip.setParaFontDefault();
                    tooltip.setParaFontColor(tooltipColor);
                }
            } catch (Throwable th) {
                log.info("Caught upgrade description exception", th);
                tooltip.addPara("Caught an error! See starsector.log", Color.RED, 0);
            }
        }

        if (!exoticsExpand && !upgradesExpand) {
            StringUtils.getTranslation("CommonOptions", "ExpandExotics").addToTooltip(hullmodTooltip);
            StringUtils.getTranslation("CommonOptions", "ExpandUpgrades").addToTooltip(hullmodTooltip);
        } else {
            StringUtils.getTranslation("CommonOptions", "UnexpandInfo").addToTooltip(hullmodTooltip);
        }
    }

    public static void removeHullModFromVariant(ShipVariantAPI v) {
        v.removePermaMod("exoticatech");

        List<String> slots = v.getModuleSlots();
        if (slots == null || slots.isEmpty()) return;

        for (String slot : slots) {
            ShipVariantAPI module = v.getModuleVariant(slot);
            if (module != null) {
                removeHullModFromVariant(module);
            }
        }
    }
}