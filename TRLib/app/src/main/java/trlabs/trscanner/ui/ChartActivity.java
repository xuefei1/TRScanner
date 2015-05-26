package trlabs.trscanner.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import trlabs.trscanner.R;
import trlabs.trscanner.trtabs.config.GlobalConsts;


public class ChartActivity extends BaseActivity {

    private LineChart mChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mChart = (LineChart) findViewById(R.id.chart1);
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


    private void setData() {

        Map<Date,Integer> dataSource = CalendarUtil.getScanDatesMap(GlobalConsts.ROOT_PATH);
        List<Integer> valList = new ArrayList<Integer>(dataSource.values());
        List<Date> sortedDateList = new ArrayList<Date>();
        int minDate, maxDate;
        int YMin = Collections.min(valList);
        int YMax = Collections.max(valList);

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

