package com.itcherry.themoviecatcher;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;

import com.itcherry.themoviecatcher.data.MovieContract;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks {
    private TextView tvDesription;
    private TextView tvReleaseDate;
    private TextView tvRate;
    private TextView tvCountRate;
    private TextView tvTitle;
    private RatingBar ratingBar;

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.COLUMN_TITLE,
            MovieContract.COLUMN_OVERVIEW,
            MovieContract.COLUMN_RELEASE_DATE,
            MovieContract.COLUMN_VOTE_AVERAGE,
            MovieContract.COLUMN_VOTE_COUNT,
            MovieContract.COLUMN_IS_FAVOURITE
    };
    public static final int COL_MOVIE_TITLE = 0;
    public static final int COL_MOVIE_OVERVIEW = 1;
    public static final int COL_MOVIE_RELEASE_DATE = 2;
    public static final int COL_MOVIE_VOTE_AVERAGE = 3;
    public static final int COL_MOVIE_VOTE_COUNT = 4;
    public static final int COL_MOVIE_IS_FAVOURITE = 5;

    public static final int LOADER_FOR_DETAIL_FRAGMENT_ID = 2;
    public static final String DETAIL_URI = "uri";
    private Uri mUri;
    private boolean isCheckedStar = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DETAIL_URI);
        }

        View v = inflater.inflate(R.layout.fragment_detail, container, false);
        tvTitle = (TextView) v.findViewById(R.id.tvTitle);
        tvDesription = (TextView) v.findViewById(R.id.tvTextDescription);
        tvReleaseDate = (TextView) v.findViewById(R.id.tvReleaseDate);
        tvRate = (TextView) v.findViewById(R.id.tvNumberRate);
        tvCountRate = (TextView) v.findViewById(R.id.tvRate);
        ratingBar = (RatingBar) v.findViewById(R.id.ratingBar);

        setHasOptionsMenu(true);
        return v;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d("LOG_TAG","Fragment, onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);
        /*getActivity().getMenuInflater()*/inflater.inflate(R.menu.detail_fragment, menu);
        MenuItem item = menu.findItem(R.id.menu_favourite_star);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Log.d("LOG_TAG", "isChecked : " + isCheckedStar);
            ((CheckBox)item.getActionView()).setChecked(isCheckedStar);
            item.getActionView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.menu_favourite_star:
                            //item.setChecked(!item.isChecked());
                            ContentValues cv = new ContentValues();
                            if (((CheckBox)v).isChecked()) {
                                Log.d("/D", "uri : " + mUri + " ; is favourite");
                                cv.put(MovieContract.COLUMN_IS_FAVOURITE, 1);
                                getActivity().getContentResolver().update(mUri, cv, null, null);
                            } else {
                                Log.d("/D", "uri : " + mUri + " ; is not favourite");
                                cv.put(MovieContract.COLUMN_IS_FAVOURITE, 0);
                                getActivity().getContentResolver().update(mUri, cv, null, null);
                            }
                    }
                }
            });

        }
        //menu.getItem(R.id.menu_favourite_star).setChecked(isCheckedStar);
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("LOG_TAG", "fragment, pressed menu");
        switch (item.getItemId()) {
            case R.id.menu_favourite_star:
                //item.setChecked(!item.isChecked());
                ContentValues cv = new ContentValues();
                if (item.isChecked()) {
                    Log.d("/D", "uri : " + mUri + " ; is favourite");
                    cv.put(MovieContract.COLUMN_IS_FAVOURITE, 1);
                    getActivity().getContentResolver().update(mUri, cv, null, null);
                } else {
                    Log.d("/D", "uri : " + mUri + " ; is not favourite");
                    cv.put(MovieContract.COLUMN_IS_FAVOURITE, 0);
                    getActivity().getContentResolver().update(mUri, cv, null, null);
                }
                return true;
        }
        return false;
    }*/

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_FOR_DETAIL_FRAGMENT_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        if (data != null) {
            if (((Cursor) data).moveToFirst()) {
                tvTitle.setText(((Cursor) data).getString(COL_MOVIE_TITLE));
                tvDesription.setText(((Cursor) data).getString(COL_MOVIE_OVERVIEW));
                tvReleaseDate.setText(tvReleaseDate.getText() +
                        ((Cursor) data).getString(COL_MOVIE_RELEASE_DATE));
                int rate = (int) ((Cursor) data).getDouble(COL_MOVIE_VOTE_AVERAGE);
                tvRate.setText(String.valueOf(rate));
                ratingBar.setNumStars(5);
                ratingBar.setProgress(rate);
                ratingBar.setClickable(false);
                //ratingBar.setStepSize(10);
                if (((Cursor) data).getDouble(COL_MOVIE_VOTE_AVERAGE) >= 6.5 &&
                        ((Cursor) data).getDouble(COL_MOVIE_VOTE_AVERAGE) <= 7.7) {
                    tvRate.setTextColor(getResources().getColor(R.color.orange));
                } else if (((Cursor) data).getDouble(COL_MOVIE_VOTE_AVERAGE) > 7.7) {
                    tvRate.setTextColor(getResources().getColor(R.color.green));
                } else {
                    tvRate.setTextColor(getResources().getColor(R.color.red));
                }
                tvCountRate.setText(tvCountRate.getText() +
                        String.valueOf(((Cursor) data).getDouble(COL_MOVIE_VOTE_COUNT)) + " people rated");
                if (((Cursor) data).getInt(COL_MOVIE_IS_FAVOURITE) == 1) {
                    Log.d("LOG_TAG","Is favourite");
                    isCheckedStar = true;
                } else {
                    Log.d("LOG_TAG","Is favourite");
                    isCheckedStar = false;
                }
            }
        }
    }


    @Override
    public void onLoaderReset(Loader loader) {

    }
}
