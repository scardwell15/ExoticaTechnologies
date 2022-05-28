package exoticatechnologies.modifications.exotics;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import lombok.extern.log4j.Log4j;

@Log4j
public class GenericExoticItemPlugin extends ExoticSpecialItemPlugin {
    @Override
    public String getName() {
        return String.format("%s - %s", super.getName(), getExotic().getName());
    }

    @Override
    public void render(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemRendererAPI renderer) {
        //you know what to do
        Exotic exotic = this.getExotic();

        SpriteAPI exoticSprite = Global.getSettings().getSprite("exotics", exotic.getKey());

        float tX = -0.0f;
        float tY = -0.03f;
        float tW = 1.1f;
        float tH = 1.1f;

        float mult = 1f;
        exoticSprite.setAlphaMult(alphaMult * mult);
        exoticSprite.setNormalBlend();
        exoticSprite.setSize(tW * exoticSprite.getWidth(), tH * exoticSprite.getHeight());
        exoticSprite.renderRegionAtCenter(x + (1 + tX) * w / 2, y + (1 + tY) * h/2, 0.21f, 0.21f, 0.58f, 0.58f);
    }
}
