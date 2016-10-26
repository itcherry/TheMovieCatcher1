package com.itcherry.themoviecatcher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.itcherry.themoviecatcher.sync.MovieCatcherSyncAdapter;

import java.util.Calendar;

public class MainActivity extends ActionBarActivity implements PostersFragment.Callback {
    public static boolean sIsActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sIsActive = true;
        Log.d("LOG_TAG","sIsActive : " + sIsActive);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //StartAppSDK.init(this,"208203317",true);
        if(savedInstanceState == null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_fragment, new PostersFragment())
                    .commit();
        MovieCatcherSyncAdapter.initializeSyncAdapter(this);
        Utility.setNetworkStateFromPreferences(this,Utility.isNetworkConnected(this));
        setAlarm();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void setAlarm() {

        Intent intent = new Intent(this, NotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this,
                NotificationService.MOVIE_NOTIFICATION_ID, intent, 0);

        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 19);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
        /*alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis()+1000*20,
                1000*60*3, pendingIntent);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        sIsActive = false;
        Log.d("LOG_TAG","sIsActive : " + sIsActive);
        super.onDestroy();
    }

    @Override
    public void onItemSelected(Uri uri) {
        Intent intent = new Intent(this,DetailActivity.class).setData(uri);
        startActivity(intent);
    }
}
