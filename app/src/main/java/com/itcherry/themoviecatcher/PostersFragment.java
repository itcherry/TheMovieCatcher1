package com.itcherry.themoviecatcher;


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
    private ImageAdapter mMovieAdapter;
    private static final int MY_CURSOR_LOADER_ID = 1;

    public PostersFragment() {
        // Required empty public constructor
    }
    public interface Callback{
        public void onItemSelected(Uri uri);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_posters, container, false);
        gridView = (GridView) v.findViewById(R.id.grid_layout);

        String sortOrder = Utility.getFriendlySortingFromPreferences(getActivity());
        Uri movieURI = MovieContract.buildMovieSorting(sortOrder,"20");
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
                    ((Callback)getActivity()).onItemSelected(
                            MovieContract.buildMovieUri(
                                    cur.getInt(cur.getColumnIndex(MovieContract.COLUMN_ID)
                                    )
                            )
                    );
                }
            }
        });

        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(MY_CURSOR_LOADER_ID,null,this);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MY_CURSOR_LOADER_ID, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            updatePosters();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updatePosters() {
        MovieCatcherSyncAdapter.syncImmediately(getActivity());
        gridView.invalidateViews();
        getLoaderManager().restartLoader(MY_CURSOR_LOADER_ID,null,this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String sortOrder = getFriendlySortingFromPreferences(getActivity());
        return new CursorLoader(getActivity(),
                MovieContract.buildMovieSorting(sortOrder,"20"),
                null, null, null,
                null);
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
