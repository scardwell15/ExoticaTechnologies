package exoticatechnologies.modifications.upgrades;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.util.MagicSettings;
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
    public static final Map<String, Upgrade> UPGRADES = new HashMap<>();
    public static final List<Upgrade> UPGRADES_LIST = new ArrayList<>();
    public static final Set<UpgradeMethod> UPGRADE_METHODS = new LinkedHashSet<>();

    public static void addUpgradeMethod(UpgradeMethod method) {
        UPGRADE_METHODS.add(method);
    }

    public static void initialize() {
        UPGRADES.clear();
        UPGRADE_METHODS.clear();
        UPGRADE_METHODS.add(new CreditsMethod());
        UPGRADE_METHODS.add(new ResourcesMethod());
        UPGRADE_METHODS.add(new ChipMethod());
        UPGRADE_METHODS.add(new RecoverMethod());


        UpgradesHandler.populateUpgrades();

        UPGRADES_LIST.clear();
        List<String> orderedKeys = MagicSettings.getList("exoticatechnologies", "upgradeOrder");
        for (int i = 0; i < orderedKeys.size(); i++) {
            String key = orderedKeys.get(i);
            UPGRADES_LIST.add(UPGRADES.get(key));
        }

        for (Upgrade upgrade : UPGRADES.values()) {
            if (!orderedKeys.contains(upgrade.getKey())) {
                UPGRADES_LIST.add(upgrade);
            }
        }

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
    }
}
