package com.itcherry.themoviecatcher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.itcherry.themoviecatcher.sync.MovieCatcherSyncAdapter;

public class MainActivity extends ActionBarActivity implements PostersFragment.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //StartAppSDK.init(this,"208203317",true);
        if(savedInstanceState == null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_fragment, new PostersFragment())
                    .commit();
        MovieCatcherSyncAdapter.initializeSyncAdapter(this);
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

    @Override
    protected void onResume() {
        super.onResume();
        //MovieCatcherSyncAdapter.syncImmediately(this);
    }

    @Override
    public void onItemSelected(Uri uri) {
        Intent intent = new Intent(this,DetailActivity.class).setData(uri);
        startActivity(intent);
    }
}
