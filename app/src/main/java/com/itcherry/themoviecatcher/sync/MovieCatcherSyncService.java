package com.itcherry.themoviecatcher.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MovieCatcherSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static MovieCatcherSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("SunshineSyncService", "onCreate - SunshineSyncService");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new MovieCatcherSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("SunshineSyncService","OnUnbind - SunshineSyncService");

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d("SunshineSyncService","OnDestroy - SunshineSyncService");
        //ForecastFragment.stopSwipeRefresh();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}