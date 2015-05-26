package trlabs.trscanner.trtabs.config;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import java.util.Date;

import trlabs.trscanner.TRScanner;

public abstract class GlobalConsts {
    public static final String KEY_BASE_SD = "key_base_sd";

    public static final String KEY_SHOW_CATEGORY = "key_show_category";

    public static final String ScanFolder = "TRScan";
    public static final String SdCard = "sdcard/pictures";
    public static final String RootFolder = "ScanPictures";
    public static final String INTENT_EXTRA_TAB = "TAB";

    public static final String SERVER_ADDR = "http://192.168.100.191/app/php/";
    public static final String PHP_LOGIN = "android-login-portal.php";
    public static final String PHP_UPLOAD = "img-upload.php";
    public static final String HealthRSSSource = "http://www.nlm.nih.gov/medlineplus/feeds/news_en.xml";


    public static String ROOT_PATH;
    public static String UPLOAD_FOLDER_PATH;
    public static String CALENDAR_CURRENT_DATE;     // in the format YYYYmmDD
    public static String CURRENT_DIRECTORY_CURSOR;
    public static Boolean JOURNAL_REEDIT = false;
    public static Boolean UPDATE_CALENDAR_TEXT_COLOR = false;
    // Menu id
    public static final int MENU_NEW_FOLDER = 100;
    public static final int MENU_FAVORITE = 101;
    public static final int MENU_COPY = 104;
    public static final int MENU_PASTE = 105;
    public static final int MENU_MOVE = 106;
    public static final int MENU_SHOWHIDE = 117;
    public static final int MENU_COPY_PATH = 118;

    public static final int OPERATION_UP_LEVEL = 3;

    public static void setRootPath(String path){
        ROOT_PATH = path;
    }
    public static void setUploadPath(String path){
        UPLOAD_FOLDER_PATH = path;
    }
    public static String SEED = "TRLabsTRTech";
    // journal
    public static final int JOURNAL_MAX_LINES = 15;

    public static final String UserProfileDir = "user";

    //crop image
    public static String protraitPath;
    public static Uri origUri;
    public static Uri cropUri;
    public static boolean isNewsFeeed = false;

    public static String USER_PREF_NAME = "user_preference";
    public final static String NOMEDIA = ".nomedia";
}
