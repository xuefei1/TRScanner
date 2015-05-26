package trlabs.trscanner.jni;


import android.graphics.Bitmap;

/**
 * C++ interface to Gaussian Blur NDK
 * http://blog.edwards-research.com/2012/04/tutorial-android-jni/
 */
public class ImageBlur {
    public static native void blurIntArray(int[] pImg, int w, int h, int r);

    public static native void blurBitMap(Bitmap bitmap, int r);

    static {
        System.loadLibrary("ImageBlur");
    }

}
