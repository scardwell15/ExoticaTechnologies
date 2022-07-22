package exoticatechnologies.modifications.upgrades;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class UpgradeSpecialItemPlugin extends BaseSpecialItemPlugin {

    protected String upgradeId;
    protected int upgradeLevel = 0;
    protected Upgrade upgrade;

    @Override
    public String getName() {
        return String.format("%s - %s (%s)", super.getName(), getUpgrade().getName(), upgradeLevel);
    }

    @Override
    public void render(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemRendererAPI renderer) {
        //you know what to do
        Upgrade upgrade = this.getUpgrade();

        SpriteAPI upgradeSprite = Global.getSettings().getSprite("upgrades", upgrade.getKey());

        float tX = -0.045f;
        float tY = -0.0f;
        float tW = upgradeSprite.getWidth() * 0.54f;
        float tH = upgradeSprite.getHeight() * 0.54f;

        float mult = 1f;
        upgradeSprite.setAlphaMult(alphaMult * mult);
        upgradeSprite.setNormalBlend();
        upgradeSprite.renderRegionAtCenter(x + (1 + tX) * w / 2, y + (1 + tY) * h/2, 0.22f, 0.21f, 0.565f, 0.56f);
    }

    public String getUpgradeId() {
        return upgradeId;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

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
                handleParam(i, param);
            }
        }
    }

    public final Upgrade getUpgrade() {
        if (upgrade == null) {
            upgrade = UpgradesHandler.UPGRADES.get(upgradeId);

            if (upgrade == null) {
                upgrade = UpgradesHandler.UPGRADES.get("WeldedArmor");
            }
        }
        return upgrade;
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

        if(this.getUpgrade() != null) {
            tooltip.addPara(this.getUpgrade().getDescription(), Misc.getTextColor(), opad);
        }
    }

    private void handleParam(int index, String param) {
        switch(Param.get(index)) {
            case UPGRADE_ID: {
                upgradeId = param;
                if (UpgradesHandler.UPGRADES.containsKey(upgradeId)) {
                    upgrade = UpgradesHandler.UPGRADES.get(upgradeId);
                }
                break;
            }
            case UPGRADE_LEVEL: {
                upgradeLevel = Integer.valueOf(param);
                break;
            }
        }
    }

    public boolean shouldRemoveOnRightClickAction() {
        return false;
    }

    private enum Param {
        UPGRADE_ID,
        UPGRADE_LEVEL;

        private static Param get(int index) {
            return Param.values()[index];
        }
    }
}
