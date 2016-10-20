package com.itcherry.themoviecatcher.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.itcherry.themoviecatcher.FetchBackdropsTask;
import com.itcherry.themoviecatcher.MainActivity;
import com.itcherry.themoviecatcher.R;
import com.itcherry.themoviecatcher.Utility;
import com.itcherry.themoviecatcher.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_ID;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_IMAGE_URL;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_IS_FAVOURITE;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_OVERVIEW;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_PAGE;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_POPULARITY;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_RELEASE_DATE;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_TITLE;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_VOTE_AVERAGE;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_VOTE_COUNT;
import static com.itcherry.themoviecatcher.data.MovieContract.getDirForImages;

public class MovieCatcherSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = MovieCatcherSyncAdapter.class.getSimpleName();

    public MovieCatcherSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute)  180 = 3 hours
    public static final long SYNC_INTERVAL = 60L * 180L;
    public static final long SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public static final String SYNC_FINISHED = "sync_finished";
    private static final long DAY_IN_MILLIS = 1000 * 60 * 3;//1000 * 60 * 60 * 24;
    private static final int MOVIE_NOTIFICATION_ID = 3004;

    private static final String[] NOTIFY_MOVIE_PROJECTION = new String[]{
            MovieContract.COLUMN_ID,
            MovieContract.COLUMN_TITLE,
    };
    // these indices must match the projection
    private static final int INDEX_ID = 0;
    private static final int INDEX_TITLE = 1;

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getMoviesFromJsonString(String postersJsonStr, String page) {

        final String RESULTS_ARRAY = "results";
        final String POSTER_ID = "id";
        final String POSTER_IMAGE_URL = "poster_path";
        final String POSTER_OVERVIEW = "overview";
        final String POSTER_RELEASE_DATE = "release_date";
        final String POSTER_TITLE = "title";
        final String POSTER_POPULARITY = "popularity";
        final String POSTER_VOTE_COUNT = "vote_count";
        final String POSTER_VOTE_AVERAGE = "vote_average";

        //ArrayList<MovieDescription> result = new ArrayList<>();
        //MovieDescription movie;
        try {
            JSONObject jsonObject = new JSONObject(postersJsonStr);
            JSONArray results = jsonObject.getJSONArray(RESULTS_ARRAY);

            JSONObject object;

            Vector<ContentValues> cVVector = new Vector<ContentValues>(results.length());

            for (int i = 0; i < results.length(); i++) {
                object = results.getJSONObject(i);

                ContentValues cv = new ContentValues();

                cv.put(COLUMN_ID, object.getInt(POSTER_ID));
                cv.put(COLUMN_VOTE_COUNT, object.getInt(POSTER_VOTE_COUNT));
                cv.put(COLUMN_TITLE, object.getString(POSTER_TITLE));
                cv.put(COLUMN_VOTE_AVERAGE, object.getDouble(POSTER_VOTE_AVERAGE));
                cv.put(COLUMN_POPULARITY, object.getDouble(POSTER_POPULARITY));
                cv.put(COLUMN_RELEASE_DATE, object.getString(POSTER_RELEASE_DATE));
                cv.put(COLUMN_IMAGE_URL, object.getString(POSTER_IMAGE_URL));
                cv.put(COLUMN_OVERVIEW, object.getString(POSTER_OVERVIEW));
                cv.put(COLUMN_PAGE, page);

                cVVector.add(cv);

                Bitmap bitmap = Picasso.with(getContext())
                        .load(MovieContract.URL_PICTURE + object.getString(POSTER_IMAGE_URL))
                        .get();
                FileOutputStream streamWriter = new FileOutputStream(
                        getDirForImages(object.getString(POSTER_IMAGE_URL), getContext())
                );

                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, streamWriter);
                streamWriter.flush();
                streamWriter.close();
            }

            int inserted = 0;
            if (cVVector.size() > 0) {
                ContentValues[] arrayCV = new ContentValues[cVVector.size()];
                cVVector.toArray(arrayCV);
                inserted = getContext().getContentResolver().bulkInsert(MovieContract.CONTENT_URI, arrayCV);
                notifyMovies();
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return result;
    }
    public static void deleteOldRows(Context context, String page){
        Uri uri = MovieContract.buildMoviePage(Integer.parseInt(page));
        Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{COLUMN_IMAGE_URL},null,null,null);
        if(cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isLast()) {
                if(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FAVOURITE)) == 0) {
                    File file = MovieContract.getDirForImages(
                            cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL)),
                            context);
                    if (file.exists()) { //Если файл или директория существует
                        String deleteCmd = "rm -r " + file.getAbsolutePath(); //Создаем текстовую командную строку
                        Runtime runtime = Runtime.getRuntime();
                        try {
                            runtime.exec(deleteCmd); //Выполняем системные команды
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                cursor.moveToNext();
            }
        }
        int deleted = context.getContentResolver().delete(
                MovieContract.buildMoviePage(Integer.parseInt(page)),null,null);

    }
    private void notifyMovies() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        String lastBackdropKey = context.getString(R.string.pref_last_backdrop);
        int lastBackdrop = prefs.getInt(lastBackdropKey, 0);
        long lastSync = prefs.getLong(lastNotificationKey, 0);

        if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
            // Last sync was more than 1 day ago, let's send a notification with the weather.

            Uri movieUri = MovieContract.buildMovieSorting("popularity", "1");

            // we'll query our contentProvider, as always
            Cursor cursor = context.getContentResolver().query(movieUri, NOTIFY_MOVIE_PROJECTION, null, null, null);

            if (cursor.moveToFirst()) {
                NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();

                String title = cursor.getString(INDEX_TITLE);
                String id = cursor.getString(INDEX_ID);

                try {
                    String[] backdrops = new FetchBackdropsTask().execute(id).get();
                    if (backdrops.length != 0) {
                        if (lastBackdrop >= backdrops.length)
                            lastBackdrop = 0;
                        Bitmap bitmap = Picasso.with(getContext())
                                .load(MovieContract.URL_PICTURE + backdrops[lastBackdrop])
                                .get();
                        bigPictureStyle.bigPicture(bitmap);
                        bigPictureStyle.setBigContentTitle(title);
                        bigPictureStyle.setSummaryText("Time to watch new movie!");
                    }
                } catch (InterruptedException | ExecutionException | IOException e) {
                    e.printStackTrace();
                }

                if (Utility.isNotificationEnabled(context)) {
                    NotificationCompat.Builder notif = new NotificationCompat.Builder(context)
                            .setContentTitle(title)
                            .setContentText("Time to watch new movie!")
                            .setStyle(bigPictureStyle)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setDefaults(Notification.DEFAULT_VIBRATE)
                            .setSound(Uri.parse("android.resource://" +
                                    context.getPackageName() + "/" +
                                    R.raw.notification_sound)
                            );

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    TaskStackBuilder tsb = TaskStackBuilder.create(context);
                    tsb.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = tsb.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    notif.setContentIntent(resultPendingIntent);


                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(MOVIE_NOTIFICATION_ID, notif.build());
                    //deleteOldRows(getContext(),"2");
                }
                //refreshing last sync
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.putInt(lastBackdropKey, ++lastBackdrop);
                editor.apply();
            }
        }

    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context, int page) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putInt(context.getString(R.string.bundle_page_key),page);
        Account account = getSyncAccount(context);
        ContentResolver.requestSync(account,
                context.getString(R.string.content_authority), bundle);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");

        String sortOrder = Utility.getSortingFromPreferences(getContext());
        String postersJsonStr = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        //ArrayList<MovieDescription> movies;

        final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/" + sortOrder;
        //final String MOVIE_BACKDROPS_URL = "http://api.themoviedb.org/3/movie/" + "/images?";
        final String API_KEY = "api_key";
        final String SORTING = "sort_by";
        final String PAGE = "page";

        String page = String.valueOf(
                extras.getInt(getContext().getString(R.string.bundle_page_key),1));
        try {
            Uri uri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(SORTING, "vote_average.desc")
                    .appendQueryParameter(API_KEY, "277455b8532b51d0dd24b2446e50a0ad")
                    .appendQueryParameter(PAGE, page)
                    .build();

            URL url = new URL(uri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            postersJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            return;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }
        getMoviesFromJsonString(postersJsonStr,page);
        Intent intent = new Intent(SYNC_FINISHED);
        getContext().sendBroadcast(intent);
    }



    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, long syncInterval, long flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MovieCatcherSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context,20);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }
}