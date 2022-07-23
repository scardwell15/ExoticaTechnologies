package exoticatechnologies.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public abstract class TabbedCustomUIPanelPlugin implements CustomUIPanelPlugin {
    private float SWITCHER_BUTTON_WIDTH = 68;

    @Getter
    @Setter
    protected CustomPanelAPI myPanel;
    @Getter
    @Setter
    protected TooltipMakerAPI myTooltip;

    protected TooltipMakerAPI switcherPanel;
    protected ButtonAPI switcherButton;

    protected CustomPanelAPI currentPanel;
    protected int currentPanelIndex = 0;

    @Override
    public void advance(float amount) {
        if (switcherPanel == null) {
            if (shouldMakeSwitcher()) {
                makeSwitcher(currentPanelIndex);
                switchPanels(currentPanelIndex); //generate first panel
            }
            return;
        } else if (!shouldMakeSwitcher()) {
            //note: i can't remove the whole panel here for some reason.
            killSwitcherButton();
            return;
        }

        if (!canSwitch() && switcherButton.isEnabled()) {
            switcherButton.setEnabled(false);
        }

        if (switcherButton.isChecked()) {
            switcherButton.setChecked(false);
        }
    }

    protected void switcherButtonClicked() {
        if (canSwitch()) {
            cyclePanelIndex();
            switchPanels(currentPanelIndex);
        }
    }

    protected abstract int getMaxPanelIndex();

    private void cyclePanelIndex() {
        currentPanelIndex++;
        if (currentPanelIndex > getMaxPanelIndex()) {
            currentPanelIndex = 0;
        }
    }

    protected boolean shouldMakeSwitcher() {
        return true;
    }

    protected boolean canSwitch() {
        return true;
    }

    protected void switchPanels(int newPanelIndex) {
        if (currentPanel != null) {
            //must remove whole ass panel here because alex
            myPanel.removeComponent(switcherPanel);
            makeSwitcher(newPanelIndex);
        }

        currentPanel = createNewPanel(newPanelIndex, getSwitcherPanelWidth(), getSwitcherPanelHeight());
        switcherPanel.addCustom(currentPanel, 3).getPosition().rightOfTop(switcherButton, 3);
    }

    protected abstract CustomPanelAPI createNewPanel(int newPanelIndex, float panelWidth, float panelHeight);

    protected abstract String getSwitcherLabelText(int newPanelIndex);

    protected abstract float getSwitcherLabelWidth(int newPanelIndex);

    private void makeSwitcher(int newPanelIndex) {
        makeSwitcherPanel(newPanelIndex, myPanel.getPosition().getWidth() - getSwitcherPanelWidth(), getSwitcherPanelWidth(), getSwitcherPanelHeight());
        makeSwitcherButtons(newPanelIndex, getSwitcherButtonWidth(), getSwitcherButtonHeight());
    }

    protected void makeSwitcherPanel(int newPanelIndex, float panelX, float panelWidth, float panelHeight) {
        switcherPanel = myPanel.createUIElement(panelWidth, panelHeight, false);
        myPanel.addUIElement(switcherPanel).inTL(panelX, 0);
    }

    protected void makeSwitcherButtons(int newPanelIndex, float buttonW, float buttonH) {
        FactionAPI playerFaction = Global.getSector().getPlayerFaction();

        switcherButton = switcherPanel.addAreaCheckbox(getSwitcherLabelText(newPanelIndex), "id",
                playerFaction.getBaseUIColor(), playerFaction.getDarkUIColor(), playerFaction.getBrightUIColor(), buttonW, buttonH, 0);
    }

    protected void killSwitcherButton() {
        switcherPanel.removeComponent(switcherButton);
        switcherButton = null;
    }

    private float getSwitcherPanelWidth() {
        return myPanel.getPosition().getWidth() - 340;
    }

    private float getSwitcherPanelHeight() {
        return 64;
    }

    protected float getSwitcherButtonWidth() {
        return SWITCHER_BUTTON_WIDTH;
    }

    protected float getSwitcherButtonHeight() {
        return 64;
    }

    @Override
    public void positionChanged(PositionAPI position) {
    }

    @Override
    public void renderBelow(float alphaMult) {
    }

    @Override
    public void render(float alphaMult) {
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
    }
}
