package com.commeto.kuleuven.MP.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Jonas on 13/04/2018.
 *
 * viewPAger that does not allow scrolling through pages.
 */

public class LockableViewPager extends ViewPager {

    private boolean swipeLocked;

    public LockableViewPager(Context context) {
        super(context);
        this.swipeLocked = true;
    }

    public LockableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.swipeLocked = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !swipeLocked && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return !swipeLocked && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return !swipeLocked && super.canScrollHorizontally(direction);
    }
}
