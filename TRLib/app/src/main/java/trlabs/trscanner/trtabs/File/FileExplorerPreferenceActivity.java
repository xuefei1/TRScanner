package trlabs.trscanner.trtabs.File;
import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import trlabs.trscanner.R;
import trlabs.trscanner.TRScanner;
import trlabs.trscanner.trtabs.config.GlobalConsts;
import trlabs.trscanner.trtabs.Util;

public class FileExplorerPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = FileExplorerPreferenceActivity.class.getSimpleName();

    private static final String PRIMARY_FOLDER = "pref_key_primary_folder";
    private static final String READ_ROOT = "pref_key_read_root";
    private static final String SHOW_REAL_PATH = "pref_key_show_real_path";
    private static final String SYSTEM_SEPARATOR = File.separator;

    private EditTextPreference mEditTextPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mEditTextPreference = (EditTextPreference) findPreference(PRIMARY_FOLDER);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Setup the initial values
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

        mEditTextPreference.setSummary(this.getString(
                R.string.pref_primary_folder_summary,
                sharedPreferences.getString(PRIMARY_FOLDER, GlobalConsts.ROOT_PATH)));

        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedpreferences, String key) {
        if (PRIMARY_FOLDER.equals(key)) {
            mEditTextPreference.setSummary(this.getString(
                    R.string.pref_primary_folder_summary,
                    sharedpreferences.getString(PRIMARY_FOLDER, GlobalConsts.ROOT_PATH)));
        }
    }

    public static String getPrimaryFolder(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String primaryFolder = settings.getString(PRIMARY_FOLDER, GlobalConsts.ROOT_PATH); //context.getString(R.string.default_primary_folder, GlobalConsts.ROOT_PATH));

        if (TextUtils.isEmpty(primaryFolder)) { // setting primary folder = empty("") create one if not presented
            primaryFolder = GlobalConsts.ROOT_PATH;
            File homeFolder = new File(primaryFolder);
            if (!homeFolder.exists()){
                if (!homeFolder.mkdir()) {
                    Log.d(LOG_TAG, "Failed to create home directory");
                    return null;
                }
            }
        }

        // it's remove the end char of the home folder setting when it with the '/' at the end.
        // if has the backslash at end of the home folder, it's has minor bug at "UpLevel" function.
        int length = primaryFolder.length();
        int HomeLength = GlobalConsts.ROOT_PATH.length();
        if (length > HomeLength && SYSTEM_SEPARATOR.equals(primaryFolder.substring(length - 1))) {           // length = 1, ROOT_PATH
            return primaryFolder.substring(0, length - 1);
        } else {
            return primaryFolder;
        }
    }


    // for root directory
    public static boolean isReadRoot(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        boolean isReadRootFromSetting = settings.getBoolean(READ_ROOT, false);
        boolean isReadRootWhenSettingPrimaryFolderWithoutSdCardPrefix = !getPrimaryFolder(context).startsWith(Util.getSdDirectory());

        return isReadRootFromSetting || isReadRootWhenSettingPrimaryFolderWithoutSdCardPrefix;
    }
    
    public static boolean showRealPath(Context context) {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    	return settings.getBoolean(SHOW_REAL_PATH, false);
    }

}
