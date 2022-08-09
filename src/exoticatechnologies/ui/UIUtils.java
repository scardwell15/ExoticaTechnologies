package exoticatechnologies.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.bandwidth.Bandwidth;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;

import java.util.Map;

public class UIUtils {
    public static void addResourceMap(TooltipMakerAPI resourceTooltip, Map<String, Float> resourceMap) {
        for (Map.Entry<String, Float> resource : resourceMap.entrySet()) {
            String id = resource.getKey();

            if (id.equals(Bandwidth.BANDWIDTH_RESOURCE)) continue;

            float cost = resource.getValue();

            //credits
            if (Utilities.isResourceString(id)) {
                String quantityText = "0";
                if (cost != 0) {
                    String translationKey = "SpecialItemTextWithCost";
                    if (cost < 0) {
                        translationKey = "SpecialItemTextWithPay";
                    }

                    StringUtils.getTranslation("CommonOptions", translationKey)
                            .format("name", id.substring(1))
                            .format("amount", quantityText)
                            .format("cost", StringUtils.formatCost(cost))
                            .addToTooltip(resourceTooltip);
                } else {
                    StringUtils.getTranslation("CommonOptions", "ResourceText")
                            .format("name", id.substring(1))
                            .format("amount", quantityText)
                            .addToTooltip(resourceTooltip);
                }
            } else if (id.equals(Commodities.CREDITS)) {
                if (cost > 0) {
                    StringUtils.getTranslation("CommonOptions", "CreditsCost")
                            .format("credits", StringUtils.formatCost(cost))
                            .addToTooltip(resourceTooltip);
                } else if (cost < 0) {
                    StringUtils.getTranslation("CommonOptions", "CreditsPay")
                            .format("credits", StringUtils.formatCost(cost))
                            .addToTooltip(resourceTooltip);
                }
            } else if (id.equals(Utilities.STORY_POINTS)) {
                if (cost > 0) {
                    StringUtils.getTranslation("CommonOptions", "StoryPointCost")
                            .format("storyPoints", StringUtils.formatCost(cost))
                            .addToTooltip(resourceTooltip);
                }
            } else {
                //special items (chips and others)
                if (Utilities.isSpecialItemId(id)) {
                    String specialId = Utilities.getSpecialItemId(id);
                    String specialParams = Utilities.getSpecialItemParams(id);

                    CargoStackAPI stack = Utilities.getSpecialStack(Global.getSector().getPlayerFleet().getCargo(), specialId, specialParams);
                    String name;
                    float quantity = 0;
                    if (stack != null) {
                        name = stack.getDisplayName();
                        quantity = stack.getSize();
                    } else {
                        CargoAPI cargo = Global.getFactory().createCargo(true);
                        SpecialItemData fakeData = new SpecialItemData(specialId, specialParams);
                        CargoStackAPI fakeStack = Global.getFactory().createCargoStack(CargoAPI.CargoItemType.SPECIAL, fakeData, cargo);
                        name = fakeStack.getDisplayName();
                    }

                    if (cost != 0) {
                        String translationKey = "SpecialItemTextWithCost";
                        if (cost < 0) {
                            translationKey = "SpecialItemTextWithPay";
                        }

                        StringUtils.getTranslation("CommonOptions", translationKey)
                                .format("name", name)
                                .format("amount", Misc.getWithDGS(quantity))
                                .format("cost", StringUtils.formatCost(cost))
                                .addToTooltip(resourceTooltip);
                    } else {
                        String quantityText = "-";
                        if (quantity > 0) {
                            quantityText = Misc.getWithDGS(quantity);
                        }
                        StringUtils.getTranslation("CommonOptions", "ResourceText")
                                .format("name", name)
                                .format("amount", quantityText)
                                .addToTooltip(resourceTooltip);
                    }
                } else {
                    //commodities
                    String name = Utilities.getItemName(id);
                    float quantity = Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(id);

                    if (cost != 0) {
                        StringUtils.getTranslation("CommonOptions", "ResourceTextWithCost")
                                .format("name", name)
                                .format("amount", Misc.getWithDGS(quantity))
                                .format("cost", StringUtils.formatCost(cost))
                                .addToTooltip(resourceTooltip);
                    }
                }
            }
        }
    }
}
