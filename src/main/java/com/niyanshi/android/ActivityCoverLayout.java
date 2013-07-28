package com.niyanshi.android;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class ActivityCoverLayout extends LinearLayout {

    private int mDragableResourceId = -1;

    private static final int MODE_IDLE = 0;

    private static final int MODE_DRAGGING = 1;

    private static final int MODE_RELEASING = 2;

    private float mMaximumVelocity;

    private float mMinimumVelocity;

    private Scroller mScroller;

    private int mMode = MODE_IDLE;

    private OnDisappearListener mOnDisappareListener;

    private int mBaseScrollTime = 1000;

    private float mStartY;

    private float mLastY;

    private int mScrollStart = 0;

    private VelocityTracker mVelocityTracker;
    private static final int SCROLL_VELOCITY_FACTOR = 4;

    public ActivityCoverLayout(Context context) {
        super(context);
        init(context);
    }


    public ActivityCoverLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        mScroller = new Scroller(context);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

    }

    private void obtainVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }


    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mMode == MODE_RELEASING) {
            return false;
        }

        int action = event.getAction();
        obtainVelocityTracker(event);

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (isDraggable(event)) {
                    mMode = MODE_DRAGGING;
                    mLastY = event.getY();
                    mStartY = event.getY();

                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mMode == MODE_DRAGGING) {
                    float deltaY = mLastY - event.getY();

                    if (event.getY() >= mStartY) {
                        mScroller.startScroll(0, mScrollStart, 0, (int) deltaY);
                        invalidate();
                    }
                    mLastY = event.getY();
                    mScrollStart += deltaY;
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (mMode == MODE_DRAGGING) {
                    mMode = MODE_RELEASING;
                    mLastY = event.getY();
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getYVelocity();
                    if ((Math.abs(initialVelocity) > mMinimumVelocity) && Math.abs(getScrollY()) > 30 && initialVelocity > 0 || Math.abs(getScrollY()) > getHeight() / 3) {
                        slideDown(initialVelocity);
                    } else {
                        slideDown();
                    }
                    mScrollStart = 0;
                    releaseVelocityTracker();
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    public void slideUp() {
        slideUp(1000);
    }

    public void slideUp(int duration) {

        if (duration != 0) {
            mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), duration);
        } else {
            mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), 1000);
        }
        invalidate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mScroller.computeScrollOffset()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        //ignore;
                    }
                }
                mMode = MODE_IDLE;
            }
        }).start();
        mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), 1000);
        mMode = MODE_IDLE;
    }

    public void slideDown() {
        if (mMode == MODE_IDLE) {
            slideDown(1000, 0, -getHeight());
        }
    }

    private void slideDown(int initialVelocity) {
        float distance = getHeight() - getScrollY();
        int duration = Math.min(2000, Math.max(1000, (int) (mBaseScrollTime * initialVelocity * SCROLL_VELOCITY_FACTOR / (mMaximumVelocity))));

        slideDown(duration, mScrollStart, (int) -distance);
    }

    private void slideDown(int duration, int startY, int distance) {
        mScroller.startScroll(0, startY, 0, distance, duration);
        invalidate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mScroller.computeScrollOffset()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        //ignore;
                    }
                }
                if (mOnDisappareListener != null) {
                    mOnDisappareListener.onDisappear();
                }
                mMode = MODE_IDLE;
            }
        }).start();
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY() > 0 ? 0 : mScroller.getCurrY());
            postInvalidate();
        }
    }

    public interface OnDisappearListener {
        void onDisappear();
    }

    public void setOnDisappareListener(OnDisappearListener onDisappareListener) {
        mOnDisappareListener = onDisappareListener;
    }

    public void setDragableResourceId(int dragableResourceId) {
        mDragableResourceId = dragableResourceId;
    }

    private boolean isDraggable(MotionEvent event) {
        View view = findViewById(mDragableResourceId);
        if (view == null) {
            return true;
        }

        Rect rect = new Rect();
        view.getDrawingRect(rect);
        if (rect.contains((int) event.getX(), (int) event.getY())) {
            return true;
        } else {
            return false;
        }
    }
}