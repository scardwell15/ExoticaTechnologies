package exoticatechnologies.modifications.upgrades.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.dialog.DialogOption;
import exoticatechnologies.dialog.PaginationOption;
import exoticatechnologies.dialog.modifications.SystemState;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.modifications.exotics.dialog.ChosenExoticState;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin.PAGINATION_KEY;

public class UpgradesPickerState extends SystemState {
    private static final int MIN_CHOICES_FOR_PAGINATION = 8;
    private static final int CHOICES_PER_PAGE = 6;
    private static final DialogOption OPTION_NEXTPAGE = new PaginationOption(true);
    private static final DialogOption OPTION_PREVPAGE = new PaginationOption(false);

    @Override
    public String getOptionText(ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications mods) {
        return StringUtils.getString("UpgradesDialog", "OpenUpgradeOptions");
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

        List<Upgrade> sortedUpgradesList = getSortedUpgradeList(fm, mods, market);
        boolean paginated = sortedUpgradesList.size() >= MIN_CHOICES_FOR_PAGINATION;
        int pages = 1;
        if(paginated) {
            pages = (int) Math.ceil(((float) sortedUpgradesList.size()) / ((float) CHOICES_PER_PAGE));
        }

        int startIndex = 0;
        int endIndex = sortedUpgradesList.size();
        int pageIndex = 0;

        if(paginated) {
            if(plugin.getMemoryMap().get(MemKeys.LOCAL).contains(PAGINATION_KEY)) {
                pageIndex = plugin.getMemoryMap().get(MemKeys.LOCAL).getInt(PAGINATION_KEY);
            } else {
                plugin.getMemoryMap().get(MemKeys.LOCAL).set(PAGINATION_KEY, 1);
            }
            pageIndex = MathUtils.clamp(pageIndex, 1, pages);
            startIndex = CHOICES_PER_PAGE * (pageIndex - 1);
            endIndex = Math.min(startIndex + CHOICES_PER_PAGE, sortedUpgradesList.size());
        }

        addUpgradesToOptions(options, plugin, fm, mods, market, sortedUpgradesList, startIndex, endIndex);

        if(paginated) {
            OPTION_PREVPAGE.addToOptions(options, plugin, fm, mods);
            OPTION_NEXTPAGE.addToOptions(options, plugin, fm, mods);
            if (startIndex == 0) {
                options.setEnabled(OPTION_PREVPAGE, false);
            }
            if (endIndex >= sortedUpgradesList.size()) {
                options.setEnabled(OPTION_NEXTPAGE, false);
            }
        }

        ETInteractionDialogPlugin.SYSTEM_PICKER.addToOptions(options, plugin, fm, mods, Keyboard.KEY_ESCAPE);
    }

    private void addUpgradesToOptions(OptionPanelAPI options, ETInteractionDialogPlugin plugin, FleetMemberAPI fm, ShipModifications es, MarketAPI market, List<Upgrade> upgrades, int startIndex, int endIndex) {
        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();

        for (int i = startIndex; i < endIndex; i++) {
            Upgrade upgrade = upgrades.get(i);
            ChosenUpgradeState upgradeState = new ChosenUpgradeState(upgrade);
            upgradeState.addToOptions(options, plugin, fm, es, null, -1);
        }
    }

    //sorted upgrade list
    private List<Upgrade> getSortedUpgradeList(FleetMemberAPI fm, ShipModifications es, MarketAPI market) {//sort upgrade list so that upgrades that we can't upgrade are put in last.
        List<Upgrade> sortedUpgradeList = new ArrayList<>();

        //can afford an upgrade, and actually perform it.
        for(Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            if(!upgrade.shouldShow(fm, es, market)) {
                continue;
            }

            boolean canUpgrade = (es.hasUpgrade(upgrade) || upgrade.canApply(fm))
                    && !es.isMaxLevel(fm, upgrade);
            if (canUpgrade) {
                canUpgrade = UpgradesHandler.canUseUpgradeMethods(fm, es, fm.getHullSpec().getHullSize(), upgrade, fm.getFleetData().getFleet(), market);
            }

            if(canUpgrade) {
                sortedUpgradeList.add(upgrade);
            }
        }

        //can not afford an upgrade
        for(Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            if(!upgrade.shouldShow(fm, es, market)) {
                continue;
            }

            if(!sortedUpgradeList.contains(upgrade)) {
                boolean canUpgrade = (es.hasUpgrade(upgrade) || upgrade.canApply(fm))
                        && !es.isMaxLevel(fm, upgrade);
                if (canUpgrade) {
                    sortedUpgradeList.add(upgrade);
                }
            }
        }

        //cannot do an upgrade
        for(Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            if(!upgrade.shouldShow(fm, es, market)) {
                continue;
            }

            if(!sortedUpgradeList.contains(upgrade) && (es.hasUpgrade(upgrade) || upgrade.canApply(fm))) {
                sortedUpgradeList.add(upgrade);
            }
        }

        return sortedUpgradeList;
    }

    @Override
    public void modifyResourcesPanel(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts) {
        if(plugin.getHoveredOption() instanceof ChosenUpgradeState) {
            Upgrade upgrade = ((ChosenUpgradeState) plugin.getHoveredOption()).getUpgrade();
            resourceCosts.put(Bandwidth.BANDWIDTH_RESOURCE, upgrade.getBandwidthUsage());
        }
    }
}
