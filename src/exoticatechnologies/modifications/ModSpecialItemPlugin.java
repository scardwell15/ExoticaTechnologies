package exoticatechnologies.modifications;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticData;
import exoticatechnologies.modifications.exotics.ExoticType;
import exoticatechnologies.modifications.exotics.ExoticsHandler;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

public abstract class ModSpecialItemPlugin extends BaseSpecialItemPlugin {
    @Getter
    public String modId;

    @Getter @Setter
    protected boolean ignoreCrate = false;

    public abstract ModType getType();

    public Modification getMod() {
        if (ModType.UPGRADE.equals(getType())) {
            return UpgradesHandler.UPGRADES.get(modId);
        } else {
            return ExoticsHandler.EXOTICS.get(modId);
        }
    }

    public abstract SpriteAPI getSprite();

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
        }
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

        if(this.getMod() != null) {
            tooltip.addPara(this.getMod().getDescription(), Misc.getTextColor(), opad);
        }
    }

    @Override
    public void render(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemRendererAPI renderer) {
        SpriteAPI upgradeSprite = getSprite();

        float tX = -0.045f;
        float tY = -0.0f;
        float tW = upgradeSprite.getWidth() * 0.54f;
        float tH = upgradeSprite.getHeight() * 0.54f;

        float mult = 1f;
        upgradeSprite.setAlphaMult(alphaMult * mult);
        upgradeSprite.setNormalBlend();
        upgradeSprite.renderRegionAtCenter(x + (1 + tX) * w / 2, y + (1 + tY) * h/2, 0.22f, 0.21f, 0.565f, 0.56f);
    }


    protected abstract void handleParam(int index, String param, CargoStackAPI stack);

    public boolean shouldRemoveOnRightClickAction() {
        return false;
    }

    public enum ModType {
        UPGRADE,
        EXOTIC
    }
}
