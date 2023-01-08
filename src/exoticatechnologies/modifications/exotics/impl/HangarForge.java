package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import exoticatechnologies.modifications.exotics.Exotic;
import lombok.Getter;
import org.json.JSONObject;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class HangarForge extends Exotic {
    private static final String ITEM = "et_hangarforge";
    private static final String REPLACEMENT_COUNT_ID = "et_fighterReplacements";
    private static final String REPLACEMENT_INTERVAL_ID = "et_fighterReplacementInterval";

    private static float RATE_DECREASE_MODIFIER = 25f;

    @Getter private final Color color = Color.GREEN;

    public HangarForge(String key, JSONObject settings) {
        super(key, settings);
    }

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return Utilities.hasItem(fleet.getCargo(), ITEM);
    }

    @Override
    public boolean canApply(FleetMemberAPI member, ShipModifications mods) {
        if (member.getStats() != null) {
            if (member.getStats().getNumFighterBays().getModifiedInt() > 0) {
                return canApplyToVariant(member.getVariant());
            }
        }
        return false;
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm, MarketAPI market) {
        Utilities.takeItemQuantity(fleet.getCargo(), ITEM, 1);
        return true;
    }

    @Override
    public Map<String, Float> getResourceCostMap(FleetMemberAPI fm, ShipModifications mods, MarketAPI market) {
        Map<String, Float> resourceCosts = new HashMap<>();
        resourceCosts.put(Utilities.formatSpecialItem(ITEM), 1f);
        return resourceCosts;
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        if (expand) {
            StringUtils.getTranslation(this.getKey(), "longDescription")
                    .format("rateDecreaseBuff", RATE_DECREASE_MODIFIER)
                    .addToTooltip(tooltip, title);
        }
    }

    @Override
    public void applyExoticToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, float bandwidth, String id) {
        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(getBuffId(), 1f + RATE_DECREASE_MODIFIER / 100f);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount, float bandwidth) {
        int replacements = getFreeReplacements(ship);

        maintainStatus(ship,
                this.getBuffId(),
                StringUtils.getTranslation(this.getKey(), "statusText")
                        .format("freeReplacements", replacements)
                        .toStringNoFormats()
        );

        if (replacements < calculateMaxReplacements(ship)) {
            advanceReplacementInterval(ship, amount);
        }

        if (replacements > 0) {
            for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
                if (getDeadFighters(bay) > 0 && bay.getFastReplacements() == 0) {
                    bay.setFastReplacements(1);
                    bay.makeCurrentIntervalFast();
                    addFreeReplacements(ship, -1);
                    replacements--;

                    if (replacements == 0) break;
                }
            }
        }
    }

    public static int getDeadFighters(FighterLaunchBayAPI bay) {
        int dead = 0;

        if (bay.getWing() != null && bay.getWing().getWingMembers() != null) {
            dead += (getNumFighters(bay) - bay.getWing().getWingMembers().size());
            for (ShipAPI ship : bay.getWing().getWingMembers()) {
                if (!ship.isAlive()) {
                    dead++;
                }
            }
        }

        return dead;
    }

    public static int getNumFighters(FighterLaunchBayAPI bay) {
        return bay.getWing().getSpec().getNumFighters() + bay.getExtraDeployments();
    }

    public static void advanceReplacementInterval(ShipAPI ship, float amount) {
        IntervalUtil replacementInterval = getReplacementInterval(ship);
        replacementInterval.advance(amount);

        if (replacementInterval.intervalElapsed()) {
            addFreeReplacements(ship, calculateAddedReplacements(ship));
        }
    }

    private static IntervalUtil getReplacementInterval(ShipAPI ship) {
        if (!ship.getCustomData().containsKey(REPLACEMENT_INTERVAL_ID)) {
            ship.setCustomData(REPLACEMENT_INTERVAL_ID, new IntervalUtil(60f, 60f));
        }
        return (IntervalUtil) ship.getCustomData().get(REPLACEMENT_INTERVAL_ID);
    }

    private static int getFreeReplacements(ShipAPI ship) {
        if (!ship.getCustomData().containsKey(REPLACEMENT_COUNT_ID)) {
            int maxReplacements = calculateMaxReplacements(ship);
            setFreeReplacements(ship, maxReplacements);
        }
        return (int) ship.getCustomData().get(REPLACEMENT_COUNT_ID);
    }

    private static void setFreeReplacements(ShipAPI ship, int value) {
        ship.setCustomData(REPLACEMENT_COUNT_ID, value);
    }

    private static void addFreeReplacements(ShipAPI ship, int value) {
        setFreeReplacements(ship, getFreeReplacements(ship) + value);
    }

    private static int calculateAddedReplacements(ShipAPI ship) {
        return (int) Math.ceil(calculateMaxReplacements(ship) / 2f);
    }

    private static int calculateMaxReplacements(ShipAPI ship) {
        int wingCount = 0;
        for (String wingId : ship.getVariant().getWings()) {
            wingCount += Global.getSettings().getFighterWingSpec(wingId).getNumFighters();
        }

        return wingCount;
    }

    public void maintainStatus(ShipAPI ship, String id, String translation) {
        if(Global.getCombatEngine().getPlayerShip() != null
                && Global.getCombatEngine().getPlayerShip().equals(ship)) {
            Global.getCombatEngine().maintainStatusForPlayerShip(
                    id,
                    "graphics/icons/hullsys/reserve_deployment.png",
                    this.getName(),
                    translation,
                    false);
        }
    }
}
