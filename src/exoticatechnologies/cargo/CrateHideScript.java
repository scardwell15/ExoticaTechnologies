package exoticatechnologies.cargo;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import lombok.SneakyThrows;

import java.awt.*;

/**
 * thank you to Schaf-Unschaf for the idea
 */
public class CrateHideScript implements EveryFrameScript {
    private final Robot robot;
    private boolean success = false;

    @SneakyThrows
    public CrateHideScript() {
        this.robot = new Robot();
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (success) {
            Global.getSector().removeTransientScript(this);
        } else {
            Global.getSector().setPaused(true);
            Global.getSector().getCampaignUI().showCoreUITab(CoreUITabId.CARGO);
            success = Global.getSector().getCampaignUI().getCurrentCoreTab().equals(CoreUITabId.CARGO);
        }
    }
}
