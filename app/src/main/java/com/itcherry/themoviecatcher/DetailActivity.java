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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.concurrent.ExecutionException;

import static com.itcherry.themoviecatcher.Utility.isNetworkConnected;


public class DetailActivity extends ActionBarActivity {
    private final String LOG_TAG = getClass().getSimpleName();
    String[] backdrops = null;
    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    private View noNetworkView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
            noNetworkView = Utility.getEmptyView(this, getLayoutInflater(), null);
        if (Utility.isNetworkConnected(this)) {
            setContentView(R.layout.activity_detail);
            if (savedInstanceState == null) {
                String id = getIntent().getData().getLastPathSegment();

                try {
                    backdrops = new FetchBackdropsTask().execute(id).get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

                pager = (ViewPager) findViewById(R.id.pager);
                pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
                pager.setAdapter(pagerAdapter);


                Bundle arguments = new Bundle();
                arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

                DetailFragment df = new DetailFragment();
                df.setArguments(arguments);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.movie_detail_container, df)
                        .commit();
            }
        } else {
            setContentView(noNetworkView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkConnected(context)) {
                noNetworkView.findViewById(R.id.empty_layout).setVisibility(View.VISIBLE);
                noNetworkView.findViewById(R.id.no_network_layout).setVisibility(View.GONE);
                //onCreate(null);
            } else {
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
