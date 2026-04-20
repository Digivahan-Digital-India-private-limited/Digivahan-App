package com.digivahan.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;

public class NetworkUtils {

    /**
     * Checks if device is connected to the internet.
     *
     * @param context Application context
     * @return true if internet is available, false otherwise
     */
    public static boolean isInternetAvailable(Context context) {
        if (context == null) return false;

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities =
                    cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (capabilities != null) {
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
            }
            return false;
        } else {
            // For older devices
            try {
                return cm.getActiveNetworkInfo() != null &&
                        cm.getActiveNetworkInfo().isConnected();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}