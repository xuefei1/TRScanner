package trlabs.trscanner.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

// monitor network status change
public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStateREceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isNetworkAvailable(context)) {
            ToastUtil.showToast(context, "TRScanner: network is disconnected");
        } else {
            ToastUtil.showToast(context, "TRScanner: network is connected");
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager mgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        //current network connection list
        NetworkInfo[] info = mgr.getAllNetworkInfo();
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }
}
