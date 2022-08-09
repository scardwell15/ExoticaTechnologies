package exoticatechnologies.cargo;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import lombok.Getter;

public class CrateSpecialData extends SpecialItemData {
    @Getter private final CargoAPI cargo;

    public CrateSpecialData() {
        super("et_crate", "");
        this.cargo = Global.getFactory().createCargo(true);
    }
}
