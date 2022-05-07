package extrasystemreloaded.systems.upgrades.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.dialog.DialogOption;
import extrasystemreloaded.dialog.PaginationOption;
import extrasystemreloaded.dialog.modifications.SystemState;
import extrasystemreloaded.systems.bandwidth.Bandwidth;
import extrasystemreloaded.systems.upgrades.Upgrade;
import extrasystemreloaded.systems.upgrades.UpgradesHandler;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin.PAGINATION_KEY;

public class UpgradesPickerState extends SystemState {
    private static final int MIN_CHOICES_FOR_PAGINATION = 8;
    private static final int CHOICES_PER_PAGE = 6;
    private static final DialogOption OPTION_NEXTPAGE = new PaginationOption(true);
    private static final DialogOption OPTION_PREVPAGE = new PaginationOption(false);

    @Override
    public String getOptionText(ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es) {
        return StringUtils.getString("UpgradesDialog", "OpenUpgradeOptions");
    }

    @Override
    public void execute(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin) {
        //populate upgrade options
        OptionPanelAPI options = dialog.getOptionPanel();
        FleetMemberAPI fm = plugin.getShip();
        ExtraSystems es = ExtraSystems.getForFleetMember(fm);
        MarketAPI market = plugin.getMarket();

        List<Upgrade> sortedUpgradesList = getSortedUpgradeList(fm, es, market);

        int startIndex = 0;
        int endIndex = sortedUpgradesList.size();
        int pageIndex = 0;

        if(plugin.getMemoryMap().get(MemKeys.LOCAL).contains(PAGINATION_KEY)) {
            pageIndex = plugin.getMemoryMap().get(MemKeys.LOCAL).getInt(PAGINATION_KEY);
        }

        if(endIndex >= MIN_CHOICES_FOR_PAGINATION) {
            pageIndex = MathUtils.clamp(pageIndex, 0, sortedUpgradesList.size() / CHOICES_PER_PAGE);
            startIndex = CHOICES_PER_PAGE * pageIndex;
            endIndex = Math.min(startIndex + CHOICES_PER_PAGE, sortedUpgradesList.size());
        }

        addUpgradesToOptions(options, plugin, fm, es, market, sortedUpgradesList, startIndex, endIndex);

        if(endIndex >= MIN_CHOICES_FOR_PAGINATION) {
            OPTION_PREVPAGE.addToOptions(options, plugin, fm, es);
            OPTION_NEXTPAGE.addToOptions(options, plugin, fm, es);
            if (startIndex == 0) {
                options.setEnabled(OPTION_PREVPAGE, false);
            }
            if (endIndex + CHOICES_PER_PAGE >= sortedUpgradesList.size()) {
                options.setEnabled(OPTION_NEXTPAGE, false);
            }
        }

        ESInteractionDialogPlugin.SYSTEM_PICKER.addToOptions(options, plugin, fm, es, Keyboard.KEY_ESCAPE);
    }

    private void addUpgradesToOptions(OptionPanelAPI options, ESInteractionDialogPlugin plugin, FleetMemberAPI fm, ExtraSystems es, MarketAPI market, List<Upgrade> upgrades, int startIndex, int endIndex) {
        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();

        for (int i = startIndex; i < endIndex; i++) {
            Upgrade upgrade = upgrades.get(i);
            int level = es.getUpgrade(upgrade.getKey());
            int max = upgrade.getMaxLevel(hullSize);

            Color color = Misc.getButtonTextColor();

            if (es.isMaxLevel(fm, upgrade)) {
                color = new Color(196, 189, 56);
            } else if (!UpgradesHandler.canUseUpgradeMethods(fm, es, hullSize, upgrade, Global.getSector().getPlayerFleet(), market)) {
                color = new Color(241, 100, 100);
            }

            ChosenUpgradeState upgradeState = new ChosenUpgradeState(upgrade);
            options.addOption(
                    upgradeState.getOptionText(plugin, fm, es),
                    upgradeState,
                    color,
                    upgrade.getDescription());
        }
    }

    //sorted upgrade list
    private List<Upgrade> getSortedUpgradeList(FleetMemberAPI fm, ExtraSystems es, MarketAPI market) {//sort upgrade list so that upgrades that we can't upgrade are put in last.
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
    public void modifyResourcesPanel(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin, Map<String, Float> resourceCosts) {
        if(plugin.getHoveredOption() instanceof ChosenUpgradeState) {
            Upgrade upgrade = ((ChosenUpgradeState) plugin.getHoveredOption()).getUpgrade();
            resourceCosts.put(Bandwidth.BANDWIDTH_RESOURCE, upgrade.getBandwidthUsage());
        }
    }
}
