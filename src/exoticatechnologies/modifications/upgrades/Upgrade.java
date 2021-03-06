package exoticatechnologies.modifications.upgrades;

import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.ETModSettings;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.methods.UpgradeMethod;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Upgrade {
    public static String ITEM = "et_upgrade";
    @Getter @Setter public String key;
    @Getter @Setter protected String name;
    @Getter @Setter protected String description;
    @Getter @Setter protected String tooltip;
    @Getter protected JSONObject upgradeSettings;
    @Getter protected Map<String, Float> resourceRatios = new HashMap<>();

    public static Upgrade get(String upgradeKey) {
        return UpgradesHandler.UPGRADES.get(upgradeKey);
    }


    public abstract float getBandwidthUsage();

    public boolean shouldLoad() {
        return true;
    }

    public boolean shouldShow(FleetMemberAPI fm, ShipModifications es, MarketAPI market) {
        if (es.getUpgrade(this) > 0) {
            return true;
        }

        return shouldShow();
    }

    public boolean shouldShow() {
        return true;
    }

    public boolean canApply(ShipVariantAPI var) {
        return true;
    }

    public boolean canApply(FleetMemberAPI fm) {
        return canApply(fm.getVariant());
    }

    public boolean canUseUpgradeMethod(FleetMemberAPI fm, ShipModifications mods, UpgradeMethod method) {
        return true;
    }

    public Color getColor() {
        return Misc.getBasePlayerColor();
    }

    public final void setConfig(JSONObject upgradeSettings) throws JSONException {
        this.upgradeSettings = upgradeSettings;
        loadConfig();

        resourceRatios.clear();
        JSONObject settingRatios = upgradeSettings.getJSONObject("resourceRatios");
        for (String resource : Utilities.RESOURCES_LIST) {
            float ratio = 0f;
            if(settingRatios.has(resource)) {
                //class cast exception indicates an improperly configured config
                ratio = ((Number) settingRatios.getDouble(resource)).floatValue();
            }
            resourceRatios.put(resource, ratio);
        }
    }

    protected void loadConfig() throws JSONException {};


    public String getBuffId() {
        return "ESR_" + getName();
    }

    private int getMaxLevel() {
        return -1;
    }

    public int getMaxLevel(ShipAPI.HullSize hullSize) {
        return getMaxLevel() != -1 ? getMaxLevel() : ETModSettings.getHullSizeToMaxLevel().get(hullSize);
    }

    public String getIcon() {
        return "graphics/icons/upgrades/" + getKey().toLowerCase() + ".png";
    }

    /**
     * note: overrides should be done to the HullSize method
     * @param fm
     * @return
     */
    public final int getMaxLevel(FleetMemberAPI fm) {
        return getMaxLevel(fm.getHullSpec().getHullSize());
    }

    public int getLevel(ETUpgrades upgrades) {
        return upgrades.getUpgrade(this.getKey());
    }

    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {

    }

    public void applyUpgradeToShip(FleetMemberAPI fm, ShipAPI ship, int level, int maxLevel) {

    }

    public void advanceInCombat(ShipAPI ship, float amount, int level, float bandwidth) {

    }

    public void advanceInCampaign(FleetMemberAPI fm, int level, int maxLevel) {

    }

    public float getSpawnChance() {
        return 0.9f;
    }

    public abstract void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand);

    protected void addIncreaseWithFinalToTooltip(TooltipMakerAPI tooltip, String translation, Float increase, Float base) {
        StringUtils.getTranslation(this.getKey(), translation)
                .formatWithOneDecimalAndModifier("percent", increase)
                .formatWithOneDecimalAndModifier("finalValue", base * increase / 100f)
                .addToTooltip(tooltip, 2f);
    }

    protected void addDecreaseWithFinalToTooltip(TooltipMakerAPI tooltip, String translation, Float decrease, Float base) {
        float finalMult = -(1f - decrease);
        StringUtils.getTranslation(this.getKey(), translation)
                .formatWithOneDecimalAndModifier("percent", finalMult * 100f)
                .formatWithOneDecimalAndModifier("finalValue", base * finalMult)
                .addToTooltip(tooltip, 2f);
    }

    protected void addIncreaseToTooltip(TooltipMakerAPI tooltip, String translation, Float increase) {
        StringUtils.getTranslation(this.getKey(), translation)
                .formatWithOneDecimalAndModifier("percent", increase)
                .addToTooltip(tooltip, 2f);
    }

    protected void addDecreaseToTooltip(TooltipMakerAPI tooltip, String translation, Float decrease) {
        StringUtils.getTranslation(this.getKey(), translation)
                .formatWithOneDecimalAndModifier("percent", -(1f - decrease) * 100f)
                .addToTooltip(tooltip, 2f);
    }

    public SpecialItemData getNewSpecialItemData(int level) {
        return new SpecialItemData(Upgrade.ITEM, String.format("%s,%s", this.getKey(), level));
    }

    public Map<String, Integer> getResourceCosts(FleetMemberAPI shipSelected, int level) {
        int max = getMaxLevel(shipSelected.getHullSpec().getHullSize());

        float hullBaseValue = shipSelected.getHullSpec().getBaseValue();
        if(hullBaseValue > 450000) {
            hullBaseValue = 225000;
        } else {
            hullBaseValue = (float) (hullBaseValue - (1d / 900000d) * Math.pow(hullBaseValue, 2));
        }
        hullBaseValue *= 0.01f;

        float upgradeCostRatioByLevel = 0.25f + 0.75f * ((float)level / (float)max);
        float upgradeCostByHull = hullBaseValue * upgradeCostRatioByLevel;

        Map<String, Integer> resourceCosts = new HashMap<>();
        Map<String, Float> resourceRatios = getResourceRatios();
        for (Map.Entry<String, Float> ratio : resourceRatios.entrySet()) {
            int commodityCost = Math.round(Utilities.getItemPrice(ratio.getKey()));
            int finalCost = Math.round(ratio.getValue() * upgradeCostByHull / commodityCost);
            resourceCosts.put(ratio.getKey(), finalCost);
        }

        return resourceCosts;
    }

    protected static <T extends Upgrade> T getInstance(Class<T> test) {
        for(Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            if(upgrade.getClass().equals(test)) {
                return (T) upgrade;
            }
        }
        return null;
    }
}
