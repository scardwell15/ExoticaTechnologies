package exoticatechnologies.modifications.exotics;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.ModSpecialItemPlugin;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

public class ExoticSpecialItemPlugin extends ModSpecialItemPlugin {
    @Getter @Setter
    protected boolean ignoreCrate = false;

    protected Exotic exotic;

    @Getter
    protected ExoticData exoticData;

    @Override
    public ModType getType() {
        return ModType.EXOTIC;
    }

    public final Exotic getExotic() {
        if (exotic == null) {
            exotic = ExoticsHandler.EXOTICS.get(modId);

            if (exotic == null) {
                modId = "DriveFluxVent";
                exotic = ExoticsHandler.EXOTICS.get("DriveFluxVent");
                exoticData = new ExoticData(exotic);
            }
        }

        if (exoticData == null) {
            exoticData = new ExoticData(exotic);
        }

        return exotic;
    }

    @Override
    public SpriteAPI getSprite() {
        return Global.getSettings().getSprite("exotics", exoticData.getKey());
    }

    @Override
    protected void handleParam(int index, String param, CargoStackAPI stack) {
        switch(Param.get(index)) {
            case EXOTIC_ID:
                modId = param;
                if (ExoticsHandler.EXOTICS.containsKey(modId)) {
                    exotic = ExoticsHandler.EXOTICS.get(modId);
                }
                break;
            case EXOTIC_TYPE:
                if (param.equals("true") || param.equals("false")) {
                    String[] split = stack.getSpecialDataIfSpecial().getData().split(",");
                    String newData = String.format("%s,NORMAL,%s", split[0], split[1]);
                    stack.getSpecialDataIfSpecial().setData(newData); //fix saves
                    this.exoticData = new ExoticData(exotic);
                } else {
                    this.exoticData = new ExoticData(exotic, ExoticType.valueOf(param));
                    break;
                }
            case IGNORE_CRATE:
                ignoreCrate = Boolean.parseBoolean(param);
                break;
        }
    }

    private enum Param {
        EXOTIC_ID,
        EXOTIC_TYPE,
        IGNORE_CRATE;

        private static Param get(int index) {
            return Param.values()[index];
        }
    }
}
