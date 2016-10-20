package com.itcherry.themoviecatcher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.concurrent.ExecutionException;


public class DetailActivity extends ActionBarActivity {
    private final String LOG_TAG = getClass().getSimpleName();
    String[] backdrops = null;
    private ViewPager pager;
    private PagerAdapter pagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        String id = getIntent().getData().getLastPathSegment();

        try {
            backdrops = new FetchBackdropsTask().execute(id).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        if(savedInstanceState == null){
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI,getIntent().getData());

            DetailFragment df = new DetailFragment();
            df.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, df)
                    .commit();
        }
    }

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
