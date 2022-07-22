package exoticatechnologies.modifications.exotics.dialog;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.dialog.DialogOption;
import exoticatechnologies.dialog.PaginationOption;
import exoticatechnologies.dialog.modifications.SystemState;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticsHandler;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

import static exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin.PAGINATION_KEY;

public class ExoticsPickerState extends SystemState {
    private static final int MIN_CHOICES_FOR_PAGINATION = 8;
    private static final int CHOICES_PER_PAGE = 6;
    private static final DialogOption OPTION_NEXTPAGE = new PaginationOption(true);
    private static final DialogOption OPTION_PREVPAGE = new PaginationOption(false);

    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        return StringUtils.getString("ExoticsDialog", "OpenExoticOptions");
    }

    @Override
    public boolean consumesOptionPickedEvent(Object option) {
        return OPTION_PREVPAGE.equals(option) || OPTION_NEXTPAGE.equals(option);
    }

    @Override
    public void optionPicked(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, Object option) {
        int pageIndex = 1;
        if(plugin.getMemoryMap().get(MemKeys.LOCAL).contains(PAGINATION_KEY)) {
            pageIndex = plugin.getMemoryMap().get(MemKeys.LOCAL).getInt(PAGINATION_KEY);
        }

        if (OPTION_PREVPAGE.equals(option)) {
            pageIndex--;
        } else if (OPTION_NEXTPAGE.equals(option)) {
            pageIndex++;
        }

        plugin.getMemoryMap().get(MemKeys.LOCAL).set(PAGINATION_KEY, pageIndex);

        execute(dialog, plugin);
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin) {
        //populate upgrade options
        OptionPanelAPI options = dialog.getOptionPanel();
        FleetMemberAPI fm = plugin.getShip();
        ShipModifications mods = ShipModFactory.getForFleetMember(fm);
        MarketAPI market = plugin.getMarket();

        options.clearOptions();

        List<Exotic> sortedExoticsList = getSortedExoticList(fm, mods, market);
        boolean paginated = sortedExoticsList.size() >= MIN_CHOICES_FOR_PAGINATION;
        int pages = 1;
        if(paginated) {
            pages = (int) Math.ceil(((float) sortedExoticsList.size()) / ((float) CHOICES_PER_PAGE));
        }

        int startIndex = 0;
        int endIndex = sortedExoticsList.size();
        int pageIndex = 0;

        if(paginated) {
            if(plugin.getMemoryMap().get(MemKeys.LOCAL).contains(PAGINATION_KEY)) {
                pageIndex = plugin.getMemoryMap().get(MemKeys.LOCAL).getInt(PAGINATION_KEY);
            } else {
                plugin.getMemoryMap().get(MemKeys.LOCAL).set(PAGINATION_KEY, 1);
            }
            pageIndex = MathUtils.clamp(pageIndex, 1, pages);
            startIndex = CHOICES_PER_PAGE * (pageIndex - 1);
            endIndex = Math.min(startIndex + CHOICES_PER_PAGE, sortedExoticsList.size());
        }

        addExoticsToOptions(options, plugin, fm, mods, market, sortedExoticsList, startIndex, endIndex);

        if(paginated) {
            OPTION_PREVPAGE.addToOptions(options, plugin, fm, mods);
            OPTION_NEXTPAGE.addToOptions(options, plugin, fm, mods);
            if (startIndex == 0) {
                options.setEnabled(OPTION_PREVPAGE, false);
            }
            if (endIndex >= sortedExoticsList.size()) {
                options.setEnabled(OPTION_NEXTPAGE, false);
            }
        }

        ETInteractionDialogPlugin.SYSTEM_PICKER.addToOptions(options, plugin, fm, mods, Keyboard.KEY_ESCAPE);
    }

    private void addExoticsToOptions(OptionPanelAPI options, ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications es, MarketAPI market, List<Exotic> exotics, int startIndex, int endIndex) {
        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();

        for (int i = startIndex; i < endIndex; i++) {
            Exotic exotic = exotics.get(i);
            ChosenExoticState upgradeState = new ChosenExoticState(exotic);
            upgradeState.addToOptions(options, plugin, fm, es, null, -1);
        }
    }

    private List<Exotic> getSortedExoticList(FleetMemberAPI fm, ShipModifications buff, MarketAPI market) {
        //sort exotic list so that augmnets that we can't install are put in last.
        List<Exotic> sortedExoticList = new ArrayList<>();

        //can afford an upgrade, and actually perform it.
        for(Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if(!exotic.shouldShow(fm, buff, market)) {
                continue;
            }

            if (buff.hasExotic(exotic)) {
                continue;
            }

            boolean canApply = exotic.canApply(fm);
            if (canApply) {
                canApply = Utilities.hasExoticChip(fm.getFleetData().getFleet().getCargo(), exotic.getKey())
                        || exotic.canAfford(fm.getFleetData().getFleet(), market);
            }

            if(canApply) {
                sortedExoticList.add(exotic);
            }
        }

        //can not afford an upgrade
        for(Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if(!exotic.shouldShow(fm, buff, market)) {
                continue;
            }

            if (buff.hasExotic(exotic)) {
                continue;
            }

            if(!sortedExoticList.contains(exotic)) {
                sortedExoticList.add(exotic);
            }
        }

        //cannot do an upgrade
        for(Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if(!exotic.shouldShow(fm, buff, market)) {
                continue;
            }

            if(!sortedExoticList.contains(exotic)) {
                sortedExoticList.add(exotic);
            }
        }

        return sortedExoticList;
    }
}
