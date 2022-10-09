package exoticatechnologies.ui.java;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import java.util.List;

public class TimedUIPlugin implements CustomUIPanelPlugin {
    private final float endLife;
    private final Listener listener;
    private boolean finished = false;
    private float currLife;
    private PositionAPI pos;

    public TimedUIPlugin(float lifeInSeconds, Listener endListener) {
        currLife = 0f;
        endLife = lifeInSeconds;
        listener = endListener;
    }

    @Override
    public void advance(float amount) {
        if (finished) return;

        currLife += amount;

        if (currLife >= endLife) {
            finished = true;
            listener.end();
        }
    }

    @Override
    public void positionChanged(PositionAPI position) {
        this.pos = position;
    }

    @Override
    public void renderBelow(float alphaMult) {
        listener.renderBelow(pos, alphaMult, currLife, endLife);
    }

    @Override
    public void render(float alphaMult) {
        listener.render(pos, alphaMult, currLife, endLife);
    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    public interface Listener {
        void end();
        void render(PositionAPI pos, float alphaMult, float currLife, float endLife);
        void renderBelow(PositionAPI pos, float alphaMult, float currLife, float endLife);
    }
}
