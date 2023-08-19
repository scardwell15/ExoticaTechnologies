package exoticatechnologies.ui.java;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public abstract class TabbedCustomUIPanelPlugin implements CustomUIPanelPlugin {
    private final float SWITCHER_BUTTON_WIDTH = 68;

    protected PositionAPI pos;

    @Getter
    @Setter
    protected CustomPanelAPI myPanel;
    @Getter
    protected final TooltipMakerAPI myTooltip;

    protected TooltipMakerAPI switcherPanel;
    protected ButtonAPI switcherButton;

    protected CustomPanelAPI currentPanel;
    protected int lastPanelIndex = 0;
    protected int currentPanelIndex = 0;

    protected float switcherPanelDefaultWidth;
    protected final float switcherButtonHeight = 64;
    protected final float switcherPanelHeight = 64;

    public TabbedCustomUIPanelPlugin(TooltipMakerAPI tooltip, float defaultSwitcherPanelWidth) {
        this.myTooltip = tooltip;
        this.switcherPanelDefaultWidth = defaultSwitcherPanelWidth;
    }

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
            switcherButtonClicked();
        }
    }

    protected void switcherButtonClicked() {
        if (canSwitch()) {
            cyclePanelIndex();
            switchPanels(currentPanelIndex);
        }
    }

    protected abstract int getMaxPanelIndex();

    protected void cyclePanelIndex() {
        currentPanelIndex++;
        if (currentPanelIndex > getMaxPanelIndex()) {
            currentPanelIndex = 0;
        }
    }

    protected void switchPanels(int newPanelIndex) {
        if (switcherPanel == null && shouldMakeSwitcher()) {
            makeSwitcher(newPanelIndex);
        }

        myPanel.getPosition().setSize(getSwitcherPanelWidth(newPanelIndex), getSwitcherPanelHeight(newPanelIndex));
        myTooltip.getPosition().inTL(0,0);

        if (currentPanel != null) {
            //must remove whole ass panel here because alex
            myPanel.removeComponent(switcherPanel);
            makeSwitcher(newPanelIndex);
        }

        currentPanel = createNewPanel(newPanelIndex, getSwitcherPanelWidth(newPanelIndex), getSwitcherPanelHeight(newPanelIndex));
        switcherPanel.addCustom(currentPanel, 3).getPosition().inTL(3, 3);

        switchedPanels(newPanelIndex, lastPanelIndex);
        lastPanelIndex = newPanelIndex;
    }

    protected void switchedPanels(int newPanelIndex, int lastPanelIndex) {
    }

    public void switchToPanel(int newPanelIndex) {
        currentPanelIndex = newPanelIndex;
        if (currentPanelIndex > getMaxPanelIndex()) {
            currentPanelIndex = 0;
        }

        switchPanels(newPanelIndex);
    }

    protected boolean shouldMakeSwitcher() {
        return true;
    }

    protected boolean canSwitch() {
        return true;
    }

    protected abstract CustomPanelAPI createNewPanel(int newPanelIndex, float panelWidth, float panelHeight);

    protected abstract String getSwitcherLabelText(int newPanelIndex);

    protected abstract float getSwitcherLabelWidth(int newPanelIndex);

    private void makeSwitcher(int newPanelIndex) {
        makeSwitcherPanel(getSwitcherPanelWidth(newPanelIndex), getSwitcherPanelHeight(newPanelIndex));
        makeSwitcherButtons(newPanelIndex, getSwitcherButtonWidth(newPanelIndex), getSwitcherButtonHeight(newPanelIndex));
    }

    protected void makeSwitcherPanel(float panelWidth, float panelHeight) {
        switcherPanel = myPanel.createUIElement(panelWidth, panelHeight, false);
        myPanel.addUIElement(switcherPanel).inTL(0, 0);
    }

    protected void makeSwitcherButtons(int newPanelIndex, float buttonW, float buttonH) {
        FactionAPI playerFaction = Global.getSector().getPlayerFaction();

        switcherButton = switcherPanel.addAreaCheckbox(getSwitcherLabelText(newPanelIndex), "id",
                playerFaction.getBaseUIColor(), playerFaction.getDarkUIColor(), playerFaction.getBrightUIColor(), buttonW, buttonH, 0);
        switcherButton.getPosition().inTL(getSwitcherButtonXOffset(newPanelIndex), switcherPanelHeight / 4 - 6);
    }

    protected void killSwitcherButton() {
        switcherPanel.removeComponent(switcherButton);
        switcherButton = null;
    }

    protected float getSwitcherButtonXOffset(int newPanelIndex) {
        return getSwitcherButtonWidth(newPanelIndex) + 3;
    }

    protected float getSwitcherPanelWidth(int newPanelIndex) {
        return switcherPanelDefaultWidth; // - getSwitcherPanelXOffset(newPanelIndex);
    }

    protected float getSwitcherPanelHeight(int newPanelIndex) {
        return switcherPanelHeight;
    }

    protected float getSwitcherButtonWidth(int newPanelIndex) {
        return SWITCHER_BUTTON_WIDTH;
    }

    protected float getSwitcherButtonHeight(int newPanelIndex) {
        return switcherButtonHeight;
    }

    protected float getSwitcherPanelUpperOffset(int newPanelInex) {
        return getSwitcherButtonXOffset(newPanelInex) + getSwitcherButtonWidth(newPanelInex) + 3;
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
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
