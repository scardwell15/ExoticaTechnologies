package exoticatechnologies.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.ui.impl.ShipModificationDialogDelegate;
import exoticatechnologies.util.RenderUtils;
import lombok.extern.log4j.Log4j;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.Display;

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

        ShipModificationDialogDelegate delegate = new ShipModificationDialogDelegate(dialog, dialog.getInteractionTarget().getMarket());
        dialog.showCustomDialog(panelWidth, panelHeight, delegate);
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
