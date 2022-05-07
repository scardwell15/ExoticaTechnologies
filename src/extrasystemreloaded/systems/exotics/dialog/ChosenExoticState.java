package extrasystemreloaded.systems.exotics.dialog;

import com.fs.starfarer.api.campaign.BaseStoryPointActionDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.dialog.DialogOption;
import extrasystemreloaded.dialog.DialogState;
import extrasystemreloaded.systems.exotics.Exotic;
import extrasystemreloaded.systems.exotics.ExoticsHandler;
import extrasystemreloaded.util.ExtraSystems;
import lombok.Getter;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Map;

public class ChosenExoticState extends DialogState {
    @Getter
    private final Exotic exotic;
    private final ExoticInstallOption installOption;
    private final ExoticRecoverOption recoverOption;
    private final ExoticRemoveOption removeOption;

    public ChosenExoticState(Exotic exotic) {
        this.exotic = exotic;
        installOption = new ExoticInstallOption(exotic);
        recoverOption = new ExoticRecoverOption(exotic);
        removeOption = new ExoticRemoveOption(exotic);
    }

    @Override
    public String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        return exotic.getName();
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin) {
        FleetMemberAPI fm = plugin.getShip();
        ExtraSystems es = ExtraSystems.getForFleetMember(fm);
        MarketAPI market = plugin.getMarket();

        TextPanelAPI textPanel = dialog.getTextPanel();
        OptionPanelAPI options = dialog.getOptionPanel();

        dialog.getVisualPanel().showFleetMemberInfo(fm);

        textPanel.addParagraph(exotic.getName(), new Color(150, 220, 255));
        textPanel.addParagraph(exotic.getDescription());

        addExoticOptions(dialog, plugin, fm, es);

        ExoticsHandler.EXOTICS_PICKER_DIALOG.addToOptions(options, plugin, fm, es, Keyboard.KEY_ESCAPE);
    }

    @Override
    public boolean consumesOptionPickedEvent(Object option) {
        return installOption.equals(option) || removeOption.equals(option);
    }

    @Override
    public void optionPicked(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin, Object option) {
        ((DialogOption) option).execute(dialog, plugin);

        plugin.redrawResourcesPanel();

        OptionPanelAPI options = dialog.getOptionPanel();
        TextPanelAPI textPanel = dialog.getTextPanel();

        FleetMemberAPI fm = plugin.getShip();
        MarketAPI market = plugin.getMarket();
        ExtraSystems es = ExtraSystems.getForFleetMember(fm);

        addExoticOptions(dialog, plugin, fm, es);
    }

    private void addExoticOptions(InteractionDialogAPI dialog, final ESInteractionDialogPlugin plugin, final FleetMemberAPI fm, final ExtraSystems es) {
        OptionPanelAPI options = dialog.getOptionPanel();
        boolean hasExotic = es.hasExotic(exotic);

        this.installOption.addToOptions(options, plugin, fm, es);
        this.recoverOption.addToOptions(options, plugin, fm, es);
        this.removeOption.addToOptions(options, plugin, fm, es);

        final SetStoryOption.StoryOptionParams params = new SetStoryOption.StoryOptionParams(this, 1, "esr_recoverExotic", Sounds.STORY_POINT_SPEND,
                "Recovered an exotic mod from a ship.");
        SetStoryOption.set(dialog, params, new BaseStoryPointActionDelegate() {
            @Override
            public String getLogText() {
                return params.logText;
            }

            @Override
            public void confirm() {
                plugin.optionSelected(ChosenExoticState.this.getOptionText(plugin, fm, es), ChosenExoticState.this);
            }
        });

        if (hasExotic) {
            options.setEnabled(installOption, false);
        } else if (!exotic.canApply(fm)) {
            options.setEnabled(installOption, false);
            options.setEnabled(recoverOption, false);
            options.setEnabled(removeOption, false);
        } else {
            options.setEnabled(recoverOption, false);
            options.setEnabled(removeOption, false);
        }
    }

    @Override
    public void addToOptions(OptionPanelAPI options, ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es, String tooltip, int hotkey) {
        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();
        boolean hasExotic = es.hasExotic(exotic);

        Color color = Misc.getButtonTextColor();

        if (hasExotic) {
            color = new Color(218, 218, 79);
        } else if (!exotic.canApply(fm)) {
            color = new Color(241, 100, 100);
        }

        options.addOption(getOptionText(plugin, fm, es), this, color, tooltip);

        if(hotkey >= 0) {
            options.setShortcut(this, hotkey, false, false, false, true);
        }
    }

    @Override
    public void modifyResourcesPanel(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin, Map<String, Float> resourceCosts) {
        FleetMemberAPI fm = plugin.getShip();
        this.getExotic().modifyResourcesPanel(dialog, plugin, resourceCosts, fm);
    }
}
