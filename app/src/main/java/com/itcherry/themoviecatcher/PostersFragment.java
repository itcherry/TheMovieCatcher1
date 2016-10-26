package com.itcherry.themoviecatcher;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.itcherry.themoviecatcher.data.MovieContract;
import com.itcherry.themoviecatcher.sync.MovieCatcherSyncAdapter;

import static com.itcherry.themoviecatcher.Utility.getFriendlySortingFromPreferences;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostersFragment extends Fragment implements LoaderManager.LoaderCallbacks {
    private GridView gridView;
    private View mFooter;
    private ImageAdapter mMovieAdapter;
    private static final int MY_CURSOR_LOADER_ID = 1;
    private static boolean updateFlag = true;
    private int lastPage;
    private int lastRowQuantity;

    public PostersFragment() {
        // Required empty public constructor
    }

    public interface Callback {
        public void onItemSelected(Uri uri);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            //lastPage = savedInstanceState.getInt(getActivity().getString(R.string.pref_last_page));
            //lastRowQuantity = savedInstanceState.getInt(getActivity().getString(R.string.pref_last_row_quantity));
            updateFlag = savedInstanceState.getBoolean(getActivity().getString(R.string.pref_last_update_flag));
        //Log.d("LOG_TAG", " onCreate, last page : " + lastPage + "; rowQuantity : " + lastRowQuantity);

        lastRowQuantity = Utility.getRowQuantityFromPreferences(getActivity());
        lastPage = Utility.getPageQuantityFromPreferences(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LOG_TAG", "onDestroyFragment");
        getActivity().unregisterReceiver(syncFinishedReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Utility.setPageQuantityFromPreferences(getActivity(), lastPage);
        Utility.setRowQuantityFromPreferences(getActivity(), lastRowQuantity);
        outState.putBoolean(getActivity().getString(R.string.pref_last_update_flag), updateFlag);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        Log.d("LOG_TAG", "onCreateVIew");
        View v = inflater.inflate(R.layout.fragment_posters, container, false);
        gridView = (GridView) v.findViewById(R.id.grid_layout);

        String sortOrder = Utility.getFriendlySortingFromPreferences(getActivity());
        Uri movieURI = MovieContract.buildMovieSorting(sortOrder, String.valueOf(lastRowQuantity));
        Cursor cursor = (getActivity()).getContentResolver().query(
                movieURI,
                null,
                null,
                null,
                sortOrder
        );

        gridView.setEmptyView(getEmptyViewForGridView(getActivity().getLayoutInflater(), container));

        mMovieAdapter = new ImageAdapter(getActivity(), cursor, false);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cur = (Cursor) parent.getItemAtPosition(position);
                if (cur != null) {
                    ((Callback) getActivity()).onItemSelected(
                            MovieContract.buildMovieUri(
                                    cur.getInt(cur.getColumnIndex(MovieContract.COLUMN_ID)
                                    )
                            )
                    );
                }
            }
        });

        mFooter = v.findViewById(R.id.footer_view_id);
        mFooter.setVisibility(View.GONE);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (firstVisibleItem + visibleItemCount == totalItemCount &&
                        totalItemCount != 0 &&
                        lastRowQuantity == totalItemCount &&
                        getUpdateFlag()) {

                    setUpdateFlag(false);
                    mFooter.setVisibility(View.VISIBLE);
                    MovieCatcherSyncAdapter.syncImmediately(getActivity(), ++lastPage);
                    lastRowQuantity += 20;
                }
            }
        });

        setHasOptionsMenu(true);
        return v;
    }

    private View getEmptyViewForGridView(LayoutInflater inflater, ViewGroup container) {
        View emptyView;
        emptyView = inflater.inflate(R.layout.grid_view_empty_layout, container, false);
        if (Utility.isNetworkConnected(getActivity())) {
            emptyView.findViewById(R.id.empty_layout).setVisibility(View.VISIBLE);
            emptyView.findViewById(R.id.no_network_layout).setVisibility(View.GONE);
        } else {
            emptyView.findViewById(R.id.empty_layout).setVisibility(View.GONE);
            emptyView.findViewById(R.id.no_network_layout).setVisibility(View.VISIBLE);
            Button showNetworkButton = (Button) emptyView.findViewById(R.id.open_network_button);
            showNetworkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClassName("com.android.settings",
                            "com.android.settings.Settings$DataUsageSummaryActivity");
                    startActivity(intent);
                }
            });
            Button showWifiButton = (Button) emptyView.findViewById(R.id.open_wifi_button);
            showWifiButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(intent);
                }
            });

        }
        getActivity().addContentView(emptyView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );
        return emptyView;
    }

    public static void setUpdateFlag(boolean flag) {
        updateFlag = flag;
    }

    public static boolean getUpdateFlag() {
        return updateFlag;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().registerReceiver(syncFinishedReceiver, new IntentFilter(MovieCatcherSyncAdapter.SYNC_FINISHED));
        if (!getUpdateFlag() && mFooter.getVisibility() == View.GONE) {
            // last item in grid not on the screen, show footer:
            mFooter.setVisibility(View.VISIBLE);
        }
        getLoaderManager().restartLoader(MY_CURSOR_LOADER_ID, null, this);
    }

    private void checkEmptyView() {
        if (!Utility.isNetworkConnected(getActivity())) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    do {
                    }
                    while (!Utility.isNetworkConnected(getActivity()) && MainActivity.sIsActive);
                    if(MainActivity.sIsActive)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gridView.getEmptyView().findViewById(R.id.empty_layout).setVisibility(View.VISIBLE);
                            gridView.getEmptyView().findViewById(R.id.no_network_layout).setVisibility(View.GONE);
                            MovieCatcherSyncAdapter.syncImmediately(getActivity(), 1);
                        }
                    });

                }
            }).start();
        }
    }

    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (mFooter.getVisibility() != View.GONE) {
                // last item in grid not on the screen, hide footer:
                mFooter.setVisibility(View.GONE);
            }
            getLoaderManager().restartLoader(MY_CURSOR_LOADER_ID, null, PostersFragment.this);
            gridView.invalidateViews();
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(gridView != null && gridView.getCount() == 0)
            checkEmptyView();
        getLoaderManager().initLoader(MY_CURSOR_LOADER_ID, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            MovieCatcherSyncAdapter.syncImmediately(getActivity(), 1);
            return true;
        } else if (item.getItemId() == R.id.deleteAll) {
            lastPage = 1;
            lastRowQuantity = 20;
            NotificationService.deleteOldRows(getActivity(), "1");
        } else if (item.getItemId() == R.id.menu_refresh_loader) {
            getLoaderManager().restartLoader(MY_CURSOR_LOADER_ID, null, this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String sortOrder = getFriendlySortingFromPreferences(getActivity());
        Uri uri;
        if (sortOrder.equals(MovieContract.COLUMN_IS_FAVOURITE)) {
            uri = MovieContract.buildMovieFavourites();
        } else {
            uri = MovieContract.buildMovieSorting(
                    sortOrder,
                    String.valueOf(lastRowQuantity)
            );
        }
        return new CursorLoader(
                getActivity(),
                uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mMovieAdapter.swapCursor((Cursor) data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mMovieAdapter.swapCursor(null);
    }


}
