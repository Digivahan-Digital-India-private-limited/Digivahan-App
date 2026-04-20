package com.digivahan.other.CustomDialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.digivahan.R;
import com.digivahan.utils.NetworkUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class AshDialog {

    private Dialog progress;
    private ImageView iconImageView;
    private TextView titleTextView;
    private TextView messageTextView;

    private long showTimestamp = 0L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Context context;

    private LinearProgressIndicator progressIndicator;
    private int progressValue = 0;


    private final String[] loadingTexts = {
            "Loading…",
            "Please wait…",
            "Fetching data…",
            "Almost done…"
    };
    private int loadingIndex = 0;
    private Runnable textAnimator;

    public AshDialog(Context context) {
        this.context = context;
        DialogBox(context, "Please wait", "Loading…");
    }

    public AshDialog(Context context, String title, String message) {
        this.context = context;
        DialogBox(context, title, message);
    }

    public void setTitle(String title) {
        if (titleTextView != null) titleTextView.setText(title);
    }

    public void setMessage(String message) {
        if (messageTextView != null) messageTextView.setText(message);
    }

    private void DialogBox(Context context, String title, String message) {
        try {
            progress = new Dialog(context);
            progress.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progress.setContentView(R.layout.custom_dialog_layout);
            progress.setCancelable(false);

            Window window = progress.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            iconImageView = progress.findViewById(R.id.iconImageView);
            titleTextView = progress.findViewById(R.id.titleTextView);
            messageTextView = progress.findViewById(R.id.messageTextView);
            progressIndicator = progress.findViewById(R.id.progressIndicator);


            titleTextView.setText(title);
            messageTextView.setText(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startProgressAnimation() {
        progressValue = 0;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progress == null || !progress.isShowing()) return;

                progressValue += 5;
                progressIndicator.setProgress(progressValue);

                if (progressValue < 100) {
                    handler.postDelayed(this, 80);
                }
            }
        }, 80);
    }


    public void show() {
        if (progress == null) return;
        if (!(context instanceof Activity)) return;

        Activity activity = (Activity) context;

        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        if (!NetworkUtils.isInternetAvailable(context)) {
            Toast.makeText(context, "Internet may not be available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!progress.isShowing()) {
            progress.show();
            showTimestamp = System.currentTimeMillis();
            startLoadingTextAnimation();
            startProgressAnimation();
        }
    }


    public void dismiss() {
        if (progress == null) return;

        handler.removeCallbacksAndMessages(null);

        long elapsed = System.currentTimeMillis() - showTimestamp;
        long delay = Math.max(0, 800 - elapsed);

        handler.postDelayed(() -> {
            try {
                if (progress != null && progress.isShowing()) {
                    if (context instanceof Activity) {
                        Activity activity = (Activity) context;
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                    }
                    progress.dismiss();
                }
            } catch (Exception ignored) {
            }
        }, delay);
    }

    private void startLoadingTextAnimation() {
        textAnimator = new Runnable() {
            @Override
            public void run() {
                if (progress != null && progress.isShowing()) {
                    messageTextView.setText(loadingTexts[loadingIndex]);
                    loadingIndex = (loadingIndex + 1) % loadingTexts.length;
                    handler.postDelayed(this, 1200);
                }
            }
        };
        handler.post(textAnimator);
    }

    public boolean isVisible() {
        return progress != null && progress.isShowing();
    }
}
