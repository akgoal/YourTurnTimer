package com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.timerdisplay.tabledisplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.deakishin.yourturntimer.R;

/**
 * Created by Dmitry Akishin on 24.01.2017.
 * <p>
 * View that can display progress.
 * It's a rectangle view that is filled from left to right
 * according to progress.
 */

public class ProgressView extends View {

    /** Colors for displaying progress and its background. */
    protected int mProgressColor, mBgColor;

    /** Paint for drawing progress colors. */
    protected Paint mProgressPaint, mBgPaint;

    /** Progress from 0 to 1. */
    protected float mProgress;

    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialise colors from resources.
        mProgressColor = ContextCompat.getColor(context, getProgressColorResId());
        mBgColor = ContextCompat.getColor(context, getBgColorResId());

        // Create paints.
        mProgressPaint = new Paint();
        mProgressPaint.setColor(mProgressColor);
        mBgPaint = new Paint();
        mBgPaint.setColor(mBgColor);
    }

    /**
     * Returns resource ids for color to use for drawing progress.
     *
     * @return resource identifier for color.
     */
    protected int getProgressColorResId() {
        return R.color.timer_list_item_progress_color;
    }

    /**
     * Returns resource ids for color to use for drawing background for progress.
     *
     * @return resource identifier for color.
     */
    protected int getBgColorResId() {
        return R.color.timer_list_item_progress_bg_color;
    }

    /**
     * Sets displayed progress.
     *
     * @param progress progress that must be ffrom 0 to 1;
     *                 otherwise the closest value is set.
     */
    public void setProgress(float progress) {
        if (progress < 0) {
            mProgress = 0;
        } else if (progress > 1) {
            mProgress = 1;
        } else {
            mProgress = progress;
        }
        this.invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        int w = this.getWidth();
        int h = this.getHeight();

        if (mBgPaint != null) {
            canvas.drawRect(0, 0, w, h, mBgPaint);
        }
        if (mProgressPaint != null) {
            canvas.drawRect(0, 0, w * mProgress, h, mProgressPaint);
        }
    }
}
