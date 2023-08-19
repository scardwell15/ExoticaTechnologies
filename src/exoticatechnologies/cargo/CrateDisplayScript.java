package exoticatechnologies.cargo;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import lombok.SneakyThrows;

import java.awt.Robot;
import java.awt.event.KeyEvent;

/**
 * thank you to Schaf-Unschaf for the idea
 */
public class CrateDisplayScript implements EveryFrameScript {
    private final CargoAPI cargo;
    private final Robot robot;
    private boolean success = false;

    @SneakyThrows
    public CrateDisplayScript(CargoAPI cargo) {
        this.cargo = cargo;
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
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
            success = Global.getSector().getCampaignUI().showInteractionDialog(new CrateItemDialog(cargo, CrateGlobalData.getInstance().getCargo()), null);
        }
    }
}
