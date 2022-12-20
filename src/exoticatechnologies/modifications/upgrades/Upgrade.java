package exoticatechnologies.modifications.upgrades;

import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.ETModSettings;
import exoticatechnologies.modifications.Modification;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.stats.UpgradeModEffect;
import exoticatechnologies.modifications.stats.impl.UpgradeModEffectDict;
import exoticatechnologies.ui.impl.shop.upgrades.methods.UpgradeMethod;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

@SuppressWarnings("EmptyMethod")
public class Upgrade extends Modification {
    public static final String ITEM = "et_upgrade";
    @Getter
    @Setter
    public String description;
    @Getter
    public float bandwidthUsage;
    @Getter
    protected final Map<String, Float> resourceRatios = new LinkedHashMap<>();
    @Getter
    public final Map<String, UpgradeModEffect> upgradeEffects = new LinkedHashMap<>();
    private final Set<String> allowedFactions = new HashSet<>();
    @Getter
    public float spawnChance;

    public Upgrade(String key, JSONObject settings) throws JSONException {
        super(key, settings);

        setColor(Utilities.colorFromJSONArray(settings.getJSONArray("color")));
        description = StringUtils.getString(key, "description");
        bandwidthUsage = (float) settings.getDouble("bandwidthPerLevel");
        upgradeEffects.putAll(UpgradeModEffectDict.Companion.getStatsFromJSONArray(settings.getJSONArray("stats")));
        spawnChance = (float) settings.optDouble("spawnChance", 100f);

        JSONObject settingRatios = settings.getJSONObject("resourceRatios");
        for (String resource : Utilities.RESOURCES_LIST) {
            float ratio = 0f;
            if (settingRatios.has(resource)) {
                //class cast exception indicates an improperly configured config
                ratio = ((Number) settingRatios.getDouble(resource)).floatValue();
            }
            resourceRatios.put(resource, ratio);
        }

        if (settings.has("allowedFactions")) {
            JSONArray settingFactions = settings.getJSONArray("allowedFactions");
            for (int i = 0; i < settingFactions.length(); i++) {
                String factionId = settingFactions.getString(i);
                allowedFactions.add(factionId);
            }
        }
    }

    public static Upgrade get(String upgradeKey) {
        return UpgradesHandler.UPGRADES.get(upgradeKey);
    }

    @Override
    public boolean shouldShow(FleetMemberAPI member, ShipModifications mods, MarketAPI market) {
        if (mods.getUpgrade(this) > 0) {
            return true;
        }

        if (member.getFleetData() != null
                && member.getFleetData().getFleet() != null) {
            if (Misc.isPlayerOrCombinedContainingPlayer(member.getFleetData().getFleet())) {
                if (Utilities.hasUpgradeChip(member.getFleetData().getFleet().getCargo(), this.getKey())) {
                    return true;
                }
            }
        }

        if (!allowedFactions.isEmpty()) {
            if (!allowedFactions.contains(member.getFleetData().getFleet().getFaction().getId())) {
                return false;
            }
        }

        return shouldShow();
    }

    @Override
    public boolean shouldShow() {
        return true;
    }

    @Override
    public boolean canApply(FleetMemberAPI member) {
        if (member.getFleetData() != null
                && member.getFleetData().getFleet() != null) {
            if (Misc.isPlayerOrCombinedContainingPlayer(member.getFleetData().getFleet())) {
                if (Utilities.hasUpgradeChip(member.getFleetData().getFleet().getCargo(), this.getKey())) {
                    return canApply(member.getVariant());
                }
            }
        }

        if (!allowedFactions.isEmpty()) {
            String faction = null;
            if (member.getHullId().contains("ziggurat")) {
                faction = "omega";
            } else if (member.getFleetData() != null
                    && member.getFleetData().getFleet() != null) {
                faction = member.getFleetData().getFleet().getFaction().getId();
            }

            if (faction == null || !allowedForFaction(faction)) {
                return false;
            }
        }

        return canApply(member.getVariant());
    }

    public boolean canUseUpgradeMethod(FleetMemberAPI member, ShipModifications mods, UpgradeMethod method) {
        return true;
    }

    public boolean allowedForFaction(String faction) {
        if (!allowedFactions.isEmpty()) {
            if (allowedFactions.contains(faction)) {
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public String getIcon() {
        return "graphics/icons/upgrades/" + getKey().toLowerCase() + ".png";
    }

    private int getMaxLevel() {
        return -1;
    }

    public int getMaxLevel(ShipAPI.HullSize hullSize) {
        return getMaxLevel() != -1 ? getMaxLevel() : ETModSettings.getHullSizeToMaxLevel().get(hullSize);
    }

    /**
     * note: overrides should be done to the HullSize method
     *
     * @param fm
     * @return
     */
    public final int getMaxLevel(FleetMemberAPI fm) {
        return getMaxLevel(fm.getHullSpec().getHullSize());
    }

    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, ShipModifications mods) {
        for (UpgradeModEffect effect : upgradeEffects.values()) {
            effect.applyToStats(stats, fm, mods, this);
        }
    }

    public void applyUpgradeToShip(FleetMemberAPI member, ShipAPI ship, ShipModifications mods) {
        for (UpgradeModEffect effect : upgradeEffects.values()) {
            effect.applyToShip(ship, member, mods, this);
        }
    }

    public void advanceInCombat(ShipAPI ship, float amount, FleetMemberAPI member, ShipModifications mods) {
        for (UpgradeModEffect effect : upgradeEffects.values()) {
            effect.advanceInCombat(ship, amount, member, mods, this);
        }
    }

    public void advanceInCampaign(FleetMemberAPI member, ShipModifications mods, Float amount) {
        for (UpgradeModEffect effect : upgradeEffects.values()) {
            effect.advanceInCampaign(member, mods, this, amount);
        }
    }

    public void modifyToolTip(TooltipMakerAPI tooltip, MutableShipStatsAPI stats, FleetMemberAPI member, ShipModifications mods, boolean expand) {
        tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(mods.getUpgrade(this)));
        if (expand) {
            for (UpgradeModEffect effect : upgradeEffects.values()) {
                effect.printToTooltip(tooltip, stats, member, mods, this);
            }
        }
    }

    public void modifyInShop(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipModifications mods) {
        Map<Integer, List<UpgradeModEffect>> levelToEffectMap = new HashMap<>();

        for (Map.Entry<String, UpgradeModEffect> effectPair : upgradeEffects.entrySet()) {
            UpgradeModEffect effect = effectPair.getValue();
            int startingLevel = effect.getStartingLevel();
            List<UpgradeModEffect> levelList;
            if (!levelToEffectMap.containsKey(startingLevel)) {
                levelList = new ArrayList<>();
                levelToEffectMap.put(startingLevel, levelList);
            } else {
                levelList = levelToEffectMap.get(startingLevel);
            }

            levelList.add(effect);
        }

        for (int startingLevel = 0; startingLevel < getMaxLevel(member); startingLevel++) {
            if (levelToEffectMap.containsKey(startingLevel)) {
                List<UpgradeModEffect> levelList = levelToEffectMap.get(startingLevel);

                if (startingLevel > 1) {
                    StringUtils.getTranslation("UpgradesDialog", "UpgradeDrawbackAfterLevel")
                            .format("level", startingLevel)
                            .addToTooltip(tooltip).getPosition().setYAlignOffset(-10);
                }

                for (UpgradeModEffect effect : levelList) {
                    effect.printToShop(tooltip, member, mods, this);
                }
            }
        }
    }

    public SpecialItemData getNewSpecialItemData(int level) {
        return new SpecialItemData(Upgrade.ITEM, String.format("%s,%s", this.getKey(), level));
    }

    public Map<String, Integer> getResourceCosts(FleetMemberAPI shipSelected, int level) {
        int max = getMaxLevel(shipSelected.getHullSpec().getHullSize());

        float hullBaseValue = shipSelected.getHullSpec().getBaseValue();
        if (hullBaseValue > 450000) {
            hullBaseValue = 225000;
        } else {
            hullBaseValue = (float) (hullBaseValue - (1d / 900000d) * Math.pow(hullBaseValue, 2));
        }
        hullBaseValue *= 0.01f;

        float upgradeCostRatioByLevel = 0.25f + 0.75f * ((float) level / (float) max);
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

    @Override
    public String toString() {
        return "Upgrade{name=" + getName() + "}";
    }

    public void applyUpgradeToFighter(FleetMemberAPI member, ShipAPI fighter, ShipAPI ship, ShipModifications mods) {}
}
