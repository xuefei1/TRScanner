package trlabs.trscanner.trtabs.config;


import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import trlabs.trscanner.R;

public class BannerView extends RelativeLayout implements OnTouchListener {

    /**
     *  menu swipe needed speed
     */
    public static final int SNAP_VELOCITY = 200;
    private int BannerViewWidth;
    private int currentItemIndex;
    private int itemsCount;

    /**
     *  element offset value
     */
    private int[] borders;

    /**
     * swipe to marginleft at most depends on items number
     */
    private int leftEdge = 0;

    /**
     * offset 0 when reach here at most
     */
    private int rightEdge = 0;

    /**
     * pressed x coordinate
     */
    private float xDown;

    /**
     * moved x coordinate
     */
    private float xMove;

    /**
     * uplift x coordinate
     */
    private float xUp;
    private LinearLayout itemsLayout;

    /**
     * labels layout
     */
    private LinearLayout dotsLayout;

    private View firstItem;

    private MarginLayoutParams firstItemParams;

    private VelocityTracker mVelocityTracker;

    /**
     * rewrite BannerView constructor, allow using predefined layout
     * @param context
     * @param attrs
     */
    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BannerView);
        boolean isAutoPlay = a.getBoolean(R.styleable.BannerView_auto_play, false);
        if (isAutoPlay) {
            startAutoPlay();
        }
        a.recycle();
    }

    /**
     * scroll to next item
     */
    public void scrollToNext() {
        new ScrollTask().execute(-20);
    }

    public void scrollToPrevious() {
        new ScrollTask().execute(20);
    }

    public void scrollToFirstItem() {
        new ScrollToFirstItemTask().execute(20 * itemsCount);
    }

    /**
     * timer to operate UI
     */
    private Handler handler = new Handler();

    /**
     * start auto-play, scroll to front when reach the end
     */
    public void startAutoPlay() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentItemIndex == itemsCount - 1) {
                    currentItemIndex = 0;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollToFirstItem();
                            refreshDotsLayout();
                        }
                    });
                } else {
                    currentItemIndex++;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollToNext();
                            refreshDotsLayout();
                        }
                    });
                }
            }
        }, 3000, 3000);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            initializeItems();
            initializeDots();
        }
    }

    /**
     * initialize items, and add listener to each item
     */
    private void initializeItems() {
        BannerViewWidth = getWidth();
        itemsLayout = (LinearLayout) getChildAt(0);
        itemsCount = itemsLayout.getChildCount();
        borders = new int[itemsCount];
        for (int i = 0; i < itemsCount; i++) {
            borders[i] = -i * BannerViewWidth;
            View item = itemsLayout.getChildAt(i);
            MarginLayoutParams params = (MarginLayoutParams) item.getLayoutParams();
            params.width = BannerViewWidth;
            item.setLayoutParams(params);
            item.setOnTouchListener(this);
        }
        leftEdge = borders[itemsCount - 1];
        firstItem = itemsLayout.getChildAt(0);
        firstItemParams = (MarginLayoutParams) firstItem.getLayoutParams();
    }

    /**
     * init dots label
     */
    private void initializeDots() {
        dotsLayout = (LinearLayout) getChildAt(1);
        refreshDotsLayout();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        createVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDown = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                xMove = event.getRawX();
                int distanceX = (int) (xMove - xDown) - (currentItemIndex * BannerViewWidth);
                firstItemParams.leftMargin = distanceX;
                if (beAbleToScroll()) {
                    firstItem.setLayoutParams(firstItemParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                xUp = event.getRawX();
                if (beAbleToScroll()) {
                    if (wantScrollToPrevious()) {
                        if (shouldScrollToPrevious()) {
                            currentItemIndex--;
                            scrollToPrevious();
                            refreshDotsLayout();
                        } else {
                            scrollToNext();
                        }
                    } else if (wantScrollToNext()) {
                        if (shouldScrollToNext()) {
                            currentItemIndex++;
                            scrollToNext();
                            refreshDotsLayout();
                        } else {
                            scrollToPrevious();
                        }
                    }
                }
                recycleVelocityTracker();
                break;
        }
        return false;
    }

    /**
     * test if scrollable, disable when reaches end
     *
     * @return current leftMargin value is between leftEdge and rightEdge returns true, otherwise false
     */
    private boolean beAbleToScroll() {
        return firstItemParams.leftMargin < rightEdge && firstItemParams.leftMargin > leftEdge;
    }

    /**
     * test if user intend to swipe to previous one, if offset is positive, goes to previous
     *
     * @return swipe to previous item returns true; otherwise false。
     */
    private boolean wantScrollToPrevious() {
        return xUp - xDown > 0;
    }

    /**
     * test if user intend to swipe to previous one, if offset is negative, goes to next
     *
     * @return swipe to next item returns true; otherwise false。
     */
    private boolean wantScrollToNext() {
        return xUp - xDown < 0;
    }

    /**
     * test if scroll to next item (cond swipe distance > 1/2 screen width || swipe speed > SNAP_VELOCITY)
     *
     * @return to next item returns true; otherwise false。
     */
    private boolean shouldScrollToNext() {
        return xDown - xUp > BannerViewWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }

    /**
     * same to condition on if goes to previous item
     *
     * @return to previous item returns true; otherwise false。
     */
    private boolean shouldScrollToPrevious() {
        return xUp - xDown > BannerViewWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }

    /**
     * test on label layout, should update when current item index changes
     */
    private void refreshDotsLayout() {
        dotsLayout.removeAllViews();
        for (int i = 0; i < itemsCount; i++) {
            LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(0,
                    LayoutParams.FILL_PARENT);
            linearParams.weight = 1;
            RelativeLayout relativeLayout = new RelativeLayout(getContext());
            ImageView image = new ImageView(getContext());
            if (i == currentItemIndex) {
                image.setBackgroundResource(R.drawable.dot_selected);
            } else {
                image.setBackgroundResource(R.drawable.dot_unselected);
            }
            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            relativeParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            relativeLayout.addView(image, relativeParams);
            dotsLayout.addView(relativeLayout, linearParams);
        }
    }

    /**
     * create VelocityTracker object, add touch event into VelocityTracker
     *
     * @param event
     */
    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     *
     * @return sweep speed takes pixels number as unit
     */
    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }

    /**
     * collecting VelocityTracker object
     */
    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    /**
     * test when menu is scrolled, if it across border, valued stored in @link #borders
     *
     * @param leftMargin 1st item
     * @param speed      scroll speed. > 0 to right, < 0 to left
     * @return if across any border return true; otherwise false。
     */
    private boolean isCrossBorder(int leftMargin, int speed) {
        for (int border : borders) {
            if (speed > 0) {
                if (leftMargin >= border && leftMargin - speed < border) {
                    return true;
                }
            } else {
                if (leftMargin <= border && leftMargin - speed > border) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param leftMargin
     *
     * @return the nearest border value offset from leftMargin
     */
    private int findClosestBorder(int leftMargin) {
        int absLeftMargin = Math.abs(leftMargin);
        int closestBorder = borders[0];
        int closestMargin = Math.abs(Math.abs(closestBorder) - absLeftMargin);
        for (int border : borders) {
            int margin = Math.abs(Math.abs(border) - absLeftMargin);
            if (margin < closestMargin) {
                closestBorder = border;
                closestMargin = margin;
            }
        }
        return closestBorder;
    }

    class ScrollToFirstItemTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... speed) {
            int leftMargin = firstItemParams.leftMargin;
            while (true) {
                leftMargin = leftMargin + speed[0];
                // when leftmargin > 0, means reach 1st item, loop over
                if (leftMargin > 0) {
                    leftMargin = 0;
                    break;
                }
                publishProgress(leftMargin);
                sleep(20);
            }
            return leftMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... leftMargin) {
            firstItemParams.leftMargin = leftMargin[0];
            firstItem.setLayoutParams(firstItemParams);
        }

        @Override
        protected void onPostExecute(Integer leftMargin) {
            firstItemParams.leftMargin = leftMargin;
            firstItem.setLayoutParams(firstItemParams);
        }

    }

    class ScrollTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... speed) {
            int leftMargin = firstItemParams.leftMargin;
            while (true) {
                leftMargin = leftMargin + speed[0];
                if (isCrossBorder(leftMargin, speed[0])) {
                    leftMargin = findClosestBorder(leftMargin);
                    break;
                }
                publishProgress(leftMargin);
                // 10 millisecond for visualize scrolling effect
                sleep(10);
            }
            return leftMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... leftMargin) {
            firstItemParams.leftMargin = leftMargin[0];
            firstItem.setLayoutParams(firstItemParams);
        }

        @Override
        protected void onPostExecute(Integer leftMargin) {
            firstItemParams.leftMargin = leftMargin;
            firstItem.setLayoutParams(firstItemParams);
        }
    }

    /**
     * post delay to auto-play
     *
     * @param millis
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
