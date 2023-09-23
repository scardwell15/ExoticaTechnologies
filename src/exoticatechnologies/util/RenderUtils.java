package exoticatechnologies.util;

import com.fs.starfarer.api.Global;
import org.magiclib.util.MagicUI;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtils {
    public static LazyFont.DrawableString CAMPAIGN_ITEM_FONT;
    private static final float UIscaling = Global.getSettings().getScreenScaleMult();
    public static final Color INSTALLED_COLOR = Color.white;
    public static final Color CAN_APPLY_COLOR = new Color(90, 90, 90);
    public static final Color CANT_INSTALL_COLOR = new Color(50, 50, 50);


    static {
        try {
            LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/insignia16.fnt");
            CAMPAIGN_ITEM_FONT = fontdraw.createText();
        } catch (FontException ex) {
        }
    }


    public static int getWidth() {
        return (int) Global.getSettings().getScreenWidth();
    }

    public static int getHeight() {
        return (int) Global.getSettings().getScreenHeight();
    }

    public static void pushUIRenderingStack() {
        final int width = getWidth(),
                height = getHeight();
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glViewport(0, 0, width, height);
        glOrtho(0, width, 0, height, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glTranslatef(0.01f, 0.01f, 0);
    }

    public static void popUIRenderingStack() {
        glDisable(GL_BLEND);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glPopAttrib();
    }

    public static void renderBox(float x, float y, float w, float h, Color color, float alpha) {
        x = x * Global.getSettings().getScreenScaleMult();
        y = y * Global.getSettings().getScreenScaleMult();
        w = w * Global.getSettings().getScreenScaleMult();
        h = h * Global.getSettings().getScreenScaleMult();

        GL11.glBegin(7);
        GL11.glColor4f((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, alpha);
        GL11.glVertex2f(x - 1.0F, y - 1.0F);
        GL11.glVertex2f(x + w + 1.0F, y - 1.0F);
        GL11.glVertex2f(x + w + 1.0F, y + h + 1.0F);
        GL11.glVertex2f(x - 1.0F, y + h + 1.0F);
        GL11.glEnd();
    }

    public static Color getAliveUIColor() {
        return Global.getSettings().getColor("textFriendColor");
    }

    public static Color getDeadUIColor() {
        return Global.getSettings().getColor("textNeutralColor");
    }

    public static Color getEnemyUIColor() {
        return Global.getSettings().getColor("textEnemyColor");
    }

    public static Color mergeColors(Color fromColor, Color toColor, float ratio) {
        return mergeColors(fromColor, toColor, ratio, ratio);
    }

    public static Color mergeColors(Color fromColor, Color toColor, float ratio, float alphaRatio) {
        float r = fromColor.getRed() * (1 - ratio) + toColor.getRed() * ratio;
        float g = fromColor.getGreen() * (1 - ratio) + toColor.getGreen() * ratio;
        float b = fromColor.getBlue() * (1 - ratio) + toColor.getBlue() * ratio;
        float a = fromColor.getAlpha() * (1 - alphaRatio) + toColor.getAlpha() * alphaRatio;
        return new Color(r / 255f, g / 255f, b / 255f, a / 255f);
    }

    /**
     * GL11 to start, when you want render text of Lazyfont.
     */
    public static void openGL11ForText() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
        GL11.glOrtho(0.0, Display.getWidth(), 0.0, Display.getHeight(), -1.0, 1.0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * GL11 to close, when you want render text of Lazyfont.
     */
    public static void closeGL11ForText() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    public static void addText(String text, Color textColor, Vector2f screenPos) {
        addText(text, textColor, screenPos, LazyFont.TextAlignment.LEFT);
    }

    public static void addText(String text, Color textColor, Vector2f screenPos, LazyFont.TextAlignment alignment) {
        Color borderCol = textColor == null ? MagicUI.GREENCOLOR : textColor;
        float alpha = borderCol.getAlpha() / 255f;

        Color shadowcolor = new Color(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f, alpha);
        Color color = new Color(borderCol.getRed() / 255f, borderCol.getGreen() / 255f, borderCol.getBlue() / 255f, alpha);

        final Vector2f shadowLoc = new Vector2f(screenPos.getX() + 1f, screenPos.getY() - 1f);
        if (UIscaling != 1) {
            screenPos.scale(UIscaling);
            shadowLoc.scale(UIscaling);
            CAMPAIGN_ITEM_FONT.setFontSize(14 * UIscaling);
        }

        CAMPAIGN_ITEM_FONT.setAlignment(alignment);
        CAMPAIGN_ITEM_FONT.setText(text);
        CAMPAIGN_ITEM_FONT.setColor(shadowcolor);
        CAMPAIGN_ITEM_FONT.draw(shadowLoc);
        CAMPAIGN_ITEM_FONT.setColor(color);
        CAMPAIGN_ITEM_FONT.draw(screenPos);
    }
}
