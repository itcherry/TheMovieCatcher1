package com.itcherry.themoviecatcher;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

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
    private int lastPage = 1;
    private int lastRowQuantity = 20;

    public PostersFragment() {
        // Required empty public constructor
    }

    public interface Callback {
        public void onItemSelected(Uri uri);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            lastPage = savedInstanceState.getInt(getActivity().getString(R.string.pref_last_page));
            lastRowQuantity = savedInstanceState.getInt(getActivity().getString(R.string.pref_last_page));
            updateFlag = savedInstanceState.getBoolean(getActivity().getString(R.string.pref_last_update_flag));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(getActivity().getString(R.string.pref_last_page),lastPage);
        outState.putInt(getActivity().getString(R.string.pref_last_row_quantity),lastRowQuantity);
        outState.putBoolean(getActivity().getString(R.string.pref_last_update_flag),updateFlag);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
                    /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    int prefLastPage = prefs.getInt(getActivity().getString(R.string.pref_last_page), 2);
                    int prefRowsQuantity = prefs.getInt(getActivity().getString(R.string.pref_last_row_quantity), 40);*/

                    //lastRowQuantity += 20;
                    MovieCatcherSyncAdapter.syncImmediately(getActivity(), ++lastPage);

                   /* SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(getActivity().getString(R.string.pref_last_page), ++prefLastPage);
                    editor.putInt(
                            getActivity().getString(R.string.pref_last_row_quantity),
                            prefRowsQuantity += 20);
                    editor.apply();*/
                }
            }
        });

        setHasOptionsMenu(true);
        return v;
    }
    public static void setUpdateFlag(boolean flag){
        updateFlag = flag;
    }
    public static boolean getUpdateFlag(){
        return updateFlag;
    }
    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(syncFinishedReceiver, new IntentFilter(MovieCatcherSyncAdapter.SYNC_FINISHED));
        //getLoaderManager().restartLoader(MY_CURSOR_LOADER_ID, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(syncFinishedReceiver);
    }

    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mFooter.getVisibility() != View.GONE) {
                // last item in grid not on the screen, hide footer:
                mFooter.setVisibility(View.GONE);
            }
            lastRowQuantity += 20;
            getLoaderManager().restartLoader(MY_CURSOR_LOADER_ID, null, PostersFragment.this);
            gridView.invalidateViews();
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MY_CURSOR_LOADER_ID, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            MovieCatcherSyncAdapter.syncImmediately(getActivity(), 1);
            return true;
        }else if(item.getItemId() == R.id.deleteAll){
            lastPage = 1;
            lastRowQuantity = 20;
            MovieCatcherSyncAdapter.deleteOldRows(getActivity(),"1");
        }else if(item.getItemId() == R.id.menu_refresh_loader){
            getLoaderManager().restartLoader(MY_CURSOR_LOADER_ID,null,this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String sortOrder = getFriendlySortingFromPreferences(getActivity());
        Uri uri;
        if(sortOrder.equals(MovieContract.COLUMN_IS_FAVOURITE)){
            uri = MovieContract.buildMovieFavourites();
        }else{
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
