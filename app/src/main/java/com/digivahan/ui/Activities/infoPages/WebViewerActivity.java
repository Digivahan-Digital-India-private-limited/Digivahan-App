package com.digivahan.ui.Activities.infoPages;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.appcompat.widget.Toolbar;

import com.ashu.ashuutils.APIHelper;
import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.utils.Constants;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebViewerActivity extends BaseActivity {

    public static final String EXTRA_POLICY_TYPE = "extra_policy_type";

    private WebView webView;
    private ProgressBar progressBar;
    private TextView tvTitle;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_viewer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tvToolbarTitle);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        String policyType = getIntent().getStringExtra(EXTRA_POLICY_TYPE);
        if (policyType == null) {
            finish();
            return;
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                finish();
            }
        });

        setupWebView();
        fetchPolicyAndLoad(policyType);
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view,
                                        WebResourceRequest request,
                                        WebResourceError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchPolicyAndLoad(String policyType) {

        ApiClient.getApiService(this)
                .commonGETMethodToHitAllAPIs(APIData.GET_APP_INFO)
                .enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call,
                                           @NonNull Response<JsonObject> response) {

                        JSONObject body = APIHelper.getResponseData(
                                "WebViewer", response);

                        try {
                            if (!body.optBoolean("success")) {
                                showError();
                                return;
                            }

                            JSONObject policy =
                                    body.getJSONObject("data")
                                            .getJSONObject("policy");

                            String url = null;

                            switch (policyType) {

                                case "privacy_policy":
                                    tvTitle.setText("Privacy Policy");
                                    url = policy.getJSONObject("privacy_policy")
                                            .getString("policy_page_url");
                                    break;

                                case "terms_condition":
                                    tvTitle.setText("Terms & Conditions");
                                    url = policy.getJSONObject("terms_condition")
                                            .getString("terms_condition_page_url");
                                    break;

                                case "about_page":
                                    tvTitle.setText("About Us");
                                    url = policy.getJSONObject("About_page")
                                            .getString("about_page_url");
                                    break;
                            }

                            if (url == null || url.isEmpty()) {
                                showError();
                            } else {
                                webView.loadUrl(url);
                            }

                        } catch (Exception e) {
                            showError();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call,
                                          @NonNull Throwable t) {
                        showError();
                    }
                });
    }

    private void showError() {
        Toast.makeText(this,
                "Unable to load page. Please try again later.",
                Toast.LENGTH_SHORT).show();
//        finish();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
