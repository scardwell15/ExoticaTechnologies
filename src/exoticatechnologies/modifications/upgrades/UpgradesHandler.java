package exoticatechnologies.modifications.upgrades;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.ui.impl.shop.ShopManager;
import exoticatechnologies.ui.impl.shop.exotics.ExoticShopUIPlugin;
import exoticatechnologies.ui.impl.shop.upgrades.UpgradeShopUIPlugin;
import exoticatechnologies.ui.impl.shop.upgrades.methods.*;
import lombok.extern.log4j.Log4j;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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
    }

    public static void populateUpgrades() {
        try {
            JSONObject settings = Global.getSettings().getMergedJSONForMod("data/config/upgrades.json", "exoticatechnologies");

            Iterator upgIterator = settings.keys();
            while (upgIterator.hasNext()) {
                String upgKey = (String) upgIterator.next();

                if (UPGRADES.containsKey(upgKey)) continue;

                Upgrade upgrade;
                try {
                    JSONObject upgradeSettings = settings.getJSONObject(upgKey);

                    if (upgradeSettings.has("upgradeClass")) {
                        Class<?> clzz = Global.getSettings().getScriptClassLoader().loadClass(upgradeSettings.getString("upgradeClass"));

                        //magic to get around reflection block
                        upgrade = (Upgrade) MethodHandles.lookup().findConstructor(clzz, MethodType.methodType(void.class, String.class, JSONObject.class))
                                .invoke(upgKey, upgradeSettings);
                        if (!upgrade.shouldLoad()) {
                            upgrade = null;
                        }
                    } else {
                        upgrade = new Upgrade(upgKey, upgradeSettings);

                        if (!upgrade.shouldLoad()) {
                            upgrade = null;
                        }
                    }

                    if (upgrade != null) {
                        UpgradesHandler.addUpgrade(upgrade);

                        log.info(String.format("loaded upgrade [%s]", upgrade.getName()));
                    }
                } catch (JSONException ex) {
                    String logStr = String.format("Upgrade [%s] had an error.", upgKey);
                    log.error(logStr);
                    throw new RuntimeException(logStr, ex);
                }
            }
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void addUpgrade(Upgrade upgrade) {
        if (UPGRADES.containsKey(upgrade.getKey())) return;

        UPGRADES.put(upgrade.getKey(), upgrade);
        UPGRADES_LIST.add(upgrade);
    }

    public static List<Upgrade> getAllowedUpgrades(FleetMemberAPI member) {
        List<Upgrade> upgrades = new ArrayList<>();

        for (Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            if (upgrade.canApply(member, null)) {
                upgrades.add(upgrade);
            }
        }

        return upgrades;
    }
}
