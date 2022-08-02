package exoticatechnologies.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowDefaultVisual;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.modifications.bandwidth.BandwidthUtil;
import exoticatechnologies.ui.ShipModificationUIPanelPlugin;
import exoticatechnologies.util.RenderUtils;
import exoticatechnologies.util.StringUtils;
import lombok.extern.log4j.Log4j;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Log4j
public class ETPrototypeUI extends BaseCommandPlugin {
    private static InteractionDialogAPI dialog;
    private static Map<String, MemoryAPI> memoryMap;
    private static final float SHIP_ROW_HEIGHT = 64;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        ETPrototypeUI.dialog = dialog;
        ETPrototypeUI.memoryMap = memoryMap;

        List<FleetMemberAPI> fleetMembers = Global.getSector().getPlayerFleet().getFleetData().getMembersInPriorityOrder();
        Iterator<FleetMemberAPI> iterator = fleetMembers.iterator();
        while (iterator.hasNext()) {
            FleetMemberAPI fleetMemberAPI = iterator.next();
            if (fleetMemberAPI.isFighterWing()) {
                iterator.remove();
            }
        }

        showModifyShipsPanel(dialog, fleetMembers);
        return true;
    }

    private static void showModifyShipsPanel(InteractionDialogAPI dialog, List<FleetMemberAPI> members) {
        float screenWidth = Display.getWidth();
        float screenHeight = Display.getHeight();

        float allRowsHeight = (SHIP_ROW_HEIGHT + 10) * (members.size() * 3) + 3;

        float panelHeight = Math.min(allRowsHeight + 20 + 16, screenHeight * 0.65f);
        float panelWidth = Math.min(screenWidth * 0.85f, 1280); // maybe could scale it to the largest number of icons we'll have to show?

        ShipModificationDialogDelegate delegate = new ShipModificationDialogDelegate(members, dialog.getInteractionTarget().getMarket(), panelWidth, panelHeight);
        dialog.showCustomDialog(panelWidth, panelHeight, delegate);
    }

    /**
     * Handles drawing of the custom dialog that shows notable ships' info.
     * @author Histidine
     */
    public static class ShipModificationDialogDelegate implements CustomDialogDelegate {
        private final DrawCreditsPanelPlugin plugin;
        protected final float panelWidth;
        protected final float panelHeight;

        protected final MarketAPI market;
        protected final List<FleetMemberAPI> members;
        protected final List<ShipModificationUIPanelPlugin> rowPlugins = new ArrayList<>();

        public ShipModificationDialogDelegate(List<FleetMemberAPI> members, MarketAPI market, float panelWidth, float panelHeight) {
            this.members = members;
            this.market = market;
            this.panelWidth = panelWidth;
            this.panelHeight = panelHeight;
            this.plugin = new DrawCreditsPanelPlugin();
        }

        @Override
        public void createCustomDialog(CustomPanelAPI panel) {
            float allRowsHeight = (SHIP_ROW_HEIGHT + 10) * (members.size() + 3) - 16;
            TooltipMakerAPI tt = panel.createUIElement(panelWidth, panelHeight, true);

            String headerStr = StringUtils.getTranslation("FleetScanner", "NotableShipsHeader").toString();
            tt.addSectionHeading(headerStr, Alignment.MID, 0);
            UIComponentAPI heading = tt.getPrev();

            CustomPanelAPI newPanel = panel.createCustomPanel(panelWidth, allRowsHeight, null);
            TooltipMakerAPI tooltip = newPanel.createUIElement(panelWidth, allRowsHeight, false);


            for (FleetMemberAPI member : members) {
                rowPlugins.add(addRow(panel, tooltip, member));
            }

            newPanel.addUIElement(tooltip).inTL(0, 0);
            tt.addCustom(newPanel, 3).getPosition().belowMid(heading, 3);
            panel.addUIElement(tt).inTL(0, 0);
        }

        /**
         * Adds a row for the specified fleet member's info.
         */
        public ShipModificationUIPanelPlugin addRow(CustomPanelAPI outer, TooltipMakerAPI tooltip, FleetMemberAPI member) {
            float pad = 3;
            float opad = 10;
            float textWidth = 240;
            Color f = member.getCaptain().getFaction().getBaseUIColor();

            ShipModifications mods = ShipModFactory.getForFleetMember(member);

            ShipModificationUIPanelPlugin scanMemberPanelPlugin = new ShipModificationUIPanelPlugin(this, tooltip, panelWidth, member, mods, market);
            CustomPanelAPI rowHolder = outer.createCustomPanel(panelWidth, SHIP_ROW_HEIGHT, scanMemberPanelPlugin);
            scanMemberPanelPlugin.setMyPanel(rowHolder);

            // Ship image with tooltip of the ship class
            TooltipMakerAPI shipImg = rowHolder.createUIElement(SHIP_ROW_HEIGHT, SHIP_ROW_HEIGHT, false);
            List<FleetMemberAPI> memberAsList = new ArrayList<>();
            memberAsList.add(member);
            shipImg.addShipList(1, 1, SHIP_ROW_HEIGHT, Misc.getBasePlayerColor(), memberAsList, 0);
            rowHolder.addUIElement(shipImg).inTL(0, 0);

            // Ship name, class, bandwidth
            TooltipMakerAPI shipText = rowHolder.createUIElement(textWidth, SHIP_ROW_HEIGHT, false);
            shipText.addPara(member.getShipName(), f, 0);
            shipText.addPara(member.getHullSpec().getNameWithDesignationWithDashClass(), 0);

            float bandwidth = mods.getBandwidthWithExotics(member);
            LabelAPI bandwidthLabel = StringUtils.getTranslation("FleetScanner", "ShipBandwidthShort")
                    .format("bandwidth", BandwidthUtil.getFormattedBandwidthWithName(bandwidth), Bandwidth.getBandwidthColor(bandwidth))
                    .addToTooltip(shipText, pad);
            rowHolder.addUIElement(shipText).rightOfTop(shipImg, pad);

            scanMemberPanelPlugin.setBandwidthLabel(bandwidthLabel);

            scanMemberPanelPlugin.setUpperXOffset(shipText.getPosition().getWidth());
            // done, add row to TooltipMakerAPI
            tooltip.addCustom(rowHolder, opad);

            return scanMemberPanelPlugin;
        }

        public void selectedPanelEvent(ShipModificationUIPanelPlugin rowPlugin) {
            for (ShipModificationUIPanelPlugin disableThisPlugin : rowPlugins) {
                if (disableThisPlugin.isSelected() && !disableThisPlugin.equals(rowPlugin)) {
                    disableThisPlugin.switchToPanel(ShipModificationUIPanelPlugin.PICKER_INDEX);
                }
            }
        }

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
        public void customDialogConfirm() {
            new ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), memoryMap);

            memoryMap.get(MemKeys.LOCAL).set("$option", "ETDialogBack", 0f);
            FireAll.fire(null, dialog, memoryMap, "DialogOptionSelected");
        }

        @Override
        public void customDialogCancel() {}

        @Override
        public CustomUIPanelPlugin getCustomPanelPlugin() {
            return this.plugin;
        }
    }

    public static class DrawCreditsPanelPlugin implements CustomUIPanelPlugin {
        private static LazyFont.DrawableString TODRAW14;
        private PositionAPI pos = null;
        static {
            try {
                LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/orbitron20aa.fnt");
                TODRAW14 = fontdraw.createText();
            } catch (FontException ignored) {
            }
        }

        @Override
        public void positionChanged(PositionAPI position) {
            this.pos = position;
        }

        @Override
        public void renderBelow(float alphaMult) {

        }

        @Override
        public void render(float alphaMult) {
            float credits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
            int width = RenderUtils.getWidth();
            int height = (int) (pos.getY() + pos.getHeight() * 0.2); //RenderUtils.getHeight();

            RenderUtils.pushUIRenderingStack();
            TODRAW14.setText("Credits: " + Misc.getDGSCredits(credits));
            TODRAW14.setBaseColor(Misc.getHighlightColor());
            TODRAW14.draw(width * 0.25f, height * 0.2f);
            RenderUtils.popUIRenderingStack();
        }

        @Override
        public void advance(float amount) {
        }

        @Override
        public void processInput(List<InputEventAPI> events) {

        }
    }
}
