package exoticatechnologies.modifications.exotics;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.Map;

public abstract class Exotic {
    public static final String ITEM = "et_exotic";
    @Getter
    @Setter
    public String key;
    @Getter
    @Setter
    public String name;
    @Getter
    @Setter
    protected String description;
    @Getter
    @Setter
    protected String tooltip;
    @Getter
    protected JSONObject exoticSettings;

    public static Exotic get(String exoticKey) {
        return ExoticsHandler.EXOTICS.get(exoticKey);
    }

    public abstract Color getMainColor();

    public String getTextDescription() {
        return getDescription() + "\n\n" + getTooltip();
    }

    public void setConfig(JSONObject exoticSettings) throws JSONException {
        this.exoticSettings = exoticSettings;
        loadConfig();
    }

    protected void loadConfig() throws JSONException {
    }

    public String getIcon() {
        return "graphics/icons/exotics/" + getKey().toLowerCase() + ".png";
    }

    public String getBuffId() {
        return "ESR_" + getKey();
    }

    protected boolean isNPC(FleetMemberAPI fm) {
        return fm.getFleetData() == null
                || fm.getFleetData().getFleet() == null
                || !fm.getFleetData().getFleet().equals(Global.getSector().getPlayerFleet());
    }

    public void onInstall(FleetMemberAPI fm) {

    }

    public void onDestroy(FleetMemberAPI fm) {

    }

    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return false;
    }

    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        return false;
    }

    public boolean canApply(FleetMemberAPI fm) {
        return canApply(fm.getVariant());
    }

    public boolean canApply(ShipVariantAPI fm) {
        return true;
    }

    public boolean shouldLoad() {
        return true;
    }

    public boolean shouldShow(FleetMemberAPI fm, ShipModifications es, MarketAPI market) {
        return true;
    }

    public void printDescriptionToTooltip(FleetMemberAPI fm, TooltipMakerAPI tooltip) {
        StringUtils.getTranslation(this.getKey(), "description")
                .addToTooltip(tooltip);
    }

    public void printStatInfoToTooltip(FleetMemberAPI fm, TooltipMakerAPI tooltip) {
        StringUtils.getTranslation(this.getKey(), "longDescription")
                .addToTooltip(tooltip);
    }

    public void printDescriptionToTooltip(TooltipMakerAPI tooltip, FleetMemberAPI member) {
        StringUtils.getTranslation(this.getKey(), "description")
                .addToTooltip(tooltip);
    }

    public void printStatInfoToTooltip(TooltipMakerAPI tooltip, FleetMemberAPI member) {
        StringUtils.getTranslation(this.getKey(), "longDescription")
                .addToTooltip(tooltip);
    }

    public Map<String, Float> getResourceCostMap(FleetMemberAPI fm, ShipModifications mods, MarketAPI market) {
        return null;
    }


    /**
     * extra bandwidth allowed to be installed.
     *
     * @param fm
     * @param es
     * @return
     */
    public float getExtraBandwidthPurchaseable(FleetMemberAPI fm, ShipModifications es) {
        return 0f;
    }

    /**
     * extra bandwidth added directly to ship.
     *
     * @param fm
     * @param es
     * @return
     */
    public float getExtraBandwidth(FleetMemberAPI fm, ShipModifications es) {
        return 0f;
    }

    public void applyExoticToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, float bandwidth, String id) {

    }

    public void applyExoticToShip(FleetMemberAPI fm, ShipAPI ship, float bandwidth, String id) {

    }

    public void advanceInCombat(ShipAPI ship, float amount, float bandwidth) {

    }

    public boolean canDropFromFleets() {
        return true;
    }

    public float getSpawnChance(float chanceMult) {
        return 0.05f * chanceMult;
    }

    public SpecialItemData getNewSpecialItemData() {
        return new SpecialItemData(Exotic.ITEM, this.getKey());
    }

    public abstract void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications systems, boolean expand);
}
