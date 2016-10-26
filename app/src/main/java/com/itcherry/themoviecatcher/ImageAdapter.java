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
import com.itcherry.themoviecatcher.sync.MovieCatcherSyncAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageAdapter extends CursorAdapter {

    private final String LOG_TAG = this.getClass().getSimpleName();

    public ImageAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView iv = new ImageView(context);
        iv.setLayoutParams(new GridView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return iv;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        FileInputStream inputStream;
        try {
            File file;
            String filename = cursor.getString(cursor.getColumnIndex(
                    MovieContract.COLUMN_IMAGE_URL));
            if (!(file = MovieContract.getDirForImages(filename, context)).exists()) {
                NotificationService.deleteOldRows(context,"1");
                MovieCatcherSyncAdapter.syncImmediately(context,1);
                /*Toast.makeText(
                        context,
                        "Wait a second, somebody had deleted photos from storage",
                        Toast.LENGTH_SHORT).show();*/
                PostersFragment.setUpdateFlag(false);
                Utility.setRowQuantityFromPreferences(context,20);
                Utility.setPageQuantityFromPreferences(context,1);
            }

            inputStream = new FileInputStream(file);

            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
            ((ImageView) view).setImageBitmap(bmp);
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}