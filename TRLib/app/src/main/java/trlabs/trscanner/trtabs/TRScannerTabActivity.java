package trlabs.trscanner.trtabs;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ActionMode;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import trlabs.trscanner.R;
import trlabs.trscanner.TRScanner;
import trlabs.trscanner.trtabs.config.GlobalConsts;
import trlabs.trscanner.ui.AppManager;
import trlabs.trscanner.ui.UIHelper;
import trlabs.trscanner.ui.greendroid.MyQuickAction;
import trlabs.trscanner.ui.greendroid.QuickActionGrid;
import trlabs.trscanner.ui.greendroid.QuickActionWidget;
import trlabs.trscanner.users.rss.RssListAdapter;
import trlabs.trscanner.users.rss.RssReader;
import trlabs.trscanner.utils.BitmapHelper;
import trlabs.trscanner.utils.CircularImageView;
import trlabs.trscanner.utils.Constants;
import trlabs.trscanner.utils.FileTools;
import trlabs.trscanner.utils.ToastUtil;


public class TRScannerTabActivity extends FragmentActivity {

    private static final String INSTANCESTATE_TAB = "tab";
    private static final int DEFAULT_OFFSCREEN_PAGES = 2;
    CustomViewPager mViewPager;
    TabsAdapter mTabsAdapter;
    ActionMode mActionMode;
    Context context = this;
    ListView list;
    RssListAdapter adapter;
    AlertDialog.Builder builder;
    public static ActionBar bar;
    CircularImageView user_icon;
    private QuickActionWidget mGrid;   // shortcut panel GreenDroid
    static AlertDialog dialog;
    TRScanner ac;

    // AsyncTask for fetch Json objects from RSS feed
    class FeedJobs extends AsyncTask<Void,Void,Void> {
        List<JSONObject> jobs = new ArrayList<JSONObject>();
        @Override
        protected Void doInBackground(Void...params){
            try {
                jobs = RssReader.getLatestRssFeed();   // android.os.NetworkOnMainThreadException when request is too long
            } catch (Exception e) {
                Log.e("RSS ERROR", "Error loading RSS Feed Stream >> " + e.getMessage() + " //" + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            builder.setView(list);
            adapter = new RssListAdapter((Activity) context, jobs);
            list.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            builder.setTitle("Health News").setCancelable(true);
            builder.setPositiveButton("got it!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            dialog = builder.create();
        }
    }


    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pager);
        AppManager.getAppManager().addActivity(this);
        ac = (TRScanner) getApplication();
        mViewPager = (CustomViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(DEFAULT_OFFSCREEN_PAGES);
        initQuickActionGrid();
        bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);


        //View view = new View(context);
        //bar.setCustomView(view);
        // navigation tabs
        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_category),
                FileCategoryActivity.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_doc),
                FileViewActivity.class, null);

        mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_user),
                UserFragment.class, null);
        bar.setSelectedNavigationItem(PreferenceManager.getDefaultSharedPreferences(this).getInt(INSTANCESTATE_TAB, Util.CATEGORY_TAB_INDEX));

        builder = new AlertDialog.Builder(context);
        list = new ListView(context);
        new FeedJobs().execute();

    }


    @Override
    protected void onResume() {
        super.onResume();
        mTabsAdapter.notifyDataSetChanged();
    }


    // intent receiver from UserActivity (set up user profile image)
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        user_icon = (CircularImageView) findViewById(R.id.user_icon);
        if(resultCode != -1 ) return;

        switch(requestCode){
            case Constants.Extra.REQUEST_CODE_GETIMAGE_BYCAMERA:
                startActionCrop(GlobalConsts.origUri, GlobalConsts.cropUri);
                break;
            case Constants.Extra.REQUEST_CODE_GETIMAGE_BYSDCARD:
            case Constants.Extra.REQUEST_CODE_GETIMAGE_BYCROP:
                uploadNewPhoto();
                break;
        }
    }

    private void uploadNewPhoto() {
        Bitmap protraitBitmap = BitmapHelper.loadImgThumbnail(GlobalConsts.protraitPath, 200, 200);

        ac.user.saveUserprofileIMG(protraitBitmap);
        user_icon.setImageBitmap(protraitBitmap);
    }


    private void startActionCrop(Uri data, Uri output) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data, "image/*");
        intent.putExtra("output", output);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        //http://stackoverflow.com/questions/22557965/onactivityresult-in-dialog-fragment
        startActivityForResult(intent, Constants.Extra.REQUEST_CODE_GETIMAGE_BYCROP);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt(INSTANCESTATE_TAB, getActionBar().getSelectedNavigationIndex());
        editor.commit();
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (getActionBar().getSelectedNavigationIndex() == Util.CATEGORY_TAB_INDEX) {
            FileCategoryActivity categoryFragement =(FileCategoryActivity) mTabsAdapter.getItem(Util.CATEGORY_TAB_INDEX);
            if (categoryFragement.isHomePage()) {
                reInstantiateCategoryTab();
            } else {

                categoryFragement.setConfigurationChanged(true);
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    public void reInstantiateCategoryTab() {
        mTabsAdapter.destroyItem(mViewPager, Util.CATEGORY_TAB_INDEX,
                mTabsAdapter.getItem(Util.CATEGORY_TAB_INDEX));
        mTabsAdapter.instantiateItem(mViewPager, Util.CATEGORY_TAB_INDEX);
    }

    @Override
    public void onBackPressed() {
        IBackPressedListener backPressedListener = (IBackPressedListener) mTabsAdapter
                .getItem(mViewPager.getCurrentItem());
        if (!backPressedListener.onBack()) {
            super.onBackPressed();
        }
    }


    public interface IBackPressedListener {
        /**
         * deal with back pressed event
         * @return True: proceeded; False: non-proceed, let base class deal with it
         */
        boolean onBack();
    }

    public void setActionMode(ActionMode actionMode) {
        mActionMode = actionMode;
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }

    public Fragment getFragment(int tabIndex) {
        return mTabsAdapter.getItem(tabIndex);
    }

    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     */
     class TabsAdapter extends FragmentPagerAdapter
            implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
        private FragmentManager fm;

        final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }

        public TabsAdapter(Activity activity, ViewPager pager) {
            super(activity.getFragmentManager());
            mContext = activity;
            mActionBar = activity.getActionBar();
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
            this.fm = activity.getFragmentManager();
            mViewPager.setCurrentItem(0);
        }

        public ViewPager getPager(){
            return this.mViewPager;
        }

        @SuppressWarnings("deprecation")
        public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(info);
            mActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @SuppressWarnings("deprecation")
        public void removeTab(int index){
            mTabs.remove(index);
            notifyDataSetChanged();
            mActionBar.removeTabAt(index);
            //mViewPager.setCurrentItem(1);

        }


        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            if (info.fragment == null) {
                info.fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
            }
            return info.fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            Fragment frag = (Fragment) object;
            try{
                UserFragment activity = (UserFragment)object;
                activity.update();
            }catch(Exception e){
                e.printStackTrace();
            }
            return super.getItemPosition(object);
        }


        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }



        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            Object tag = tab.getTag();
            for (int i = 0; i < mTabs.size(); i++) {
                if (mTabs.get(i) == tag) {
                    mViewPager.setCurrentItem(i);
                }
            }
            if(!tab.getText().equals(mContext.getString(R.string.tab_doc))) {
                ActionMode actionMode = ((TRScannerTabActivity) mContext).getActionMode();
                if (actionMode != null) {
                    actionMode.finish();
                }
            }
            if(tab.getText().equals(mContext.getString(R.string.tab_user)) && !((TRScanner) getApplication()).user.getLoginState()) {
                UIHelper.showLoginDialog(mContext);
            }
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }
        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            if(!ac.user.getLoginState() && tab.getText().equals(mContext.getString(R.string.tab_user))) {
                UIHelper.showSettingLoginOrLogout(TRScannerTabActivity.this, mGrid.getQuickAction(0));
                //mGrid.show(tab.getCustomView().findFocus());
            }
        }
    }


    private void initQuickActionGrid(){
        mGrid = new QuickActionGrid(this);

        mGrid.addQuickAction(new MyQuickAction(this, R.drawable.qa_logout, R.string.qa_logout) );
        mGrid.addQuickAction(new MyQuickAction(this, R.drawable.qa_logout, R.string.qa_logout) );
        mGrid.addQuickAction(new MyQuickAction(this, R.drawable.qa_logout, R.string.qa_logout) );
        mGrid.addQuickAction(new MyQuickAction(this, R.drawable.qa_logout, R.string.qa_logout) );
        mGrid.addQuickAction(new MyQuickAction(this, R.drawable.qa_logout, R.string.qa_logout) );
        mGrid.addQuickAction(new MyQuickAction(this, R.drawable.qa_logout, R.string.qa_logout) );

        mGrid.setOnQuickActionClickListener(mActionListener);
    }

    private QuickActionWidget.OnQuickActionClickListener mActionListener = new QuickActionWidget.OnQuickActionClickListener() {
        public void onQuickActionClicked(QuickActionWidget widget, int position) {
            switch (position) {




            }
        }
    };


    @Override
    public void onDestroy(){
            super.onDestroy();
            AppManager.getAppManager().finishActivity(this);
        }



}

