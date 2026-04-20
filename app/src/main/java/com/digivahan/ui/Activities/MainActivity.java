package com.digivahan.ui.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.digivahan.R;
import com.digivahan.data.adapters.ExpandableListAdapter;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.MenuModel;
import com.digivahan.databinding.ActivityMainBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.chat.ChatActivity;
import com.digivahan.ui.Activities.documentVault.DocumentVaultActivity;
import com.digivahan.ui.Activities.garage.MyGarageActivity;
import com.digivahan.ui.Activities.infoPages.WebViewerActivity;
import com.digivahan.ui.Activities.orderDetails.OrderListPage;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;

import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.onesignal.OneSignal;
//import com.permissionx.guolindev.PermissionX;
//import com.permissionx.guolindev.callback.ExplainReasonCallback;
//import com.permissionx.guolindev.callback.RequestCallback;
//import com.permissionx.guolindev.request.ExplainScope;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements DrawerController {
    String TAG = "MainActivityData";

    ActivityMainBinding binding;

    String changeFragment = "";

//    NavController navController;
    private AppBarConfiguration mAppBarConfiguration;
    NavController navController;
    ExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    List<MenuModel> headerList = new ArrayList<>();
    HashMap<MenuModel, List<MenuModel>> childList = new HashMap<>();
    private MainActivity activity;

    public FragmentManager fm = null;
    PreferencesManager manager;

    String INTERNET = Manifest.permission.INTERNET;
    String CAMERA = Manifest.permission.CAMERA;
    String READ_MEDIA_AUDIO = Manifest.permission.READ_MEDIA_AUDIO;
    String NOTIFICATION = Manifest.permission.POST_NOTIFICATIONS;
    String LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static String READ_STORAGE_PERMISSION;
    private final int REQUEST_CODE = 11;

    AshDialog loadingDialog;


    @Override
    protected void onResume() {
        super.onResume();
        updateNavHeader();
        CommonMethods.setNotificationCount(MainActivity.this, binding.toolbarLayout.notificationCount, binding.toolbarLayout.ivBell, manager.getUserId());
    }

    @SuppressLint({"SetTextI18n", "ObsoleteSdkInt"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setHomeStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        setSupportActionBar(binding.toolbarLayout.toolbar);

        loadingDialog = new AshDialog(MainActivity.this, "Please wait", "");

        binding.toolbarLayout.ivBell.setOnClickListener(view -> {
            openFragment(R.id.nav_notification, false, false);
        });

        activity = this;

        /*getOnBackPressedDispatcher().addCallback(MainActivity.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                finish();
            }
        });*/

        fm = getSupportFragmentManager();

        manager = new PreferencesManager(this);

        CommonLogic.checkAppVersion(TAG, MainActivity.this);


        expandableListView = findViewById(R.id.expandableListView);
//        binding.footerVersion.setText("Version " + AppKit.getAppVersion(MainActivity.this, "1"));

        requestNotificationPermission();
        showPermissionDialog();


        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                READ_STORAGE_PERMISSION = Manifest.permission.READ_MEDIA_IMAGES;
            } else {
                READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;

            }
            showPermissionDialog();
        }*/

        /*if (!isBatteryOptimizationDisabled()) {
            showBatteryOptimizationDialog();
        } else {
            CommonLogic.showTestLog(TAG, "✅ Battery optimization already disabled");
        }*/



        prepareMenuData();

        expandableListAdapter = new ExpandableListAdapter(this, headerList, childList);
        expandableListView.setAdapter(expandableListAdapter);

        binding.navView.getHeaderView(0).post(() -> {
            updateNavHeader();
        });


        binding.toolbarLayout.ivProfile.setOnClickListener(v -> {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });

        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                openFragment(item.getItemId(), true, true);

                /*if (item.getItemId() == R.id.nav_dashboard) {
                    navController.navigate(R.id.nav_dashboard);
                } else if (item.getItemId() == R.id.nav_home) {
                    openFragment(true, homeFragment, "QR Code", true, true);
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else if (item.getItemId() == R.id.nav_profile) {
                    openFragment(true, profileMenuFragment, "Profile", true, true);
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                }*/
                return true;
            }
        });


        /*binding.dashBoardLayout.setOnClickListener(v -> {
            openFragment(true, dashBoardFragment, "", true, true);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });

        binding.profileLayout.setOnClickListener(v -> {
            openFragment(true, profileMenuFragment, "Profile", true, true);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });*/


        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_dashboard,
                R.id.nav_home,
                R.id.nav_notification,
                R.id.nav_order,
                R.id.nav_virtualQR,
                R.id.nav_profile
        )
                .setOpenableLayout(binding.drawerLayout)
                .build();

        navController = Navigation.findNavController(
                MainActivity.this,
                R.id.nav_host_fragment_content_main
        );

        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) -> {

                    if (destination.getId() == R.id.nav_profile_menu || destination.getId() == R.id.nav_home || destination.getId() == R.id.nav_dashboard){
                        binding.toolbarLayout.notificationBellLayout.setVisibility(View.VISIBLE);
                        binding.bottomNav.setVisibility(View.VISIBLE);
                    }

                    if (destination.getId() == R.id.nav_dashboard){
                        binding.toolbarLayout.tvTitle.setText("Dashboard");
                        binding.toolbarMainLayout.setVisibility(View.GONE);
                    }else {
                        binding.toolbarMainLayout.setVisibility(View.VISIBLE);
                    }

                    if (destination.getId() != R.id.nav_profile){
                        binding.toolbarLayout.tvTitle.setText("Profile");
                    }

                    if (destination.getId() == R.id.nav_home){
                        binding.toolbarLayout.tvTitle.setText("Home");
                    }
                    else if (destination.getId() == R.id.nav_profile_menu || destination.getId() == R.id.nav_profile){
                        binding.toolbarLayout.tvTitle.setText("Profile");
                    }
                    else if (destination.getId() == R.id.nav_notification){
                        binding.toolbarLayout.tvTitle.setText("Notifications");
                    }

                    else if (destination.getId() == R.id.nav_order){
                        binding.toolbarLayout.tvTitle.setText("Order QR");
                    }
                    else if (destination.getId() == R.id.nav_virtualQR){
                        binding.toolbarLayout.tvTitle.setText("My Virtual QRs");
                    }
                    else if (destination.getId() == R.id.nav_about){
                        binding.toolbarLayout.tvTitle.setText("About Us");
                    }
                    else if (destination.getId() == R.id.nav_terms){
                        binding.toolbarLayout.tvTitle.setText("Terms & Conditions");
                    }
                    else if (destination.getId() == R.id.nav_privacy){
                        binding.toolbarLayout.tvTitle.setText("Privacy Policy");
                    }
                }
        );


        /*NavigationUI.setupActionBarWithNavController(
                MainActivity.this,
                navController,
                mAppBarConfiguration
        );*/

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

//        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);



        getSupportFragmentManager().setFragmentResultListener(
                "profile_update",
                this,
                (requestKey, bundle) -> {
                    boolean updated = bundle.getBoolean("profile_updated", false);
                    if (updated) {
                        updateNavHeader();
                    }
                }
        );

        // 🔹 Fragment navigation
        if (getIntent().hasExtra("changeFragment")) {

            String fragment = getIntent().getStringExtra("changeFragment");
            CommonLogic.showTestLog(TAG, "changeFragment found: " + fragment);

            if ("dashboard".equalsIgnoreCase(fragment)) {
                CommonLogic.showTestLog(TAG, "➡ Opening DASHBOARD fragment");
                openFragment(R.id.nav_dashboard, true, true);

            } else if ("notification".equalsIgnoreCase(fragment)) {
                CommonLogic.showTestLog(TAG, "➡ Opening NOTIFICATION fragment");
                openFragment(R.id.nav_notification, false, false);
            } else if ("profile".equalsIgnoreCase(fragment)) {
                CommonLogic.showTestLog(TAG, "➡ Opening NOTIFICATION fragment");
                openFragment(R.id.nav_profile, false, false);
            } else {
                CommonLogic.showTestLog(TAG, "➡ Unknown fragment, opening HOME");
                openFragment(R.id.nav_dashboard, true, true);
            }

        } else {
            CommonLogic.showTestLog(TAG, "No changeFragment found, opening HOME");
            openFragment(R.id.nav_dashboard, true, false);
        }



        handelNotificationClicked();

    }

    private void handelNotificationClicked() {

        try {

            CommonLogic.showTestLog(TAG, "================ Notification HANDLING Check START ================");

            // 🔹 Notification type navigation
            if (manager.getBoolean(PreferencesManager.NOTIFICATION_CLICKED, false)) {
                manager.setBoolean(PreferencesManager.NOTIFICATION_CLICKED, false);

                String type = manager.getString(PreferencesManager.NOTIFICATION_TYPE_TEMP, "");
                CommonLogic.showTestLog(TAG, "notification_type found: " + type);

                if ("chat".equalsIgnoreCase(type)) {
                    manager.setString(PreferencesManager.NOTIFICATION_TYPE_TEMP, "");
                    String roomId = manager.getString(PreferencesManager.NOTIFICATION_CHAT_ROOM_ID_TEMP, "");
                    String receiverId = manager.getString(PreferencesManager.NOTIFICATION_SENDER_ID_TEMP, "");

                    CommonLogic.showTestLog(TAG,
                            "➡ Opening ChatActivity | roomId=" + roomId +
                                    ", receiverId=" + receiverId);

                    disableHideContentSecureForNextNavigation();
                    Intent openChatPage = new Intent(this, ChatActivity.class);
                    openChatPage.putExtra("chatRoomId", roomId);
                    openChatPage.putExtra("receiverId", receiverId);
                    startActivity(openChatPage);

                } else if ("doc_access".equalsIgnoreCase(type)) {
                    manager.setString(PreferencesManager.NOTIFICATION_TYPE_TEMP, "");
                    String docType = "check";
                    String vehicleId = manager.getString(PreferencesManager.NOTIFICATION_VEHICLE_ID_TEMP, "");;
                    String ownerId = manager.getString(PreferencesManager.NOTIFICATION_SENDER_ID_TEMP, "");

                    CommonLogic.showTestLog(TAG,
                            "➡ Opening DocumentVaultActivity | docType=" + docType +
                                    ", vehicleId=" + vehicleId +
                                    ", ownerId=" + ownerId);

                    disableHideContentSecureForNextNavigation();
                    Intent openDocPage = new Intent(this, DocumentVaultActivity.class);
                    openDocPage.putExtra("docAccessType", docType);
                    openDocPage.putExtra("vehicleId", vehicleId);
                    openDocPage.putExtra("vehicleOwnerId", ownerId);
                    startActivity(openDocPage);

                } else {
                    CommonLogic.showTestLog(TAG, "➡ Opening NOTIFICATION fragment");
                    openFragment(R.id.nav_notification, false, false);
                }

            } else {
                CommonLogic.showTestLog(TAG, "No notification clicked");
            }

            CommonLogic.showTestLog(TAG, "================ INTENT HANDLING END =================");

        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, "❌ intent error: " + e.getMessage());
        }
    }


    private void updateNavHeader() {
        // Get the height of the navHeaderLayout
        int headerHeight = binding.navView.getHeaderView(0).getHeight();

        // Get current layout params of the expandableListView
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) expandableListView.getLayoutParams();

        // Set top margin equal to header height
        params.topMargin = headerHeight;

        // Apply updated layout params
        expandableListView.setLayoutParams(params);

        TextView progressText = binding.navView.getHeaderView(0).findViewById(R.id.progressText);
        progressText.setText(manager.getUser().getProfile_completion_percent() + "% Complete");

        TextView userName = binding.navView.getHeaderView(0).findViewById(R.id.userName);
        userName.setText(manager.getUser().getFirst_name() + " " + manager.getUser().getLast_name());

        ImageView profileImage = binding.navView.getHeaderView(0).findViewById(R.id.profileImage);
        ImageHelperMethods.loadImage(TAG, MainActivity.this, manager.getUser().getProfile_pic(), profileImage, R.drawable.temp_profile_icon);

        ProgressBar progressCircle = binding.navView.getHeaderView(0).findViewById(R.id.progressCircle);
        progressCircle.setProgress(manager.getUser().getProfile_completion_percent() != null ? Integer.parseInt(manager.getUser().getProfile_completion_percent()) : 0);

        ImageHelperMethods.loadImage(TAG, MainActivity.this, manager.getUser().getProfile_pic(), binding.toolbarLayout.ivProfile, R.drawable.temp_profile_icon);
    }

    private void showPermissionDialog() {

        if (ContextCompat.checkSelfPermission(this, INTERNET) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(this, READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(this, LOCATION) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(this, NOTIFICATION) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(this, READ_STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED
        ) {
//            Toast.makeText(this, "Permission accepted", Toast.LENGTH_SHORT).show();
            Log.d("Message", "Permission accepted");

            /*// need a activityContext.
            PermissionX.init(MainActivity.this).permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
                    .onExplainRequestReason(new ExplainReasonCallback() {
                        @Override
                        public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {
                            String message = "We need your consent for the following permissions in order to use the in app calling function properly";
                            scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny");
                        }
                    }).request(new RequestCallback() {
                        @Override
                        public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                                             @NonNull List<String> deniedList) {
                        }
                    });*/

        }/* else if (ContextCompat.checkSelfPermission(this, LOCATION) != PackageManager.PERMISSION_GRANTED){
            showLocationDisclosure();
        }*/
        else {
            ActivityCompat.requestPermissions(this, new String[]{INTERNET
//                    , READ_MEDIA_AUDIO
//                    , LOCATION, CAMERA, READ_STORAGE_PERMISSION, NOTIFICATION,
                    }, REQUEST_CODE);
        }
    }

    private void showLocationDisclosure() {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Required")
                .setMessage("DigiVahan collects location data to:\n\n" +
                        "• Track your vehicle during trips\n" +
                        "• Show nearby services\n" +
                        "• Navigation and safety features\n\n" +
                        "Location may be collected even when app is in background during an active trip.")
                .setCancelable(false)
                .setPositiveButton("Allow", (dialog, which) -> {
                    requestActualPermission();
                })
                .setNegativeButton("Deny", (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Location is required for core features", Toast.LENGTH_LONG).show();
                })
                .show();
    }

    private void requestActualPermission() {

        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                },
                REQUEST_CODE
        );
    }


    private void loadFragment(String tittle) {
        binding.toolbarLayout.tvTitle.setText(tittle);
    }

    @SuppressLint("SetTextI18n")
    public void setHeaderData() {
//        navHeaderUserId.setText(getString(R.string.user_id_bold_text)+ helper.getUserIdData());
//        navHeaderUserName.setText(getString(R.string.user_name_bold_text)+ helper.getUserProfileData().getName());
    }

    private static final int REQ_NOTIFICATION = 999;

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED) {

            CommonLogic.showTestLog(
                    "PermissionFlow",
                    "✅ Notification permission already granted"
            );
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQ_NOTIFICATION
        );
    }

    private void handleNotificationPermissionDenied() {

        boolean showRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                );

        /*if (manager.getBoolean(PreferencesManager.APP_OPEN, false)){
            showRationale = manager.getBoolean(PreferencesManager.APP_OPEN, false);
        }*/

        if (showRationale) {
            // ❌ Denied once → we can ask again
            showNotificationRationaleDialog();
        } else {
            // ❌❌ Denied permanently ("Don't ask again")
            showGoToSettingsDialog();
        }

        /*if (manager.getBoolean(PreferencesManager.APP_OPEN, false)){
            manager.setBoolean(PreferencesManager.APP_OPEN, false);
        }*/
    }

    private void showNotificationRationaleDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Enable Notifications")
                .setMessage(
                        "If you don’t allow notifications, you may miss important updates, alerts, and reminders.\n\nPlease allow notifications to stay informed."
                )
                .setCancelable(false)
                .setPositiveButton("Allow", (dialog, which) -> {
                    dialog.dismiss();
                    requestNotificationPermission(); // 🔁 Retry
                })
                /*.setNegativeButton("Not now", (dialog, which) -> {
                    dialog.dismiss();
                })*/
                .show();
    }

    private void showGoToSettingsDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Enable Notifications from Settings")
                .setMessage(
                        "Notifications are disabled permanently.\n\nPlease enable them from app settings to receive important updates."
                )
                .setCancelable(false)
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    dialog.dismiss();
                    openAppSettings();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void openAppSettings() {

        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        );
        intent.setData(Uri.fromParts(
                "package",
                getPackageName(),
                null
        ));
//        disableHideContentSecureForNextNavigation();
        startActivity(intent);
    }



    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        CommonLogic.showTestLog(
                "PermissionFlow",
                "🟡 onRequestPermissionsResult | requestCode=" + requestCode
        );

        if (requestCode == REQUEST_CODE) {

            boolean allGranted = true;

            for (int i = 0; i < permissions.length; i++) {

                boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;

                CommonLogic.showTestLog(
                        "PermissionFlow",
                        "➡ " + permissions[i] + " = " + (granted ? "GRANTED" : "DENIED")
                );

                if (!granted) {
                    allGranted = false;
                }
            }

            if (allGranted) {
                CommonLogic.showTestLog(
                        "PermissionFlow",
                        "✅ All runtime permissions granted"
                );

                // Now move to next permission step (notification, etc.)
                askNotificationPermissionIfNeeded();

            } else {
                CommonLogic.showTestLog(
                        "PermissionFlow",
                        "❌ Some permissions denied"
                );

                // Show explanation dialog ONLY (do NOT request again automatically)
//                showPermissionRationaleDialog();
            }

        } else if (requestCode == REQ_NOTIFICATION) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                CommonLogic.showTestLog(
                        "PermissionFlow",
                        "✅ Notification permission GRANTED"
                );

                observeOneSignalSubscription();

                showPermissionDialog();

            } else {

                CommonLogic.showTestLog(
                        "PermissionFlow",
                        "❌ Notification permission DENIED"
                );
            }
        }
    }

    private void askNotificationPermissionIfNeeded() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            CommonLogic.showTestLog(
                    "PermissionFlow",
                    "ℹ️ Notification permission not required (Android < 13)"
            );
            observeOneSignalSubscription();
            return;
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED) {

            CommonLogic.showTestLog(
                    "PermissionFlow",
                    "✅ Notification permission already granted"
            );

            observeOneSignalSubscription();
            return;
        }

        CommonLogic.showTestLog(
                "PermissionFlow",
                "🔔 Requesting notification permission"
        );

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQ_NOTIFICATION
        );
    }


    private void showPermissionRationaleDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage(
                        "This app needs location and related permissions to function properly. " +
                                "Please allow them to continue."
                )
                .setCancelable(false)
                .setPositiveButton("Allow", (dialog, which) -> {

                    CommonLogic.showTestLog(
                            "PermissionFlow",
                            "🟢 User agreed to re-try permissions"
                    );

                    showPermissionDialog(); // SAFE: user initiated
                })
                .setNegativeButton("Cancel", (dialog, which) -> {

                    CommonLogic.showTestLog(
                            "PermissionFlow",
                            "🔴 User cancelled permission request"
                    );

                    dialog.dismiss();
                })
                .show();
    }




    private void observeOneSignalSubscription() {

        CommonLogic.showTestLog(
                "OneSignalTest",
                "🟣 observeOneSignalSubscription() STARTED"
        );

        OneSignal.getUser().getPushSubscription().addObserver(state -> {

            boolean optedIn = state.getCurrent().getOptedIn();
            String token = state.getCurrent().getToken();
            String subscriptionId = state.getCurrent().getId();

            CommonLogic.showTestLog(
                    "OneSignalTest",
                    "📡 PushSubscription CHANGED"
            );

            CommonLogic.showTestLog(
                    "OneSignalTest",
                    "   • optedIn        = " + optedIn
            );

            CommonLogic.showTestLog(
                    "OneSignalTest",
                    "   • token          = " + token
            );

            CommonLogic.showTestLog(
                    "OneSignalTest",
                    "   • subscriptionId = " + subscriptionId
            );

            PreferencesManager manager = new PreferencesManager(this);
            String userId = manager.getUserId();

            CommonLogic.showTestLog(
                    "OneSignalTest",
                    "👤 Local userId = " + userId
            );

            if (optedIn && userId != null) {

                CommonLogic.showTestLog(
                        "OneSignalTest",
                        "🔐 Calling OneSignal.login(" + userId + ")"
                );

                CommonMethods.onUserLogin(TAG, userId);

            } else {

                CommonLogic.showTestLog(
                        "OneSignalTest",
                        "⚠ Login skipped (optedIn=" + optedIn + ", userId=" + userId + ")"
                );
            }
        });
    }






    private void refreshOneSignalSubscription() {

        OneSignal.getUser().getPushSubscription().addObserver(state -> {

            boolean subscribed = state.getCurrent().getOptedIn();
            String token = state.getCurrent().getToken();
            String subscriptionId = state.getCurrent().getId();

            CommonLogic.showTestLog(
                    "OneSignal",
                    "✅ Subscription refreshed" +
                            " | subscribed=" + subscribed +
                            " | token=" + token +
                            " | id=" + subscriptionId
            );
        });

        // Re-login user if needed
        PreferencesManager manager = new PreferencesManager(this);

        String userId = manager.getUserId();

        if (userId != null && !userId.isEmpty()) {

            CommonMethods.onUserLogin(TAG, userId);

            CommonLogic.showTestLog(TAG,
                    "🔁 Re-linked OneSignal to user: " + userId
            );

        }

    }




    @SuppressLint("UseCompatLoadingForDrawables")
    private void prepareMenuData() {
        MenuModel menuModel = new MenuModel(activity.getResources().getString(R.string.nav_notification), activity.getResources().getDrawable(R.drawable.notification_bell_icon), true, false, true); //Menu of Android Tutorial. No sub menus
//        headerList.add(menuModel);

        menuModel = new MenuModel(activity.getResources().getString(R.string.nav_my_garage), activity.getResources().getDrawable(R.drawable.my_garage_icon), true, false, false);
        headerList.add(menuModel);

        menuModel = new MenuModel(activity.getResources().getString(R.string.nav_my_order), activity.getResources().getDrawable(R.drawable.my_order_icon), true, false, false);
        headerList.add(menuModel);

        menuModel = new MenuModel(activity.getResources().getString(R.string.nav_virtual_qrs), activity.getResources().getDrawable(R.drawable.ic_qr), true, false, false);
        headerList.add(menuModel);

        menuModel = new MenuModel(activity.getResources().getString(R.string.nav_update_profile), activity.getResources().getDrawable(R.drawable.profile_icon1), true, false, false);
        headerList.add(menuModel);

        menuModel = new MenuModel(activity.getResources().getString(R.string.nav_about_us), activity.getResources().getDrawable(R.drawable.group_icon), true, false, false);
        headerList.add(menuModel);

        menuModel = new MenuModel(activity.getResources().getString(R.string.nav_term_condition), activity.getResources().getDrawable(R.drawable.term_condition_icon), true, false, false);
        headerList.add(menuModel);

        menuModel = new MenuModel(activity.getResources().getString(R.string.nav_privacy_policy), activity.getResources().getDrawable(R.drawable.policy_icon), true, false, false);
        headerList.add(menuModel);

        menuModel = new MenuModel(activity.getResources().getString(R.string.nav_share_app), activity.getResources().getDrawable(R.drawable.share_icon), true, false, false);
        headerList.add(menuModel);

        menuModel = new MenuModel(activity.getResources().getString(R.string.nav_log_out), activity.getResources().getDrawable(R.drawable.logout_icon), true, false, false);
        headerList.add(menuModel);

    }


    public void drawerClicked(MenuModel menuItem, boolean switchBtnClicked, boolean switchBtnChecked) {
        binding.toolbarLayout.notificationBellLayout.setVisibility(View.VISIBLE);

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (this.getResources().getString(R.string.nav_notification).equalsIgnoreCase(menuItem.menuName)) {
            if (switchBtnClicked){
                Toast.makeText(activity, "switchBtnClicked", Toast.LENGTH_SHORT).show();
            }else {
                openFragment(R.id.nav_notification, false, false);
            }
        }
        else if (this.getResources().getString(R.string.nav_my_garage).equalsIgnoreCase(menuItem.menuName)) {
            disableHideContentSecureForNextNavigation();
            Intent mainPage = new Intent(MainActivity.this, MyGarageActivity.class);
            startActivity(mainPage);
        }
        else if (this.getResources().getString(R.string.nav_my_order).equalsIgnoreCase(menuItem.menuName)) {
            disableHideContentSecureForNextNavigation();
            Intent intent = new Intent(this, OrderListPage.class);
            startActivity(intent);
        }
        else if (this.getResources().getString(R.string.nav_virtual_qrs).equalsIgnoreCase(menuItem.menuName)) {
            openFragment(R.id.nav_virtualQR, false, true);
        }
        else if (this.getResources().getString(R.string.nav_update_profile).equalsIgnoreCase(menuItem.menuName)) {
            openFragment(R.id.nav_profile, false, true);
        }else if (this.getResources().getString(R.string.nav_about_us).equalsIgnoreCase(menuItem.menuName)) {
            disableHideContentSecureForNextNavigation();
            Intent aboutUsPage = new Intent(MainActivity.this, WebViewerActivity.class);
            aboutUsPage.putExtra("extra_policy_type", "about_page");
            startActivity(aboutUsPage);
        } else if (this.getResources().getString(R.string.nav_term_condition).equalsIgnoreCase(menuItem.menuName)) {
            disableHideContentSecureForNextNavigation();
            Intent termConditionPage = new Intent(MainActivity.this, WebViewerActivity.class);
            termConditionPage.putExtra("extra_policy_type", "terms_condition");
            startActivity(termConditionPage);
        }else if (this.getResources().getString(R.string.nav_privacy_policy).equalsIgnoreCase(menuItem.menuName)) {
            disableHideContentSecureForNextNavigation();
            Intent privacyPolicyPage = new Intent(MainActivity.this, WebViewerActivity.class);
            privacyPolicyPage.putExtra("extra_policy_type", "privacy_policy");
            startActivity(privacyPolicyPage);
        } else if (this.getResources().getString(R.string.nav_share_app).equalsIgnoreCase(menuItem.menuName)) {
            disableHideContentSecureForNextNavigation();
            CommonLogic.shareAppWithImage(MainActivity.this, loadingDialog);
        } else if (this.getResources().getString(R.string.nav_log_out).equalsIgnoreCase(menuItem.menuName)) {
            CommonMethods.logout(MainActivity.this, true);
        }
    }

    public void openFragment(
            int targetFragment,
            boolean showBottomNav,
            boolean showNotificationIcon
    ) {
        try {

            CommonLogic.showTestLog(TAG, "------------------------------------");
            CommonLogic.showTestLog(TAG, "openFragment() called");
            CommonLogic.showTestLog(TAG, "Target Fragment ID: " + targetFragment);
            CommonLogic.showTestLog(TAG, "Show BottomNav: " + showBottomNav);
            CommonLogic.showTestLog(TAG, "Show Notification Icon: " + showNotificationIcon);

            // 🔔 Update notification count
            CommonLogic.showTestLog(TAG, "Updating notification count");
            CommonMethods.setNotificationCount(
                    MainActivity.this,
                    binding.toolbarLayout.notificationCount,
                    binding.toolbarLayout.ivBell,
                    manager.getUserId()
            );

            // 📍 Current destination
            NavDestination currentDestination = navController.getCurrentDestination();

            if (currentDestination == null) {
                CommonLogic.showTestLog(TAG, "❌ Current destination is NULL. Navigation aborted.");
                return;
            }

            CommonLogic.showTestLog(
                    TAG,
                    "Current Destination ID: " + currentDestination.getId()
            );

            // 🚫 Prevent duplicate navigation
            if (currentDestination.getId() == targetFragment) {
                CommonLogic.showTestLog(
                        TAG,
                        "⚠️ Target fragment is same as current. Navigation skipped."
                );
                return;
            }

            // 🧭 Navigation options
            NavOptions navOptions = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .build();

            CommonLogic.showTestLog(TAG, "Navigating to fragment ID: " + targetFragment);

            // 🚀 Perform navigation
            navController.navigate(targetFragment, null, navOptions);

            CommonLogic.showTestLog(TAG, "Navigation successful");

            // 👇 Bottom navigation visibility
            binding.bottomNav.setVisibility(
                    showBottomNav ? View.VISIBLE : View.GONE
            );

            CommonLogic.showTestLog(
                    TAG,
                    "BottomNav visibility set to: "
                            + (showBottomNav ? "VISIBLE" : "GONE")
            );

            // 🔔 Notification icon visibility
            binding.toolbarLayout.notificationBellLayout.setVisibility(
                    showNotificationIcon ? View.VISIBLE : View.INVISIBLE
            );

            CommonLogic.showTestLog(
                    TAG,
                    "Notification icon visibility set to: "
                            + (showNotificationIcon ? "VISIBLE" : "INVISIBLE")
            );

        } catch (IllegalArgumentException e) {
            CommonLogic.showTestLog(
                    TAG,
                    "❌ Navigation IllegalArgumentException: " + e.getMessage()
            );
        } catch (Exception e) {
            CommonLogic.showTestLog(
                    TAG,
                    "❌ openFragment() Exception: " + e.toString()
            );
        }
    }





    @SuppressLint({"GestureBackNavigation", "MissingSuperCall"})
    @Override
    public void onBackPressed() {
        NavDestination currentDestination =
                navController.getCurrentDestination();

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (currentDestination != null &&
                currentDestination.getId() == R.id.nav_dashboard) {

            // We are already on Home → close activity
            finish();

        } else if (currentDestination != null &&
                currentDestination.getId() == R.id.nav_profile) {
            openFragment(R.id.nav_profile_menu, true, true);
        } else {
            openFragment(R.id.nav_dashboard, true, true);

       /*     // Navigate back to Home
            navController.navigate(
                    R.id.nav_home,
                    null,
                    new NavOptions.Builder()
                            .setPopUpTo(
                                    R.id.nav_home,
                                    true
                            )
                            .build()
            );

            binding.bottomNav.setVisibility(View.VISIBLE);
            binding.toolbarLayout.notificationBellLayout.setVisibility(View.VISIBLE);*/
        }
    }

    private boolean isBatteryOptimizationDisabled() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true; // No battery optimization before Android 6
        }

        PowerManager powerManager =
                (PowerManager) getSystemService(Context.POWER_SERVICE);

        if (powerManager == null) return false;

        return powerManager.isIgnoringBatteryOptimizations(getPackageName());
    }


    private void showBatteryOptimizationDialog() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        new AlertDialog.Builder(this)
                .setTitle("Allow background calls")
                .setMessage(
                        "To receive incoming calls when the app is closed, " +
                                "please allow the app to run without battery restrictions."
                )
                .setCancelable(false)
                .setPositiveButton("Allow", (dialog, which) -> {
                    openBatteryOptimizationSettings();
                })
                .setNegativeButton("Later", null)
                .show();
    }


    private void openBatteryOptimizationSettings() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(
                    Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            );
            startActivity(intent);
        }
    }

    @Override
    public void openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START);
    }
}