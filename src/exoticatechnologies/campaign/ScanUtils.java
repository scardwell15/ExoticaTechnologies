package exoticatechnologies.campaign;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.modifications.ShipModFactory;
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
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Log4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScanUtils {
    private static float NOTABLE_BANDWIDTH = 180f;
    private static float NOTABLE_SHIPS_ROW_HEIGHT = 64;

    public static Long getPerShipDataSeed(ShipRecoverySpecial.PerShipData shipData, int i) {
        ShipVariantAPI var = shipData.getVariant();
        if (var == null) return null;

        long seed = -1;
        if (shipData.fleetMemberId != null) {
            seed = shipData.fleetMemberId.hashCode();
        } else {
            long hash = var.hashCode();
            if (var.getHullVariantId() != null) {
                hash = var.getHullVariantId().hashCode();
            }

            seed = hash + i;

            if (shipData.shipName != null) {
                seed = seed + shipData.shipName.hashCode();
            }
        }

        return seed;
    }

    public static String getPerShipDataId(ShipRecoverySpecial.PerShipData shipData, int i) {
        Long seed = getPerShipDataSeed(shipData, i);
        if (seed == null) return null;
        return String.valueOf(seed);
    }

    public static boolean isPerShipDataNotable(ShipRecoverySpecial.PerShipData shipData, int i) {
        String entityId = getPerShipDataId(shipData, i);
        if (entityId == null) return false;

        log.info(String.format("searching for entity ID [%s]", entityId));
        if (ETModPlugin.hasData(entityId)
                && ScanUtils.doesEntityHaveNotableMods(ETModPlugin.getData(entityId))) {
            return true;
        }
        return false;
    }

    public static List<FleetMemberAPI> getNotableFleetMembers(CampaignFleetAPI fleet) {
        List<FleetMemberAPI> notableMembers = new ArrayList<>();

        if (fleet == null) return notableMembers;

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
                    mods.getBandwidthWithExotics(fm)));

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

    public static void showNotableShipsPanel(InteractionDialogAPI dialog, List<FleetMemberAPI> members) {
        float screenWidth = Display.getWidth();
        float screenHeight = Display.getHeight();

        float allRowsHeight = (NOTABLE_SHIPS_ROW_HEIGHT + 10) * members.size() + 3;

        float panelHeight = Math.min(allRowsHeight + 20 + 16, screenHeight * 0.65f);
        float panelWidth = screenWidth * 0.65f; // maybe could scale it to the largest number of icons we'll have to show?

        NotableShipsDialogDelegate delegate = new NotableShipsDialogDelegate();
        delegate.init(members, panelWidth, panelHeight, allRowsHeight);
        dialog.showCustomDialog(panelWidth, panelHeight, delegate);
    }

    /**
     * Handles drawing of the custom dialog that shows notable ships' info.
     * @author Histidine
     */
    protected static class NotableShipsDialogDelegate implements CustomDialogDelegate {

        protected float panelWidth;
        protected float panelHeight;
        protected float allRowsHeight;

        protected List<FleetMemberAPI> members;

        public void init(List<FleetMemberAPI> members, float panelWidth, float panelHeight, float allRowsHeight) {
            this.members = members;
            this.panelWidth = panelWidth;
            this.panelHeight = panelHeight;
            this.allRowsHeight = allRowsHeight;
        }

        @Override
        public void createCustomDialog(CustomPanelAPI panel) {
            TooltipMakerAPI tt = panel.createUIElement(panelWidth, panelHeight - 16, true);
            String headerStr = StringUtils.getTranslation("FleetScanner", "NotableShipsHeader").toString();
            tt.addSectionHeading(headerStr, Alignment.MID, 0);

            for (FleetMemberAPI member : members) {
                addRow(panel, tt, member);
            }

            panel.addUIElement(tt).inTL(0, 0);
        }

        /**
         * Adds a row for the specified fleet member's info.
         */
        public void addRow(CustomPanelAPI outer, TooltipMakerAPI tooltip, FleetMemberAPI member) {
            float pad = 3;
            float opad = 10;
            float textWidth = 240;
            Color f = member.getCaptain().getFaction().getBaseUIColor();

            ShipModifications mods = ShipModFactory.getForFleetMember(member);

            CustomPanelAPI rowHolder = outer.createCustomPanel(panelWidth, NOTABLE_SHIPS_ROW_HEIGHT, null);

            // Ship image with tooltip of the ship class
            TooltipMakerAPI shipImg = rowHolder.createUIElement(NOTABLE_SHIPS_ROW_HEIGHT, NOTABLE_SHIPS_ROW_HEIGHT, false);
            List<FleetMemberAPI> memberAsList = new ArrayList<>();
            memberAsList.add(member);
            shipImg.addShipList(1, 1, NOTABLE_SHIPS_ROW_HEIGHT, Misc.getBasePlayerColor(), memberAsList, 0);
            rowHolder.addUIElement(shipImg).inTL(0, 0);

            // Ship name, class, bandwidth
            TooltipMakerAPI shipText = rowHolder.createUIElement(textWidth, NOTABLE_SHIPS_ROW_HEIGHT, false);
            shipText.addPara(member.getShipName(), f, 0);
            shipText.addPara(member.getHullSpec().getNameWithDesignationWithDashClass(), 0);
            float bandwidth = mods.getBandwidth();
            StringUtils.getTranslation("FleetScanner", "ShipBandwidthShort")
                    .format("bandwidth", BandwidthUtil.getFormattedBandwidthWithName(bandwidth), Bandwidth.getBandwidthColor(bandwidth))
                    .addToTooltip(shipText, pad);
            rowHolder.addUIElement(shipText).rightOfTop(shipImg, pad);

            // Draw icons for ship upgrades/exotics, with tooltips
            if (mods.hasUpgrades() || mods.hasExotics()) {
                // FIXME: no handling of the case where the ship has too many upgrades to fit on one row, if such a ship exists
                TooltipMakerAPI lastImg = null;

                for (int i = 0; i < UpgradesHandler.UPGRADES_LIST.size(); i++) {
                    Upgrade upgrade = UpgradesHandler.UPGRADES_LIST.get(i);
                    if (!mods.hasUpgrade(upgrade)) continue;

                    TooltipMakerAPI upgIcon = rowHolder.createUIElement(64, 64, false);
                    upgIcon.addImage(upgrade.getIcon(), 64, 0);
                    upgIcon.addTooltipToPrevious(new UpgradeTooltip(upgrade, tooltip, mods, member.getHullSpec().getHullSize()),
                            TooltipMakerAPI.TooltipLocation.BELOW);
                    rowHolder.addUIElement(upgIcon).rightOfTop(lastImg == null ? shipText : lastImg, pad);

                    lastImg = upgIcon;
                }
            }

            // done, add row to TooltipMakerAPI
            tooltip.addCustom(rowHolder, opad);
        };

        @Override
        public boolean hasCancelButton() {
            return false;
        }

        @Override
        public String getConfirmText() {
            return null;
        }

        @Override
        public String getCancelText() {
            return null;
        }

        @Override
        public void customDialogConfirm() {}

        @Override
        public void customDialogCancel() {}

        @Override
        public CustomUIPanelPlugin getCustomPanelPlugin() {
            return null;    //new NotableShipsPanelPlugin();
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
