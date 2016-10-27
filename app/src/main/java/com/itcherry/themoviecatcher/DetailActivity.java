package com.itcherry.themoviecatcher;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.concurrent.ExecutionException;

public class DetailActivity extends ActionBarActivity {
    private final String LOG_TAG = getClass().getSimpleName();
    String[] backdrops = null;
    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    Bundle savedInstanceState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        setContentView(R.layout.activity_detail);
        onCreateViewPager();

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

            DetailFragment df = new DetailFragment();
            df.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, df)
                    .commit();
        }
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void onCreateViewPager() {
        pager = (ViewPager) findViewById(R.id.pager);
        if (Utility.isNetworkConnected(this)) {
            if (savedInstanceState == null) {

                String id = getIntent().getData().getLastPathSegment();
                try {
                    backdrops = new FetchBackdropsTask().execute(id).get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                backdrops = savedInstanceState.getStringArray("backdrops");
            }

            pager.setVisibility(View.VISIBLE);
            findViewById(R.id.no_network_pb_detail_activity).setVisibility(View.GONE);
            pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(pagerAdapter);
        }else{
            pager.setVisibility(View.GONE);
            findViewById(R.id.no_network_pb_detail_activity).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "NetworkChangeReceiver onReceive!");
            onCreateViewPager();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ViewPagerFragment.newInstance(backdrops[position]);
        }

        @Override
        public int getCount() {
            return backdrops.length;
        }

    }
}
/*public class DetailActivity extends ActionBarActivity {
    private final String LOG_TAG = getClass().getSimpleName();
    String[] backdrops = null;
    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    private View noNetworkView;
    boolean isConnected;
    Bundle savedInstanceState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "OnCreate DetailActivity");
        noNetworkView = Utility.getEmptyView(this, getLayoutInflater(), null);
        this.savedInstanceState = savedInstanceState;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        if(savedInstanceState == null) {
            if (Utility.isNetworkConnected(this)) {
                isConnected = true;
                startDetailFragment();
            } else {
                isConnected = false;
                setContentView(noNetworkView);
            }
        }else{
            if(isConnected = savedInstanceState.getBoolean("isConnected")){
                startDetailFragment();
            }else{
                setContentView(noNetworkView);
            }

        }
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray("backdrops", backdrops);
        outState.putBoolean("isConnected",isConnected);
    }

    private void startDetailFragment() {
        if (savedInstanceState == null) {
            String id = getIntent().getData().getLastPathSegment();
            try {
                backdrops = new FetchBackdropsTask().execute(id).get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            backdrops = savedInstanceState.getStringArray("backdrops");
        }
            setContentView(R.layout.activity_detail);
            pager = (ViewPager) findViewById(R.id.pager);
            pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(pagerAdapter);


        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

            DetailFragment df = new DetailFragment();
            df.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, df)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             Log.d(LOG_TAG, "NetworkChangeReceiver onReceive!");
            if (isNetworkConnected(context) && !isConnected) {
                //isConnected = true;
                Log.d(LOG_TAG, "NetworkChangeReceiver network is connected!");
                noNetworkView.findViewById(R.id.empty_layout).setVisibility(View.VISIBLE);
                noNetworkView.findViewById(R.id.no_network_layout).setVisibility(View.GONE);
                //setContentView(R.layout.activity_detail);
                startDetailFragment();
            } else if(!isNetworkConnected(context)){
                noNetworkView.findViewById(R.id.empty_layout).setVisibility(View.GONE);
                noNetworkView.findViewById(R.id.no_network_layout).setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.detail_fragment,menu);
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.detail_fragment, menu);
        return super.onCreateOptionsMenu(menu);
        //return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter{

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ViewPagerFragment.newInstance(backdrops[position]);
        }

        @Override
        public int getCount() {
            return backdrops.length;
        }

    }
}*/
