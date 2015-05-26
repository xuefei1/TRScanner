package trlabs.trscanner.cameras.graycamera;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;


import org.opencv.android.*;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import trlabs.trscanner.R;
import trlabs.trscanner.cameras.BaseActivity;
import trlabs.trscanner.cameras.graycamera.livecamera.ImageServer;
import trlabs.trscanner.cameras.rgbcamera.CameraActivity;
import trlabs.trscanner.cameras.rgbcamera.PictureMaker;
import trlabs.trscanner.ui.UIHelper;
import trlabs.trscanner.utils.FileTools;


// Listener2 can specified the CamViewFrame to be gray, rgb
public class grayCameraActivity extends BaseActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public enum POSITION {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;
    }

    private static final String LOG_TAG = CameraActivity.class.getSimpleName();
    private JavaCameraView cameraView;

    private double distance = 0.0;
    private static double cameraStartTime = 0.0;
    private static double shootingCounter = 0.0;
    private static double high_thresh_val,  lower_thresh_val;
    private Mat mGray;
    private Mat lines;
    private Mat mRgba;
    private Mat mGrayScale;

    private boolean CaptureState;
    private boolean PrepareShootingState;
    ImageServer image_server;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setContentView(R.layout.graycamera_activity);
            cameraStartTime = System.currentTimeMillis();

            cameraView = (JavaCameraView) findViewById(R.id.camView);
            cameraView.setFocusable(false);
            cameraView.setMaxFrameSize(640, 480);  // frameSize to allow fast processing
            cameraView.setCvCameraViewListener(this);


            image_server = new ImageServer(8080);
            image_server.getIpAddress(getApplicationContext());

            PrepareShootingState = false;
        }

        /***  OPENCV lib callback after successfully load and initialization   ***/
        private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                // TODO Auto-generated method stub
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        cameraView.enableView();
                        Log.i(LOG_TAG, "load success");
                        break;
                    default:
                        super.onManagerConnected(status);
                        Log.i(LOG_TAG, "load failed");
                        break;
                }
            }
        };
        @Override
        public void onPause() {
            super.onPause();
            if (cameraView != null) {
                cameraView.disableView();
            }
            image_server.stop();
            UIHelper.finish(this);
        }
        @Override
        public void onResume() {
            super.onResume();
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);

            try {
                image_server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        @Override
        public void onCameraViewStarted(int width, int height) {
               lines = new Mat();

        }
        @Override
        public void onCameraViewStopped() {
                mGray.release();

        }

        /* processing video stream frames */
        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {


            mGray = inputFrame.gray();
            mRgba = inputFrame.rgba();

            image_server.setImage(inputFrame.rgba());
            //image_server.setText(FileTools.getTimeStamp());
            Processing();
            /*
            if (PrepareShootingState && System.currentTimeMillis() - shootingCounter > 1000 ) { // wait the autofocus

                saveImage(inputFrame.gray(), "nc" + FileTools.getTimeStamp());
                finish();
            }

            if (PrepareShootingState || System.currentTimeMillis() - cameraStartTime < 3000) {
                // skip just to wait until capture image when PrepareShooting state is true
            } else {
                Processing();

            }
            */

                return inputFrame.rgba();
    }


    public void Processing() {
        List<Point> rect_coordinates;    // save the four coordinates
        List<Point> corners;             // sorted order for rect
        List<Point> massCenterCollections;// stored center for each contours
        Point p1, p2, p3, p4;            // save the rect coordinates

                /*
                Mat circles = new Mat();
                List<HoughCirle> houghCircles = new ArrayList<HoughCirle>();
                */
                /*
                apply gaussian blur to smoothen lines of dots
                The function convolves the source image with the specified Gaussian kernel. In-place filtering is supported.
                    src - input image; the image can have any number of channels, which are processed independently, but the depth should be CV_8U, CV_16U, CV_16S, CV_32F or CV_64F.
                    dst - output image of the same size and type as src.
                    ksize - Gaussian kernel size. ksize.width and ksize.height can differ but they both must be positive and odd. Or, they can be zero's and then they are computed from sigma*.
                    sigmaX - Gaussian kernel standard deviation in X direction.
                 */
        Imgproc.GaussianBlur(mGray, mGray, Constant.Extra.KERNEL_SIZE, Constant.Extra.SIGMA);
                /* Applies an adaptive threshold to an array.
                    src – Source 8-bit single-channel image.
                    dst – Destination image of the same size and the same type as src .
                    maxValue – Non-zero value assigned to the pixels for which the condition is satisfied. See the details below.
                    adaptiveMethod – Adaptive thresholding algorithm to use, ADAPTIVE_THRESH_MEAN_C or ADAPTIVE_THRESH_GAUSSIAN_C . See the details below.
                    thresholdType – Thresholding type that must be either THRESH_BINARY or THRESH_BINARY_INV .
                    blockSize – Size of a pixel neighborhood that is used to calculate a threshold value for the pixel: 3, 5, 7, and so on.
                    C – Constant subtracted from the mean or weighted mean (see the details below). Normally, it is positive but may be zero or negative as well.
                 */
        // since we are interested in the border, and they are black, invert the image, then border are white.
        //Core.bitwise_not(mGray, mGray);

                /*
                Imgproc.adaptiveThreshold(mGray,
                        mGray,
                        MAX_VALUE,
                        ADAPTIVE_TYPE,
                        THRESHOLD_ADAPTIVE_TYPE,
                        BLOCK_SIZE,
                        THRESHOLD_CONSTANT);
                */
                /*
                Canny(Mat image, Mat edges, double threshold1, double threshold2) Canny86 algorithm
                - The smallest value between threshold1 and threshold2 is used for edge linking.
                - The largest value is used to find initial segments of strong edges.
                    image - single-channel 8-bit input image.
                    edges - output edge map; it has the same size and type as image.
                    threshold1 - first threshold for the hysteresis procedure.
                    threshold2 - second threshold for the hysteresis procedure.
                 50 100 */
        Imgproc.Canny(mGray, mGray, Constant.Extra.CANNY_MIN_THRESHOLD, Constant.Extra.CANNY_MAX_THRESHOLD);

        // Apply dilation this will bring a lot overhead to sharpen edge, could remove tiny contours
        // with kernel < 0,1,0,1,1,1,0,1,0);
        // Mat kernel = (Mat_<int>(3,3) << 0,1,0,1,1,1,0,1,0 >>
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(5,5));

        Imgproc.dilate(mGray,mGray, kernel);

        //find all the contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        List<Integer> rectContourIdx = new ArrayList<Integer>();

        Mat hierarchy = new Mat();
        //findContours(Mat image, List<MatOfPoint> contours, Mat hierarchy, int mode, int method)
        // retrieve the external contour, this could remove the interior info, filter out unnecessary contours
        Imgproc.findContours(mGray, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours( mGray, contours, -1, new Scalar(255), 0, 8, hierarchy, 1, new Point(0,0));
        Log.i("grayCameraActivity contour size", Integer.toString(contours.size()));


        Core.putText(mGray, "http://" + image_server.getIpAddress(getApplicationContext()) + ":8080/" , new Point(10,10), 1, 0.8, new Scalar(200,200,250));


        if (contours.size() > Constant.Extra.CONTOUR_SIZE || contours.size() == 0) {
            Core.putText(mGray, "please move your camera to the object", new Point(100, 50), 1, 0.8, new Scalar(200, 200, 250));
            return;
        }

        String dump = hierarchy.dump();   // to view this model
        Log.d("graycamera", dump);

        // find contour for circles
        //HoughCircles(Mat image, Mat circles, int method, double dp, double minDist, double param1, double param2, int minRadius, int maxRadius)
            /*    Imgproc.HoughCircles(mGray, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 10, 350, 15, 5, 15);
                for (int i = 0; i < circles.cols(); i++) {
                    double[] circ = circles.get(0, i);
                    houghCircles.add(new HoughCirle(circ));
                    Core.circle(mGray, new Point(circ[0], circ[1]), 10,  new Scalar(255,255,255), 10);
                }
            */

        double tmp_contour_area = 0.0, tmp_contour_max = 0;
        // Core.circle( mGray, new Point(150.0,150.0), 100, new Scalar(0, 255, 0), 5);
        double maxArea = Integer.MIN_VALUE;
        int maxAreaIdx = -1;

        MatOfPoint temp_contour;  //the largest is at the index 0 for starting point
        MatOfPoint2f approxCurve = new MatOfPoint2f();

        rect_coordinates = new ArrayList<Point>(contours.size());

        p1 = new Point(0, 0);
        p2 = new Point(0, 0);
        p3 = new Point(0, 0);
        p4 = new Point(0, 0);


        // list all area filter to find largest contour this part crop out the whole test bed
        for (int idx = 0; idx < contours.size(); idx++) {
            temp_contour = contours.get(idx);
            double ContourArea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (ContourArea > maxArea) {
                //check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f(temp_contour.toArray());
                int contourSize = (int) temp_contour.total();
                MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
                            /* approxPolyDP(MatOfPoint2f curve, MatOfPoint2f approxCurve, double epsilon, boolean closed)
                        approxCurve – Result of the approximation. The type should match the type of the input curve. In case of C interface the approximated curve is stored in the memory storage and pointer to it is returned.
                        epsilon – Parameter specifying the approximation accuracy. This is the maximum distance between the original curve and its approximation.
                        closed – If true, the approximated curve is closed (its first and last vertices are connected). Otherwise, it is not closed.
                         */
                Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize * 0.20, true);

                if (approxCurve_temp.total() == 4 && ContourArea > Constant.BoxThreshSize)
                {   // if number of vertices is 4, then it is rect
                    maxArea = ContourArea;
                    maxAreaIdx = idx;

                    rectContourIdx.add(idx);
                    approxCurve = approxCurve_temp;
                    double[] tmp_double;
                    tmp_double = approxCurve_temp.get(0, 0);
                    p1 = new Point(tmp_double[0], tmp_double[1]);
                    tmp_double = approxCurve_temp.get(1, 0);
                    p2 = new Point(tmp_double[0], tmp_double[1]);
                    tmp_double = approxCurve_temp.get(2, 0);
                    p3 = new Point(tmp_double[0], tmp_double[1]);
                    tmp_double = approxCurve_temp.get(3, 0);
                    p4 = new Point(tmp_double[0], tmp_double[1]);
                }
            }
        }
        if (rectContourIdx.size() == 0) { // no rectangle detected yet
            return;
        }
        distance = maxArea / Constant.unit_distance;

        rect_coordinates.add(p1);
        rect_coordinates.add(p2);
        rect_coordinates.add(p3);
        rect_coordinates.add(p4);

        massCenterCollections =  PictureMaker.getMassCenterCollection(contours);
        corners = PictureMaker.setCorners(rect_coordinates, massCenterCollections.get(maxAreaIdx));
        if (corners == null) {
            return;
        }

        Core.putText(mGray, "     contour size is " + rectContourIdx.size(), new Point(200, 50), 1, 0.8, new Scalar(200, 200, 250));
        Core.putText(mGray, "top left  "  , corners.get(0), 1, 0.8, new Scalar(200, 200, 250));
        Core.putText(mGray, "top right  " , corners.get(1), 1, 0.8, new Scalar(200, 200, 250));
        Core.putText(mGray, "bottom left  " , corners.get(2), 1, 0.8, new Scalar(200, 200, 250));
        Core.putText(mGray, "bottom right  "  ,corners.get(3), 1, 0.8, new Scalar(200, 200, 250));
        Core.putText(mGray, "center", PictureMaker.getMassCenterCollection(contours).get(maxAreaIdx),  1, 0.8, new Scalar(200, 200, 250));





        for(int i = 0; i < rectContourIdx.size(); i++) {
            Imgproc.drawContours(mGray, contours, rectContourIdx.get(i), new Scalar(200, 200, 250), 3);
        }


        Scalar scalar = Core.mean(mRgba);
        Core.putText(mRgba, "RGBA " + Constant.newLine +
                        "R: " + String.format("%.2f", scalar.val[0]) + Constant.newLine +
                        "G: " + String.format("%.2f", scalar.val[1]) + Constant.newLine +
                        "B: " + String.format("%.2f", scalar.val[2]) + Constant.newLine +
                        "A: " + String.format("%.1f", scalar.val[3])
                ,new Point(10, 100), 1, 0.8,
                new Scalar(200, 200, 250));


        //drawBoundingBox(rect_coordinates, contours, maxAreaIdx);
        // draw corners of our rectangle plane.

        Log.i("the detected contour area is: ", Double.toString(maxArea));
        if (Constant.testValidDistance(maxArea)) {
            double dist = Constant.getLinearInterpolateDistance(maxArea);
            Core.putText(mGray, "distance: " + dist + " cm", new Point(50, 50), 1, 0.8, new Scalar(200, 200, 250));
            if (dist < 11.0 && dist > 10.0) {
                PrepareShootingState = true;
                shootingCounter = System.currentTimeMillis();
                ExitMsg();
            }
        } else {
            Core.putText(mGray, "distance: " + "invalid", new Point(50, 50), 1, 0.8, new Scalar(200, 200, 250));
        }



        //Mat startM = Converters.vector_Point2f_to_Mat(rect_coordinates);
        //Mat result= warp(mGray,startM);
            /*

            mGray = inputFrame.gray();
            Mat cannyMat = new Mat();
            mGray.submat(1, mGray.rows()-1, 1, mGray.cols()-1).copyTo(cannyMat);
            Imgproc.Canny(mGray, cannyMat, HYSTERESIS_THRESHOLD1, HYSTERESIS_THRESHOLD2, 3, false);
            Imgproc.HoughLinesP(cannyMat, lines, 1, Math.PI/180, ACCUMULATOR_THRESHOLD, MINLINELENGTH, MAXLINEGAP);

            // draw lines
            for (int x = 0; x < lines.cols() && x < HOUGH_LINE_COUNT; x++) {
                double[] vec = lines.get(0, x);
                if(vec!=null) {
                    double x1 = vec[0],
                            y1 = vec[1],
                            x2 = vec[2],
                            y2 = vec[3];
                    Point start = new Point(x1, y1);
                    Point end = new Point(x2, y2);
                    Core.line(mGray, start, end, new Scalar(255, 0, 0), 3);
                }
            }
            */

        //return inputFrame.gray();
    }



    public void drawBoundingBox(List<Point> rect_coordinates,List<MatOfPoint> contours, int maxAreaIdx ) {
        // drawContours(Mat image, java.util.List<MatOfPoint> contours, int contourIdx, Scalar color, int thickness)
        Imgproc.drawContours(mGray, contours, maxAreaIdx, new Scalar(200, 200, 250), 3);
        if (!rect_coordinates.isEmpty()) {
            for (int i = 1; i <= rect_coordinates.size(); i++) {
                //Core.line(mGray, points.get(i % points.size()), points.get(i % points.size()), new Scalar(255, 255, 255), 10);
                Core.putText(mGray, "p"+ i + " " + rect_coordinates.get(i % rect_coordinates.size()).toString(), rect_coordinates.get(i % rect_coordinates.size()), 1, 0.8,  new Scalar(200,200,250));
            }
            Core.putText(mGray, "http://" + image_server.getIpAddress(getApplicationContext()) + ":8080/" , new Point(100,100), 1, 0.8, new Scalar(200,200,250));

        }
    }


    public void ExitMsg() {
        Intent intent = new Intent();
        setResult(Constant.Extra.OBJECT_CAPTURED, intent);
        finishActivity(Constant.Extra.DECTION_REQUESTED);
        finish();
    }
}
