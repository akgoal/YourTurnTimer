package com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.timerdisplay.tabledisplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;

import com.deakishin.yourturntimer.R;

/**
 * Created by Dmitry Akishin on 25.01.2017.
 * <p>
 * Button that displays progress as a border around it.
 * This class is also manages drawing background.
 */

public class ButtonWithBorderProgress extends Button {

    /**
     * Colors for displaying progress and its background.
     */
    private int mProgressColor, mProgressBgColor,
            mDisabledTintColor,
            mBgColor, mBgPressedColor,
            mFlashTintColor;

    /**
     * Paint for drawing progress colors.
     */
    private Paint mProgressPaint, mProgressBgPaint,
            mDisabledTintBgPaint, mDisabledTintProgressPaint,
            mBgPaint, mBgPressedPaint,
            mFlashTintPaint;

    /**
     * Progress from 0 to 1.
     */
    private float mProgress;

    /* Constants for drawing flash animation when little time if left. */
    private static final long FLASH_START = 10500;
    private static final long FLASH_CYCLE = 1000;
    private static final long FLASH_MAX_TRANSPARENCY = 96;

    /* Constant parameters for drawing in pixels, converted from dp. */
    private final int CORNER_RADIUS = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8, this.getContext().getResources().getDisplayMetrics());

    private final int STROKE_WIDTH = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8, this.getContext().getResources().getDisplayMetrics());

    public ButtonWithBorderProgress(Context context) {
        this(context, null);
    }

    public ButtonWithBorderProgress(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialise colors from resources.
        mProgressColor = ContextCompat.getColor(context, R.color.timer_switch_progress_color);
        mProgressBgColor = ContextCompat.getColor(context, R.color.timer_switch_progress_bg_color);

        mBgColor = ContextCompat.getColor(context, R.color.timer_switch_bg_color_normal);
        mBgPressedColor = ContextCompat.getColor(context, R.color.timer_switch_bg_color_pressed);

        mDisabledTintColor = ContextCompat.getColor(context, R.color.timer_switch_disabled_tint);

        mFlashTintColor = ContextCompat.getColor(context, R.color.timer_switch_flash_tint);

        createPaints();
    }

    /* Creates paints for drawing progress and its background. */
    private void createPaints() {
        mProgressPaint = new Paint();
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(STROKE_WIDTH);
        mProgressPaint.setColor(mProgressColor);
        mProgressBgPaint = new Paint(mProgressPaint);
        mProgressBgPaint.setColor(mProgressBgColor);
        mProgressBgPaint.setPathEffect(new CornerPathEffect(CORNER_RADIUS));

        mBgPaint = new Paint();
        mBgPaint.setColor(mBgColor);
        mBgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mBgPaint.setPathEffect(new CornerPathEffect(CORNER_RADIUS));
        mBgPressedPaint = new Paint(mBgPaint);
        mBgPressedPaint.setColor(mBgPressedColor);

        mDisabledTintBgPaint = new Paint(mBgPaint);
        mDisabledTintBgPaint.setColor(mDisabledTintColor);
        mDisabledTintProgressPaint = new Paint(mProgressPaint);
        mDisabledTintProgressPaint.setColor(mDisabledTintColor);

        mFlashTintPaint = new Paint(mBgPaint);
        mFlashTintPaint.setColor(mFlashTintColor);
    }

    /**
     * Sets displayed progress.
     *
     * @param progress     progress that must be from 0 to 1;
     *                     otherwise the closest value is set.
     * @param milliSecLeft number of milliseconds left;
     *                     needed for animation when little time is left.
     */
    public void setProgress(float progress, long milliSecLeft) {
        if (progress < 0) {
            mProgress = 0;
        } else if (progress > 1) {
            mProgress = 1;
        } else {
            mProgress = progress;
        }

        float flashTransp = 0;
        if (milliSecLeft <= FLASH_START) {
            flashTransp = 2 * Math.abs(milliSecLeft % FLASH_CYCLE / (float) FLASH_CYCLE - 0.5f);
        }
        mFlashTintPaint.setAlpha((int) (FLASH_MAX_TRANSPARENCY * flashTransp));

        this.invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        int w = this.getWidth() - STROKE_WIDTH;
        int h = this.getHeight() - STROKE_WIDTH;

        int l = STROKE_WIDTH / 2;
        int t = STROKE_WIDTH / 2;

        //Draw background.
        canvas.drawRect(l, t, l + w, t + h,
                isEnabled() && isPressed() ? mBgPressedPaint : mBgPaint);
        if (!isEnabled()) {
            canvas.drawRect(l, t, l + w, t + h, mDisabledTintBgPaint);
        } else {
            canvas.drawRect(l, t, l + w, t + h, mFlashTintPaint);
        }

        //Draw progress.
        canvas.drawRect(l, t, l + w, t + h, mProgressBgPaint);

        int r = CORNER_RADIUS;
        if (r > w / 2) r = w / 2;
        if (r > h / 2) r = h / 2;

        int wholePathLen = 2 * (w + h) - 8 * r;
        int progressLineLen = (int) (wholePathLen * mProgress);

        drawProgress(canvas, progressLineLen, l, t, w, h, r,
                mProgressPaint, isEnabled() ? null : mDisabledTintProgressPaint);

        // Let button draw itself.
        super.onDraw(canvas);
    }

    /* Draws progress line with given length on the border of a rectangle with given
     * width, height and coordinates of the top left corner,
     * and given radius of border curve,
     * starting from point with given starting coordinates.
     * Drawing starts from the center of the top border and goes clockwise.
     * Progress is drawn with paint and tintPaint on top of it (if not null). */
    private void drawProgress(Canvas canvas, int progressLineLen, int top, int left,
                              int w, int h, int r, Paint paint, Paint tintPaint) {
        // Build path on the border with appropriate line length.
        Path path = new Path();
        path.moveTo(left + w / 2, top);
        int lineLen = w / 2 - r;
        if (lineLen > progressLineLen) {
            path.rLineTo(progressLineLen, 0);
        } else {
            path.lineTo(left + w - r, top);
            path.rQuadTo(r, 0, r, r);
            progressLineLen -= lineLen;
            lineLen = h - 2 * r;
            if (lineLen > progressLineLen) {
                path.rLineTo(0, progressLineLen);
            } else {
                path.lineTo(left + w, top + h - r);
                path.rQuadTo(0, r, -r, r);
                progressLineLen -= lineLen;
                lineLen = w - 2 * r;
                if (lineLen > progressLineLen) {
                    path.rLineTo(-progressLineLen, 0);
                } else {
                    path.lineTo(left + r, top + h);
                    path.rQuadTo(-r, 0, -r, -r);
                    progressLineLen -= lineLen;
                    lineLen = h - 2 * r;
                    if (lineLen > progressLineLen) {
                        path.rLineTo(0, -progressLineLen);
                    } else {
                        path.lineTo(left, top + r);
                        path.rQuadTo(0, -r, r, -r);
                        progressLineLen -= lineLen;
                        lineLen = w / 2 - r;
                        if (lineLen > progressLineLen) {
                            path.rLineTo(progressLineLen, 0);
                        } else {
                            path.lineTo(left + w / 2, top);
                        }
                    }
                }
            }
        }

        canvas.drawPath(path, paint);
        if (tintPaint != null) {
            canvas.drawPath(path, tintPaint);
        }
    }
}
