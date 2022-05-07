package extrasystemreloaded.dialog;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import java.util.List;

public class ResourceDisplayPlugin implements CustomUIPanelPlugin {
    protected PositionAPI pos;

    @Override
    public void positionChanged(PositionAPI pos) {
        this.pos = pos;
    }

    @Override
    public void renderBelow(float alphaMult) {
    }

    @Override
    public void render(float alphaMult) {
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void processInput(List<InputEventAPI> arg0) {
    }
}