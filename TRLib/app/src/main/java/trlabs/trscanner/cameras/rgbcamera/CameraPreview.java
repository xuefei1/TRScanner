package trlabs.trscanner.cameras.rgbcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

// it has preview since it does not needed to save it

// extra added package


/* when inherit viewgroup, has to override two methods, OnMeasure and OnLayout.
    Relative layout, linearlayout, framelayout... are extended from ViewGroup; those layout are impletented
     from onLayout method from ViewGroup.
    Use OnLayout and OnMeasure to define complex layout View, call OnMeasure to measure defined View, and call
    OnLayout to dynamically obtain child view and size of child view, then process layout.
 */
class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
    private static final String LOG_TAG = CameraPreview.class.getSimpleName();
    private final String TAG = "CameraPreview";
    // extra define variables
    private final int PHASE_NORMAL = 0;
    private int phase = PHASE_NORMAL;
    private final int PHASE_TIMER = 1;
    private final int PHASE_TAKING_PHOTO = 2;
    private final int PHASE_PREVIEW_PAUSED = 3; // the paused state after taking a photo
    public ImageView mImageView;
    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    Camera.Size mPreviewSize;
    List<Camera.Size> mSupportedPreviewSizes;
    Camera mCamera;
    Camera.PreviewCallback mPreviewCallback;
    Camera.AutoFocusCallback mAutoFocusCallback;
    private byte mBuffer[];
    private Timer takePictureTimer = new Timer();
    private long take_photo_time = 0;
    private Bitmap thumbnail = null; // thumbnail of last picture taken


    CameraPreview(Context context, Camera.PreviewCallback previewCallback, Camera.AutoFocusCallback autoFocusCb) {
        super(context);
        mPreviewCallback = previewCallback;
        mAutoFocusCallback = autoFocusCb;
        mSurfaceView = new SurfaceView(context);
        /*
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.test_frame);
        // resize bitmap
        Bitmap resized_bmp = Bitmap.createScaledBitmap(bmp, 200, 200, false);
        Drawable drawable = new BitmapDrawable(getResources(), bmp);   // drawable d = new drawable(bmp) is deprecated

        LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mImageView.setLayoutParams(param);
        */
        /* two views added in order */
        addView(mSurfaceView);
        // mSurfaceView.setZOrderOnTop(true);
        Log.i("", "drawable");
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        //bMapSurfaceView.setZOrderOnTop(true);

        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);

        /* if uncomment cannot use Surfaceview to draw bitmap and take photo */
        // mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //bringChildToFront(mImageView);
    }

    public CameraPreview(Context context) {
        super(context);
    }


    /*
    A camera instance and its related preview must be created in a specific order,
    with the camera object being first. In the snippet below, the process of initializing
    the camera is encapsulated so that Camera.startPreview() is called by the setCamera() method,
    whenever the user does something to change the camera. The preview must also be restarted in the
    preview class surfaceChanged() callback method.
     */
    public void setCamera(Camera camera) {

        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

            requestLayout();
        }
    }

    /**
     * Ask all children to measure themselves and compute the measurement of this
     * layout based on the children.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        Log.d("number of child", String.valueOf(count));   // just 1 child
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.

        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        // set the values
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    /*
        Override it to set up positions for the child views.
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(0);

                final int width = r - l;
                final int height = b - t;

                int previewWidth = width;
                int previewHeight = height;
                if (mPreviewSize != null) {
                    previewWidth = mPreviewSize.width;
                    previewHeight = mPreviewSize.height;
                }

                // Center the child SurfaceView within the parent.
                if (width * previewHeight > height * previewWidth) {
                    final int scaledChildWidth = previewWidth * height / previewHeight;
                    child.layout((width - scaledChildWidth) / 2, 0,
                            (width + scaledChildWidth) / 2, height);
                } else {
                    final int scaledChildHeight = previewHeight * width / previewWidth;
                    child.layout(0, (height - scaledChildHeight) / 2,
                            width, (height + scaledChildHeight) / 2);
                }
            }
        }
    }

    public void hideSurfaceView() {
        mSurfaceView.setVisibility(View.INVISIBLE);
    }

    public void showSurfaceView() {
        mSurfaceView.setVisibility(View.VISIBLE);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            //setWillNotDraw(false);
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    /*
        Once your application is done using the camera, it's time to clean up. In particular,
        you must release the Camera object, or you risk crashing other applications,
        including new instances of your own application.
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    private void stopPreviewAndFreeCamera() {

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
    }

    /**
     * This helper method can be called by subclasses to select camera preview size.
     * It goes over the list of the supported preview sizes and selects the maximum one which
     * fits both values set via setMaxFrameSize() and surface frame allocated for this view
     *
     * @param supportedSizes
     * @param surfaceWidth
     * @param surfaceHeight
     * @return optimal frame size
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> supportedSizes, int surfaceWidth, int surfaceHeight) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) surfaceWidth / surfaceHeight;
        if (supportedSizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;   // around 1.7*10^308

        int targetHeight = surfaceHeight;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : supportedSizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : supportedSizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        // Log.i("optimalsize is height ", Integer.toString(optimalSize.height) + " width  " + Integer.toString(optimalSize.width)); HTC height 1088, width 1920. wa
        return optimalSize;
    }


    @SuppressWarnings("deprecation")
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (holder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        if (mCamera != null) {
            // Now that the size is known, set up the camera parameters and begin
            // the preview.
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);  // calibrate surface view size
            parameters.setPictureFormat(PixelFormat.JPEG);
            //parameters.setPictureSize(mPreviewSize.width, mPreviewSize.height);


            //parameters.setPreviewFrameRate(5);


            requestLayout();

            /*
            int size = mPreviewSize.width * mPreviewSize.height;
            size = size * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat())/8;
            mBuffer = new byte[size];
            mCamera.addCallbackBuffer(mBuffer);
            mCamera.setPreviewCallbackWithBuffer(this);
            */

            mCamera.setParameters(parameters);
            mCamera.setPreviewCallback(mPreviewCallback);
            Log.d(TAG, "startPreview");
            mCamera.startPreview();
            mCamera.autoFocus(mAutoFocusCallback);
        }
    }

}

