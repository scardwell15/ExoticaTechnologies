package exoticatechnologies.modifications.exotics;

import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

public class ExoticSpecialItemPlugin extends BaseSpecialItemPlugin {

    @Getter
    public String exoticId;
    @Getter @Setter
    protected boolean ignoreCrate = false;

    protected Exotic exotic;
    protected ExoticData exoticData;

    @Override
    public void init(CargoStackAPI stack) {
        super.init(stack);

        String passedParams = stack.getSpecialDataIfSpecial().getData();
        if (passedParams == null) {
            passedParams = spec.getParams();
        }
        if (!passedParams.isEmpty()) {
            String[] paramsArray = passedParams.split(",");
            for (int i = 0; i < paramsArray.length; i++) {
                String param = paramsArray[i];
                param = param.trim();
                handleParam(i, param, stack);
            }

            if (paramsArray.length == 1) {
                this.exoticData = new ExoticData(exotic);
            }
        }
    }

    public final Exotic getExotic() {
        if (exotic == null) {
            exotic = ExoticsHandler.EXOTICS.get(exoticId);

            if (exotic == null) {
                exotic = ExoticsHandler.EXOTICS.get("DriveFluxVent");
            }
        }

        if (exoticData == null) {
            exoticData = new ExoticData(exotic);
        }

        return exotic;
    }

    public CargoStackAPI getStack() {
        return stack;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource, boolean useGray) {
        float opad = 10.0F;
        tooltip.addTitle(this.getName());
        String design = this.getDesignType();
        Misc.addDesignTypePara(tooltip, design, opad);
        if (!this.spec.getDesc().isEmpty()) {
            Color c = Misc.getTextColor();
            if (useGray) {
                c = Misc.getGrayColor();
            }

            tooltip.addPara(this.spec.getDesc(), c, opad);
        }

        if(this.getExotic() != null) {
            tooltip.addPara(this.getExotic().getTooltip(), Misc.getTextColor(), opad);
        }
    }

    @Override
    public void render(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemRendererAPI renderer) {
        //you know what to do
    }

    private void handleParam(int index, String param, CargoStackAPI stack) {
        switch(Param.get(index)) {
            case EXOTIC_ID:
                exoticId = param;
                if (ExoticsHandler.EXOTICS.containsKey(exoticId)) {
                    exotic = ExoticsHandler.EXOTICS.get(exoticId);
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

    public boolean shouldRemoveOnRightClickAction() {
        return false;
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
