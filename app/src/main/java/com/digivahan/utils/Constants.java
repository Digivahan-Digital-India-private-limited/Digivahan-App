package com.digivahan.utils;


public interface Constants {

    // image folders
    public static String vehicleAccidents= "vehicle-alert-image";
    public static String chatImages= "chat-image";
    public static String profile= "profile-images";
    public static String emergencyContact= "emergency-contact-images";

    // App images folder in device
    public static String appImageFolder= "DigiVahan";


    // 🔗 API Base URLs
    public static final String BASE_URL = "https://api.example.com/";
    public static final String SOCKET_URL = "https://socket.example.com/";

    // 📱 SharedPreferences Keys
    public static final String PREF_NAME = "digivahan_prefs";
    public static final String KEY_USER_TOKEN = "user_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // 🚗 RTO Feature Keys
    public static final String VEHICLE_NUMBER = "vehicle_number";
    public static final String VEHICLE_INFO = "vehicle_info";

    // 💬 Chat Feature Keys
    public static final String CHAT_ROOM_ID = "chat_room_id";
    public static final String CHAT_USER_ID = "chat_user_id";

    // 🔔 Notifications
    public static final String CHANNEL_ID = "digivahan_channel";
    public static final String CHANNEL_NAME = "Digivahan Notifications";

    // 🌐 Network
    public static final int TIMEOUT_SECONDS = 30;

    // 🛠 Common
    public static final String EMPTY_STRING = "";
    public static final boolean ENABLE_TESTING = false;


    // zigo cloud
    public static final long APP_ID = 1304125511L;
    public static final String zigoAppSign = "e1ad2acc3948fabc46022b047549dc9249b7af84bc200f9149f2fae314746c66";

    // for chat timer

}

