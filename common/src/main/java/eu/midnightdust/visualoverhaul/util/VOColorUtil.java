package eu.midnightdust.visualoverhaul.util;

import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class VOColorUtil {
    public static int convertRgbToArgb(int rgb, int alpha) {
        int red = 0xFF & (rgb >> 16);
        int green = 0xFF & (rgb >> 8);
        int blue = 0xFF & (rgb);

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
    public static int alphaAndBrightness(float alpha, float brightness) {
        return ARGB.color(Mth.floor(alpha*255), Mth.floor(brightness*255), Mth.floor(brightness*255), Mth.floor(brightness*255));
    }
}
