package com.digivahan.other;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MultiColorDonutChartView extends View {

    private List<Float> values = new ArrayList<>();
    private List<Integer> colors = new ArrayList<>();
    private float strokeWidth = 40f; // thickness
    private RectF oval = new RectF();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public MultiColorDonutChartView(Context context) {
        super(context);
        init();
    }

    public MultiColorDonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float pad = strokeWidth / 2f + 4f;
        oval.set(pad, pad, w - pad, h - pad);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (values.isEmpty() || colors.isEmpty()) return;

        float total = 0f;
        for (Float v : values) total += v;

        float startAngle = -90f; // start from top
        for (int i = 0; i < values.size(); i++) {
            float sweep = (values.get(i) / total) * 360f;
            paint.setColor(colors.get(i));
            canvas.drawArc(oval, startAngle, sweep, false, paint);
            startAngle += sweep;
        }
    }

    // --- Public APIs ---
    public void setData(List<Float> values, List<Integer> colors) {
        if (values == null || colors == null) return;
        if (values.size() != colors.size()) {
            throw new IllegalArgumentException("Values and Colors must have same size");
        }
        this.values = values;
        this.colors = colors;
        invalidate();
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        paint.setStrokeWidth(strokeWidth);
        requestLayout();
        invalidate();
    }
}

