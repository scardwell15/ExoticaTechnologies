package exoticatechnologies.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.opengl.ColorUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.glBindBuffer;

public class RenderUtils {
    public static int getWidth() {
        return (int) (Display.getWidth() * Display.getPixelScaleFactor());
    }

    public static int getHeight() {
        return (int) (Display.getHeight() * Display.getPixelScaleFactor());
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
        float r = fromColor.getRed() * (1 - ratio) + toColor.getRed() * ratio;
        float g = fromColor.getGreen() * (1 - ratio) + toColor.getGreen() * ratio;
        float b = fromColor.getBlue() * (1 - ratio) + toColor.getBlue() * ratio;
        float a = fromColor.getAlpha() * (1 - ratio) + toColor.getAlpha() * ratio;
        return new Color(r / 255f, g / 255f, b / 255f, a / 255f);
    }
}
