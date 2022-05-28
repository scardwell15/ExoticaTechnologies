package exoticatechnologies.modifications.exotics.dialog;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.dialog.DialogOption;
import exoticatechnologies.dialog.DialogState;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticsHandler;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChosenExoticState extends DialogState {
    private Set<DialogOption> consumedOptions = new HashSet<>();
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

        consumedOptions.add(installOption);
        consumedOptions.add(recoverOption);
        consumedOptions.add(removeOption);
    }

    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        return exotic.getName();
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {
        FleetMemberAPI fm = plugin.getShip();
        ShipModifications mods = ShipModFactory.getForFleetMember(fm);
        MarketAPI market = plugin.getMarket();

        TextPanelAPI textPanel = dialog.getTextPanel();
        OptionPanelAPI options = dialog.getOptionPanel();

        dialog.getVisualPanel().showFleetMemberInfo(fm);

        textPanel.addParagraph(exotic.getName(), new Color(150, 220, 255));
        textPanel.addParagraph(exotic.getDescription());

        addExoticOptions(dialog, plugin, fm, mods, market);

        ExoticsHandler.EXOTICS_PICKER_DIALOG.addToOptions(options, plugin, fm, mods, Keyboard.KEY_ESCAPE);
    }

    @Override
    public boolean consumesOptionPickedEvent(Object option) {
        return consumedOptions.contains(option);
    }

    @Override
    public void optionPicked(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, Object option) {
        ((DialogOption) option).execute(dialog, plugin);

        plugin.redrawResourcesPanel();

        OptionPanelAPI options = dialog.getOptionPanel();
        TextPanelAPI textPanel = dialog.getTextPanel();

        FleetMemberAPI fm = plugin.getShip();
        MarketAPI market = plugin.getMarket();
        ShipModifications mods = ShipModFactory.getForFleetMember(fm);

        addExoticOptions(dialog, plugin, fm, mods, market);
    }

    private void addExoticOptions(InteractionDialogAPI dialog, final ETInteractionDialogPlugin plugin, final FleetMemberAPI fm, final ShipModifications mods, MarketAPI market) {
        OptionPanelAPI options = dialog.getOptionPanel();
        boolean hasExotic = mods.hasExotic(exotic);

        this.installOption.addToOptions(options, plugin, fm, mods);
        this.recoverOption.addToOptions(options, plugin, fm, mods);
        this.removeOption.addToOptions(options, plugin, fm, mods);

        final SetStoryOption.StoryOptionParams params = new SetStoryOption.StoryOptionParams(this.recoverOption, 1, "et_recoverExotic", Sounds.STORY_POINT_SPEND,
                "Recovered an exotic mod from a ship.");
        SetStoryOption.set(dialog, params, new BaseStoryPointActionDelegate() {
            @Override
            public String getLogText() {
                return params.logText;
            }

            @Override
            public void confirm() {
                plugin.optionSelected(ChosenExoticState.this.getOptionText(plugin, fm, mods), ChosenExoticState.this);
            }
        });

        boolean canApply = exotic.canApply(fm);
        if (canApply) {
            canApply = Utilities.hasExoticChip(fm.getFleetData().getFleet().getCargo(), exotic.getKey())
                            || exotic.canAfford(fm.getFleetData().getFleet(), market);
        }

        if (hasExotic) {
            options.setEnabled(installOption, false);
        } else if (canApply) {
            options.setEnabled(recoverOption, false);
            options.setEnabled(removeOption, false);
        } else {
            options.setEnabled(installOption, false);
            options.setEnabled(recoverOption, false);
            options.setEnabled(removeOption, false);
        }
    }

    @Override
    public void addToOptions(OptionPanelAPI options, ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods, String tooltip, int hotkey) {
        MarketAPI market = plugin.getMarket();
        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();
        boolean hasExotic = mods.hasExotic(exotic);

        Color color = Misc.getButtonTextColor();

        if (hasExotic) {
            color = new Color(218, 218, 79);
        } else {
            boolean canApply = exotic.canApply(fm);
            if (canApply) {
                canApply = Utilities.hasExoticChip(fm.getFleetData().getFleet().getCargo(), exotic.getKey())
                        || exotic.canAfford(fm.getFleetData().getFleet(), market);
            }

            if (!canApply) {
                color = new Color(241, 100, 100);
            }
        }

        options.addOption(getOptionText(plugin, fm, mods), this, color, tooltip);

        if(hotkey >= 0) {
            options.setShortcut(this, hotkey, false, false, false, true);
        }
    }

    @Override
    public void modifyResourcesPanel(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts) {
        FleetMemberAPI fm = plugin.getShip();
        this.getExotic().modifyResourcesPanel(dialog, plugin, resourceCosts, fm);
    }
}
