package trlabs.trscanner.cameras.graycamera;

import trlabs.trscanner.cameras.rgbcamera.CameraActivity;

import org.opencv.android.JavaCameraView;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by intern2 on 08/12/2014.
 */
public final class Constant {

    private Constant() {};
    public static final double unit_distance = 4000.0;    // pixel distance approximated 1 centimeter
    public static final double max_area_index = 100000;   // 100k pixel area near 8 cm
    public static final double min_area_index = 20000;    // 20k pixel area  near 24 cm
    public static String newLine = System.getProperty("line.separator");
    public static final double BoxThreshSize = 500;
    public static final String LogfileType = "log";
    public static class Extra {
        public static final Size KERNEL_SIZE = new Size(5, 5);
        public static final int SIGMA = 1;

        /* thresholding constants; see opencv docs for more info */
        public static final double THRESHOLD_VALUE = 150;
        public static final double MAX_VALUE = 255;
        public static final int THRESHOLD_TYPE = Imgproc.THRESH_TOZERO + Imgproc.THRESH_OTSU;
        public static final int ADAPTIVE_TYPE = Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
        public static final int THRESHOLD_ADAPTIVE_TYPE = Imgproc.THRESH_BINARY;
        public static final int BLOCK_SIZE = 5;    // pixel neighbor used to calculate threshold value 3, 5, 7, ...
        public static final double THRESHOLD_CONSTANT = 2; // mean over 5x5 windows and substracts 2 from mean
        public static final double CANNY_MAX_THRESHOLD = 150;
        public static final double CANNY_MIN_THRESHOLD = 50;


        public static final double BOX_UPPER_RATIO = 0.15;
        public static final double BOX_LOWER_RATIO = 0.25;

        public static final int OBJECT_CAPTURED = 0x1;
        public static final int DECTION_REQUESTED = 0x2;
        public static final String IMAGE_CAPTURED = "Image Captured";

        public static final int CONTOUR_SIZE = 30;
        public static final int RECT_SIZE = 4;
    }
    public static double getLinearInterpolateDistance (double area) {
         return (170000 - area) / 7000;     // linear inside 10 cm - 20 cm
    }
    public static boolean testValidDistance(double area) {
        return (max_area_index > area && min_area_index < area)?true:false;
    }

}
