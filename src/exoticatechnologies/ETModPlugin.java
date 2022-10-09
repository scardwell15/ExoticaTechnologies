package exoticatechnologies;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.integration.indevo.IndEvoUtil;
import exoticatechnologies.modifications.exotics.ExoticsHandler;
import exoticatechnologies.campaign.listeners.CampaignEventListener;
import exoticatechnologies.campaign.listeners.SalvageListener;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.ui.impl.shop.ShopManager;
import exoticatechnologies.util.FleetMemberUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.lazywizard.console.Console;
import org.lazywizard.lazylib.MathUtils;

import java.util.HashMap;
import java.util.Map;

@Log4j
public class ETModPlugin extends BaseModPlugin {
	public static final String ET_PERSISTENTUPGRADEMAP = "ET_MODMAP";
	@Getter private static Map<String, ShipModifications> shipModificationMap = null;
	private static boolean debugUpgradeCosts = false;

	private static CampaignEventListener campaignListener = null;
	private static SalvageListener salvageListener = null;

	@Override
	public void onApplicationLoad() {
		ETModSettings.loadModSettings();
		UpgradesHandler.initialize();
		ExoticsHandler.initialize();
	}

	@Override
    public void onGameLoad(boolean newGame) {
		FleetMemberUtils.moduleMap.clear();
		ShopManager.getShopMenuUIPlugins().clear();

		ETModSettings.loadModSettings();

		UpgradesHandler.initialize();
		ExoticsHandler.initialize();

		if(Global.getSettings().getModManager().isModEnabled("IndEvo")) {
			IndEvoUtil.loadIntegration();
		}

		addListeners();

		loadModificationData();
	}

	public static void loadModificationData() {
		if(Global.getSector().getPersistentData().get(ET_PERSISTENTUPGRADEMAP)==null) {
			Global.getSector().getPersistentData().put(ET_PERSISTENTUPGRADEMAP, new HashMap<String, ShipModifications>());
		}

		shipModificationMap = (Map<String, ShipModifications>) Global.getSector().getPersistentData().get(ET_PERSISTENTUPGRADEMAP);

		if (campaignListener != null) {
			campaignListener.invalidateShipModifications();
		}
	}

	public static String getSectorSeedString() {
		if (!Global.getSector().getPersistentData().containsKey("ET_seedString")) {
			Global.getSector().getPersistentData().put("ET_seedString", String.valueOf(MathUtils.getRandomNumberInRange(1000,10000)).hashCode());
		}
		return String.valueOf(Global.getSector().getPersistentData().get("ET_seedString"));
	}

	public static void setZigguratDuplicateId(String ziggId) {
		Global.getSector().getPersistentData().put("ET_zigguratId", ziggId);
	}

	public static String getZigguratDuplicateId() {
		if (Global.getSector().getPersistentData().containsKey("ET_zigguratId")) {
			return String.valueOf(Global.getSector().getPersistentData().get("ET_zigguratId"));
		}
		return null;
	}

	public static ShipModifications getData(String shipId) {
		if(shipModificationMap == null) {
			loadModificationData();
		}

		return shipModificationMap.get(shipId);
	}

	public static boolean hasData(String shipId) {
		if(shipModificationMap == null) {
			loadModificationData();
		}

		return shipModificationMap.containsKey(shipId);
	}

	public static void saveData(String shipId, ShipModifications systems) {
		if(shipModificationMap == null) {
			loadModificationData();
		}

		shipModificationMap.put(shipId, systems);
	}

	public static void removeData(String shipId) {
		if (shipId.equals(ETModPlugin.getZigguratDuplicateId())) return;

		if(shipModificationMap == null) {
			loadModificationData();
		}

		shipModificationMap.remove(shipId);
	}

	@Override
	public void beforeGameSave() {
    	removeListeners();
		ETModPlugin.removeHullmodsFromAutoFitGoalVariants();
	}

	@Override
	public void afterGameSave() {
    	addListeners();
	}

	private static void addListeners() {
		campaignListener = new CampaignEventListener(false);
		Global.getSector().getListenerManager().addListener(salvageListener = new SalvageListener(), true);
		Global.getSector().addTransientScript(campaignListener);
		Global.getSector().addListener(campaignListener);
	}

	private static void removeListeners() {
		Global.getSector().removeTransientScript(campaignListener);
		Global.getSector().removeListener(campaignListener);
		Global.getSector().getListenerManager().removeListener(salvageListener);

	}

	public static void removeHullmodsFromAutoFitGoalVariants() {
		try {
			for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
				for (ShipVariantAPI v : Global.getSector().getAutofitVariants().getTargetVariants(spec.getHullId())) {
					if(v != null) ExoticaTechHM.removeHullModFromVariant(v);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void removeHullmodsFromEveryVariant() {
		try {
			for (CampaignFleetAPI campaignFleetAPI : Global.getSector().getCurrentLocation().getFleets()) {
				for (FleetMemberAPI member : campaignFleetAPI.getFleetData().getMembersListCopy()) {
					ExoticaTechHM.removeHullModFromVariant(member.getVariant());
					Console.showMessage("Cleared ET data from: " + member.getShipName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		removeHullmodsFromAutoFitGoalVariants();
	}

    public static void setDebugUpgradeCosts(boolean set) {
		debugUpgradeCosts = set;
	}

	public static boolean isDebugUpgradeCosts() {
		return debugUpgradeCosts;
	}
}
