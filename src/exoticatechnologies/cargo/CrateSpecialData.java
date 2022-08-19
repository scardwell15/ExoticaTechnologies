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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CrateSpecialData that = (CrateSpecialData) o;

        return cargo != null ? cargo.equals(that.cargo) : that.cargo == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (cargo != null ? cargo.hashCode() : 0);
        return result;
    }
}
