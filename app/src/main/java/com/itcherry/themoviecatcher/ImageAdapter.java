package com.itcherry.themoviecatcher;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.itcherry.themoviecatcher.data.MovieContract;

import java.io.FileInputStream;
import java.io.IOException;

public class ImageAdapter extends CursorAdapter {
    //private Context mContext;
    private final String LOG_TAG = this.getClass().getSimpleName();
    //ArrayList<MovieDescription> mMovies;
    //private ArrayList<ImageView> images;

    public ImageAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    /*public ImageAdapter(Context c) {
        mContext = c;
        updatePoster();
    }

    public int getCount() {
        return mMovies.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public void updatePoster(){
        String sorting = Utility.getSortingFromPreferences(mContext);

        try {
            mMovies = new FetchMovieTask().execute(sorting).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if (mMovies != null) {
            images = new ArrayList<>();
            ImageView vi;
            for (int i = 0; i < mMovies.size(); i++) {
                vi = new ImageView(mContext);
                FileInputStream inputStream = new FileInputStream()
                vi.setImageBitmap();
                Picasso.with(mContext).load(MovieContract.URL_PICTURE + mMovies.get(i).getImageUrl()).into(vi);
                images.add(vi);
            }
        } else Log.e(LOG_TAG, "Null pointer exception when loading pictures from TMDB");
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
    }*/

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView iv = new ImageView(context);
        iv.setLayoutParams(new GridView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT));
        return iv;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(
                    MovieContract.getDirForImages(
                            cursor.getString(cursor.getColumnIndex(
                                    MovieContract.COLUMN_IMAGE_URL)),
                            context
                    )
            );
            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
            ((ImageView) view).setImageBitmap(bmp);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}