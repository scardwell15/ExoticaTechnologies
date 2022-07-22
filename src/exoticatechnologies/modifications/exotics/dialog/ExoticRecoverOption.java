package exoticatechnologies.modifications.exotics.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.dialog.DialogOption;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ExoticRecoverOption extends DialogOption {
    @Getter
    private final Exotic exotic;

    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        return StringUtils.getString("ExoticsDialog", "RecoverExoticStoryOption");
    }

    @Override
    public void addToOptions(OptionPanelAPI options, ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods, String tooltip, int hotkey) {
        super.addToOptions(options, plugin, fm, mods, tooltip, hotkey);
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        MarketAPI market = plugin.getMarket();
        FleetMemberAPI fm = plugin.getShip();
        ShipModifications mods = plugin.getExtraSystems();
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();

        CargoStackAPI stack = Utilities.getExoticChip(fleet.getCargo(), exotic.getKey());
        if (stack != null) {
            stack.add(1);
        } else {
            fleet.getCargo().addSpecial(exotic.getNewSpecialItemData(), 1);
        }

        if (exotic.getKey().equals("AlphaSubcore")) {
            if (fm.getVariant() != null) {
                fm.getVariant().removeMod("AlphaSubcoreHM");
            }
        }

        mods.removeExotic(exotic);
        mods.save(fm);
        ExoticaTechHM.addToFleetMember(fm);

        textPanel.addParagraph(StringUtils.getString("ExoticsDialog", "ExoticRecoveredSuccessfully"));
    }
}
