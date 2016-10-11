package com.itcherry.themoviecatcher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * Created by Dron on 22-Feb-16.
 */
public class DetailFragment extends Fragment {
    MovieDescription movieDescription;
    private TextView tvDesription;
    private TextView tvReleaseDate;
    private TextView tvRate;
    private TextView tvCountRate;
    private RatingBar ratingBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieDescription = getActivity().getIntent().getParcelableExtra(getString(R.string.movie_desctiption));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, container, false);
        tvDesription = (TextView) v.findViewById(R.id.tvTextDescription);
        tvReleaseDate = (TextView) v.findViewById(R.id.tvReleaseDate);
        tvRate = (TextView) v.findViewById(R.id.tvNumberRate);
        tvCountRate = (TextView) v.findViewById(R.id.tvRate);
        ratingBar = (RatingBar) v.findViewById(R.id.ratingBar);

        tvDesription.setText(movieDescription.getOverview());
        tvReleaseDate.setText(tvReleaseDate.getText() + movieDescription.getReleaseDate());
        tvRate.setText(String.valueOf(movieDescription.getVoteAverage()));
        int rate = (int) movieDescription.getVoteAverage();
        ratingBar.setNumStars(5);
        ratingBar.setProgress(rate);
        ratingBar.setClickable(false);
        //ratingBar.setStepSize(10);
        if (movieDescription.getVoteAverage() >= 6.5 && movieDescription.getVoteAverage() <= 7.7) {
            tvRate.setTextColor(getResources().getColor(R.color.orange));
        } else if (movieDescription.getVoteAverage() > 7.7) {
            tvRate.setTextColor(getResources().getColor(R.color.green));
        } else {
            tvRate.setTextColor(getResources().getColor(R.color.red));
        }
        tvCountRate.setText(tvCountRate.getText() + String.valueOf(movieDescription.getVoteCount()) + " people rated");
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
}
