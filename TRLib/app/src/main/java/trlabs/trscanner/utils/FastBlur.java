package trlabs.trscanner.utils;

import android.graphics.Bitmap;

import trlabs.trscanner.jni.ImageBlur;

/**
 * http://blog.csdn.net/eclipsexys/article/details/39642865
 * blur effect used in camera in/out, and user fragment
 */
public class FastBlur {
    public static Bitmap doBlurJniArray(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);
        //Jni Pixels
        ImageBlur.blurIntArray(pix, w, h, radius);

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }

    public static Bitmap doBlurJniBitMap(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (radius < 1) {
            return (null);
        }
        //Jni BitMap
        ImageBlur.blurBitMap(bitmap, radius);

        return (bitmap);
    }
}
