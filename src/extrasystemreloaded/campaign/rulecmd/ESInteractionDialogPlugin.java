package extrasystemreloaded.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowDefaultVisual;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicTxt;
import extrasystemreloaded.dialog.*;
import extrasystemreloaded.dialog.modifications.SystemPickerState;
import extrasystemreloaded.dialog.shippicker.ShipPickerOption;
import extrasystemreloaded.systems.bandwidth.Bandwidth;
import extrasystemreloaded.systems.bandwidth.BandwidthUtil;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import extrasystemreloaded.util.Utilities;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j
public class ESInteractionDialogPlugin extends BaseCommandPlugin implements InteractionDialogPlugin {
    public static ESMainMenu MAIN_MENU = new ESMainMenu();
    public static ReturnToMarketOption RETURN_TO_MARKET = new ReturnToMarketOption();
    public static SystemPickerState SYSTEM_PICKER = new SystemPickerState();
    public static ShipPickerOption SHIP_PICKER = new ShipPickerOption(SYSTEM_PICKER);

    public static final String PAGINATION_KEY = "$ESRPage";
    public static final String SHIP_MEMKEY = "$ESRshipId";

    private InteractionDialogAPI dialog;
    private InteractionDialogPlugin oldPlugin;
    private Map<String, MemoryAPI> memoryMap;

    private ResourceDisplayPlugin resourceDisplayPlugin = new ResourceDisplayPlugin();
    private float lastCredits = 0xffffffff;

    private static String CREDITS_FONT = Fonts.INSIGNIA_VERY_LARGE;
    private static String CREDITS_BOLD_FONT = Fonts.INSIGNIA_VERY_LARGE;
    private static String RESOURCE_FONT = Fonts.INSIGNIA_LARGE;
    private static String RESOURCE_BOLD_FONT = Fonts.INSIGNIA_LARGE;


    @Getter
    private List<FleetMemberAPI> fleetMembers;
    @Getter
    private DialogState currentState;
    @Getter
    private Object lastOption;
    @Getter
    private MarketAPI market;
    @Getter
    private DialogOption hoveredOption;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        this.dialog = dialog;
        this.oldPlugin = dialog.getPlugin();
        this.memoryMap = oldPlugin.getMemoryMap();
        this.market = dialog.getInteractionTarget().getMarket();
        this.fleetMembers = Global.getSector().getPlayerFleet().getFleetData().getMembersInPriorityOrder();

        Iterator<FleetMemberAPI> iterator = fleetMembers.iterator();
        while (iterator.hasNext()) {
            FleetMemberAPI fleetMemberAPI = iterator.next();
            if (fleetMemberAPI.isFighterWing()) {
                iterator.remove();
            }
        }

        dialog.setPlugin(this);
        this.init(dialog);

        return true;
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        dialog.getOptionPanel().clearOptions();
        this.currentState = MAIN_MENU;
        SHIP_PICKER.execute(dialog, this);
    }


    public void redrawResourcesPanel() {
        dialog.getVisualPanel().setVisualFade(0f, 0f);

        CustomPanelAPI resourcesPanel = dialog.getVisualPanel().showCustomPanel(350, 200, resourceDisplayPlugin);
        TooltipMakerAPI resourcesTooltip = resourcesPanel.createUIElement(350, 200, false);
        resourcesPanel.getPosition().inTR(Global.getSettings().getScreenWidth() * 0.2f, Global.getSettings().getScreenHeight() * 0.15f);
        resourcesPanel.addUIElement(resourcesTooltip);

        FleetMemberAPI fm = this.getShip();
        if (fm != null) {
            //get resources
            Map<String, Float> resourceCosts = new LinkedHashMap<>();

            resourceCosts.put(Commodities.CREDITS, 0f);
            for (String resource : Utilities.RESOURCES_LIST) {
                resourceCosts.put(resource, 0f);
            }

            currentState.modifyResourcesPanel(dialog, this, resourceCosts);

            //draw ship
            SpriteAPI sprite = Global.getSettings().getSprite(fm.getHullSpec().getSpriteName());
            float shipSize = Math.min(sprite.getWidth(), sprite.getHeight());
            shipSize = MathUtils.clamp(shipSize, Global.getSettings().getScreenHeight() * 0.08f, Global.getSettings().getScreenHeight() * 0.16f);

            resourcesTooltip.addSectionHeading(" " + fm.getShipName(), Alignment.LMID, 2f);
            resourcesTooltip.addImage(fm.getHullSpec().getSpriteName(), shipSize, 6f);

            //show bandwidth and usage, and show used bandwidth from upgrade
            float bandwidth = getExtraSystems().getBandwidth(fm);

            StringUtils.getTranslation("CommonOptions", "BandwidthForShip")
                    .format("shipBandwidth", BandwidthUtil.getFormattedBandwidthWithName(bandwidth))
                    .addToTooltip(resourcesTooltip, new Color[]{Bandwidth.getBandwidthColor(bandwidth)});

            float used = getExtraSystems().getUsedBandwidth();
            if (resourceCosts.containsKey(Bandwidth.BANDWIDTH_RESOURCE)) {
                float upgradeBandwidth = resourceCosts.get(Bandwidth.BANDWIDTH_RESOURCE);
                StringUtils.getTranslation("CommonOptions", "BandwidthUsedWithUpgrade")
                        .format("usedBandwidth", BandwidthUtil.getFormattedBandwidth(used))
                        .format("upgradeBandwidth", BandwidthUtil.getFormattedBandwidth(upgradeBandwidth))
                        .addToTooltip(resourcesTooltip);
            } else {
                StringUtils.getTranslation("CommonOptions", "BandwidthUsed")
                        .format("usedBandwidth", BandwidthUtil.getFormattedBandwidth(used))
                        .addToTooltip(resourcesTooltip);
            }

            //resources
            resourcesTooltip.addSpacer(Global.getSettings().getScreenWidth() * 0.02f);
            resourcesTooltip.addSectionHeading(" " + StringUtils.getString("CommonOptions","ResourcesHeader"), Alignment.LMID, 2f);

            for (Map.Entry<String, Float> resourceCost : resourceCosts.entrySet()) {
                String id = resourceCost.getKey();

                if(id.equals(Bandwidth.BANDWIDTH_RESOURCE)) continue;

                float cost = resourceCost.getValue();


                if (id.equals(Commodities.CREDITS)) {
                    if (cost == 0) {
                        resourcesTooltip.setParaFont(CREDITS_FONT);
                    } else {
                        resourcesTooltip.setParaFont(CREDITS_BOLD_FONT);
                    }

                    float quantity = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
                    if (cost > 0) {
                        StringUtils.getTranslation("CommonOptions", "CreditsTextWithCost")
                                .format("credits", Misc.getDGSCredits(quantity))
                                .format("cost", Misc.getDGSCredits(cost))
                                .addToTooltip(resourcesTooltip);
                    } else {
                        StringUtils.getTranslation("CommonOptions", "CreditsText")
                                .format("credits", Misc.getDGSCredits(quantity))
                                .addToTooltip(resourcesTooltip);
                    }
                } else {
                    if (cost == 0) {
                        resourcesTooltip.setParaFont(RESOURCE_FONT);
                    } else {
                        resourcesTooltip.setParaFont(RESOURCE_BOLD_FONT);
                    }

                    String name = Utilities.getItemName(id);
                    float quantity = Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(id);
                    if (cost > 0) {
                        StringUtils.getTranslation("CommonOptions", "ResourceTextWithCost")
                                .format("name", name)
                                .format("amount", Misc.getWithDGS(quantity))
                                .format("cost", Misc.getWithDGS(cost))
                                .addToTooltip(resourcesTooltip);
                    } else {
                        String quantityText = "-";
                        if(quantity > 0) {
                            quantityText = Misc.getWithDGS(quantity);
                        }
                        StringUtils.getTranslation("CommonOptions", "ResourceText")
                                .format("name", name)
                                .format("amount", quantityText)
                                .addToTooltip(resourcesTooltip);
                    }
                }
            }
        } else {
            resourcesTooltip.addSpacer(Global.getSettings().getScreenWidth() * 0.15f);
        }
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionData.equals(RETURN_TO_MARKET)) {
            returnToMarket();
            this.lastOption = null;
            return;
        }

        boolean consumed = false;
        if (currentState.consumesOptionPickedEvent(optionData)) {
            if (optionData instanceof DialogOption) {
                if (((DialogOption) optionData).canBeConsumedByState(currentState)) {
                    currentState.optionPicked(dialog, this, optionData);
                    consumed = true;
                }
            } else {
                currentState.optionPicked(dialog, this, optionData);
                consumed = true;
            }
        }

        if (!consumed) {
            if (optionData instanceof DialogOption) {
                DialogOption option = (DialogOption) optionData;

                if (option.requiresFleetMember() && this.getShip() == null) {
                    option = RETURN_TO_MARKET;
                }

                if (optionData instanceof DialogState) {
                    DialogState state = (DialogState) optionData;

                    if (state.clearsOptions()) {
                        this.dialog.getOptionPanel().clearOptions();
                    }

                    if (!this.currentState.equals(state)) {
                        this.currentState.switchedToDifferentState(dialog, this, state);
                        this.currentState = state;
                    }
                }

                option.execute(this.dialog, this);
            } else {
                log.info(String.format("Unknown option [%s] in state [%s]", optionData, currentState));
                optionSelected("The engineering team was confused by your request and restarted the connection.",
                        RETURN_TO_MARKET);
            }
        }

        this.lastOption = optionData;

        redrawResourcesPanel();
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return memoryMap;
    }

    public FleetMemberAPI getShip() {
        return (FleetMemberAPI) this.getMemoryMap().get(MemKeys.LOCAL).get(SHIP_MEMKEY);
    }

    public ExtraSystems getExtraSystems() {
        return ExtraSystems.getForFleetMember(this.getShip());
    }

    private void returnToMarket() {
        dialog.setPlugin(oldPlugin);
        new ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), memoryMap);

        memoryMap.get(MemKeys.LOCAL).set("$option", "ESDialogBack", 0f);
        FireAll.fire(null, dialog, memoryMap, "DialogOptionSelected");
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
        if (optionData instanceof DialogOption) {
            this.hoveredOption = (DialogOption) optionData;
            hoveredOption.hovered(dialog, this);
            redrawResourcesPanel();
        }
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {
    }

    @Override
    public Object getContext() {
        return null;
    }
}
