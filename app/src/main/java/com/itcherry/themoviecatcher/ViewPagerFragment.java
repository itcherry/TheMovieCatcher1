package com.itcherry.themoviecatcher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ViewPagerFragment extends Fragment {
    private String backdrop;
    private static final String ARGUMENT_IMAGE = "argument_bitmap_image";

    static ViewPagerFragment newInstance(String backdrop){
        ViewPagerFragment pageFragment = new ViewPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_IMAGE, backdrop);
        pageFragment.setArguments(bundle);
        return pageFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backdrop = getArguments().getString(ARGUMENT_IMAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pageview_item, container,false);
        ImageView imageView = (ImageView) v.findViewById(R.id.fragment_imageview_item);
        final String URL_PICTURE = "http://image.tmdb.org/t/p/w500/";
        Picasso.with(getActivity()).load(URL_PICTURE + backdrop).into(imageView);
        return v;
    }
}
