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


public class FetchBackdropsTask extends AsyncTask<String, Void, String[]> {
    private final String LOG_TAG = getClass().getSimpleName();

    private String[] getBackdropsFromJsonString(String postersJsonStr) {

        final String RESULTS_ARRAY = "backdrops";
        final String BACKDROPS_PATH = "file_path";

        String[] result = null;
        try {
            JSONObject jsonObject = new JSONObject(postersJsonStr);
            JSONArray results = jsonObject.getJSONArray(RESULTS_ARRAY);

            JSONObject object;
            result = new String[results.length()];
            for (int i = 0; i < results.length(); i++) {
                object = results.getJSONObject(i);
                result[i] = object.getString(BACKDROPS_PATH);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return result;
    }

    @Override
    protected String[] doInBackground(String... params) {
        if (params[0] == null) return null;
        String backdropsJsonStr = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        final String MOVIE_BACKDROPS_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/images?";
        final String API_KEY = "api_key";

        try {
            Uri uri = Uri.parse(MOVIE_BACKDROPS_URL).buildUpon()
                    .appendQueryParameter(API_KEY, "MyApiKey")
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
            backdropsJsonStr = buffer.toString();

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
        return getBackdropsFromJsonString(backdropsJsonStr);
    }
}

