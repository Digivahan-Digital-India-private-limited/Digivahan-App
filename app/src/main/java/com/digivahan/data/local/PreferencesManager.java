package com.digivahan.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.digivahan.data.model.ChallanModel;
import com.digivahan.data.model.User;
import com.digivahan.utils.CommonMethods;
import com.google.gson.Gson;

import java.util.List;

public class PreferencesManager {

    private static final String PREF_NAME = "digivahan_prefs";
    private static final String USER_ID = "user_id";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_FIRST_LAUNCH = "is_first_launch";

    public static final String NOTIF_DENIAL_COUNT = "notif_denial_count";
    public static final String NOTIF_PERMISSION_REQUESTED = "notif_permission_requested";


    public static final String KEY_GARAGE_CACHE = "garage_cache";
    public static final String KEY_FUEL_CACHE = "fuel_cache";
    public static final String KEY_TRENDING_CACHE = "trending_cache";



    public static final String BASE_URL = "base_url";
    public static final String NOTIFICATION_CLICKED = "notification_clicked";
    public static final String NOTIFICATION_TYPE_TEMP = "notification_type_temp";
    public static final String NOTIFICATION_CHAT_ROOM_ID_TEMP = "notification_chat_room_id_temp";
    public static final String NOTIFICATION_SENDER_ID_TEMP = "notification_sender_id_temp";
    public static final String NOTIFICATION_VEHICLE_ID_TEMP = "notification_vehicle_id_temp";
    public static final String NOTIFICATION_SOUND = "notification_sound";
    public static final String NOTIFICATION_SEND_COUNT = "notification_send_count";
    public static final String LAST_SCANNED_USER = "last_scanned_user";
    public static final String KEY_COOLDOWN_END_TIME = "cooldown_end_time";

    private static final String KEY_USER_DATA = "user_data";
    public static final String IMAGE_PATH = "image_path";
    public static final String LIVE_TRACKING = "live_tracking";
    public static final String CURRENT_DATE = "current_date";
    public static final String APP_SHARING_MESSAGE = "app_sharing_message";

    public static final String SHOW_HIDE_CONTENT = "show_hide_content";


    // to manage challan data
    private static final String PREF_CHALLAN = "challan_cache_pref";
    public static final String KEY_LAST_HIT_DATE = "last_challan_hit_date";
    public static final String KEY_CHALLAN_DATA = "challan_data";



    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    // --- Save User Object ---
    public void saveUser(User user) {
        if (user != null) {
            String userJson = gson.toJson(user);
            editor.putString(KEY_USER_DATA, userJson);
            editor.apply();
        }
    }

    // --- Get User Object ---
    public User getUser() {
        String userJson = prefs.getString(KEY_USER_DATA, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null; // no user data saved
    }

    // --- Clear User Data Only ---
    public void clearUser() {
        editor.remove(KEY_USER_DATA);
        editor.apply();
    }

    // --- User ID ---
    public void setUserId(String userId) {
        editor.putString(USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return prefs.getString(USER_ID, null);
    }

    // --- Auth Token ---
    public void setAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, "");
    }

    // --- Login Status ---
    public void setLoggedIn(boolean loggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, loggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Boolean
    public void setBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    // String
    public void setString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    // Integer
    public void setInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    public void removeValue(String key) {
        prefs.edit().remove(key).apply();
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    // Long
    public void setLong(String key, long value) {
        prefs.edit().putLong(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }


    // --- Clear all prefs ---
    public void clear() {
        editor.clear();
        editor.apply();
    }

    public void saveChallanToCache(Context context, List<ChallanModel> challanList) {
        editor.putString(KEY_LAST_HIT_DATE, CommonMethods.getCurrentDate(context,"dd-MM-yyyy"));
        editor.putString(KEY_CHALLAN_DATA, new Gson().toJson(challanList));
        editor.apply();
    }

}

