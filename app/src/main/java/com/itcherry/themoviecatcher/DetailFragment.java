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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.itcherry.themoviecatcher.data.MovieContract;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks{
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if(arguments != null){
            mUri = arguments.getParcelable(DETAIL_URI);
        }

        View v = inflater.inflate(R.layout.fragment_detail, container, false);
        tvTitle = (TextView) v.findViewById(R.id.tvTitle);
        tvDesription = (TextView) v.findViewById(R.id.tvTextDescription);
        tvReleaseDate = (TextView) v.findViewById(R.id.tvReleaseDate);
        tvRate = (TextView) v.findViewById(R.id.tvNumberRate);
        tvCountRate = (TextView) v.findViewById(R.id.tvRate);
        ratingBar = (RatingBar) v.findViewById(R.id.ratingBar);

        return v;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_FOR_DETAIL_FRAGMENT_ID,null,this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if(mUri != null) {
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
        if(data != null){
            if(((Cursor)data).moveToFirst()){
                tvTitle.setText(((Cursor)data).getString(COL_MOVIE_TITLE));
                tvDesription.setText(((Cursor)data).getString(COL_MOVIE_OVERVIEW));
                tvReleaseDate.setText(tvReleaseDate.getText() +
                        ((Cursor)data).getString(COL_MOVIE_RELEASE_DATE));
                int rate = (int) ((Cursor)data).getDouble(COL_MOVIE_VOTE_AVERAGE);
                tvRate.setText(String.valueOf(rate));
                ratingBar.setNumStars(5);
                ratingBar.setProgress(rate);
                ratingBar.setClickable(false);
                //ratingBar.setStepSize(10);
                if (((Cursor)data).getDouble(COL_MOVIE_VOTE_AVERAGE) >= 6.5 &&
                        ((Cursor)data).getDouble(COL_MOVIE_VOTE_AVERAGE) <= 7.7) {
                    tvRate.setTextColor(getResources().getColor(R.color.orange));
                } else if (((Cursor)data).getDouble(COL_MOVIE_VOTE_AVERAGE) > 7.7) {
                    tvRate.setTextColor(getResources().getColor(R.color.green));
                } else {
                    tvRate.setTextColor(getResources().getColor(R.color.red));
                }
                tvCountRate.setText(tvCountRate.getText() +
                        String.valueOf(((Cursor)data).getDouble(COL_MOVIE_VOTE_COUNT)) + " people rated");
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
