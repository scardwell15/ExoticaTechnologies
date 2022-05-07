package extrasystemreloaded;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import extrasystemreloaded.dialog.modifications.SystemOptionsHandler;
import extrasystemreloaded.integration.indevo.IndEvoUtil;
import extrasystemreloaded.systems.exotics.ExoticsHandler;
import extrasystemreloaded.campaign.listeners.ESCampaignEventListener;
import extrasystemreloaded.campaign.listeners.SalvageListener;
import extrasystemreloaded.hullmods.ExtraSystemHM;
import extrasystemreloaded.systems.bandwidth.BandwidthHandler;
import extrasystemreloaded.systems.upgrades.UpgradesHandler;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.FleetMemberUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.lazywizard.console.Console;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Log4j
public class Es_ModPlugin extends BaseModPlugin {
	public static final String ES_PERSISTENTUPGRADEMAP = "ES_UPGRADEMAP";
	@Getter private static Map<String, ExtraSystems> ShipUpgradeData;
	private static boolean debugUpgradeCosts = false;

	private static ESCampaignEventListener campaignListener = null;
	private static SalvageListener salvageListener = null;

    @Override
    public void onGameLoad(boolean newGame) {
		FleetMemberUtils.moduleMap.clear();
		SystemOptionsHandler.getValidSystemsOptions().clear();

		ESModSettings.loadModSettings();

		BandwidthHandler.initialize();
		UpgradesHandler.initialize();
		ExoticsHandler.initialize();

		if(Global.getSettings().getModManager().isModEnabled("IndEvo")) {
			IndEvoUtil.loadIntegration();
		}

		addListeners();

		loadUpgradeData();
	}

	public static void loadUpgradeData() {
		if(Global.getSector().getPersistentData().get(ES_PERSISTENTUPGRADEMAP)==null) {
			Global.getSector().getPersistentData().put(ES_PERSISTENTUPGRADEMAP, new HashMap<String, ExtraSystems>());
		}
		ShipUpgradeData = (Map<String, ExtraSystems>) Global.getSector().getPersistentData().get(ES_PERSISTENTUPGRADEMAP);

		for (String fmId : ShipUpgradeData.keySet()) {
			FleetMemberAPI fm = ESCampaignEventListener.findFM(fmId);

			if (fm != null) {
				ExtraSystemHM.addToFleetMember(fm);
			}
		}
	}

	public static ExtraSystems getData(String shipId) {
		if(ShipUpgradeData == null) {
			loadUpgradeData();
		}

		return ShipUpgradeData.get(shipId);
	}

	public static boolean hasData(String shipId) {
		if(ShipUpgradeData == null) {
			loadUpgradeData();
		}

		return ShipUpgradeData.containsKey(shipId);
	}

	public static void saveData(String shipId, ExtraSystems systems) {
		if(ShipUpgradeData == null) {
			loadUpgradeData();
		}

		ShipUpgradeData.put(shipId, systems);
	}

	public static void removeData(String shipId) {
		if(ShipUpgradeData == null) {
			loadUpgradeData();
		}

		ShipUpgradeData.remove(shipId);
	}

	@Override
	public void beforeGameSave() {
    	removeListeners();
		Es_ModPlugin.removeESHullmodsFromAutoFitGoalVariants();
	}

	@Override
	public void afterGameSave() {
    	addListeners();
	}

	private static void addListeners() {
		campaignListener = new ESCampaignEventListener(false);
		Global.getSector().getListenerManager().addListener(salvageListener = new SalvageListener(), true);
		Global.getSector().addTransientScript(campaignListener);
		Global.getSector().addListener(campaignListener);
	}

	private static void removeListeners() {
		Global.getSector().removeTransientScript(campaignListener);
		Global.getSector().removeListener(campaignListener);
		Global.getSector().getListenerManager().removeListener(salvageListener);

	}

	public static void removeESHullmodsFromAutoFitGoalVariants() {
		try {
			for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
				for (ShipVariantAPI v : Global.getSector().getAutofitVariants().getTargetVariants(spec.getHullId())) {
					if(v != null) ExtraSystemHM.removeESHullModsFromVariant(v);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void removeESHullmodsFromEveryVariant() {
		try {
			for (CampaignFleetAPI campaignFleetAPI : Global.getSector().getCurrentLocation().getFleets()) {
				for (FleetMemberAPI member : campaignFleetAPI.getFleetData().getMembersListCopy()) {
					ExtraSystemHM.removeESHullModsFromVariant(member.getVariant());
					Console.showMessage("Cleared ESR data from: "+member.getShipName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		removeESHullmodsFromAutoFitGoalVariants();
	}

	public static void removeExtraSystemsFromFleet(CampaignFleetAPI fleet) {
		if (fleet.getFleetData() == null || fleet.getFleetData().getMembersListCopy() == null) {
			log.info("Fleet data was null");
			return;
		}

		for (Iterator<FleetMemberAPI> it = fleet.getFleetData().getMembersListCopy().iterator(); it.hasNext(); ) {
			removeData(it.next().getId());
		}
	}

	public static void applyExtraSystemsToFleet(CampaignFleetAPI fleet) {
		for (FleetMemberAPI fm : fleet.getFleetData().getMembersListCopy()) {
			if (fm.isFighterWing()) continue;

			//generate random extra system
			ExtraSystems es = ExtraSystems.generateRandom(fm);
			es.save(fm);
			ExtraSystemHM.addToFleetMember(fm);

			log.info(String.format("Added extra systems to member %s", fm.getShipName()));
		}
	}

    public static void setDebugUpgradeCosts(boolean set) {
		debugUpgradeCosts = set;
	}

	public static boolean isDebugUpgradeCosts() {
		return debugUpgradeCosts;
	}
}
