package com.itcherry.themoviecatcher;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<MovieDescription>> {
    private final String LOG_TAG = getClass().getSimpleName();

    private ArrayList<MovieDescription> getImagesFromJsonString(String postersJsonStr){

        final String RESULTS_ARRAY = "results";
        final String POSTER_IMAGE_URL = "poster_path";
        final String POSTER_OVERVIEW = "overview";
        final String POSTER_RELEASE_DATE = "release_date";
        final String POSTER_ID = "id";
        final String POSTER_TITLE = "title";
        final String POSTER_VOTE_COUNT = "vote_count";
        final String POSTER_VOTE_AVERAGE = "vote_average";

        ArrayList<MovieDescription> result = new ArrayList<>();
        MovieDescription movie;
        try {
            JSONObject jsonObject = new JSONObject(postersJsonStr);
            JSONArray results = jsonObject.getJSONArray(RESULTS_ARRAY);

            JSONObject object;

            for (int i = 0; i < results.length(); i++) {
                object = results.getJSONObject(i);
                movie = new MovieDescription(
                        object.getInt(POSTER_ID),
                        object.getInt(POSTER_VOTE_COUNT),
                        object.getString(POSTER_TITLE),
                        object.getDouble(POSTER_VOTE_AVERAGE),
                        object.getString(POSTER_RELEASE_DATE),
                        object.getString(POSTER_IMAGE_URL),
                        object.getString(POSTER_OVERVIEW)
                        );
                result.add(movie);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG,e.getMessage());
        }
        return result;
    }
    @Override
    protected ArrayList<MovieDescription> doInBackground(String... params) {
        if (params[0] == null) return null;
        String postersJsonStr = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "?";
        /*final String POPULAR = "popular?";
        final String TOP_RATED = "top_rated?";*/
        final String API_KEY = "api_key";
        final String SORTING = "sort_by";

        try {
            Uri uri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(SORTING, "vote_average.desc")
                    .appendQueryParameter(API_KEY, "277455b8532b51d0dd24b2446e50a0ad")
                    .build();

            URL url = new URL(uri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            postersJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }

        return getImagesFromJsonString(postersJsonStr);
    }
}
