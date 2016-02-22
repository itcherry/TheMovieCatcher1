package com.itcherry.themoviecatcher;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostersFragment extends Fragment {


    public PostersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_posters, container, false);
        GridView gridView = (GridView) v.findViewById(R.id.grid_layout);
        final ImageAdapter adapter = new ImageAdapter(getActivity());
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(),DetailActivity.class)
                        .putExtra(
                                getString(R.string.movie_id),
                                String.valueOf(adapter.mMovies.get(position).getId()))
                        .putExtra(getString(R.string.movie_desctiption), adapter.mMovies.get(position));
                startActivity(intent);
            }
        });
        return v;
    }


    public class ImageAdapter extends BaseAdapter {
        private final String LOG_TAG = getActivity().getClass().getSimpleName();
        private Context mContext;
        ArrayList<MovieDescription> mMovies = null;
        private ArrayList<ImageView> images;

        public ImageAdapter(Context c) {
            mContext = c;
            final String URL_PICTURE = "http://image.tmdb.org/t/p/w500/";

            try {
                mMovies = new FetchMovieTask().execute("popular?").get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            if (mMovies != null) {
                images = new ArrayList<>();
                ImageView vi;
                for (int i = 0; i < mMovies.size(); i++) {
                    vi = new ImageView(getActivity());
                    Picasso.with(mContext).load(URL_PICTURE + mMovies.get(i).getImageUrl()).into(vi);
                    images.add(vi);
                }
            } else Log.e(LOG_TAG, "Null pointer exception when loading pictures from TMDB");
        }

        public int getCount() {
            return mMovies.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)
                );
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView = images.get(position);
            return imageView;
        }
    }

}
