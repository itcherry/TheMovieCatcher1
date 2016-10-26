package com.itcherry.themoviecatcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.itcherry.themoviecatcher.data.MovieContract;


public class Utility {
    final static String LOG_TAG = Utility.class.getSimpleName();

    public static boolean isNotificationEnabled(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.key_notification), true);
    }

    public static String getSortingFromPreferences(Context context) { // temporary
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d(LOG_TAG, prefs.getString(context.getString(R.string.pref_sorting_key),
                context.getString(R.string.pref_sorting_default)));
        return prefs.getString(context.getString(R.string.pref_sorting_key),
                context.getString(R.string.pref_sorting_default));
    }

    public static String getFriendlySortingFromPreferences(Context context) { // temporary
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String sort = prefs.getString(context.getString(R.string.pref_sorting_key),
                context.getString(R.string.pref_sorting_default));
        switch (sort) {
            case "top_rated?":
                return MovieContract.COLUMN_VOTE_AVERAGE;
            case "popular?":
                return MovieContract.COLUMN_POPULARITY;
            case "favourite?":
                return MovieContract.COLUMN_IS_FAVOURITE;
            default:
                throw new IllegalArgumentException("Unknown sort order parameter");
        }
    }

    public static int getRowQuantityFromPreferences(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(context.getString(R.string.pref_last_row_quantity),20);
    }
    public static int getPageQuantityFromPreferences(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(context.getString(R.string.pref_last_page),1);
    }
    public static boolean getNetworkStateFromPreference(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_is_connected_to_network),false);
    }
    public static void setPageQuantityFromPreferences(Context context, int pageQuantity){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(context.getString(R.string.pref_last_page), pageQuantity);
        editor.apply();
    }
    public static void setRowQuantityFromPreferences(Context context, int rowQuantity){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(
                context.getString(R.string.pref_last_row_quantity),
                rowQuantity);
        editor.apply();
    }
    public static void setNetworkStateFromPreferences(Context context, boolean isConnected){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(
                context.getString(R.string.pref_is_connected_to_network),
                isConnected);
        editor.apply();
    }
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    public static View getEmptyView(final Activity activity, LayoutInflater inflater, ViewGroup container) {
        View emptyView;
        emptyView = inflater.inflate(R.layout.no_network_layout, container, false);
        if (Utility.isNetworkConnected(activity)) {
            emptyView.findViewById(R.id.empty_layout).setVisibility(View.VISIBLE);
            emptyView.findViewById(R.id.no_network_layout).setVisibility(View.GONE);
        } else {
            emptyView.findViewById(R.id.empty_layout).setVisibility(View.GONE);
            emptyView.findViewById(R.id.no_network_layout).setVisibility(View.VISIBLE);

        }
        Button showNetworkButton = (Button) emptyView.findViewById(R.id.open_network_button);
        showNetworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings",
                        "com.android.settings.Settings$DataUsageSummaryActivity");
                activity.startActivity(intent);
            }
        });
        Button showWifiButton = (Button) emptyView.findViewById(R.id.open_wifi_button);
        showWifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                activity.startActivity(intent);
            }
        });

        activity.addContentView(emptyView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );
        return emptyView;
    }

    /*public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }

    }*/
}
