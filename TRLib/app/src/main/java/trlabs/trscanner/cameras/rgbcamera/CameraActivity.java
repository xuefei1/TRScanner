package trlabs.trscanner.cameras.rgbcamera;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import trlabs.trscanner.R;
import trlabs.trscanner.cameras.graycamera.Constant;
import trlabs.trscanner.cameras.graycamera.grayCameraActivity;
import trlabs.trscanner.trtabs.File.FileCategoryHelper;
import trlabs.trscanner.trtabs.config.GlobalConsts;
import trlabs.trscanner.ui.UIHelper;
import trlabs.trscanner.utils.FileTools;
import trlabs.trscanner.utils.BitmapHelper;
import trlabs.trscanner.utils.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
//------------------- opencv ------------------------------\\
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
//----------------------------------------------------------\\

// preview callback is used to deliver copies of preview frames as they are displayed
public class CameraActivity extends Activity implements Camera.PreviewCallback {
    protected static final String EXTRA_IMAGE_PATH = "com.example.intern2.trscanner.EXTRA_IMAGE_PATH";
    static final boolean LOG = false;     // activate printLine debug
    private static final String LOG_TAG = CameraActivity.class.getSimpleName();
    private static final String TAG = "ScannerActivity";
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        // loader for opencv
        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "cv load success");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "cv load failure");
                    break;
            }
        }
    };
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        // don't do anything here, but we need to implement the callback to get the shutter sound (at least on Galaxy Nexus and Nexus 7)
        public void onShutter() {
            Log.d(TAG, "s hutterCallback.onShutter()");
        }
    };
    /* define callback function rawCallback and jpegCallback for takePicture */
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw with data = " + ((data != null) ? data.length : " NULL"));
        }
    };
    /* processing image data, save it as jpeg using savePictureToFileSystem and setResult methods */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        /* called when image is available after picture is taken,
            format depends on context of callback and camera.parameters setting
         data: a byte array of picture data */
        public void onPictureTaken(byte[] data, Camera camera) {

            String path = savePictureToFileSystem(data);
            Log.d("jpegCallback", "saved picture to file directory: " + path);
            setResult(path);


            try {
                mCamera.stopPreview();  // call stopPreview recall surfacecreated, otherwise the lens is zoomed-in, I do not know why
                mCamera.startPreview();
                //Remove notifications after 3 seconds
                //Thread.sleep(2000);
            } catch (Exception e) {
                Log.d(TAG, "Error starting preview: " + e.toString());
            }
        }
    };
    private static final double stabilityIndex = 0.20;   // force threshold
    private final float mSENSOR_DELAY = 0.02f;   // 0.02 seconds at SENSOR_DELAY_GAME
    TextView tvAccel, tvGyro, tvSaved, tvCount, tvSaveConfirm;
    SensorManager mSensorManager;
    SensorEventListener mSensorListener;
    Sensor mAccel, mGyro;
    double force, gyroIndex;
    Boolean toggleButtonState = false;
    private CameraPreview mPreview;
    private Camera mCamera;
    private Handler mAutoFocusHandler;
    private boolean mPreviewing = true;
    private Bitmap bitmap;
    private boolean isTouched = false;
    private ToggleButton mToggleButton;
    private UIMode uimode = UIMode.USER_MODE;
    private Button mButton;     // gallery
    private Button mButton1;    // grayscale
    private boolean image_captured;
    private boolean image_saved;
    private List<Float> sensor_x = new ArrayList<Float>();
    private List<Float> sensor_y = new ArrayList<Float>();
    private List<Float> sensor_z = new ArrayList<Float>();
    private float AcceInit_X = 0.0f, AcceInit_Y = 0.0f, AcceInit_Z = 0.0f;
    private boolean frame_start = true;
    /** OnCreate - end **/

    public static boolean saveToFile(byte[] bytes, File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inDither = false; // Disable Dithering mode
        //options.inPurgeable = true;
        //options.inInputShareable = true;

        // RGB_565: 5 bits Red, 6:Green, 5: blue; each pixel is stored on 2 bytes. slight artifact
        // ARGB_8888 2 byte each pixel: best quality should use whenever possible
        //  ALPHA_8, ARGB_4444, ARGB_8888, RGB_565;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap srcBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        //Bitmap grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        //PictureMaker.procSrc2Gray(srcBitmap, grayBitmap);

        //Log.i("savetofile now graybitmap size is", "height " + grayBitmap.getHeight() + ": width " + grayBitmap.getWidth());

        Matrix matrix = new Matrix();   // rotation matrix
        matrix.postRotate(90);

        //grayBitmap = Bitmap.createBitmap(grayBitmap,0, 0, grayBitmap.getWidth(), grayBitmap.getHeight(), matrix, true);
        srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);

        //Mat mGray = PictureMaker.Bitmap2Mat(grayBitmap);

        Mat mSource = PictureMaker.Bitmap2Mat(srcBitmap); // only RGB_565, ARGB_8888 are supported


        Bitmap resultBitmap = PictureMaker.Mat2Bitmap(PictureMaker.RetrieveObject(mSource));

        // deal with null point exception here


        return BitmapHelper.saveImage(file, resultBitmap);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case Constant.Extra.DECTION_REQUESTED:
                if (resultCode == Constant.Extra.OBJECT_CAPTURED) {
                    image_captured = true;
                }
                break;
            default:
                break;
        }
    }

    /* create view for Camera Activity, UI view and Preview added */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isCameraAvailable()) {
            // Cancel request if there is no rear-facing camera.
            cancelRequest();
            return;
        }
        image_captured = false;
        image_saved = false;

        // Hide the window title and display in full screen and disabled sleep
        requestWindowFeature(Window.FEATURE_NO_TITLE);     // hide title
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //ToastUtil.showToast(getApplicationContext(), "welcome back " + setting.getUserName(getApplicationContext()));

        mAutoFocusHandler = new Handler();
        ToastUtil.showToast(getApplicationContext(), uimode.toString());
        initializeSensorView();   // added textview for values reading
        LoadUILayout();

        // Create and configure the ImageScanner, this function can be used in later image processing


        /*
            onTouchEvent detects the user touch location it is used to control the UI display
            The purpose is to switch between detection mode and setting mode
        */
        this.mPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isTouched = (isTouched) ? false : true;
                if (isTouched) {
                    mToggleButton.setVisibility(View.INVISIBLE);
                    mButton.setVisibility(View.INVISIBLE);
                } else {
                    mToggleButton.setVisibility(View.VISIBLE);
                    mButton.setVisibility(View.VISIBLE);
                }
                uimode = (uimode == UIMode.USER_MODE) ? UIMode.DETECTION_MODE : UIMode.USER_MODE;
                ToastUtil.showToast(getApplicationContext(), uimode.toString());
                return false;
            }
        });


    }

    private void initializeSensorView() {
        //tvGyro = (TextView) findViewById(R.id.tvGyro);
        // sensor listener to different sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
        //if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {

        mSensorListener = new SensorEventListener() {

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onSensorChanged(SensorEvent event) {

                // TODO Auto-generated method stub
                Sensor sensor = event.sensor;
                if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                    //Accelerometer sensor has 3 values, one for each axis
                    if (frame_start) {
                        //AcceInit_X = event.values[0];
                        //AcceInit_Y = event.values[1];
                        AcceInit_Z = event.values[2];
                        frame_start = false;
                    }
                    float x_accel = event.values[0];
                    float y_accel = event.values[1];
                    float z_accel = event.values[2];
                    Log.d(TAG, "x:" + x_accel + "y: " + y_accel + "z: " + z_accel);
                    force = Math.sqrt(x_accel * x_accel + y_accel * y_accel + z_accel * z_accel); //- SensorManager.GRAVITY_EARTH; //SensorManager.GRAVITY_EARTH; //Linear acceleration index
                    force = force < 0 ? force * -1.0 : force;  // homemade abs
                    Log.d(TAG, "force:" + force);

                    tvAccel.setText("Accelerometer: " +
                                    "\n" + "x: " + String.format("%.4f", x_accel) + "\t m/s2" +
                                    "\n" + "y: " + String.format("%.4f", y_accel) + "\t m/s2" +
                                    "\n" + "z: " + String.format("%.4f", z_accel) + "\t m/s2" +
                                    "\n" + "Stability: " + String.format("%.4f", force)
                            //  + "\n" +"Distance: " + String.format("%.4f", TOTAL_DIST* 100.0f) + "cm"
                    );

                    tvAccel.setTextColor(Color.WHITE);
                    sensor_x.add(x_accel);
                    sensor_y.add(y_accel);
                    sensor_z.add(z_accel);

                } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    //Gyroscope sensor has 3 values, one for each axis
                    float x_gyro = event.values[0];
                    float y_gyro = event.values[1];
                    float z_gyro = event.values[2];
                    gyroIndex = Math.sqrt(x_gyro + y_gyro + z_gyro) / 3;
                }
            }
        };
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
    }

    /* always release camera when finished, since camera can only be held by one APP  */
    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
        // Open the default i.e. the first rear facing camera.

        try {
            mCamera = Camera.open();

        } catch (Exception e) {
            Log.e(TAG, "Camera is not available (in use or does not exist");
        }

        if (mCamera == null) {
            cancelRequest();
            return;
        }

        mPreview.setCamera(mCamera);
        mPreview.showSurfaceView();

        mPreviewing = true;
        initializeSensorView();
        //mSensorManager.registerListener(this.mSensorListener, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

        /*
        // output Linear Acceleration sensor log
        ContextWrapper cw = new ContextWrapper(getApplication());
        try {
            File txtStorageDir = cw.getDir(FileTools.IMAGE_DIR_NAME, Context.MODE_PRIVATE);  // get path
            String filename = "SENSOR" + FileTools.getTimeStamp() + ".txt";
            PrintWriter txtFile = new PrintWriter(new FileWriter(txtStorageDir.getPath() + File.separator + filename));

            //PrintWriter printWriter = new PrintWriter(txtFile);
            float sum_x = 0;
            float sum_y = 0;
            float sum_z = 0;
            for (int i = 0; i < sensor_x.size(); i++) {
                    txtFile.println( Float.toString(sensor_x.get(i)) + "," + Float.toString(sensor_y.get(i)) + "," + Float.toString(sensor_z.get(i))  );
            }
            for (Float e : sensor_x) {  sum_x = sum_x + e;  }
            for (Float e : sensor_y) {  sum_y = sum_y + e;  }
            for (Float e : sensor_z) {  sum_z = sum_z + e;  }
            sum_x = sum_x / sensor_x.size();
            sum_y = sum_y / sensor_y.size();
            sum_z = sum_z / sensor_z.size();

            ToastUtil.showToast(getApplicationContext(), "sensor file is written" + "average force is " +
                    Double.toString(Math.sqrt( (new Float(sum_x*sum_x + sum_y * sum_y + sum_z * sum_z).doubleValue()))));
            txtFile.close();


        } catch (IOException e) {e.printStackTrace();}

        */
        releaseResource(mPreview, mCamera);

        mSensorManager.unregisterListener(this.mSensorListener);
    }

    public void releaseResource(CameraPreview mPreview, Camera mCamera) {
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.cancelAutoFocus();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();

            // According to Jason Kuang on http://stackoverflow.com/questions/6519120/how-to-recover-camera-preview-from-sleep,
            // there might be surface recreation problems when the device goes to sleep. So lets just hide it and
            // recreate on resume
            mPreview.hideSurfaceView();

            mPreviewing = false;
            mCamera = null;
        }
    }

    public boolean isCameraAvailable() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public void cancelRequest() {
        Intent dataIntent = new Intent();
        dataIntent.putExtra("ERROR_INFO", "Camera unavailable");
        setResult(Activity.RESULT_CANCELED, dataIntent);
        finish();
    }    // autofocus runner, implement it in smoothy zoom-in focus manner
    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (mCamera != null && mPreviewing) {
                try {
                    mCamera.autoFocus(autoFocusCB);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            if (image_saved)
                //image_saved = true;    // another round of test
                finish();
        }
    };

    /*
        called as preview frames are displayed, this callback is invoked on the event thread open(int)
        if using the YV12 format, refer to equation in setPreviewFormat(int) for arrangement of pixel
        data in the preview callback buffers
        acquire the raw image data from the camera. To do it, we implement the Camera.PreviewCallback
        and override the onPreviewFrame method. This method is called every time a new frame is ready.
        Not used yet util frame processing is required for vision analysis
        http://www.intransitione.com/blog/how-to-detect-motion-on-an-android-device/
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

           /*
            int imageFormat = parameters.getPreviewFormat();

            Log.i("map", "Image Format:" + imageFormat);
            Log.i("CameraPreviewCallback", "data length:" + data.length);
            if (imageFormat == ImageFormat.NV21)
            {
                // get full photo
                Bitmap image = null;
                int w = parameters.getPreviewSize().width;
                int h = parameters.getPreviewSize().height;

                Rect rect = new Rect(0, 0, w, h);

                YuvImage img = new Rect(0, 0, w, h);

            }
        }*/
    }

    // Mimic continuous auto-focusing every 2 second

    private String savePictureToFileSystem(byte[] data) {
        //File file = FileTools.getOutputMediaFile(getApplicationContext());
        //File file = new File(GlobalConsts.ROOT_PATH);
        File file = new File(GlobalConsts.UPLOAD_FOLDER_PATH);
        saveToFile(data, FileTools.getFileStamped(file));
        return file.getAbsolutePath();
    }    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            Log.d(TAG, "onAutoFocus:" + success);
            //if (  uimode == UIMode.DETECTION_MODE && success && mCamera != null && (force < stabilityIndex && force != 0.0000f) ) {
            if (success && image_captured && (force < stabilityIndex && force != 0.0000f)) {
                try {
                    mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
                    ToastUtil.showToast(getApplicationContext(), Constant.Extra.IMAGE_CAPTURED);
                    image_saved = true;

                } catch (RuntimeException e) {
                    Log.d(TAG, "runtime exception from takepicture");
                    e.printStackTrace();
                }
            }
            mAutoFocusHandler.postDelayed(doAutoFocus, 3000);  // do autofocus every 3 second
        }
    };

    /* construct the path message to intent */
    private void setResult(String path) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_IMAGE_PATH, path);
        setResult(RESULT_OK, intent);
    }

    /* this method loads the customized layout on Camera view */
    public void LoadUILayout() {
        /* Load bitmap from sd card
            ImageView test_frame = (ImageView)findViewById(R.id.top_image);
            Bitmap bMap = BitmapFactory.decodeFile("/sdcard/test2.png");
            image.setImageBitmap(bMap);
        */
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.checkerboard);
        final FrameLayout framelayout = new FrameLayout(this);
        // Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity. 1st this refer to ActivityName.this
        mPreview = new CameraPreview(this, this, autoFocusCB);
        final DrawOnTop mDraw = new DrawOnTop(this);         // pass

        LinearLayout widgets = new LinearLayout(this);
        this.tvAccel = new TextView(this);
        this.mButton = new Button(this);
        this.mButton.setWidth(200);
        this.mButton.setText("Gallery");
        this.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(ImageSwitch.this, ImageSwitch.class);
            }
        });


        this.mButton1 = new Button(this);
        this.mButton1.setWidth(200);
        this.mButton1.setText("GrayScale");
        this.mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(ImageSwitch.this, ImageSwitch.class);
                Intent mIntent = new Intent(CameraActivity.this, grayCameraActivity.class);
                startActivityForResult(mIntent, Constant.Extra.DECTION_REQUESTED);
                //startActivity(mIntent);
            }
        });


        this.mToggleButton = new ToggleButton(this);
        this.mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if (isChecked)
                    mDraw.setVisibility(View.INVISIBLE);
                else
                    mDraw.setVisibility(View.VISIBLE);
                toggleButtonState = !toggleButtonState;

            }
        });

        mToggleButton.setWidth(150);
        //mToggleButton.setGravity(Gravity.CENTER);

        //mToggleButton.setOnClickListener(onClickListener);
        widgets.addView(mButton1);
        widgets.addView(mToggleButton);
        widgets.addView(mButton);
        widgets.addView(tvAccel);



        /* manually set up layout before drawing it */
        // add preview and widgets in frameLayout
        framelayout.addView(mPreview);
        framelayout.addView(widgets);
        mDraw.setAlpha(0.60f);   // required API level 11  //mDraw.setVisibility(View.INVISIBLE);

        mToggleButton.setAlpha(0.70f);
        //setContentView(R.layout.camera_activity );
        setContentView(framelayout);
        addContentView(mDraw, new ViewGroup.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }

    @Override
    public void onBackPressed() {
        finish();
    }


    public enum UIMode {
        USER_MODE,
        DETECTION_MODE;
    }

    /**
     * Created by intern2 on 22/10/2014.
     * this nested class is used to draw bitmap (test_frame) on top layer.
     */
    class DrawOnTop extends View {
        public DrawOnTop(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (bitmap != null) {

                // src dist size
                Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                Rect dst = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());    // size invariant

                // used OnMeasure this will change size of bitmap to adapt the screen size, but we want bitmap to be fixed
                //Rect dst = new Rect(0,0, this.getMeasuredWidth(), this.getMeasuredHeight());
                //Rect dst = new Rect(0, 0, this.getWidth(), this.getHeight());
                /*
                    drawBitmap:
                    Draw the specified bitmap, scaling/translating automatically to fill the destination rectangle.
                    If the source rectangle is not null, it specifies the subset of the bitmap to draw.
                    Parameters bitmap	The bitmap to be drawn
                                src	    May be null. The subset of the bitmap to be drawn
                                dst	    The rectangle that the bitmap will be scaled/translated to fit into
                                paint	May be null. The paint used to draw the bitmap
                 */

                canvas.drawBitmap(bitmap, src, dst, null);   // drawBitmap(bitmap, src, dst, null)
            }
            super.onDraw(canvas);
        }
    }







}
