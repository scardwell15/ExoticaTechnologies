package extrasystemreloaded.systems.exotics.dialog;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import extrasystemreloaded.dialog.DialogOption;
import extrasystemreloaded.dialog.PaginationOption;
import extrasystemreloaded.dialog.modifications.SystemState;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.systems.exotics.Exotic;
import extrasystemreloaded.systems.exotics.ExoticsHandler;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin.PAGINATION_KEY;

public class ExoticsPickerState extends SystemState {
    private static final int MIN_CHOICES_FOR_PAGINATION = 8;
    private static final int CHOICES_PER_PAGE = 6;
    private static final DialogOption OPTION_NEXTPAGE = new PaginationOption(true);
    private static final DialogOption OPTION_PREVPAGE = new PaginationOption(false);

    @Override
    public String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        return StringUtils.getString("ExoticsDialog", "OpenExoticOptions");
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin) {
        //populate upgrade options
        OptionPanelAPI options = dialog.getOptionPanel();
        FleetMemberAPI fm = plugin.getShip();
        ExtraSystems es = ExtraSystems.getForFleetMember(fm);
        MarketAPI market = plugin.getMarket();

        options.clearOptions();

        List<Exotic> sortedExoticsList = getSortedExoticList(fm, es, market);

        int startIndex = 0;
        int endIndex = sortedExoticsList.size() - 1;
        int pageIndex = 0;

        if(plugin.getMemoryMap().get(MemKeys.LOCAL).contains(PAGINATION_KEY)) {
            pageIndex = plugin.getMemoryMap().get(MemKeys.LOCAL).getInt(PAGINATION_KEY);
        }

        if(endIndex >= MIN_CHOICES_FOR_PAGINATION) {
            pageIndex = MathUtils.clamp(pageIndex, 0, sortedExoticsList.size() / CHOICES_PER_PAGE);
            startIndex = CHOICES_PER_PAGE * pageIndex;
            endIndex = Math.min(startIndex + CHOICES_PER_PAGE, sortedExoticsList.size());
        }

        addExoticsToOptions(options, plugin, fm, es, market, sortedExoticsList, startIndex, endIndex);

        if(endIndex >= MIN_CHOICES_FOR_PAGINATION) {
            OPTION_PREVPAGE.addToOptions(options, plugin, fm, es);
            OPTION_NEXTPAGE.addToOptions(options, plugin, fm, es);
            if (startIndex == 0) {
                options.setEnabled(OPTION_PREVPAGE, false);
            }
            if (endIndex + CHOICES_PER_PAGE >= sortedExoticsList.size()) {
                options.setEnabled(OPTION_NEXTPAGE, false);
            }
        }

        ESInteractionDialogPlugin.SYSTEM_PICKER.addToOptions(options, plugin, fm, es, Keyboard.KEY_ESCAPE);
    }

    private void addExoticsToOptions(OptionPanelAPI options, ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es, MarketAPI market, List<Exotic> exotics, int startIndex, int endIndex) {
        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();

        for (int i = startIndex; i < endIndex; i++) {
            Exotic exotic = exotics.get(i);
            boolean hasExotic = es.hasExotic(exotic);

            Color color = Misc.getButtonTextColor();

            if (hasExotic) {
                color = new Color(196, 189, 56);
            } else if (!exotic.canApply(fm)) {
                color = new Color(173, 94, 94);
            }

            ChosenExoticState upgradeState = new ChosenExoticState(exotic);
            options.addOption(
                    upgradeState.getOptionText(plugin, fm, es),
                    upgradeState,
                    color,
                    exotic.getDescription());
        }
    }

    private List<Exotic> getSortedExoticList(FleetMemberAPI fm, ExtraSystems buff, MarketAPI market) {
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

            if(exotic.canApply(fm)) {
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
