package trlabs.trscanner.cameras.rgbcamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;


import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.utils.Converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import trlabs.trscanner.cameras.graycamera.Constant;
import trlabs.trscanner.trtabs.config.GlobalConsts;
import trlabs.trscanner.utils.BitmapHelper;
import trlabs.trscanner.utils.FileTools;


public class PictureMaker {
    protected String path;
    protected float rotation = 0;

    public PictureMaker(String path, int rotation, boolean isFront) {
        this.path = path;
        this.rotation = (float) rotation;
    }

    // RGB2Gray conversion using opencv and rotate between landscape and portrait to fix image rotated problem
    public static void procSrc2Gray(Bitmap srcBitmap, Bitmap grayBitmap) {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        //srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nanhuaijin);
        //grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B.
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat
        Utils.matToBitmap(grayMat, grayBitmap); //convert mat to bitmap

        Log.i("", "procSrc2Gray success...");
    }

    public static Mat Bitmap2Mat(Bitmap bitmap) {
        Mat mat = new Mat();
        /*
        Converts Android Bitmap to OpenCV Mat.
        This function converts an Android Bitmap image to the OpenCV Mat.
        'ARGB_8888' and 'RGB_565' input Bitmap formats are supported.
        @bmp - is a valid input Bitmap object of the type 'ARGB_8888' or 'RGB_565'.
        @mat - is a valid output Mat object, it will be reallocated if needed, so it may be empty.
        @unPremultiplyAlpha - is a flag, that determines, whether the bitmap needs to be converted from alpha premultiplied format (like Android keeps 'ARGB_8888' ones) to regular one; this flag is ignored for 'RGB_565' bitmaps.
        */
        Utils.bitmapToMat(bitmap, mat);
        return mat;     //output matrix is same size of input bitmap and in CV_8UC4 format
    }

    public static Bitmap Mat2Bitmap(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    public static Mat RetrieveObject(Mat mRGB) {
        //Mat mGrayCopy = new Mat();
        //mRGB.copyTo(mGrayCopy);
        Mat mRGBCopy = mRGB.clone();
        List<Point> corners;             // sorted order for rect
        List<Point> massCenterCollections;// stored center for each contours
        Point p1, p2, p3, p4;

        Point TopLeft, TopRight, BottomLeft, BottomRight;
        Imgproc.GaussianBlur(mRGB, mRGB, Constant.Extra.KERNEL_SIZE, Constant.Extra.SIGMA);
        Imgproc.Canny(mRGB, mRGB, Constant.Extra.CANNY_MIN_THRESHOLD, Constant.Extra.CANNY_MAX_THRESHOLD);
        // the morph size too big might bring the dots into line
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3));
        Imgproc.dilate(mRGB, mRGB, kernel);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
        List<List<Point>> largest_contours_point = new ArrayList<List<Point>>();

        Mat hierarchy = new Mat();
        Imgproc.findContours(mRGB, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = Integer.MIN_VALUE;
        int maxAreaIdx = -1;
        MatOfPoint temp_contour;
        MatOfPoint2f approxCurve = new MatOfPoint2f();

        p1 = new Point(0, 0);
        p2 = new Point(0, 0);
        p3 = new Point(0, 0);
        p4 = new Point(0, 0);

        // list all area filter to find largest contour
        for (int idx = 0; idx < contours.size(); idx++) {
            temp_contour = contours.get(idx);
            double ContourArea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (ContourArea > maxArea) {
                //check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f(temp_contour.toArray());
                int contourSize = (int) temp_contour.total();
                MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
                Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize * 0.20, true);

                if (approxCurve_temp.total() == 4
                        && ContourArea > Constant.BoxThreshSize
                    //&& Imgproc.isContourConvex(contours.get(idx)))
                        ) {
                    maxArea = ContourArea;
                    double[] tmp_double;
                    tmp_double = approxCurve_temp.get(0, 0);
                    p1 = new Point(tmp_double[0], tmp_double[1]);
                    tmp_double = approxCurve_temp.get(1, 0);
                    p2 = new Point(tmp_double[0], tmp_double[1]);
                    tmp_double = approxCurve_temp.get(2, 0);
                    p3 = new Point(tmp_double[0], tmp_double[1]);
                    tmp_double = approxCurve_temp.get(3, 0);
                    p4 = new Point(tmp_double[0], tmp_double[1]);
                    //maxAreaIdx = idx;
                    //approxCurve = approxCurve_temp;
                    largest_contours.add(contours.get(idx));
                    largest_contours_point.add(Arrays.asList(
                            p1, p2, p3, p4
                    ));
                }
            }
        }
        if (largest_contours.isEmpty()) return null;

        massCenterCollections = PictureMaker.getMassCenterCollection(largest_contours);
        // get the buffered rectangle when only two big rectangles presents
        // here we directly crop the small rectangle
        if (largest_contours.size() == 2) {
            int idx = (Imgproc.contourArea(largest_contours.get(0)) >
                    Imgproc.contourArea(largest_contours.get(1))) ? 1 : 0;

            corners = PictureMaker.setCorners(largest_contours_point.get(idx),
                    massCenterCollections.get(idx));


            //for(int i = 0; i < largest_contours.size(); i++) {
            //   Imgproc.drawContours(mGrayCopy, contours, i, new Scalar(200, 200, 250), 3);
            // }

            // rect_coordinates should be topLeft, topRight, BottomLeft, BottomRight
            Mat result = warp(mRGBCopy, crop(corners));        // normalized image
            Mat transposeResult = result.clone();
            Core.transpose(result, transposeResult);
            //this time we determine its corners again since Opencv does not provides the result mapping coordinates or corners before
            File folder = FileTools.createFolder(GlobalConsts.ROOT_PATH);
            Matrix matrix = new Matrix();   // rotation matrix
            matrix.postRotate(90);

            Mat detectArea = transposeResult.submat(200, 600, 530, 630);   // x and y are reversed remember !
            Mat bufferArea = transposeResult.submat(200, 600, 1400, 1500);

            tagRGBtoImage(detectArea, 0.8);  // tag RGB values to image matrix
            tagRGBtoImage(bufferArea, 0.8);

            Bitmap detectBitmap = Mat2Bitmap(detectArea);
            Bitmap bufferBitmap = Mat2Bitmap(bufferArea);
            detectBitmap = Bitmap.createBitmap(detectBitmap, 0, 0, detectBitmap.getWidth(), detectBitmap.getHeight(), matrix, true);
            bufferBitmap = Bitmap.createBitmap(bufferBitmap, 0, 0, bufferBitmap.getWidth(), bufferBitmap.getHeight(), matrix, true);

            BitmapHelper.saveImage(new File(folder.getAbsolutePath() + File.separator + "detectArea.jpg"),
                    detectBitmap);
            BitmapHelper.saveImage(new File(folder.getAbsolutePath() + File.separator + "bufferArea.jpg"),
                    bufferBitmap);

            ARGBMat2File(detectArea, folder.getAbsolutePath(), "detect");
            ARGBMat2File(bufferArea, folder.getAbsolutePath(), "buffer");

            OutputLogfile(folder.getAbsolutePath(), detectArea, bufferArea);
            //BitmapHelper.saveImage(new File(folder.getAbsolutePath() + File.separator + "original.jpg"),
            //      Mat2Bitmap(mRGBCopy) );

            return result;
        }

        return null;
        //return result;
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

    public static Mat crop(List<Point> rect_coordinates) {
        return Converters.vector_Point2f_to_Mat(rect_coordinates);
    }

    public static Mat warp(Mat inputMat, Mat startM) {
        int resultWidth = 800;
        int resultHeight = 1600;   // 5/12 ratio

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);  // 8bits 4 channels

        Point ocvPOut3 = new Point(resultWidth, resultHeight);
        Point ocvPOut4 = new Point(resultWidth, 0);
        Point ocvPOut1 = new Point(0, resultHeight);
        Point ocvPOut2 = new Point(0, 0);

        // mapping to target rectangle
        List<Point> target = new ArrayList<Point>();
        target.add(ocvPOut1);
        target.add(ocvPOut2);
        target.add(ocvPOut3);
        target.add(ocvPOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(target);
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat,
                outputMat,
                perspectiveTransform,
                new Size(resultWidth, resultHeight),
                Imgproc.INTER_CUBIC);

        //"http://docs.opencv.org/modules/core/doc/operations_on_arrays.html#mean"
        tagRGBtoImage(outputMat, 2.0);

        return outputMat;
    }

    // compute the center mass for each contour used for coordinates location
    public static List<Point> getMassCenterCollection(List<MatOfPoint> contours) {
        List<Point> centerCollection = new ArrayList<Point>();
        List<Moments> moments = new ArrayList<Moments>(contours.size());
        for (int idx = 0; idx < contours.size(); idx++) {
            //* @param array Raster image (single-channel, 8-bit or floating-point 2D array)
            //* or an array (<em>1 x N</em> or <em>N x 1</em>) of 2D points (<code>Point</code>
            //* or <code>Point2f</code>).
            //* @param binaryImage If it is true, all non-zero image pixels are treated as
            //      * 1's. The parameter is used for images only.
            moments.add(idx, Imgproc.moments(contours.get(idx), false));
            Moments p = moments.get(idx);
            int x = (int) (p.get_m10() / p.get_m00());
            int y = (int) (p.get_m01() / p.get_m00());

            centerCollection.add(new Point(x, y));
            // Core.circle(mGray, new Point(x,y), 4, new Scalar(255, 49, 0, 255));
        }
        return centerCollection;
    }

    // after obtain the largest rectangle contour, we have to label its corner in order
    // * corner[0] = topleft, corner[1] = topright, corner[2] = bottomleft, corner[3] = bottomright
    public static List<Point> setCorners(List<Point> rect_coordinates, Point center) {
        Point tl, tr, bl, br;
        List<Point> top, bottom, corners;
        corners = new ArrayList<Point>();
        top = new ArrayList<Point>();
        bottom = new ArrayList<Point>();

        for (int i = 0; i < rect_coordinates.size(); i++) {
            if (rect_coordinates.get(i).x < center.x) {
                top.add(rect_coordinates.get(i));
            } else {
                bottom.add(rect_coordinates.get(i));
            }
        }
        if (top.size() != 2 || bottom.size() != 2) {
            return null;    // bad tilted angle, re-posture please
        }

        tl = top.get(0).y > top.get(1).y ? top.get(0) : top.get(1);
        tr = top.get(0).y > top.get(1).y ? top.get(1) : top.get(0);
        bl = bottom.get(0).y < bottom.get(1).y ? bottom.get(1) : bottom.get(0);
        br = bottom.get(0).y < bottom.get(1).y ? bottom.get(0) : bottom.get(1);


        corners.clear();
        corners.add(tl);
        corners.add(tr);
        corners.add(bl);
        corners.add(br);
        return corners;
    }

    public static boolean ARGBMat2File(Mat src, String path, String name) {
        List<Mat> channels = new ArrayList<Mat>(4);  // create 4 channels
        Core.split(src, channels);     // split image into different channels
        //Mat matCopy = Mat.zeros(new Size(src.cols(), src.rows()),  CvType.CV_8UC1 ); // this is used to merge the empty channel
        File redMat = new File(path + File.separator + name + "red.csv");
        File greenMat = new File(path + File.separator + name + "green.csv");
        File blueMat = new File(path + File.separator + name + "blue.csv");

        CvMatToCSV(channels.get(2), redMat);
        CvMatToCSV(channels.get(1), greenMat);
        CvMatToCSV(channels.get(0), blueMat);

        // merge for testing purpose
            /*
            List<Mat> Redchannels = new ArrayList<Mat>();
            Mat finalImg = new Mat();
            Redchannels.add(matCopy);
            Redchannels.add(matCopy);
            Redchannels.add(channels.get(2));  // access reverse order as ARGB
            Core.merge(Redchannels, finalImg);
            */
        return true;
    }

    public static boolean tagRGBtoImage(Mat src, double fontSize) {
        Scalar scalar = getRGBfromMat(src);
        Core.putText(src, "R: " + String.format("%.2f", scalar.val[0]),
                new Point(10, 100), 1, fontSize, new Scalar(200, 200, 250));
        Core.putText(src, "G: " + String.format("%.2f", scalar.val[1]),
                new Point(10, 130), 1, fontSize, new Scalar(200, 200, 250));
        Core.putText(src, "B: " + String.format("%.2f", scalar.val[2]),
                new Point(10, 160), 1, fontSize, new Scalar(200, 200, 250));
        return true;
    }

    public static Scalar getRGBfromMat(Mat src) {
        return Core.mean(src);
    }

    public static void CvMatToCSV(Mat src, File csv) {
        Size size = src.size();
        byte[] a = new byte[(int) (src.rows() * src.cols())];
        src.get(0, 0, a);//I get byte array here for the whole image
        FileOutputStream fos_g = null;
        OutputStreamWriter ow = null;
        BufferedWriter fwriter = null;
        try {
            fos_g = new FileOutputStream(csv);
            ow = new OutputStreamWriter(fos_g);
            fwriter = new BufferedWriter(ow);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            for (int y = 0; y < size.width; y++) {
                for (int x = 0; x < size.height; x++) {
                    fwriter.write(String.valueOf(a[(int) (y * size.width + x)]));
                    ow.flush();
                    fwriter.write(",");
                    ow.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

            try {
                fos_g.flush();
                fos_g.close();
            } catch (IOException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }

        }
    }

    public static void OutputLogfile(String savePath, Mat detectsrc, Mat buffersrc) {
        Scalar bufferscalar = getRGBfromMat(buffersrc);
        Scalar detectscalar = getRGBfromMat(detectsrc);

        try {
            String filename = FileTools.getTimeStamp() + "." + Constant.LogfileType;
            PrintWriter txtFile = new PrintWriter(new FileWriter(savePath + File.separator + filename));

            txtFile.println("Log: " + FileTools.getTimeStamp() + Constant.newLine +
                    "Detect area" + Constant.newLine +
                    "R: " + String.format("%.2f", bufferscalar.val[0]) + Constant.newLine +
                    "G: " + String.format("%.2f", bufferscalar.val[1]) + Constant.newLine +
                    "B: " + String.format("%.2f", bufferscalar.val[2]) + Constant.newLine +
                    "Buffer area" + Constant.newLine +
                    "R: " + String.format("%.2f", detectscalar.val[0]) + Constant.newLine +
                    "G: " + String.format("%.2f", detectscalar.val[1]) + Constant.newLine +
                    "B: " + String.format("%.2f", detectscalar.val[2]));

            txtFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidthPx, int reqHeightPx) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeightPx || width > reqWidthPx) {
            final int calcHeight = height;
            final int calcWidth = width;

            while ((calcHeight / inSampleSize) > reqHeightPx
                    || (calcWidth / inSampleSize) > reqWidthPx) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public boolean make(byte[] data, int picSizeScale, int quality,
                        Bitmap.CompressFormat format) {
        final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = picSizeScale;

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                    bitmapOptions);

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width,
                    height, matrix, true);
            rotatedBitmap.compress(format, quality, fos);
            fos.close();

            if (bitmap != null) {
                bitmap.recycle();
            }

            if (rotatedBitmap != null) {
                rotatedBitmap.recycle();
            }

            return isPictureMaked();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean make(byte[] data, BitmapFactory.Options bitmapOptions,
                        Options options) {
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                    bitmapOptions);
            FileOutputStream fos = new FileOutputStream(path);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                    matrix, true);
            rotatedBitmap.compress(options.getPictureType(),
                    options.getQuality(), fos);
            fos.close();

            if (bitmap != null) {
                bitmap.recycle();
            }

            if (rotatedBitmap != null) {
                rotatedBitmap.recycle();
            }
        } catch (Exception e) {
            return false;
        }

        return isPictureMaked();
    }

    public boolean simpleMake(byte[] data) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(path);
            fos.write(data);
            fos.close();
            return isPictureMaked();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPictureMaked() {
        if (path == null) {
            return false;
        }

        return new File(path).exists();
    }

    public double getDistanceOfPoints(Point p1, Point p2) {
        double diff_x = p1.x - p2.x;
        double diff_y = p1.y - p2.y;
        return Math.sqrt(diff_x * diff_x + diff_y * diff_y);
    }

        /*
        public static int getRed(int pixel){
            return (pixel >> 16) & 0xff;
        }

        public static int getGreen(int pixel){
            return (pixel >> 8) & 0xff;
        }
        public static int getBlue(int pixel){
            return (pixel) & 0xff;
        }*/
}

