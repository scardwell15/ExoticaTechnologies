package exoticatechnologies.util;

import com.fs.starfarer.api.Global;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

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
}
