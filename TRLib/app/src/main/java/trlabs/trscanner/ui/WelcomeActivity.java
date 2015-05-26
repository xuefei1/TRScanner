package trlabs.trscanner.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import trlabs.trscanner.R;
import trlabs.trscanner.TRScanner;
import trlabs.trscanner.trtabs.TRScannerTabActivity;
import trlabs.trscanner.utils.FileTools;


public class WelcomeActivity extends BaseActivity{
    private static final String TAG  = "AppStart";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View view = View.inflate(this, R.layout.start, null);
        LinearLayout wellcome = (LinearLayout) view.findViewById(R.id.app_start_view);
        setContentView(view);

        //launching page animation
        AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
        aa.setDuration(3000);
        view.startAnimation(aa);



        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                redirectTo();

                if (!((TRScanner) getApplication()).user.isUserPrefExist())
                    startActivity(new Intent(WelcomeActivity.this,  TRScannerTabActivity.class));

                playWavId(R.raw.wavlogin);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

        });
    }

    private void redirectTo(){
        Intent intent = new Intent(this, TRScannerTabActivity.class);
        startActivity(intent);
        finish();
    }


}



