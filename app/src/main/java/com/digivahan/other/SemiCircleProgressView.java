package com.digivahan.other;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.digivahan.R;


public class SemiCircleProgressView extends View {

    private Paint trackPaint;
    private Paint progressPaint;
    private Paint innerPaint;
    private RectF arcRect = new RectF();

    private float progress = 0.36f; // normalized (0..1)
    private int progressColor = 0xFF2FB132;
    private int trackColor = 0xFFD3D6DB;
    private float progressThickness = 28f; // stroke width

    public SemiCircleProgressView(Context context) {
        this(context, null);
    }

    public SemiCircleProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SemiCircleProgressView);
            progress = a.getFloat(R.styleable.SemiCircleProgressView_progress, progress);
            progressColor = a.getColor(R.styleable.SemiCircleProgressView_progressColor, progressColor);
            trackColor = a.getColor(R.styleable.SemiCircleProgressView_trackColor, trackColor);
            progressThickness = a.getDimension(R.styleable.SemiCircleProgressView_progressThickness, progressThickness);
            a.recycle();
        }

        // Track Paint
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(progressThickness);
        trackPaint.setColor(trackColor);

        // Progress Paint
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(progressThickness);
        progressPaint.setStrokeCap(Paint.Cap.BUTT);
        progressPaint.setColor(progressColor);

        // Inner Paint (transparent for now)
        innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerPaint.setStyle(Paint.Style.FILL);
        innerPaint.setColor(Color.TRANSPARENT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float pad = progressThickness / 2f + 2f;
        arcRect.set(pad, pad, w - pad, (h * 2f) - pad);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw track
        canvas.drawArc(arcRect, 180, 180, false, trackPaint);

        // Draw progress
        float sweep = 180f * progress;
        canvas.drawArc(arcRect, 180, sweep, false, progressPaint);
    }

    // ✅ Accept progress from 0–100 instead of 0–1
    public void setProgress(int percent) {
        this.progress = Math.max(0f, Math.min(1f, percent / 100f)); // convert 1–100 → 0–1
        invalidate();
    }

    // ✅ Optional: still allow float (0–1) for flexibility
    public void setProgress(float progressFraction) {
        this.progress = Math.max(0f, Math.min(1f, progressFraction));
        invalidate();
    }

    public float getProgress() {
        return progress * 100f; // return as percentage (1–100)
    }

    // ✅ Dynamically change progress color
    public void setProgressColor(@ColorInt int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    // ✅ Dynamically change track color
    public void setTrackColor(@ColorInt int color) {
        this.trackColor = color;
        trackPaint.setColor(color);
        invalidate();
    }

    // ✅ Dynamically change thickness
    public void setProgressThickness(float thickness) {
        this.progressThickness = thickness;
        trackPaint.setStrokeWidth(thickness);
        progressPaint.setStrokeWidth(thickness);
        requestLayout();
        invalidate();
    }
}

