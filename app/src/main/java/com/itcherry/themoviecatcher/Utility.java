package com.itcherry.themoviecatcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.itcherry.themoviecatcher.data.MovieContract;


public class Utility {
    final static String LOG_TAG = Utility.class.getSimpleName();
    static int rowQuantity = 20;
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
}
