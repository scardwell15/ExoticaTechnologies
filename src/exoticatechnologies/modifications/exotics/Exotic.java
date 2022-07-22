package exoticatechnologies.modifications.exotics;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.modifications.ShipModifications;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.Map;

public abstract class Exotic {
    public static String ITEM = "et_exotic";
    @Getter
    @Setter
    protected String key;
    @Getter
    @Setter
    protected String name;
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
    };

    public String getIcon() {
        return "graphics/icons/exotics/" + getKey().toLowerCase() + ".png";
    }

    public String getBuffId() {
        return "ESR_" + getKey();
    }

    protected boolean isNPC(FleetMemberAPI fm) {
        if (fm.getFleetData() == null
                || fm.getFleetData().getFleet() == null
                || !fm.getFleetData().getFleet().equals(Global.getSector().getPlayerFleet())) {
            return true;
        }
        return false;
    }

    public abstract boolean canAfford(CampaignFleetAPI fleet, MarketAPI market);

    public boolean canApply(FleetMemberAPI fm) {
        return canApply(fm.getVariant());
    }

    public boolean canApply(ShipVariantAPI fm) {
        return true;
    }

    public abstract String getUnableToApplyTooltip(CampaignFleetAPI fleet, FleetMemberAPI fm);

    public abstract boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm);

    public abstract boolean restoreItemsToFleet(CampaignFleetAPI fleet, FleetMemberAPI fm);

    public boolean shouldLoad() {
        return true;
    }

    public boolean shouldShow(FleetMemberAPI fm, ShipModifications es, MarketAPI market) {
        return true;
    }

    public void initialize() {
        ExoticsHandler.addExotic(this);
    }

    public void modifyResourcesPanel(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm) {
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

    public float getSpawnChance(float chanceMult) {
        return 0.05f * chanceMult;
    }

    public SpecialItemData getNewSpecialItemData() {
        return new SpecialItemData(Exotic.ITEM, this.getKey());
    }

    public abstract void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand);
}
