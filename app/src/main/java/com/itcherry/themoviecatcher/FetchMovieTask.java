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
import java.util.HashMap;

public class FetchMovieTask extends AsyncTask<String, Void, HashMap<Integer,String>> {
    private final String LOG_TAG = getClass().getSimpleName();

    private HashMap<Integer,String> getImagesFromJsonString(String postersJsonStr){

        final String RESULTS_ARRAY = "results";
        final String POSTER_PATH = "poster_path";

        HashMap<Integer,String> result = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(postersJsonStr);
            JSONArray results = jsonObject.getJSONArray(RESULTS_ARRAY);
            JSONObject posterPath;
            String  posterImages;
            int posterIds;
            for (int i = 0; i <results.length(); i++) {
                posterPath = results.getJSONObject(i);
                posterImages = posterPath.getString(POSTER_PATH);
                posterIds = posterPath.getInt("id");
                result.put(posterIds,posterImages);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG,e.getMessage());
        }
        return result;
    }
    @Override
    protected HashMap<Integer,String> doInBackground(String... params) {
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
        return null;
    }
}
