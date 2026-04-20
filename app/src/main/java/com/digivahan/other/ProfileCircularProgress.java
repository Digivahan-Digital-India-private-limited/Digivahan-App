package com.digivahan.other;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.digivahan.R;

public class ProfileCircularProgress extends View {

    private int progress = 0, max = 100;
    private int indicatorColor = Color.parseColor("#4CAF50");
    private int trackColor = Color.parseColor("#D9D9D9");
    private float trackThickness;      // e.g., 10dp
    private float indicatorThickness;  // e.g., 15dp
    private boolean startAtBottom = true;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    public ProfileCircularProgress(Context context) {
        this(context, null);
    }

    public ProfileCircularProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProfileCircularProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float dp10 = dp(10);
        float dp15 = dp(15);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProfileCircularProgress);
            progress = a.getInt(R.styleable.ProfileCircularProgress_pcp_progress, 0);
            max = a.getInt(R.styleable.ProfileCircularProgress_pcp_max, 100);
            indicatorColor = a.getColor(R.styleable.ProfileCircularProgress_pcp_indicatorColor, indicatorColor);
            trackColor = a.getColor(R.styleable.ProfileCircularProgress_pcp_trackColor, trackColor);
            trackThickness = a.getDimension(R.styleable.ProfileCircularProgress_pcp_trackThickness, dp10);
            indicatorThickness = a.getDimension(R.styleable.ProfileCircularProgress_pcp_indicatorThickness, dp15);
            startAtBottom = a.getBoolean(R.styleable.ProfileCircularProgress_pcp_startAtBottom, true);
            a.recycle();
        } else {
            trackThickness = dp10;
            indicatorThickness = dp15;
        }

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND); // full circle; cap won't show but nice to keep consistent
        trackPaint.setStrokeWidth(trackThickness);
        trackPaint.setColor(trackColor);

        indicatorPaint.setStyle(Paint.Style.STROKE);
        indicatorPaint.setStrokeCap(Paint.Cap.ROUND); // << rounded ends!
        indicatorPaint.setStrokeWidth(indicatorThickness);
        indicatorPaint.setColor(indicatorColor);
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float inset = Math.max(trackThickness, indicatorThickness) / 2f;
        arcBounds.set(getPaddingLeft() + inset,
                getPaddingTop() + inset,
                w - getPaddingRight() - inset,
                h - getPaddingBottom() - inset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // background ring
        canvas.drawArc(arcBounds, 0, 360, false, trackPaint);

        // progress arc
        float sweep = max > 0 ? (progress * 360f / max) : 0f;
        float startAngle = startAtBottom ? 90f : -90f; // 90° = bottom, -90° = top
        canvas.drawArc(arcBounds, startAngle, sweep, false, indicatorPaint);
    }

    // Public API
    public void setProgress(int value) {
        progress = clamp(value, 0, max);
        invalidate();
    }

    public void setProgressAnimated(int target, long durationMs) {
        target = clamp(target, 0, max);
        ValueAnimator anim = ValueAnimator.ofInt(progress, target);
        anim.setDuration(durationMs);
        anim.addUpdateListener(a -> {
            progress = (int) a.getAnimatedValue();
            invalidate();
        });
        anim.start();
    }

    public void setMax(int max) {
        this.max = Math.max(1, max);
        invalidate();
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
