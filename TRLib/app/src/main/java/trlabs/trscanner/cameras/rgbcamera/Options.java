package trlabs.trscanner.cameras.rgbcamera;

import android.graphics.Bitmap;

/**
 * Created by intern2 on 20/11/2014.
 */
public class Options {
    private int quality = 100;
    private PictureSize pictureSize = new PictureSize();
    private PictureType pictureType = PictureType.JPEG;
    private Bitmap.Config config = Bitmap.Config.RGB_565;
    private int calculateScale = 1;
    private boolean useCalculateScale = false;
    private boolean reStartPreviewAfterShutter = true;
    private boolean execAutoFocusWhenShutter = false;

    private static boolean isJPEG(byte[] b) {
        if (b.length < 2) {
            return false;
        }
        return (b[0] == (byte) 0xFF) && (b[1] == (byte) 0xD8);
    }

    private static boolean isGIF(byte[] b) {
        if (b.length < 6) {
            return false;
        }
        return b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
    }

    private static boolean isPNG(byte[] b) {
        if (b.length < 8) {
            return false;
        }
        return (b[0] == (byte) 137 && b[1] == (byte) 80 && b[2] == (byte) 78
                && b[3] == (byte) 71 && b[4] == (byte) 13 && b[5] == (byte) 10
                && b[6] == (byte) 26 && b[7] == (byte) 10);
    }

    private static boolean isBMP(byte[] b) {
        if (b.length < 2) {
            return false;
        }
        return (b[0] == 0x42) && (b[1] == 0x4d);
    }

    public void setPictureSize(PictureSize size) {
        this.pictureSize = size;
    }

    public int getPictureWidth() {
        return this.pictureSize.width;
    }

    public int getPictureHeight() {
        return this.pictureSize.height;
    }

    public int getCalculateScale() {
        return this.calculateScale;
    }

    public void setCalculateScale(int scale) {
        this.calculateScale = scale;
        this.useCalculateScale = true;
    }

    public Bitmap.CompressFormat getPictureType() {
        return pictureType.toFormat();
    }

    public void setPictureType(PictureType type) {
        this.pictureType = type;
        this.useCalculateScale = false;
    }

    public Bitmap.Config getPictureConfig() {
        return this.config;
    }

    public void setPictureConfig(Bitmap.Config config) {
        this.config = config;
    }

    public PictureType getType() {
        return this.pictureType;
    }

    public int getQuality() {
        return this.quality;
    }

    public void setQuality(int val) {
        this.quality = val;
    }

    public boolean isUseCalculateScale() {
        return this.useCalculateScale;
    }

    public boolean isRestartPreviewAfterShutter() {
        return this.reStartPreviewAfterShutter;
    }

    public void setRestartPreviewAfterShutter(boolean restart) {
        this.reStartPreviewAfterShutter = restart;
    }

    public boolean isExecAutoFocusWhenShutter() {
        return this.execAutoFocusWhenShutter;
    }

    public void setExecAutoFocusWhenShutter(boolean shutter) {
        this.execAutoFocusWhenShutter = shutter;
    }
}
