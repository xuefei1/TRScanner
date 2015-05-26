package trlabs.trscanner.ui;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import trlabs.trscanner.R;
import trlabs.trscanner.trtabs.TRScannerTabActivity;


public class GuideActivity extends BaseActivity implements OnPageChangeListener {

    private ViewPager viewPager;
    private ArrayList<View> views;
    private View view1, view2, view3, view4;
    private TextView btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        initView();

    }


    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        LayoutInflater mLi = LayoutInflater.from(this);
        view1 = mLi.inflate(R.layout.guideview1, null);
        view2 = mLi.inflate(R.layout.guideview2, null);
        view3 = mLi.inflate(R.layout.guideview3, null);
        view4 = mLi.inflate(R.layout.guideview4, null);


        views = new ArrayList<View>();
        views.add(view1);
        views.add(view2);
        views.add(view3);
        views.add(view4);


        viewPager.setOnPageChangeListener(this);

        viewPager.setAdapter(new ViewPagerAdapter(views));


        btnStart = (TextView) view4.findViewById(R.id.startBtn);

        btnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playWavId(R.raw.wavclick);
                //WelcomeActivity.sp.edit()
                     //   .putInt("VERSION", WelcomeActivity.VERSION).commit();
                redirectTo();
            }
        });
    }

    private void redirectTo(){
        Intent intent = new Intent(this, TRScannerTabActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onPageScrollStateChanged(int arg0) {

    }


    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }


    @Override
    public void onPageSelected(int arg0) {
    }

}