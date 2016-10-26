package com.itcherry.themoviecatcher;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
import android.widget.GridView;
import android.widget.Toast;

import com.itcherry.themoviecatcher.data.MovieContract;
import com.itcherry.themoviecatcher.sync.MovieCatcherSyncAdapter;

import static com.itcherry.themoviecatcher.Utility.getFriendlySortingFromPreferences;
import static com.itcherry.themoviecatcher.Utility.isNetworkConnected;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostersFragment extends Fragment implements LoaderManager.LoaderCallbacks {
    private GridView gridView;
    private View mFooter;
    private View mFooterNoNetwork;
    private ImageAdapter mMovieAdapter;
    public static final int MY_CURSOR_LOADER_ID = 1;
    private static boolean updateFlag = true;
    private int lastPage;
    private int lastRowQuantity;
    private IntentFilter mNetworkChangeIntentFilter;

    //private SyncFinishedReceiver syncFinishedReceiver;

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
        getActivity().unregisterReceiver(networkChangeReceiver);
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

        gridView.setEmptyView(Utility.getEmptyView(getActivity(),getActivity().getLayoutInflater(), container));

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
        mFooterNoNetwork = v.findViewById(R.id.no_network_footer);
        mFooterNoNetwork.setVisibility(View.GONE);
        v.findViewById(R.id.footer_view_no_network_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.isNetworkConnected(getActivity())) {
                    mFooter.setVisibility(View.VISIBLE);
                    mFooterNoNetwork.setVisibility(View.GONE);
                } else {
                    mFooter.setVisibility(View.GONE);
                    mFooterNoNetwork.setVisibility(View.VISIBLE);
                }
            }
        });
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
                    if (Utility.isNetworkConnected(getActivity())) {
                        mFooter.setVisibility(View.VISIBLE);
                        MovieCatcherSyncAdapter.syncImmediately(getActivity(), ++lastPage);
                        lastRowQuantity += 20;
                    } else {
                        mFooterNoNetwork.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        setHasOptionsMenu(true);
        return v;
    }

    /*public View getEmptyView(LayoutInflater inflater, ViewGroup container) {
        View emptyView;
        emptyView = inflater.inflate(R.layout.no_network_layout, container, false);
        if (Utility.isNetworkConnected(getActivity())) {
            emptyView.findViewById(R.id.empty_layout).setVisibility(View.VISIBLE);
            emptyView.findViewById(R.id.no_network_layout).setVisibility(View.GONE);
        } else {
            emptyView.findViewById(R.id.empty_layout).setVisibility(View.GONE);
            emptyView.findViewById(R.id.no_network_layout).setVisibility(View.VISIBLE);

        }
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
        getActivity().addContentView(emptyView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );
        return emptyView;
    }*/

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
        getActivity().registerReceiver(networkChangeReceiver, mNetworkChangeIntentFilter);
        /*if (!getUpdateFlag() && mFooter.getVisibility() == View.GONE) {
            // last item in grid not on the screen, show footer:
            mFooter.setVisibility(View.VISIBLE);
        }*/
        getLoaderManager().restartLoader(MY_CURSOR_LOADER_ID, null, this);
    }

    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.hasExtra(MovieCatcherSyncAdapter.SYNC_ERROR_INTENT_EXTRA)) {
                if (mFooter.getVisibility() != View.GONE) {
                    // last item in grid not on the screen, hide footer:
                    mFooter.setVisibility(View.GONE);
                }
                getLoaderManager().restartLoader(MY_CURSOR_LOADER_ID, null, PostersFragment.this);
                gridView.invalidateViews();
            } else {
                Toast.makeText(getActivity(), "Error while loading from server", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkConnected(context)) {
                gridView.getEmptyView().findViewById(R.id.empty_layout).setVisibility(View.VISIBLE);
                gridView.getEmptyView().findViewById(R.id.no_network_layout).setVisibility(View.GONE);
                if (!getUpdateFlag()) {
                    mFooterNoNetwork.setVisibility(View.GONE);
                    mFooter.setVisibility(View.VISIBLE);
                    setUpdateFlag(true);
                    getLoaderManager().restartLoader(MY_CURSOR_LOADER_ID,null,PostersFragment.this);
                } else {
                    MovieCatcherSyncAdapter.syncImmediately(getActivity(), 1);
                }
            } else {
                gridView.getEmptyView().findViewById(R.id.empty_layout).setVisibility(View.GONE);
                gridView.getEmptyView().findViewById(R.id.no_network_layout).setVisibility(View.VISIBLE);

            }
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mNetworkChangeIntentFilter = new IntentFilter();
        mNetworkChangeIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkChangeIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

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
