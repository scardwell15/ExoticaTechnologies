package exoticatechnologies.modifications.upgrades;

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

public class UpgradeSpecialItemPlugin extends ModSpecialItemPlugin {
    @Getter public int upgradeLevel = 0;
    @Getter @Setter protected boolean ignoreCrate = false;
    protected Upgrade upgrade;

    @Override
    public String getName() {
        return String.format("%s - %s (%s)", super.getName(), getUpgrade().getName(), upgradeLevel);
    }

    @Override
    public ModType getType() {
        return ModType.UPGRADE;
    }

    @Override
    public SpriteAPI getSprite() {
        return Global.getSettings().getSprite("upgrades", upgrade.getKey());
    }

    public CargoStackAPI getStack() {
        return stack;
    }

    public final Upgrade getUpgrade() {
        if (upgrade == null) {
            upgrade = UpgradesHandler.UPGRADES.get(modId);

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

    @Override
    protected void handleParam(int index, String param, CargoStackAPI stack) {
        switch(Param.get(index)) {
            case UPGRADE_ID:
                modId = param;
                if (UpgradesHandler.UPGRADES.containsKey(modId)) {
                    upgrade = UpgradesHandler.UPGRADES.get(modId);
                }
                break;
            case UPGRADE_LEVEL:
                upgradeLevel = Integer.parseInt(param);
                break;
            case IGNORE_CRATE:
                ignoreCrate = Boolean.parseBoolean(param);
                break;
        }
    }

    private enum Param {
        UPGRADE_ID,
        UPGRADE_LEVEL,
        IGNORE_CRATE;

        private static Param get(int index) {
            return Param.values()[index];
        }
    }
}
