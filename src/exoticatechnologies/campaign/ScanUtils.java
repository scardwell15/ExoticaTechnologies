package exoticatechnologies.campaign;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticsHandler;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.modifications.bandwidth.BandwidthUtil;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Log4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScanUtils {
    private static float NOTABLE_BANDWIDTH = 180f;

    public static List<FleetMemberAPI> getNotableFleetMembers(CampaignFleetAPI fleet) {
        List<FleetMemberAPI> notableMembers = new ArrayList<>();

        for (FleetMemberAPI fm : fleet.getMembersWithFightersCopy()) {
            if (fm.isFighterWing()) continue;
            if (isFleetMemberNotable(fm)) {
                notableMembers.add(fm);
            }
        }

        log.info(String.format("Fleet has %s notable members", notableMembers.size()));

        return notableMembers;
    }

    public static boolean isFleetMemberNotable(FleetMemberAPI fm) {
        if (ETModPlugin.hasData(fm.getId())) {
            ShipModifications mods = ETModPlugin.getData(fm.getId());

            log.info(String.format("ShipModifications info for ship [%s]: upg [%s] aug [%s] bdw [%s]",
                    fm.getShipName(),
                    mods.hasUpgrades(),
                    mods.hasExotics(),
                    mods.getBandwidth(fm)));

            return doesEntityHaveNotableMods(mods);
        }
        return false;
    }

    public static boolean doesEntityHaveNotableMods(ShipModifications mods) {
        if (mods.hasUpgrades() || mods.hasExotics() || mods.getBandwidth() >= NOTABLE_BANDWIDTH) {
            return true;
        }
        return false;
    }

    public static void addModificationsToTextPanel(TextPanelAPI textPanel, String shipName, ShipModifications mods, ShipAPI.HullSize hullSize, Color color) {
        float bandwidth = mods.getBandwidth();

        if (color == null) {
            color = Misc.getHighlightColor();
        }

        if (!(mods.hasUpgrades() || mods.hasExotics())) {
            StringUtils.getTranslation("FleetScanner", "ShipHasBandwidth")
                    .format("name", shipName, color)
                    .format("bandwidth", BandwidthUtil.getFormattedBandwidthWithName(bandwidth), Bandwidth.getBandwidthColor(bandwidth))
                    .addToTextPanel(textPanel);
            return;
        }

        StringUtils.getTranslation("FleetScanner", "ShipHasUpgrades")
                .format("name", shipName, color)
                .format("bandwidth", BandwidthUtil.getFormattedBandwidthWithName(bandwidth), Bandwidth.getBandwidthColor(bandwidth))
                .addToTextPanel(textPanel);

        if (mods.hasUpgrades()) {
            TooltipMakerAPI upgradeBar = textPanel.beginTooltip();
            upgradeBar.addSectionHeading(StringUtils.getString("FleetScanner", "UpgradeHeader"), Alignment.MID, 2f);
            textPanel.addTooltip();


            float lastY = 0;
            int upgs = 0;
            for (int i = 0; i < UpgradesHandler.UPGRADES_LIST.size(); i++) {
                Upgrade upgrade = UpgradesHandler.UPGRADES_LIST.get(i);

                if (!mods.hasUpgrade(upgrade)) continue;

                TooltipMakerAPI tooltip = textPanel.beginTooltip();

                int x = 4 + ((upgs % 6) * 74);
                tooltip.addImage(upgrade.getIcon(), 64f, lastY);
                tooltip.getPrev().getPosition().setXAlignOffset(x);
                if (upgs % 6 == 0) {
                    lastY = -74;
                } else if (upgs % 6 == 5) {
                    lastY = 0;
                }
                tooltip.addTooltipToPrevious(new UpgradeTooltip(upgrade, tooltip, mods, hullSize), TooltipMakerAPI.TooltipLocation.BELOW);
                textPanel.addTooltip();

                upgs++;
            }
            textPanel.addPara("");
        }

        if (mods.hasExotics()) {
            TooltipMakerAPI exoticBar = textPanel.beginTooltip();
            exoticBar.addSectionHeading(StringUtils.getString("FleetScanner", "ExoticHeader"),
                    Misc.getTooltipTitleAndLightHighlightColor(),
                    new Color(255, 200, 0),
                    Alignment.MID, 2f);
            textPanel.addTooltip();

            float lastY = 0;
            int augs = 0;
            for (int i = 0; i < ExoticsHandler.EXOTIC_LIST.size(); i++) {
                Exotic exotic = ExoticsHandler.EXOTIC_LIST.get(i);

                if (!mods.hasExotic(exotic)) continue;

                TooltipMakerAPI tooltip = textPanel.beginTooltip();

                int x = 4 + ((augs % 6) * 74);
                tooltip.addImage(exotic.getIcon(), 64f, lastY);
                tooltip.getPrev().getPosition().setXAlignOffset(x);
                if (augs % 6 == 0) {
                    lastY = -74;
                } else if (augs % 6 == 5) {
                    lastY = 0;
                }
                tooltip.addTooltipToPrevious(new ExoticTooltip(exotic, tooltip), TooltipMakerAPI.TooltipLocation.BELOW);
                textPanel.addTooltip();

                augs++;
            }
            textPanel.addPara("");
        }
    }

    @RequiredArgsConstructor
    protected static class ExoticTooltip extends BaseTooltipCreator {
        @Getter
        private final Exotic exotic;
        private final TooltipMakerAPI tooltip;

        @Override
        public float getTooltipWidth(Object tooltipParam) {
            return Math.min(tooltip.computeStringWidth(exotic.getDescription()), 300f);
        }

        @Override
        public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
            tooltip.addPara(exotic.getName(), exotic.getMainColor(), 0f);
            tooltip.addPara(exotic.getTextDescription(), 0f);
        }
    }

    @RequiredArgsConstructor
    protected static class UpgradeTooltip extends BaseTooltipCreator {
        @Getter
        private final Upgrade upgrade;
        private final TooltipMakerAPI tooltip;
        private final ShipModifications mods;
        private final ShipAPI.HullSize hullSize;

        @Override
        public float getTooltipWidth(Object tooltipParam) {
            return Math.min(tooltip.computeStringWidth(upgrade.getDescription()), 300f);
        }

        @Override
        public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
            StringUtils.getTranslation("FleetScanner", "UpgradeNameWithLevelAndMax")
                    .format("upgradeName", upgrade.getName(), upgrade.getColor())
                    .format("level", mods.getUpgrade(upgrade))
                    .format("max", upgrade.getMaxLevel(hullSize))
                    .addToTooltip(tooltip);
            tooltip.addPara(upgrade.getDescription(), 0f);
        }
    }
}
