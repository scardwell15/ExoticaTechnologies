package exoticatechnologies.cargo;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import lombok.Getter;

public class CrateGlobalData {
    @Getter private final CargoAPI cargo;

    public CrateGlobalData() {
        this.cargo = Global.getFactory().createCargo(true);
    }

    public CrateGlobalData(CargoAPI cargo) {
        this.cargo = cargo;
    }

    private static String GLOBAL_KEY = "exoticaCrateData";
    public static CrateGlobalData getInstance() {
        if (!Global.getSector().getPersistentData().containsKey(GLOBAL_KEY)) {
            Global.getSector().getPersistentData().put(GLOBAL_KEY, new CrateGlobalData());
        }

        return (CrateGlobalData) Global.getSector().getPersistentData().get(GLOBAL_KEY);
    }

    public static void addCargo(CargoAPI cargo) {
        CrateGlobalData data = getInstance();
        if (data == null) {
            data = new CrateGlobalData(cargo);
        } else {
            data.cargo.addAll(cargo);
        }
        Global.getSector().getPersistentData().put(GLOBAL_KEY,  data);
    }
}
