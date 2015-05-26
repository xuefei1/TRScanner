package trlabs.trscanner.cameras.rgbcamera;

import android.graphics.Bitmap;


public enum PictureType {
    JPEG, PNG;

    public Bitmap.CompressFormat toFormat() {
        switch (this) {
            case JPEG:
                return Bitmap.CompressFormat.JPEG;
            case PNG:
                return Bitmap.CompressFormat.PNG;
            default:
                break;
        }
        return null;
    }
}