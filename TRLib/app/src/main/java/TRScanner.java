/**
 * Created by intern2 on 11/12/2014.
 */
package trlabs.trscanner;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.acra.annotation.ReportsCrashes;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import trlabs.trscanner.service.TRService;
import trlabs.trscanner.trtabs.config.GlobalConsts;
import trlabs.trscanner.ui.AppManager;
import trlabs.trscanner.users.Config;
import trlabs.trscanner.users.User;
import trlabs.trscanner.utils.CryptoUtils;
import trlabs.trscanner.utils.DialogUtil;
import trlabs.trscanner.utils.FileTools;
import trlabs.trscanner.utils.NetworkStateReceiver;
import trlabs.trscanner.utils.StringUtils;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.DEVICE_FEATURES;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.PRODUCT;
import static org.acra.ReportField.SHARED_PREFERENCES;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.USER_APP_START_DATE;
import static org.acra.ReportField.USER_CRASH_DATE;


// set this google form key later on
@ReportsCrashes(formKey = ".....", customReportContent = { APP_VERSION_NAME, PHONE_MODEL, BRAND, PRODUCT, ANDROID_VERSION, STACK_TRACE, USER_APP_START_DATE, USER_CRASH_DATE, LOGCAT, DEVICE_FEATURES, SHARED_PREFERENCES })
public class TRScanner extends android.app.Application {
    private NetworkStateReceiver networkStateReceiver = null;
    public User user;

    @Override
    public void onCreate() {
        super.onCreate();
        initialization();
    }

    private void initialization() {
        user = new User(this);
        GlobalConsts.setRootPath(GlobalConsts.SdCard);
        FileTools.createFile(GlobalConsts.ROOT_PATH, GlobalConsts.NOMEDIA);
        GlobalConsts.setUploadPath(FileTools.getAppDirectory(this));
        networkStateReceiver = new NetworkStateReceiver();   // register receiver to monitor network status
        Intent service = new Intent(this, TRService.class);
        startService(service);
        TRService.NAMESPACE = "trlabs.trscanner";
    }

    @Override
    public void onTerminate(){
        unregisterReceiver(networkStateReceiver);
        super.onTerminate();
    }





}