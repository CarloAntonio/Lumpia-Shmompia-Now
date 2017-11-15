package com.riskitbiskit.lumpiashmompianow.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.riskitbiskit.lumpiashmompianow.R;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract.MenuEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.riskitbiskit.lumpiashmompianow.activities.MenuActivity.CHECKOUT_LIST;
import static com.riskitbiskit.lumpiashmompianow.activities.MenuActivity.EMPTY;
import static com.riskitbiskit.lumpiashmompianow.data.MenuContract.MenuEntry.CONTENT_URI;

public class AboutActivity extends AppCompatActivity {
    //Constants
    public static final String LOG_TAG = AboutActivity.class.getSimpleName();

    //Variables
    private String[] mMenuTitles;
    private ActionBarDrawerToggle mDrawerToggle;
    private SharedPreferences sharedPreferences;

    //Views
    @BindView(R.id.about_toolbar)
    Toolbar aboutToolbar;

    @BindView(R.id.about_drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.about_left_drawer)
    ListView mDrawerList;

    @BindView(R.id.to_map_bt)
    Button directionsButton;

    @BindView(R.id.current_weather)
    TextView weatherTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        //Kick-off asynctask
        WeatherAsyncTask task = new WeatherAsyncTask();
        task.execute();

        //Setup custom toolbar
        setSupportActionBar(aboutToolbar);

        //Get reference of Shared Preference
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Setup Hamburger Menu
        mMenuTitles = getResources().getStringArray(R.array.menu_options);

        mDrawerLayout.setScrimColor(ContextCompat.getColor(this, android.R.color.transparent));

        mDrawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mMenuTitles));
        mDrawerList.setOnItemClickListener(new AboutActivity.AdapterClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);

        //Performs drawer <-> back button animation
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDrawerToggle.syncState();

        //Setup directions button
        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.map_uri)));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //if any items in the menu is clicked...return true
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class AdapterClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            switch (position) {
                case 0:
                    Intent reorderIntent = new Intent(getBaseContext(), OrderActivity.class);
                    startActivity(reorderIntent);
                    return;
                case 1:
                    Intent menuIntent = new Intent(getBaseContext(), MenuActivity.class);
                    startActivity(menuIntent);
                    finish();
                    return;
                case 2:
                    if (sharedPreferences.getString(CHECKOUT_LIST, EMPTY).contentEquals(EMPTY)) {
                        Toast.makeText(getBaseContext(), R.string.nothing_in_cart, Toast.LENGTH_SHORT).show();
                    } else {
                        Intent checkoutIntent = new Intent(getBaseContext(), OrderActivity.class);
                        startActivity(checkoutIntent);
                    }
                    return;
                case 3:
                    //Delete all items in Shared Preferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(CHECKOUT_LIST, EMPTY);
                    editor.apply();

                    //Reset all item totals in database
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MenuEntry.COLUMN_ITEM_COUNT, getString(R.string.one));
                    getContentResolver().update(CONTENT_URI, contentValues, null, null);

                    //Restart activity
                    Intent clearCartIntent = new Intent(getBaseContext(), MenuActivity.class);
                    startActivity(clearCartIntent);
                    finish();
                    return;
                case 4:
                    Intent aboutIntent = new Intent(getBaseContext(), AboutActivity.class);
                    startActivity(aboutIntent);
                    finish();
                    return;
                default:
                    return;
            }
        }
    }

    private class WeatherAsyncTask extends AsyncTask<URL, Void, String> {

        double latitude = 37.906795;
        double longitude = -122.062847;

        String apiKey = "890a164a7350a9b3c6ec7349c4e3dcb4";
        String forecastUrl = "https://api.darksky.net/forecast/" + apiKey + "/"
                + latitude + "," + longitude;

        @Override
        protected String doInBackground(URL... urls) {
            URL url = createUrl(forecastUrl);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, getString(R.string.unable_close_input_stream), e);
            }

            String weather = extractFeatureFromJson(jsonResponse);

            return weather;
        }

        @Override
        protected void onPostExecute(String weather) {
            weatherTV.setText(weather);
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, getString(R.string.URL_creation_error), exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            //IF the URL is null, then return early
            if (url == null) {
                return jsonResponse;
            }


            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                //if the request was successful (response code 200),
                //then read the input stream and parse the response
                if (urlConnection.getResponseCode() == 200 ){
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, getString(R.string.error_response_code)+ urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, getString(R.string.error_retrieving_json_results), e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Convert the InputStream into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        private String extractFeatureFromJson(String weatherJSON) {
            //If the JSON string is empty or null, then return early
            if (TextUtils.isEmpty(weatherJSON)) {
                return null;
            }

            try {
                //highest level JSONObject "forecast" created
                JSONObject forecast = new JSONObject(weatherJSON);
                //using the key "timezone" to get value from JSON in forcast.io
                String timezone = forecast.getString("timezone");
                Log.i(LOG_TAG, getString(R.string.from_json) + timezone);

                //create new JSON object, goes down one object level from forecast
                JSONObject currently = forecast.getJSONObject("currently");

                return currently.getString("summary");

            } catch (JSONException e) {
                Log.e(LOG_TAG, getString(R.string.problem_parsing_json), e);
            }
            return null;
        }


    }
}
