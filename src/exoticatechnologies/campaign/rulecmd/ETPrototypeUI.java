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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Log4j
public class ETPrototypeUI extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
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
        ShipModificationDialogDelegate delegate = new ShipModificationDialogDelegate(dialog, dialog.getInteractionTarget().getMarket());
        dialog.showCustomDialog(delegate.getPanelWidth(), delegate.getPanelHeight(), delegate);
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
