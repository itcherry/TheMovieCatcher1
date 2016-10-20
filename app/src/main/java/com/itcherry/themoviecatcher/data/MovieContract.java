package com.itcherry.themoviecatcher.data;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

public class MovieContract {
    /*
        Some constants, which will use in ContentProvider
     */
    public static final String CONTENT_AUTHORITY = "com.itcherry.themoviecatcher";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH = "movie";

    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

    // A lot of strings
    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;
    //One string
    public static final String CONTENT_TYPE_ITEM =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

    public static final String SORT_ORDER_KEY = "sortOrder";
    public static final String LIMIT_KEY = "limit";
    public static Uri buildMovieSorting(String sortOrder, String limit) {
        return CONTENT_URI.buildUpon()
                .appendPath(sortOrder)
                .appendPath(limit)
                .build();
    }

    public static Uri buildMovieUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }
    public static Uri buildMoviePage(int page){
        return CONTENT_URI.buildUpon()
                .appendPath(PATH_PAGE_DELETING)
                .appendPath(String.valueOf(page))
                .build();
    }
    public static Uri buildMovieFavourites(){
        return CONTENT_URI.buildUpon().appendPath("favourites").build();
    }

    public static String getProperSorting(String sortOrder){
        return sortOrder + " DESC";
    }
    //For UriMatcher
    public static final int URI_MOVIE = 1;
    public static final int URI_MOVIE_ID = 2;
    public static final int URI_MOVIE_WITH_SORTING = 3;
    public static final int URI_MOVIE_WITH_PAGE = 4;
    public static final int URI_MOVIE_FAVOURITES = 5;

    //For storing images in the external storage
    private static final String FILE_DIR = "/.com.itcherry.moviecatcher";

    public static File getDirForImages(String filename, Context context) {
        File sdPath;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            sdPath = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath()
                            + FILE_DIR
            );
        } else {
            sdPath = new File(
                    context.getFilesDir().getAbsolutePath()
                            + FILE_DIR
            );
        }
        sdPath.mkdirs();
        return new File(sdPath, filename);
    }

    /*
        Database columns
     */

    public static final String TABLE_NAME = "movies";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_OVERVIEW = "overview";
    public static final String COLUMN_RELEASE_DATE = "release_date";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_POPULARITY = "popularity";
    public static final String COLUMN_VOTE_AVERAGE = "vote_average";
    public static final String COLUMN_VOTE_COUNT = "vote_count";
    public static final String COLUMN_IS_FAVOURITE = "is_favourite";
    public static final String COLUMN_PAGE = "page";

    public final static String URL_PICTURE = "http://image.tmdb.org/t/p/w500/";
    public final static String PATH_PAGE_DELETING = "deletepage";
}
