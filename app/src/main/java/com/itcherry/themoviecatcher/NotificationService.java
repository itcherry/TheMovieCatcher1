package com.itcherry.themoviecatcher;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.itcherry.themoviecatcher.data.MovieContract;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_IMAGE_URL;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_IS_FAVOURITE;


public class NotificationService extends IntentService {

    private static final long DAY_IN_MILLIS = 1000 * 60 * 3;//1000 * 60 * 60 * 24;
    public static final int MOVIE_NOTIFICATION_ID = 3004;

    private static final String[] NOTIFY_MOVIE_PROJECTION = new String[]{
            MovieContract.COLUMN_ID,
            MovieContract.COLUMN_TITLE,
    };

    // these indices must match the projection
    private static final int INDEX_ID = 0;
    private static final int INDEX_TITLE = 1;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NotificationService(String name) {
        super(name);
    }
    public NotificationService(){
        super("NotificationIntent");
    }


    public static void deleteOldRows(Context context, String page){
        Uri uri = MovieContract.buildMoviePage(Integer.parseInt(page));
        Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{COLUMN_IS_FAVOURITE,COLUMN_IMAGE_URL},null,null,null);
        if(cursor != null && cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
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
            }
        }
        int deleted = context.getContentResolver().delete(
                MovieContract.buildMoviePage(Integer.parseInt(page)),null,null);
        Utility.setPageQuantityFromPreferences(context,1);
        Utility.setRowQuantityFromPreferences(context,20);
        Log.d("LOG_TAG","Deleted : " + deleted + " rows!!!");

    }
    private String[] getBackdrops(String id){
        try {
            return new FetchBackdropsTask().execute(id).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if(!MainActivity.sIsActive) {
            //checking the last update and notify if it' the first of the day
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            //String lastNotificationKey = context.getString(R.string.pref_last_notification);
            String lastBackdropKey = this.getString(R.string.pref_last_backdrop);
            int lastBackdrop = prefs.getInt(lastBackdropKey, 0);
            //long lastSync = prefs.getLong(lastNotificationKey, 0);

            //if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
            // Last sync was more than 1 day ago, let's send a notification with the weather.
            String sortOrder = Utility.getFriendlySortingFromPreferences(getApplication());
            Uri movieUri = MovieContract.buildMovieSorting(sortOrder, "10");

            // we'll query our contentProvider, as always
            Cursor cursor = this.getContentResolver().query(movieUri, NOTIFY_MOVIE_PROJECTION, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                for(int i = 0; i < (int) (Math.random()*(cursor.getCount()-1)); i++){
                    cursor.moveToNext();
                }
                NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();

                String title = cursor.getString(INDEX_TITLE);
                String id = cursor.getString(INDEX_ID);


                    String[] backdrops = getBackdrops(id);
                    if (backdrops != null && backdrops.length != 0) {
                        if (lastBackdrop >= backdrops.length)
                            lastBackdrop = 0;
                        Bitmap bitmap = null;
                        try {
                            bitmap = Picasso.with(this)
                                    .load(MovieContract.URL_PICTURE + backdrops[lastBackdrop])
                                    .get();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        bigPictureStyle.bigPicture(bitmap);
                        bigPictureStyle.setBigContentTitle(title);
                        bigPictureStyle.setSummaryText("Time to watch new movie!");
                    }


                if (Utility.isNotificationEnabled(this)) {
                    Log.d("LOG_TAG","notificationEnabled!!!");
                    NotificationCompat.Builder notif = new NotificationCompat.Builder(this)
                            .setContentTitle(title)
                            .setContentText("Time to watch new movie!")
                            .setStyle(bigPictureStyle)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setDefaults(Notification.DEFAULT_VIBRATE)
                            .setSound(Uri.parse("android.resource://" +
                                    this.getPackageName() + "/" +
                                    R.raw.notification_sound)
                            );

                    Intent resultIntent = new Intent(this, MainActivity.class);
                    TaskStackBuilder tsb = TaskStackBuilder.create(this);
                    tsb.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = tsb.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    notif.setContentIntent(resultPendingIntent);


                    NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(MOVIE_NOTIFICATION_ID, notif.build());
                }
                //refreshing last sync
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(lastBackdropKey, ++lastBackdrop);
                editor.apply();
                deleteOldRows(this,"2");
                cursor.close();
            }

        }
    }
}
