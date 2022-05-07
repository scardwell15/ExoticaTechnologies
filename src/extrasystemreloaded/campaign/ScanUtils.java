package extrasystemreloaded.campaign;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import extrasystemreloaded.Es_ModPlugin;
import extrasystemreloaded.campaign.rulecmd.ESScanFleet;
import extrasystemreloaded.systems.exotics.Exotic;
import extrasystemreloaded.systems.exotics.ExoticsHandler;
import extrasystemreloaded.systems.bandwidth.Bandwidth;
import extrasystemreloaded.systems.bandwidth.BandwidthUtil;
import extrasystemreloaded.systems.upgrades.Upgrade;
import extrasystemreloaded.systems.upgrades.UpgradesHandler;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
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
        if (Es_ModPlugin.hasData(fm.getId())) {
            ExtraSystems es = Es_ModPlugin.getData(fm.getId());

            log.info(String.format("ExtraSystems info for ship [%s]: upg [%s] aug [%s] bdw [%s]",
                    fm.getShipName(),
                    es.hasUpgrades(),
                    es.hasExotics(),
                    es.getBandwidth(fm)));

            return isESNotable(es);
        }
        return false;
    }

    public static boolean isESNotable(ExtraSystems es) {
        if (es.hasUpgrades() || es.hasExotics() || es.getBandwidth() >= NOTABLE_BANDWIDTH) {
            return true;
        }
        return false;
    }

    public static void addSystemsToTextPanel(TextPanelAPI textPanel, String shipName, ExtraSystems es, ShipAPI.HullSize hullSize, Color color) {
        float bandwidth = es.getBandwidth();

        if (color == null) {
            color = Misc.getHighlightColor();
        }

        if (!(es.hasUpgrades() || es.hasExotics())) {
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

        if (es.hasUpgrades()) {
            TooltipMakerAPI upgradeBar = textPanel.beginTooltip();
            upgradeBar.addSectionHeading(StringUtils.getString("FleetScanner", "UpgradeHeader"), Alignment.MID, 2f);
            textPanel.addTooltip();

            for (Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
                if(es.getUpgrade(upgrade) > 0) {
                    TooltipMakerAPI tooltip = textPanel.beginTooltip();

                    StringUtils.getTranslation("FleetScanner", "UpgradeNameWithLevelAndMax")
                            .format("upgradeName", upgrade.getName(), upgrade.getColor())
                            .format("level", es.getUpgrade(upgrade))
                            .format("max", upgrade.getMaxLevel(hullSize))
                            .addToTooltip(tooltip);

                    tooltip.addTooltipToPrevious(new UpgradeTooltip(upgrade, tooltip), TooltipMakerAPI.TooltipLocation.BELOW);

                    textPanel.addTooltip();
                }
            }
        }

        if (es.hasExotics()) {
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

                if (!es.hasExotic(exotic)) continue;

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

        @Override
        public float getTooltipWidth(Object tooltipParam) {
            return Math.min(tooltip.computeStringWidth(upgrade.getDescription()), 300f);
        }

        @Override
        public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
            tooltip.addPara(upgrade.getDescription(), 0f);
        }
    }
}
