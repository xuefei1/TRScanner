package trlabs.trscanner.trtabs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import trlabs.trscanner.R;
import trlabs.trscanner.TRScanner;
import trlabs.trscanner.service.AbstractUploadServiceReceiver;
import trlabs.trscanner.service.ContentType;
import trlabs.trscanner.service.TRService;
import trlabs.trscanner.service.UploadRequest;
import trlabs.trscanner.trtabs.config.BannerView;
import trlabs.trscanner.trtabs.config.GlobalConsts;
import trlabs.trscanner.ui.CalendarUtil;
import trlabs.trscanner.ui.Feedback;
import trlabs.trscanner.ui.ImgDialog;
import trlabs.trscanner.ui.JournalSearch;
import trlabs.trscanner.ui.LoadingDialog;
import trlabs.trscanner.ui.MenuDialogActivity;
import trlabs.trscanner.ui.UIHelper;
import trlabs.trscanner.users.journals.JournalDB;
import trlabs.trscanner.users.upload.Config;
import trlabs.trscanner.utils.BitmapHelper;
import trlabs.trscanner.utils.CircularImageView;
import trlabs.trscanner.utils.Constants;
import trlabs.trscanner.utils.FileTools;
import trlabs.trscanner.utils.HorizontalListView;
import trlabs.trscanner.utils.weather.WeatherInfo;
import trlabs.trscanner.utils.weather.YahooWeather;
import trlabs.trscanner.utils.weather.YahooWeatherExceptionListener;
import trlabs.trscanner.utils.weather.YahooWeatherInfoListener;

public class UserFragment extends Fragment implements TRScannerTabActivity.IBackPressedListener, YahooWeatherInfoListener, YahooWeatherExceptionListener{

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: // We are being told to do a UI update
                    // If more than one UI update is queued up, we only need to
                    // do one.
                    removeMessages(0);
                    break;
                case 1: // We are being told to display an error message
                    removeMessages(1);
            }
        }
    };

    private Activity mActivity;
    private Context mContext;
    private View mRootView = null;
    private Uri origUri;
    private Uri cropUri;
    private File protraitFile;
    private Bitmap protraitBitmap;
    private String protraitPath;
    private TextView mTvWeather;
    private TextView mLocation;
    private ImageView mPicWeather;
    private RelativeLayout upload_progress;
    public static ImageView button;
    private Map<Date,Integer> scan_data;
    private TreeMap<Date, Integer> sortedMap;
    private Bitmap user_bg;
    private TextView userName;
    RelativeLayout user_layout;
    LinearLayout about_layout;
    LinearLayout banner_layout;
    private TextView mFeedback;
    RelativeLayout layoutTop;
    CircularImageView user_icon;
    private LoadingDialog loading;
    JournalDB journalDB;
    trlabs.trscanner.trtabs.config.BannerView banner;
    TRScanner ac;
    private LineChart mChart;
    private final static int CROP = 200;
    private YahooWeather mYahooWeather = YahooWeather.getInstance(5000, 5000, true);
    final String origFileName = "osc_" + FileTools.getTimeStamp() + ".jpg";
    final String cropFileName = "osc_crop_" + FileTools.getTimeStamp() + ".jpg";
    HorizontalListView list;
    TextView profile;
    TextView search;

    public UserFragment() {
    }

    private void searchByGPS() {
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.GPS);
        mYahooWeather.queryYahooWeatherByGPS(mContext, this);
    }

    @Override
    public void gotWeatherInfo(WeatherInfo weatherInfo) {
        // TODO Auto-generated method stub
        if (weatherInfo != null) {
            if (weatherInfo.getCurrentConditionIcon() != null) {
                mPicWeather.setImageBitmap(weatherInfo.getCurrentConditionIcon());
            }

            mLocation.setText(weatherInfo.getLocationCity()   + ", "
                    + weatherInfo.getWOEIDState() + ", "
                    + weatherInfo.getWOEIDCountry());

            mTvWeather.setText(
                       weatherInfo.getCurrentText() + "\n" +
                       weatherInfo.getCurrentTemp() + " ºC" + "\n"
            );
        }
    }

    @Override
    public void onFailConnection(final Exception e) {
        // TODO Auto-generated method stub
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTvWeather.setText(":(");
                mLocation.setText("service not available");
            }
        });

    }

    @Override
    public void onFailParsing(final Exception e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onFailFindLocation(final Exception e) {
        // TODO Auto-generated method stub
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mLocation.setText("service not available");
            }
        });
    }


    @Override
    @SuppressLint("NewApi")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        mActivity = getActivity();
        mYahooWeather.setExceptionListener(this);
        String FILE_SAVEPATH = GlobalConsts.ROOT_PATH;
        protraitPath = FILE_SAVEPATH + cropFileName;
        protraitFile = new File(protraitPath);
        origUri = Uri.fromFile(new File(FILE_SAVEPATH, origFileName));
        cropUri = Uri.fromFile(protraitFile);

        ac = (TRScanner)getActivity().getApplication();

        if(mRootView == null) {
            mRootView = inflater.inflate(R.layout.about, container, false);
        }


        initiateView();
        postData();
        initiateListener();

        searchByGPS();
        return mRootView;
    }



    @SuppressWarnings("deprecation")
    private void initiateView(){
        userName = (TextView) mRootView.findViewById(R.id.user_name);
        button = (ImageView) mRootView.findViewById(R.id.button_operation_more);
        layoutTop = (RelativeLayout)mRootView.findViewById(R.id.layoutTop);
        user_bg = BitmapFactory.decodeResource(getResources(), R.drawable.user_background);
        user_layout = (RelativeLayout)mRootView.findViewById(R.id.user_page);
        about_layout = (LinearLayout) mRootView.findViewById(R.id.about_layout);
        banner = (BannerView) mRootView.findViewById(R.id.slidingLayout);
        banner_layout = (LinearLayout) mRootView.findViewById(R.id.operation_bar);
        upload_progress = (RelativeLayout) mRootView.findViewById(R.id.progress_layout);
        mTvWeather = (TextView) mRootView.findViewById(R.id.temperature);
        mPicWeather = (ImageView) mRootView.findViewById(R.id.pic_temperature);
        mLocation = (TextView) mRootView.findViewById(R.id.location);
        user_icon = (CircularImageView)mRootView.findViewById(R.id.user_icon);
        mChart = (LineChart) mRootView.findViewById(R.id.chart1);
        list = (HorizontalListView) mRootView.findViewById(R.id.info_list);
        journalDB = new JournalDB(mContext);
        mFeedback = (TextView) mRootView.findViewById(R.id.feedback);
        profile = (TextView) mRootView.findViewById(R.id.button_operation_profile);
        search = (TextView) mRootView.findViewById(R.id.button_operation_search);
    }


    private void postData(){
        ac.user.setUserIMG(user_icon);
        ArrayList<UserDataSet> dataList = new ArrayList<UserDataSet>();
        scan_data = CalendarUtil.getScanDatesMap(GlobalConsts.ROOT_PATH);
        sortedMap = new TreeMap<Date, Integer>(scan_data);  // sort by date
        for(Date d: sortedMap.keySet()){
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            dataList.add(new UserDataSet(cal, sortedMap.get(d).toString()));
        }
        list.setAdapter(new UserDataListAdapter(dataList , this.mContext));


        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");
        // enable value highlighting
        mChart.setHighlightEnabled(true);
        // enable touch gestures
        mChart.setTouchEnabled(true);
        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        setData();
    }

    private void initiateListener(){
        mFeedback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, Feedback.class);
                startActivity(intent);
            }
        });

        button.setOnClickListener(
                new OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        button.setImageDrawable(getResources().getDrawable(R.drawable.user_operation_more_off));
                        Intent intent = new Intent(mRootView.getContext(), MenuDialogActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().startActivityForResult(intent, Constants.Extra.REQUEST_CODE_CHANGE_PLUS_BUTTON);
                    }
                }

        );

        mRootView.findViewById(R.id.user_icon).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        //http://stackoverflow.com/questions/13733304/callback-to-a-fragment-from-a-dialogfragment
                        ImgDialog dialog = ImgDialog.newInstance(mContext);
                        Bundle args = new Bundle();
                        args.putString("cropFileName", cropFileName);
                        args.putString("origFileName", origFileName);
                        dialog.setArguments(args);
                        dialog.show(getFragmentManager(), "fragmentDialog");
                    }
                });

        // Set the application-wide context global, if not already set
        mRootView.findViewById(R.id.trtech_icon).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        String uri = "http://www.trtech.ca/trlabs/";
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(uri));
                        startActivity(intent);
                    }
                });

        profile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(view.getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.user_profile_dialog);
                dialog.setCancelable(true);
                CircularImageView profile_pic = (CircularImageView) dialog.findViewById(R.id.profile_pic);
                TextView uid = (TextView) dialog.findViewById(R.id.user_id);
                TextView name = (TextView) dialog.findViewById(R.id.user_name);
                TextView age = (TextView) dialog.findViewById(R.id.user_age);
                TextView email = (TextView) dialog.findViewById(R.id.user_email);
                TextView phone = (TextView) dialog.findViewById(R.id.user_phone);
                TextView address = (TextView) dialog.findViewById(R.id.user_address);
                TextView city = (TextView) dialog.findViewById(R.id.user_city);
                TextView province = (TextView) dialog.findViewById(R.id.user_province);
                TextView country = (TextView) dialog.findViewById(R.id.user_country);
                TextView latestLogin = (TextView) dialog.findViewById(R.id.user_Latestlogin);
                ImageButton imageButton = (ImageButton) dialog.findViewById(R.id.user_profile_close_button);
                profile_pic.setImageBitmap(ac.user.getUserIMG());
                uid.setText(Integer.toString(ac.user.getUid()));
                name.setText(ac.user.getName());
                age.setText( Integer.toString(ac.user.getAge()));
                email.setText( ac.user.getEmail());
                phone.setText( ac.user.getPhone());
                address.setText( ac.user.getAddress());
                city.setText( ac.user.getCity());
                province.setText( ac.user.getProvince());
                country.setText( ac.user.getCountry());
                latestLogin.setText( ac.user.getLatestonline());
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                            dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(mContext, JournalSearch.class);
                startActivity(i);
            }
        });



        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                refreshListAdapter(adapterView); // remove multi selection when reach new page

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    view.setBackground(getResources().getDrawable(R.drawable.text_rounded_corner));
                } else {
                    view.setBackgroundDrawable(getResources().getDrawable(R.drawable.text_rounded_corner));
                }
                TextView textDay = (TextView)view.findViewById(R.id.text_day);
                TextView journal = (TextView) mRootView.findViewById(R.id.text_journal);


                textDay.setTextColor(getResources().getColor(R.color.red));

                String journal_content = getDateQueryContent(view);
                if (null != journal_content) {
                    journal.setVisibility(View.VISIBLE);
                    journal.setText(journal_content);
                } else {
                    journal.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mChart.setOnChartGestureListener(new OnChartGestureListener(){
            @Override
            public void onChartLongPressed(MotionEvent motionEvent) {

            }
            @Override
            public void onChartDoubleTapped(MotionEvent motionEvent) {

            }
            @Override
            public void onChartSingleTapped(MotionEvent motionEvent) {

            }
            @Override
            public void onChartFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {

            }
        });
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, int i, Highlight highlight) {

            }
            @Override
            public void onNothingSelected() {

            }
        });


    }


    private void Blur(Bitmap bitmap){
        float scaleFactor = 1;
        float radius = 2;

        // problem is the widget does not know its dimension before onCreate is done
        Bitmap overlay = Bitmap.createBitmap(
                (int) (layoutTop.getMeasuredWidth() / scaleFactor),
                (int) (layoutTop.getMeasuredHeight() / scaleFactor),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-layoutTop.getLeft() / scaleFactor, -layoutTop.getTop()
                / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);

        overlay = BitmapHelper.doBlur(overlay, (int) radius, true);

        if (Build.VERSION.SDK_INT >= 16)
            layoutTop.setBackground(new BitmapDrawable(getResources(), overlay));
        else
            layoutTop.setBackgroundDrawable(new BitmapDrawable(getResources(), overlay));

    }

    private void refreshListAdapter(AdapterView<?> adapterView) {
        for(int a = 0; a<adapterView.getChildCount(); a++){
            View v = adapterView.getChildAt(a);
            TextView t = (TextView) v.findViewById(R.id.text_day);
            t.setTextColor(getResources().getColor(R.color.black));
            v.setBackgroundResource(0);
        }
    }



    private String getDateQueryContent(View view) {
        TextView day = (TextView) view.findViewById(R.id.text_day);
        TextView month = (TextView) view.findViewById(R.id.text_month);
        TextView year = (TextView) view.findViewById(R.id.text_year);
        year.setVisibility(View.VISIBLE);

        String date = year.getText().toString() +
                     CalendarUtil.getMonthInNumber(month.getText().toString()) +
                     day.getText().toString();
        if (journalDB.isExistingKey(date)) {
            return journalDB.getJournalByDate(date).getContent();
        }
        return null;
    }

    public void update(){
       TRScanner ac = (TRScanner) getActivity().getApplication();
       if(ac.user.getLoginState()){    // user page
           about_layout.setVisibility(View.GONE);
           banner.setVisibility(View.GONE);
           user_layout.setVisibility(View.VISIBLE);
           banner_layout.setVisibility(View.VISIBLE);

           layoutTop.getViewTreeObserver().addOnGlobalLayoutListener(
                   new ViewTreeObserver.OnGlobalLayoutListener() {
                       @Override
                       public void onGlobalLayout() {
                           layoutTop.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                           Blur(user_bg);     // faster to get widget dimensions
                       }
                   }
           );

           if (FileTools.isEmptyDirectory(GlobalConsts.UPLOAD_FOLDER_PATH)) {
               this.uploadPendingItems(mContext);
           }
       }else{                       // public page
           about_layout.setVisibility(View.VISIBLE);
           banner.setVisibility(View.VISIBLE);
           user_layout.setVisibility(View.GONE);
           banner_layout.setVisibility(View.GONE);
       }
    }

    private void appendFiles(UploadRequest r, File dir){
        try {
            if (dir.exists()) {
                File[] files = dir.listFiles();
                Log.d("FILESIZE____________________________________",""+files.length);
                for (int i = 0; i < files.length; ++i) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        appendFiles(r, file);
                    } else {
                        r.addFileToUpload(file.getAbsolutePath(), "image[]", file.getName(), ContentType.IMAGE_JPEG);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void uploadPendingItems(final Context context){
        final UploadRequest request = new UploadRequest(context,
                "",
                Config.FILE_UPLOAD_URL);

    /*
     * parameter-name: is the name of the parameter that will contain file's data.
     * Pass "uploaded_file" if you're using the test PHP script
     *
     * custom-file-name.extension: is the file name seen by the server.
     * E.g. value of $_FILES["uploaded_file"]["name"] of the test PHP script
     */
        appendFiles(request, new File(GlobalConsts.UPLOAD_FOLDER_PATH));
        TRScanner ac = (TRScanner) getActivity().getApplication();

        request.addParameter("id", String.valueOf(ac.user.getUid()));
        //configure the notification
        request.setNotificationConfig(android.R.drawable.ic_menu_upload,
                "Upload",
                "Uploading...",
                "upload complete",
                "upload failed",
                false);

        try {
            //Start upload service and display the notification
            TRService.startUpload(request);

        } catch (Exception exc) {
            //You will end up here only if you pass an incomplete UploadRequest
            Log.e("AndroidUploadService", exc.getLocalizedMessage(), exc);
        }

    }

    private final AbstractUploadServiceReceiver uploadReceiver =
            new AbstractUploadServiceReceiver() {

                private static final String TAG = "AndroidUploadService";
                @Override
                public void onProgress(String uploadId, int progress) {
                    Log.i(TAG, "The progress of the upload with ID "
                            + uploadId + " is: " + progress);
                    upload_progress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(String uploadId, Exception exception) {
                    Log.e(TAG, "Error in upload with ID: " + uploadId + ". "
                            + exception.getLocalizedMessage(), exception);
                    upload_progress.setVisibility(View.GONE);
                }

                @Override
                public void onCompleted(String uploadId,
                                        int serverResponseCode,
                                        String serverResponseMessage) {
                    Log.i(TAG, "Upload with ID " + uploadId
                            + " has been completed with HTTP " + serverResponseCode
                            + ". Response from server: " + serverResponseMessage);

                    //If your server responds with a JSON, you can parse it
                    //from serverResponseMessage string using a library
                    //such as org.json (embedded in Android) or google's gson
                    upload_progress.setVisibility(View.GONE);
                    new DeleteFilesTask().execute(null,null,null);
                }
            };

    @Override
    public boolean onBack() {
        return false;
    }
    /**
     * Whenever we regain focus, we should update the button text depending on
     * the state of the server service.
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();

        if (GlobalConsts.isNewsFeeed) {
            showNewsFeeds();
            userName.setText(ac.user.getName());
        }

        uploadReceiver.register(mContext);
    }



    public void showNewsFeeds(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TRScannerTabActivity.dialog.show();
                GlobalConsts.isNewsFeeed = false;
            }
        }, 1000);
    }

    /*
     * Whenever we lose focus, we must unregister from UI update messages from
     * the FTPServerService, because we may be deallocated.
     */
    @Override
    public void onPause() {
        super.onPause();
        uploadReceiver.unregister(mContext);
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private class DeleteFilesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            FileTools.delete(new File(GlobalConsts.UPLOAD_FOLDER_PATH));
            return null;
        }


    }


    class UserDataListAdapter extends BaseAdapter{

        private ArrayList<UserDataSet> data;
        View rootView =  null;
        private TextView tx_month, tx_day, tx_data, tx_year;
        private Context mContext;
        public UserDataListAdapter(ArrayList<UserDataSet> data, Context ctx){
            this.data = data;
            this.mContext = ctx;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public UserDataSet  getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return -1;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup){
            rootView = LayoutInflater.from(this.mContext).inflate(R.layout.user_data_list_adapter, viewGroup, false);
            initiateView();
            postData(i);

            if (isJournalExist(rootView)) {
                this.tx_data.setTextColor(rootView.getResources().getColor(R.color.blue));
            }
            return rootView;
        }


        private void initiateView() {
            this.tx_month = (TextView) rootView.findViewById(R.id.text_month);
            this.tx_day = (TextView) rootView.findViewById(R.id.text_day);
            this.tx_year = (TextView) rootView.findViewById(R.id.text_year);
            this.tx_data =(TextView) rootView.findViewById(R.id.text_data);
        }

        private void postData(int i){
            UserDataSet dataSet = this.getItem(i);
            this.tx_month.setText(CalendarUtil.getMonthInWords(dataSet.getCalendar().get(Calendar.MONTH)));
            this.tx_day.setText(String.valueOf(dataSet.getCalendar().get(Calendar.DAY_OF_MONTH)));
            this.tx_year.setText(String.valueOf(dataSet.getCalendar().get(Calendar.YEAR)));
            this.tx_data.setText(dataSet.getData());


        }
        private boolean isJournalExist(View view) {
            TextView day = (TextView) view.findViewById(R.id.text_day);
            TextView month = (TextView) view.findViewById(R.id.text_month);
            TextView year = (TextView) view.findViewById(R.id.text_year);

            String date = year.getText().toString() +
                    CalendarUtil.getMonthInNumber(month.getText().toString()) +
                    day.getText().toString();

            return journalDB.isExistingKey(date);
        }


    }

    static class UserDataSet{

        private Calendar calendar = Calendar.getInstance();
        private String dataVal ;

        public UserDataSet(Calendar calendar, String dataVal){
            this.calendar = calendar;
            this.dataVal = dataVal;
        }

        public Calendar getCalendar(){
            return this.calendar;
        }

        public String getData(){
            return this.dataVal;
        }
    }



    private void setData() {
        // feed data to Chart
        Map<Date,Integer> dataSource = CalendarUtil.getScanDatesMap(GlobalConsts.ROOT_PATH);
        ArrayList<Integer> valList = new ArrayList<Integer>(dataSource.values());
        List<Date> sortedDateList = new ArrayList<Date>();
        int minDate, maxDate;

        Iterator<Integer> ite = valList.iterator();
        int YMin = 0;
        int YMax = 0;
        /*
        while(ite.hasNext()){
            int i  = ite.next();
            if(i<YMin){
                YMin = i;
            }
        }
        */
        if(valList.size() != 0) {
            YMin = Collections.min(valList);

            YMax = Collections.max(valList);
        }

        /*
        Iterator<Integer> itMax = valList.iterator();
        int YMax = 0;
        while(itMax.hasNext()){
            int i  = itMax.next();
            if(i>YMax){
                YMax = i;
            }
        }
        */

        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        Calendar cal = Calendar.getInstance();
        Date date;
        sortedDateList = CalendarUtil.getSortedDates(dataSource);
        Iterator it = dataSource.entrySet().iterator();

        cal.setTime(CalendarUtil.getMinDate(dataSource));
        minDate = cal.get(Calendar.DATE);
        cal.setTime(CalendarUtil.getMaxDate(dataSource));
        maxDate = cal.get(Calendar.DATE);

        for (int index = minDate; index < maxDate + 4; index++) {
            xVals.add((index) + "");
        }

        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            //xVals.add(Integer.toString((Integer)pair.getKey()));
            date = (Date)pair.getKey();
            cal.setTime(date);
            /*
            int day = cal.get(Calendar.DAY_OF_MONTH);  // Tue Mar 03 00:00:00 MST 2015      3
            int day2 = cal.get(Calendar.DAY_OF_WEEK);  // 3
            int day3 = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);   //1
            int week = cal.get(Calendar.WEEK_OF_MONTH);    // 1
            int week2 = cal.get(Calendar.WEEK_OF_YEAR);     // 10
            int year = cal.get(Calendar.YEAR);              // 2015
            */

            yVals.add(new Entry ((Integer)pair.getValue(), cal.get(Calendar.DATE) - 2 ));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "DataSet 1");
        set1.setFillAlpha(110);
        set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        set1.enableDashedLine(10f, 5f, 0f);
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(1f);
        set1.setCircleSize(4f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.BLACK);

        // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
        // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);
        data.setValueTextSize(10f);

        LimitLine ll1 = new LimitLine(YMax, "Upper Bound");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.POS_RIGHT);
        ll1.setTextSize(10f);

        LimitLine ll2 = new LimitLine(YMin, "Lower Bound");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.POS_RIGHT);
        ll2.setTextSize(10f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaxValue(YMax + 2);
        leftAxis.setAxisMinValue(YMin - 1);
        leftAxis.setStartAtZero(false);

        mChart.getAxisRight().setEnabled(false);

        // set data
        mChart.setData(data);
    }
}
