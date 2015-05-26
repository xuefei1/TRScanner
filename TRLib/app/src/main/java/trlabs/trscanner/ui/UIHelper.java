package trlabs.trscanner.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.view.View;

import trlabs.trscanner.R;
import trlabs.trscanner.TRScanner;
import trlabs.trscanner.trtabs.TRScannerTabActivity;
import trlabs.trscanner.ui.greendroid.MyQuickAction;
import trlabs.trscanner.ui.greendroid.QuickAction;

public class UIHelper {

    public static View.OnClickListener finish(final Activity activity) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                activity.finish();
            }
        };
    }

    public static void showLoginDialog(Context context) {   // running in which activity
        Intent intent = new Intent(context, LoginDialog.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void showSettingLoginOrLogout(Activity activity,QuickAction qa)
    {
        if(((TRScanner)activity.getApplication()).user.getLoginState()){
            qa.setIcon(MyQuickAction.buildDrawable(activity, R.drawable.qa_logout));
            qa.setTitle(activity.getString(R.string.qa_logout));
        }else{
            qa.setIcon(MyQuickAction.buildDrawable(activity, R.drawable.qa_user));
            qa.setTitle(activity.getString(R.string.qa_user));
        }
    }
    public static void restart(Activity activity, Intent intent){
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        activity.startActivity(intent);
    }


}
