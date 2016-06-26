package com.anim8.edu.sunshine;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    //public static final String TAG = "ForecastFragment";
    //Define string ArrayAdapter variable
    ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //Must override onCreate to tell fragment it has OptionsMenu
    }

    @Override //Then we override onCreateOptions menu to inflate our forecastfragment XML
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);  //we created forecastfragment
    }

    @Override //we want to be notified when an item is selected so override:
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh){
            //following lines for debug only (as is this whole 'Refresh button' itself!)



            //The first new FeatchWeatherTask() line below doesn't crash for some reason,
            //even without INTERNET PERMISSION
            //but equivalent two line
            //version does (and is the version in Udacity course)

            //new FetchWeatherTask().execute();//create a new Async task (FetchWeatherTask() here)
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            //Udacity course asks to code for zip, but OpenWeatherMap apparently no longer supports

            //note: following code should not block UI but can't be in AsyncTask (weather)?
            //check to see if internet permission is available
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(),"Internet permission required for refresh",Toast.LENGTH_LONG).show();
               } else {
                weatherTask.execute("Mountain, US"); //hard code city name & country abbreviation
                Toast.makeText(getActivity(),"Refresh button selected",Toast.LENGTH_LONG).show();
            }

            return true; //debug temp return value only

        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //create some dummy data for the ListView Here's a sample weekly forecast
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thur 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHER STATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };
        //create string ArrayList from data
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));
        //create ArrayAdapter variable & assign it data from a source (like dummy data)
        mForecastAdapter = new ArrayAdapter<String>(
                //parameters
                getActivity(), //context (current activity)
                R.layout.list_item_forecast, //ID of list item layout XML file
                R.id.list_item_forecast_textview, //ID of text view element
                weekForecast); //Array List of data

        //create fragment view
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        //find list view & populate it:  fragmentView prefix limits search scope
        ListView listView = (ListView) fragmentView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        return fragmentView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        // try to open a network connection (using Github snippet from
        // https://gist.github.com/anonymous/1c04bf2423579e9d2dcd


        @Override
        protected Void doInBackground(String... params) {

            //if no City name, nothing to look up. Verify size of params
            if (params.length == 0){
                return null;
            }


            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            // setup some variables for URL builder
            String format = "json";
            String units = "metric";
            int numDays = 7;


                try {


                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.INTERNET)
                            != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(),"Internet permission required",Toast.LENGTH_LONG).show();
                        return null;
                    }
                        // Should we show an explanation?
   /*                     if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                Manifest.permission.INTERNET)) {

                            // Show an expanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                        */
                    // Construct the URL for the OpenWeatherMap query
                    // Possible parameters are available at OWM's forecast API page, at
                    // http://openweathermap.org/API#forecast
                    //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
                    //Zip doesn't work - gives Lodz PL instead of Mountain view california
                    //String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7";
                    //Instead use City id from http://openweathermap.org/help/city_list.txt
                    // ID = 5375480
                    //String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?id=5375480&mode=json&units=metric&cnt=7";

                    //Original URL worked:
                    /*String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=Mountain,US&mode=json&units=metric&cnt=7";
                    String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
                    URL url = new URL(baseUrl.concat(apiKey));*/

                    //Construct the URL from constants above
                    final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                    final String QUERY_PARAM = "q";
                    final String FORMAT_PARAM = "mode";
                    final String UNITS_PARAM = "units";  //apparently this is not the same as units String variable
                    final String DAYS_PARAM = "cnt";
                    final String APPID_PARAM = "APPID";

                    Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                            .appendQueryParameter(QUERY_PARAM, params[0])
                            .appendQueryParameter(FORMAT_PARAM, format) //format is from earlier variable
                            .appendQueryParameter(UNITS_PARAM, units)  //units is from earlier variable
                            .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                            .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                            .build();

                    URL url = new URL(builtUri.toString());

                    Log.v(LOG_TAG, "Built URI " + builtUri.toString());


                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
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
                    forecastJsonStr = buffer.toString();
                    Log.v(LOG_TAG, forecastJsonStr);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attempting
                    // to parse it.
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }




            return null;
        }

    } //closing bracket for FetchWeatherTask
}
