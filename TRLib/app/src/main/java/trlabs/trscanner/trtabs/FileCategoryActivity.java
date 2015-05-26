package trlabs.trscanner.trtabs;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import cn.pedant.SweetAlert.SweetAlertDialog;
import trlabs.trscanner.R;
import trlabs.trscanner.cameras.rgbcamera.CameraActivity;
import trlabs.trscanner.trtabs.Favorite.FavoriteDatabaseHelper.FavoriteDatabaseListener;
import trlabs.trscanner.trtabs.Favorite.FavoriteList;
import trlabs.trscanner.trtabs.File.FileCategoryHelper;
import trlabs.trscanner.trtabs.File.FileCategoryHelper.CategoryInfo;
import trlabs.trscanner.trtabs.File.FileCategoryHelper.FileCategory;
import trlabs.trscanner.trtabs.File.FileIconHelper;
import trlabs.trscanner.trtabs.File.FileInfo;
import trlabs.trscanner.trtabs.adapter.FileListCursorAdapter;
import trlabs.trscanner.trtabs.File.FileSortHelper;
import trlabs.trscanner.trtabs.File.FileViewInteractionHub;
import trlabs.trscanner.trtabs.config.CategoryBar;
import trlabs.trscanner.trtabs.config.GlobalConsts;
import trlabs.trscanner.ui.CalendarUtil;
import trlabs.trscanner.ui.ChartActivity;
import trlabs.trscanner.ui.JournalDialog;
import trlabs.trscanner.users.journals.Journal;
import trlabs.trscanner.users.journals.JournalDB;
import trlabs.trscanner.utils.DialogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FileCategoryActivity extends Fragment implements IFileInteractionListener,
        FavoriteDatabaseListener, TRScannerTabActivity.IBackPressedListener {

    public static final String EXT_FILETER_KEY = "ext_filter";
    public JournalDB journalDB;
    private static final String LOG_TAG = "FileCategoryActivity";

    private static HashMap<Integer, FileCategory> button2Category = new HashMap<Integer, FileCategory>();

    private HashMap<FileCategory, Integer> categoryIndex = new HashMap<FileCategory, Integer>();

    private FileListCursorAdapter mAdapter;

    private FileViewInteractionHub mFileViewInteractionHub;

    private FileCategoryHelper mFileCagetoryHelper;

    private FileIconHelper mFileIconHelper;

    private CategoryBar mCategoryBar;

    private ScannerReceiver mScannerReceiver;

    private FavoriteList mFavoriteList;

    private ViewPage curViewPage = ViewPage.Invalid;

    private ViewPage preViewPage = ViewPage.Invalid;

    private Activity mActivity;

    private View mRootView;

    private FileViewActivity mFileViewActivity;

    private boolean mConfigurationChanged = false;
    private boolean isCalOpen;
    private FragmentActivity myContext;

    public CaldroidFragment myCaldroid;
    public void setConfigurationChanged(boolean changed) {
        mConfigurationChanged = changed;
    }

    static {
        //!--
        button2Category.put(R.id.category_camera, FileCategory.Music);
        //button2Category.put(R.id.category_video, FileCategory.Video);
        button2Category.put(R.id.category_calendar, FileCategory.Calendar);
        //button2Category.put(R.id.category_theme, FileCategory.Theme);
        //button2Category.put(R.id.category_document, FileCategory.Doc);
        //button2Category.put(R.id.category_zip, FileCategory.Zip);
        //button2Category.put(R.id.category_apk, FileCategory.Apk);
        button2Category.put(R.id.category_favorite, FileCategory.Favorite);
    }


    @Override
    public void onResume(){
        super.onResume();
        myCaldroid.refreshView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        mFileViewActivity = (FileViewActivity) ((TRScannerTabActivity) mActivity)
                .getFragment(Util.SDCARD_TAB_INDEX);
        mRootView = inflater.inflate(R.layout.file_explorer_category, container, false);
        curViewPage = ViewPage.Invalid;
        mFileViewInteractionHub = new FileViewInteractionHub(this);
        mFileViewInteractionHub.setMode(FileViewInteractionHub.Mode.View);
        //mFileViewInteractionHub.setRootPath(GlobalConsts.ROOT_PATH);

        mFileViewInteractionHub.setRootPath("/");       // note
        mFileIconHelper = new FileIconHelper(mActivity);
        mFavoriteList = new FavoriteList(mActivity, (ListView) mRootView.findViewById(R.id.favorite_list), this, mFileIconHelper);
        mFavoriteList.initList();
        mAdapter = new FileListCursorAdapter(mActivity, null, mFileViewInteractionHub, mFileIconHelper);
        ListView fileListView = (ListView) mRootView.findViewById(R.id.file_path_list);
        fileListView.setAdapter(mAdapter);
        isCalOpen = false;
        setupClick();
        setupCategoryInfo();
        updateUI();
        registerScannerReceiver();



        myCaldroid = new CaldroidFragment();
        if (savedInstanceState != null) {
            myCaldroid.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE");
        }

        return mRootView;
    }

    private void registerScannerReceiver() {
        mScannerReceiver = new ScannerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");   // allow logcat to receive sd card  mount/unmount event http://blog.csdn.net/silenceburn/article/details/6083375
        mActivity.registerReceiver(mScannerReceiver, intentFilter);
    }

    private void setupCategoryInfo() {
        mFileCagetoryHelper = new FileCategoryHelper(mActivity);

        mCategoryBar = (CategoryBar) mRootView.findViewById(R.id.category_bar);
        // index for thumbnails bar
        int[] imgs = new int[] {
                R.drawable.category_bar_music, R.drawable.category_bar_video,
                R.drawable.category_bar_picture, R.drawable.category_bar_theme,
                R.drawable.category_bar_document, R.drawable.category_bar_zip,
                R.drawable.category_bar_apk, R.drawable.category_bar_other
        };

        for (int i = 0; i < imgs.length; i++) {
            mCategoryBar.addCategory(imgs[i]);
        }

        for (int i = 0; i < FileCategoryHelper.sCategories.length; i++) {
            categoryIndex.put(FileCategoryHelper.sCategories[i], i);
        }
    }


    // retrieve storage info and display
    public void refreshCategoryInfo() {
        Util.SDCardInfo sdCardInfo = Util.getSDCardInfo();
        if (sdCardInfo != null) {
            mCategoryBar.setFullValue(sdCardInfo.total);
            setTextView(R.id.sd_card_capacity, getString(R.string.sd_card_size, Util.convertStorage(sdCardInfo.total)));
            setTextView(R.id.sd_card_available, getString(R.string.sd_card_available, Util.convertStorage(sdCardInfo.free)));
        }

        mFileCagetoryHelper.refreshCategoryInfo();

        // the other category size should include those files didn't get scanned.
        long size = 0;
        for (FileCategory fc : FileCategoryHelper.sCategories) {
            CategoryInfo categoryInfo = mFileCagetoryHelper.getCategoryInfos().get(fc);
            setCategoryCount(fc, categoryInfo.count);

            // other category size should be set separately with calibration
            if(fc == FileCategory.Other)
                continue;

            setCategorySize(fc, categoryInfo.size);
            setCategoryBarValue(fc, categoryInfo.size);
            size += categoryInfo.size;
        }

        if (sdCardInfo != null) {   // Misc types
            long otherSize = sdCardInfo.total - sdCardInfo.free - size;
            setCategorySize(FileCategory.Other, otherSize);
            setCategoryBarValue(FileCategory.Other, otherSize);
        }

        setCategoryCount(FileCategory.Favorite, mFavoriteList.getCount());

        if (mCategoryBar.getVisibility() == View.VISIBLE) {
            mCategoryBar.startAnimation();
        }
    }

    public enum ViewPage {
        Home, Favorite, Category, NoSD, Invalid
    }

    // navigate by ViewPage types
    private void showPage(ViewPage p) {
        if (curViewPage == p) return;

        curViewPage = p;

        showView(R.id.file_path_list, false);
        showView(R.id.navigation_bar, false);
        showView(R.id.category_page, false);
        showView(R.id.operation_bar, false);
        showView(R.id.sd_not_available_page, false);
        mFavoriteList.show(false);
        showEmptyView(false);

        switch (p) {
            case Home:
                showView(R.id.category_page, true);
                if (mConfigurationChanged) {
                    ((TRScannerTabActivity) mActivity).reInstantiateCategoryTab();
                    mConfigurationChanged = false;
                }
                break;
            case Favorite:
                showView(R.id.navigation_bar, true);
                mFavoriteList.show(true);
                showEmptyView(mFavoriteList.getCount() == 0);
                break;
            case Category:
                showView(R.id.navigation_bar, true);
                showView(R.id.file_path_list, true);
                showEmptyView(mAdapter.getCount() == 0);
                break;
            case NoSD:
                showView(R.id.sd_not_available_page, true);
                break;
        }
    }

    private void showEmptyView(boolean show) {
        View emptyView = mActivity.findViewById(R.id.empty_view);
        if (emptyView != null)
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showView(int id, boolean show) {
        View view = mRootView.findViewById(id);
        if (view != null) {
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void setCategoryCount(FileCategory fc, long count) {
        int id = getCategoryCountId(fc);
        if (id == 0)
            return;

        setTextView(id, "(" + count + ")");
    }

    private void setTextView(int id, String t) {
        TextView text = (TextView) mRootView.findViewById(id);
        text.setText(t);
    }

    // category click
    private void onCategorySelected(FileCategory f) {
        if (mFileCagetoryHelper.getCurCategory() != f) {
            mFileCagetoryHelper.setCurCategory(f);
            mFileViewInteractionHub.setCurrentPath(mFileViewInteractionHub.getRootPath()
                    + getString(mFileCagetoryHelper.getCurCategoryNameResId()));
            mFileViewInteractionHub.refreshFileList();
        }
        // category page jumper
        if (f == FileCategory.Favorite) {
            showPage(ViewPage.Favorite);
        } else {
            showPage(ViewPage.Category);
        }
    }
    // category click listener
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FileCategory f = button2Category.get(v.getId());    // get category id

            if (f != null) {
                if (f.name().equals("Music")) {
                    if (!isCalOpen) {
                        Intent intent = new Intent(FileCategoryActivity.this.getActivity(),
                                CameraActivity.class);
                        startActivity(intent);
                    } else {
                        Calendar c = Calendar.getInstance();
                        int mYear = c.get(Calendar.YEAR);
                        int mMonth = c.get(Calendar.MONTH);
                        int mDay = c.get(Calendar.DAY_OF_MONTH);
                        System.out.println("the selected " + mDay);
                        DatePickerDialog dialog = new DatePickerDialog(getContext(),
                                new mDateSetListener(), mYear, mMonth, mDay);
                        dialog.show();
                    }
                }
                if (f.name().equals("Calendar")) {  // Caldroid lib
                    if (isCalOpen)
                    {
                        deAttachCalendarFragment();
                    }
                    else
                    {
                        attachCalendarFragment();
                    }
                }
                if (f.name().equals("Favorite")){

                    if (!CalendarUtil.getScanDatesMap(GlobalConsts.ROOT_PATH).isEmpty()) {
                        Intent intent = new Intent(getContext(), ChartActivity.class);
                        getContext().startActivity(intent);
                    } else {
                        //showSimpleAlertDialog(Context context, String title, String content, int type) {
                        DialogUtil.showSimpleAlertDialog(myContext, "no data available", "", DialogUtil.TYPE_ERROR);
                    }

                }

            }
            /*   used for favorite tab before
            else {
                onCategorySelected(f);      // response id clicked
                if (f != FileCategory.Favorite) {
                    setHasOptionsMenu(true);
                }
            }
            */
        }
    };

    public void attachCalendarFragment(){
        myCaldroid = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
        myCaldroid.setArguments(args);

        final Map datesMap = CalendarUtil.getScanDatesMap(GlobalConsts.ROOT_PATH);
        Iterator it = datesMap.entrySet().iterator();
        journalDB = new JournalDB(myContext);
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Date date = (Date)pair.getKey();

            if (journalDB.isExistingKey(CalendarUtil.DateToStringYYmmdd(date))){
                myCaldroid.setTextColorForDate(R.color.blue, date);
            }
            switch ((Integer)pair.getValue()) {
                case 0:
                    myCaldroid.setBackgroundResourceForDate(R.color.cal_red1, date);
                    break;
                case 1:
                    myCaldroid.setBackgroundResourceForDate(R.color.cal_red2, date);
                    break;
                case 2:
                    myCaldroid.setBackgroundResourceForDate(R.color.cal_red3, date);
                    break;
                case 3:
                    myCaldroid.setBackgroundResourceForDate(R.color.cal_red4, date);
                    break;
                case 4:
                    myCaldroid.setBackgroundResourceForDate(R.color.cal_red5, date);
                    break;
                default:
                    myCaldroid.setBackgroundResourceForDate(R.color.cal_red6, date);
                    break;
            }

        }

        final CaldroidListener listener = new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {

                if (datesMap.containsKey(date))
                {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                    String DateStr = formatter.format(date);
                    DialogUtil.showSimpleAlertDialog(getContext(), "Scan information",
                            DateStr.substring(0, 4) + " " +
                                    CalendarUtil.getMonthInWords(DateStr.substring(5, 6)) + " " +
                                    CalendarUtil.getDayInWords(Integer.toString(cal.get(Calendar.DAY_OF_WEEK))) + "\n" +
                                    "you have taken " + Integer.toString((Integer) datesMap.get(date)) + " time(s)",
                            DialogUtil.TYPE_UNDER_TEXT);
                }
            }


            /**
             * Inform client user has long clicked on a date
             */
            public void onLongClickDate(Date date, View view) {
                // for journal page
                final SweetAlertDialog dialog;
                String formatDate = CalendarUtil.DateToStringYYmmdd(date);    // db key
                GlobalConsts.CALENDAR_CURRENT_DATE = formatDate;
                journalDB = new JournalDB(myContext);
                if (datesMap.containsKey(date)) {

                    if (journalDB.isExistingKey(formatDate)) {
                        Journal journal = journalDB.getJournalByDate(formatDate);
                        dialog = new SweetAlertDialog(myContext, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
                                dialog.setTitleText("Your Journal").setCustomImage(R.drawable.journal_label_big)
                                .setContentText(journal.getContent())
                                .setCancelText("cancel").setConfirmText("edit").showCancelButton(true)
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        GlobalConsts.JOURNAL_REEDIT = true;
                                        dialog.dismiss();
                                        JournalDialog journalDialog = new JournalDialog();
                                        journalDialog.show(myContext.getSupportFragmentManager(), "journalDialog");
                                    }
                                }).show();

                    } else {   // ok

                        JournalDialog journalDialog = new JournalDialog();
                        journalDialog.show(myContext.getSupportFragmentManager(), "journalDialog");

                    }
                    // Dialog.showSimpleAlertDialog(getContext(), "Scan information","you long pressed", Dialog.TYPE_SUCCESS);
                }
            }
            /**
             * Inform client that calendar has changed month
             */
            public void onChangeMonth(int month, int year) {
                // Do nothing
            }



            /**
             * Inform client that CaldroidFragment view has been created and views are
             * no longer null. Useful for customization of button and text views
             */
            public void onCaldroidViewCreated() {
                // change the button view and textview after calendar is created
                ImageView btTimePicker = (ImageView) getActivity().findViewById(R.id.bt_camera);
                TextView txtTimePicker = (TextView) getActivity().findViewById(R.id.text_camera);
                btTimePicker.setImageDrawable(getResources().getDrawable(R.drawable.category_icon_timepicker));
                txtTimePicker.setText(R.string.category_time_picker);

                btTimePicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                         class StartDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {
                            @Override
                            public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
                                // TODO Auto-generated method stub
                                // Use the current date as the default date in the picker

                                final Calendar c = Calendar.getInstance();
                                int year = c.get(Calendar.YEAR);
                                int month = c.get(Calendar.MONTH);
                                int day = c.get(Calendar.DAY_OF_MONTH);

                                // Create a new instance of DatePickerDialog and return it
                                return new DatePickerDialog(getActivity(), this, year, month, day);
                            }

                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                                Calendar c = Calendar.getInstance();
                                c.set(i,i2,i3);
                                myCaldroid.setCalendarDate(c.getTime());
                            }
                        }
                    }} );

                }
        };



        myCaldroid.setCaldroidListener(listener);

        //LinearLayout camera = (LinearLayout) getActivity().findViewById(R.id.category_camera);
        //camera.setVisibility(View.INVISIBLE);
        LinearLayout favorite = (LinearLayout) getActivity().findViewById(R.id.category_favorite);
        favorite.setVisibility(View.INVISIBLE);

        android.support.v4.app.FragmentTransaction t = myContext
                .getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar1, myCaldroid);
        t.commit();
        isCalOpen = true;
    }



    public void deAttachCalendarFragment(){
        android.support.v4.app.FragmentTransaction t = myContext.
                getSupportFragmentManager().beginTransaction();
        // solved http://stackoverflow.com/questions/27724636/fragmentmanager-begintransaction-cannot-be-applied-to-int-android-app-fragme
        t.remove(myContext.getSupportFragmentManager().findFragmentById(R.id.calendar1));
        t.commit();
        LinearLayout camera = (LinearLayout) getActivity().findViewById(R.id.category_camera);
        camera.setVisibility(View.VISIBLE);
        LinearLayout favorite = (LinearLayout) getActivity().findViewById(R.id.category_favorite);
        favorite.setVisibility(View.VISIBLE);

        ImageView btTimePicker = (ImageView) getActivity().findViewById(R.id.bt_camera);
        TextView txtTimePicker = (TextView) getActivity().findViewById(R.id.text_camera);
        btTimePicker.setImageDrawable(getResources().getDrawable(R.drawable.category_icon_camera));
        txtTimePicker.setText(R.string.category_camera);
        isCalOpen = false;
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    // button listeners
    private void setupClick(int id) {
            View button = mRootView.findViewById(id);
            button.setOnClickListener(onClickListener);
    }

    private void setupClick() {

        setupClick(R.id.category_camera);
        //setupClick(R.id.category_video);
        setupClick(R.id.category_calendar);
        //setupClick(R.id.category_theme);
        //setupClick(R.id.category_document);
        //setupClick(R.id.category_zip);
        //setupClick(R.id.category_apk);
        setupClick(R.id.category_favorite);
    }

    @Override
    public boolean onBack() {
        if (isHomePage() || curViewPage == ViewPage.NoSD || mFileViewInteractionHub == null) {
            return false;
        }

        return mFileViewInteractionHub.onBackPressed();
    }

    public boolean isHomePage() {
        return curViewPage == ViewPage.Home;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (curViewPage != ViewPage.Category && curViewPage != ViewPage.Favorite) {
            return;
        }
        mFileViewInteractionHub.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (!isHomePage() && mFileCagetoryHelper.getCurCategory() != FileCategory.Favorite) {
            mFileViewInteractionHub.onPrepareOptionsMenu(menu);
        }
    }

    public boolean onRefreshFileList(String path, FileSortHelper sort) {
        FileCategory curCategory = mFileCagetoryHelper.getCurCategory();
        if (curCategory == FileCategory.Favorite || curCategory == FileCategory.All)
            return false;

        Cursor c = mFileCagetoryHelper.query(curCategory, sort.getSortMethod());
        showEmptyView(c == null || c.getCount() == 0);
        mAdapter.changeCursor(c);

        return true;
    }

    @Override
    public View getViewById(int id) {
        return mRootView.findViewById(id);
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public void onDataChanged() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                mFavoriteList.getArrayAdapter().notifyDataSetChanged();
                showEmptyView(mAdapter.getCount() == 0);
            }

        });
    }

    @Override
    public void onPick(FileInfo f) {
        // do nothing
    }

    @Override
    public boolean shouldShowOperationPane() {
        return true;
    }

    @Override
    public boolean onOperation(int id) {
        mFileViewInteractionHub.addContextMenuSelectedItem();
        switch (id) {
            case R.id.button_operation_copy:
            case GlobalConsts.MENU_COPY:
                copyFileInFileView(mFileViewInteractionHub.getSelectedFileList());
                mFileViewInteractionHub.clearSelection();
                break;
            case R.id.button_operation_move:
            case GlobalConsts.MENU_MOVE:
                startMoveToFileView(mFileViewInteractionHub.getSelectedFileList());
                mFileViewInteractionHub.clearSelection();
                break;
            case GlobalConsts.OPERATION_UP_LEVEL:
                setHasOptionsMenu(false);
                showPage(ViewPage.Home);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public String getDisplayPath(String path) {
        return getString(R.string.tab_category) + path;
    }

    @Override
    public String getRealPath(String displayPath) {
        return "";
    }

    @Override
    public boolean onNavigation(String path) {
        showPage(ViewPage.Home);
        return true;
    }

    @Override
    public boolean shouldHideMenu(int menu) {

        return (menu == GlobalConsts.MENU_NEW_FOLDER || menu == GlobalConsts.MENU_FAVORITE
                || menu == GlobalConsts.MENU_PASTE || menu == GlobalConsts.MENU_SHOWHIDE);
    }

    @Override
    public void addSingleFile(FileInfo file) {
        refreshList();
    }

    @Override
    public Collection<FileInfo> getAllFiles() {
        return mAdapter.getAllFiles();
    }

    @Override
    public FileInfo getItem(int pos) {
        return mAdapter.getFileItem(pos);
    }

    @Override
    public int getItemCount() {
        return mAdapter.getCount();
    }

    @Override
    public void sortCurrentList(FileSortHelper sort) {
        refreshList();
    }

    private void refreshList() {
        mFileViewInteractionHub.refreshFileList();
    }

    private void copyFileInFileView(ArrayList<FileInfo> files) {
        if (files.size() == 0) return;
        mFileViewActivity.copyFile(files);
        mActivity.getActionBar().setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
    }

    private void startMoveToFileView(ArrayList<FileInfo> files) {
        if (files.size() == 0) return;
        mFileViewActivity.moveToFile(files);
        mActivity.getActionBar().setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
    }

    @Override
    public FileIconHelper getFileIconHelper() {
        return mFileIconHelper;
    }

    private static int getCategoryCountId(FileCategory fc) {
        switch (fc) {
            case Camera:
               return R.id.category_music_count;
            //case Video:
            //    return R.id.category_video_count;
            //case Picture:
            //   return R.id.category_picture_count;
            //case Theme:
            //    return R.id.category_theme_count;
            //case Doc:
            //    return R.id.category_document_count;
            //case Zip:
            //    return R.id.category_zip_count;
            //case Apk:
            //    return R.id.category_apk_count;
            case Favorite:
                return R.id.category_favorite_count;
        }

        return 0;
    }

    // metrics for lengend builder
    private void setCategorySize(FileCategory fc, long size) {
        int txtId = 0;
        int resId = 0;
        switch (fc) {
            case Music:
                txtId = R.id.category_legend_music;
                resId = R.string.category_music;
                break;
            case Video:
                txtId = R.id.category_legend_video;
                resId = R.string.category_video;
                break;
            case Picture:
                txtId = R.id.category_legend_picture;
                resId = R.string.category_picture;
                break;
            case Theme:
                txtId = R.id.category_legend_theme;
                resId = R.string.category_theme;
                break;
            case Doc:
                txtId = R.id.category_legend_document;
                resId = R.string.category_document;
                break;
            case Zip:
                txtId = R.id.category_legend_zip;
                resId = R.string.category_zip;
                break;
            case Apk:
                txtId = R.id.category_legend_apk;
                resId = R.string.category_apk;
                break;
            case Other:
                txtId = R.id.category_legend_other;
                resId = R.string.category_other;
                break;
        }

        if (txtId == 0 || resId == 0)
            return;
        // get the size of category types
        setTextView(txtId, getString(resId) + ":" + Util.convertStorage(size));
    }

    private void setCategoryBarValue(FileCategory f, long size) {
        if (mCategoryBar == null) {
            mCategoryBar = (CategoryBar) mRootView.findViewById(R.id.category_bar);
        }
        mCategoryBar.setCategoryValue(categoryIndex.get(f), size);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mActivity != null) {
            mActivity.unregisterReceiver(mScannerReceiver);
        }
    }

    private class ScannerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(LOG_TAG, "received broadcast: " + action);
            // handle intents related to external storage
            if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED) || action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                notifyFileChanged();
            }
        }
    }

    private void updateUI() {
        boolean sdCardReady = Util.isSDCardReady();
        if (sdCardReady) {
            if (preViewPage != ViewPage.Invalid) {
                showPage(preViewPage);
                preViewPage = ViewPage.Invalid;
            } else if (curViewPage == ViewPage.Invalid || curViewPage == ViewPage.NoSD) {
                showPage(ViewPage.Home);
            }
            refreshCategoryInfo();
            // refresh file list
            mFileViewInteractionHub.refreshFileList();
            // refresh file list view in another tab
            mFileViewActivity.refresh();
        } else {
            preViewPage = curViewPage;
            showPage(ViewPage.NoSD);
        }
    }

    // process file changed notification, using a timer to avoid frequent
    // refreshing due to batch changing on file system
    synchronized public void notifyFileChanged() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                timer = null;
                Message message = new Message();
                message.what = MSG_FILE_CHANGED_TIMER;
                handler.sendMessage(message);
            }

        }, 1000);
    }

    private static final int MSG_FILE_CHANGED_TIMER = 100;

    private Timer timer;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FILE_CHANGED_TIMER:
                    updateUI();
                    break;
            }
            super.handleMessage(msg);
        }

    };

    // update the count of favorite
    @Override
    public void onFavoriteDatabaseChanged() {
        setCategoryCount(FileCategory.Favorite, mFavoriteList.getCount());
    }

    @Override
    public void runOnUiThread(Runnable r) {
        mActivity.runOnUiThread(r);
    }


    class mDateSetListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            Calendar c = Calendar.getInstance();
            c.set(year,monthOfYear,dayOfMonth);
            myCaldroid.moveToDate(c.getTime());

        }
    }

}
