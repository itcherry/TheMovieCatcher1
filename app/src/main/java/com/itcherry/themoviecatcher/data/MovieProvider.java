package com.itcherry.themoviecatcher.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_POPULARITY;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_VOTE_AVERAGE;
import static com.itcherry.themoviecatcher.data.MovieContract.CONTENT_AUTHORITY;
import static com.itcherry.themoviecatcher.data.MovieContract.CONTENT_TYPE;
import static com.itcherry.themoviecatcher.data.MovieContract.CONTENT_TYPE_ITEM;
import static com.itcherry.themoviecatcher.data.MovieContract.CONTENT_URI;
import static com.itcherry.themoviecatcher.data.MovieContract.PATH;
import static com.itcherry.themoviecatcher.data.MovieContract.TABLE_NAME;
import static com.itcherry.themoviecatcher.data.MovieContract.URI_MOVIE;
import static com.itcherry.themoviecatcher.data.MovieContract.URI_MOVIE_ID;
import static com.itcherry.themoviecatcher.data.MovieContract.URI_MOVIE_WITH_SORTING;
import static com.itcherry.themoviecatcher.data.MovieContract.buildMovieUri;
import static com.itcherry.themoviecatcher.data.MovieContract.getProperSorting;

/**
 * Creating own ContentProvider. It is wrapper for SQLite database.
 * It is using provided for sharing information with other different apps.
 * It makes code more sufficient
 */

public class MovieProvider extends ContentProvider {
    private final String LOG_TAG = getClass().getSimpleName();

    private static UriMatcher uriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;
    private SQLiteDatabase db;

    private static UriMatcher buildUriMatcher() {
        UriMatcher retUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        retUriMatcher.addURI(CONTENT_AUTHORITY, PATH, URI_MOVIE);
        retUriMatcher.addURI(CONTENT_AUTHORITY, PATH + "/#", URI_MOVIE_ID);
        retUriMatcher.addURI(CONTENT_AUTHORITY, PATH + "/*/*", URI_MOVIE_WITH_SORTING);

        return retUriMatcher;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = uriMatcher.match(uri);

        switch (match) {
            case URI_MOVIE:
                return CONTENT_TYPE;
            case URI_MOVIE_WITH_SORTING:
                return CONTENT_TYPE;
            case URI_MOVIE_ID:
                return CONTENT_TYPE_ITEM;
            default:
                throw new UnsupportedOperationException("Unknown URI : " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(LOG_TAG, "query, " + uri.toString());

        Cursor retCursor;
        final int matcher = uriMatcher.match(uri);
        String limit = null;

        switch (matcher) {
            case URI_MOVIE:
                if (TextUtils.isEmpty(sortOrder)) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

                    String sortOrderPreferences = preferences.getString("sorting", "top_rated?");
                    if(sortOrderPreferences.equals("top_rated?")){
                        sortOrder = COLUMN_VOTE_AVERAGE;
                    }else{
                        sortOrder = COLUMN_POPULARITY;
                    }
                }
                break;
            case URI_MOVIE_WITH_SORTING:
                List<String> pathSegments = uri.getPathSegments();
                sortOrder = pathSegments.get(1);
                limit = pathSegments.get(2);
                break;
            case URI_MOVIE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = MovieContract.COLUMN_ID + " = " + id;
                } else {
                    selection = selection + " AND " + MovieContract.COLUMN_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI : " + uri);
        }
        if(sortOrder!= null)
            sortOrder = getProperSorting(sortOrder);
        Log.d(LOG_TAG,"SortOrder : " + sortOrder);
        retCursor = mOpenHelper.getWritableDatabase()
                .query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder,
                        limit);
        try {
            retCursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri retUri = null;

        if (uriMatcher.match(uri) == URI_MOVIE) {
            long _id = db.insert(TABLE_NAME, null, values);
            if (_id > 0) {
                retUri = buildMovieUri(_id);
            } else throw new SQLException("Failed to insert row into " + uri);
        } else throw new IllegalArgumentException("Unknown URI : " + uri);

        return retUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete, " + uri.toString());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int retCount = 0;

        if (selection == null) selection = "1";
        switch (uriMatcher.match(uri)) {
            case URI_MOVIE:
                // do not change selection parameter
                break;
            case URI_MOVIE_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_CONTACTS_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = MovieContract.COLUMN_ID + " = " + id;
                } else {
                    selection = selection + " AND " + MovieContract.COLUMN_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI : " + uri);
        }

        retCount = db.delete(TABLE_NAME, selection, selectionArgs);
        if (retCount != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return retCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete, " + uri.toString());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int retCount = 0;

        if (selection == null) selection = "1";
        switch (uriMatcher.match(uri)) {
            case URI_MOVIE:
                // do not change selection parameter
                break;
            case URI_MOVIE_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_CONTACTS_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = MovieContract.COLUMN_ID + " = " + id;
                } else {
                    selection = selection + " AND " + MovieContract.COLUMN_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI : " + uri);
        }

        retCount = db.update(TABLE_NAME, values, selection, selectionArgs);
        if (retCount != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return retCount;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match) {
            case URI_MOVIE_WITH_SORTING:
            case URI_MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
