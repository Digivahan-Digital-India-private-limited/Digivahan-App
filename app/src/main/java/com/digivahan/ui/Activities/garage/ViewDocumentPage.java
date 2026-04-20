package com.digivahan.ui.Activities.garage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.databinding.ActivityViewDocumentPageBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;

import org.jetbrains.annotations.Nullable;

public class ViewDocumentPage extends BaseActivity {

    ActivityViewDocumentPageBinding binding;

    String docUrl = "";

    AshDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewDocumentPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Document");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> {
            back();
        });

        getOnBackPressedDispatcher().addCallback(ViewDocumentPage.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        loadingDialog = new AshDialog(ViewDocumentPage.this, "Please wait", "");

        if (getIntent().hasExtra("docUrl") && getIntent().getStringExtra("docUrl") != null) {
            docUrl = getIntent().getStringExtra("docUrl");
        }

        showDocument(ViewDocumentPage.this, docUrl, binding.webView, binding.imageView, loadingDialog);

    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }

    public void showDocument(Context context, String url, WebView webView, ImageView imageView, AshDialog loadingDialog) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(context, "Invalid document URL", Toast.LENGTH_SHORT).show();
            return;
        }

        String lowerUrl = url.toLowerCase();

        // 🔹 Show loading dialog (if provided)
        if (loadingDialog != null) {
            loadingDialog.show();
        }

        if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") ||
                lowerUrl.endsWith(".png") || lowerUrl.endsWith(".gif")) {

            // 📸 Show Image
            webView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.image_loading)
                    .error(R.drawable.image_loading)
                    .listener(new com.bumptech.glide.request.RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            CommonLogic.showTestLog("DocumentLoader", "❌ Image load failed: " + e);
                            if (loadingDialog != null) {
                                loadingDialog.dismiss();
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            CommonLogic.showTestLog("DocumentLoader", "✅ Image loaded successfully");
                            if (loadingDialog != null) {
                                loadingDialog.dismiss();
                            }
                            return false;
                        }
                    })
                    .into(imageView);

        } else if (lowerUrl.endsWith(".pdf")) {
            // 📄 Show PDF via WebView
            imageView.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);

            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setSupportZoom(true);
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    CommonLogic.showTestLog("DocumentLoader", "📄 PDF loading started...");
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    CommonLogic.showTestLog("DocumentLoader", "✅ PDF loaded successfully");
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    CommonLogic.showTestLog("DocumentLoader", "❌ PDF load failed: " + error.getDescription());
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                    Toast.makeText(context, "Failed to load document.", Toast.LENGTH_SHORT).show();
                }
            });

            String googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=" + url;
            webView.loadUrl(googleDocsUrl);

        } else {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
            Toast.makeText(context, "Unsupported file type", Toast.LENGTH_SHORT).show();
        }
    }


}