package exoticatechnologies.modifications.upgrades;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.util.MagicSettings;
import exoticatechnologies.modifications.upgrades.methods.*;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.ui.impl.shop.ShopManager;
import exoticatechnologies.ui.impl.shop.exotics.ExoticShopUIPlugin;
import exoticatechnologies.ui.impl.shop.upgrades.UpgradeShopUIPlugin;
import exoticatechnologies.util.StringUtils;
import lombok.extern.log4j.Log4j;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

@Log4j
public class UpgradesHandler {
    private static final int UPGRADE_OPTION_ORDER = 1;
    public static final Map<String, Upgrade> UPGRADES = new HashMap<>();
    public static final List<Upgrade> UPGRADES_LIST = new ArrayList<>();

    public static final Set<UpgradeMethod> UPGRADE_METHODS = new LinkedHashSet<>();


    public static void addUpgradeMethod(UpgradeMethod method) {
        UPGRADE_METHODS.add(method);
    }

    public static void initialize() {
        UPGRADE_METHODS.clear();
        UPGRADE_METHODS.add(new CreditsMethod());
        UPGRADE_METHODS.add(new ResourcesMethod());
        UPGRADE_METHODS.add(new ChipMethod());
        UPGRADE_METHODS.add(new RecoverMethod());

        UpgradesHandler.populateUpgrades();

        ShopManager.addMenu(new UpgradeShopUIPlugin());
        ShopManager.addMenu(new ExoticShopUIPlugin());
    }

    public static void populateUpgrades() {
        try {
            JSONObject settings = Global.getSettings().getMergedJSONForMod("data/config/upgrades.json", "exoticatechnologies");

            Iterator upgIterator = settings.keys();
            while(upgIterator.hasNext()) {
                String upgKey = (String) upgIterator.next();

                if(UPGRADES.containsKey(upgKey)) continue;

                JSONObject upgObj = (JSONObject) settings.getJSONObject(upgKey);

                Class<?> clzz = Global.getSettings().getScriptClassLoader().loadClass(upgObj.getString("upgradeClass"));
                Upgrade upgrade = (Upgrade) clzz.newInstance();

                if(upgrade.shouldLoad()) {
                    upgrade.setKey(upgKey);
                    upgrade.setName(StringUtils.getString(upgKey, "name"));
                    upgrade.setDescription(StringUtils.getString(upgKey, "description"));
                    upgrade.setConfig(upgObj);

                    UpgradesHandler.addUpgrade(upgrade);

                    log.info(String.format("loaded upgrade [%s]", upgrade.getName()));
                }
            }
        } catch (JSONException | IOException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void loadConfigs() {
        try {
            JSONObject settings = Global.getSettings().getMergedJSONForMod("data/config/upgrades.json", "exoticatechnologies");

            for (Upgrade upgrade : UPGRADES_LIST) {
                if (!settings.has(upgrade.getKey())) {
                    continue;
                }

                upgrade.setConfig(settings.getJSONObject(upgrade.getKey()));
            }
        } catch (JSONException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void addUpgrade(Upgrade upgrade) {
        if(UPGRADES.containsKey(upgrade.getKey())) return;

        UPGRADES.put(upgrade.getKey(),upgrade);
        UPGRADES_LIST.add(upgrade);
    }

    //can upgrade
    public static boolean canUseUpgradeMethods(FleetMemberAPI fm, ShipModifications mods, ShipAPI.HullSize hullSize, Upgrade upgrade, CampaignFleetAPI fleet, MarketAPI currMarket) {
        if(mods.getUsedBandwidth() + upgrade.getBandwidthUsage() > mods.getBandwidthWithExotics(fm)) {
            return false;
        }

        for (UpgradeMethod method : UpgradesHandler.UPGRADE_METHODS) {
            if (method.canShow(fm, mods, upgrade, currMarket)
                    && method.canUse(fm, mods, upgrade, currMarket)) {
                return true;
            }
        }

        return false;
    }

    //upgrade whitelist for faction
    public static List<String> getWhitelistForFaction(String faction) {
        List<String> factionAllowedUpgrades = MagicSettings.getList("exoticatechnologies", "rngUpgradeWhitelist");
        try {
            if(MagicSettings.modSettings.getJSONObject("exoticatechnologies").has(faction + "_UpgradeWhitelist")) {
                factionAllowedUpgrades = MagicSettings.getList("exoticatechnologies", faction + "_UpgradeWhitelist");
            }
        } catch (JSONException ex) {
            log.info("ET modSettings object doesn't exist. Is this a bug in MagicLib, or did you remove it?");
            log.info("The actual exception follows.", ex);
        }
        return factionAllowedUpgrades;
    }
}
