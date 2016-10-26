package com.itcherry.themoviecatcher.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_ID;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_IMAGE_URL;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_IS_FAVOURITE;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_OVERVIEW;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_PAGE;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_POPULARITY;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_RELEASE_DATE;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_TITLE;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_VOTE_AVERAGE;
import static com.itcherry.themoviecatcher.data.MovieContract.COLUMN_VOTE_COUNT;
import static com.itcherry.themoviecatcher.data.MovieContract.TABLE_NAME;


public class MovieDbHelper extends SQLiteOpenHelper{

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MovieCatcherDatabase.db";
    private static final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " +
            TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY," +
            COLUMN_TITLE + " TEXT NOT NULL," +
            COLUMN_OVERVIEW + " TEXT NOT NULL," +
            COLUMN_RELEASE_DATE + " DATE NOT NULL," +
            COLUMN_IMAGE_URL + " TEXT NOT NULL," +
            COLUMN_POPULARITY + " REAL NOT NULL," +
            COLUMN_VOTE_AVERAGE + " REAL NOT NULL," +
            COLUMN_VOTE_COUNT + " INTEGER NOT NULL," +
            COLUMN_IS_FAVOURITE + " INTEGER DEFAULT 0," +
            COLUMN_PAGE + " INTEGER NOT NULL," +
            "UNIQUE ( " + COLUMN_ID + ") ON CONFLICT REPLACE);";

    private static final String SQL_ON_UPGRADE = "DROP TABLE IF EXISTS" + TABLE_NAME;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_ON_UPGRADE);
        onCreate(db);
    }

}
