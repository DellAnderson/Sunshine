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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout
 */
public class ForecastFragment extends Fragment {

    //public static final String TAG = "ForecastFragment";
    //Define string ArrayAdapter variable
    private ArrayAdapter<String> mForecastAdapter;

    //constructor
    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //Must override onCreate to tell fragment it has OptionsMenu
    }

    @Override //Then we override onCreateOptions menu to inflate our forecastfragment XML
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater); //not sure this is required
        inflater.inflate(R.menu.forecastfragment, menu);  //we created forecastfragment
    }

    @Override //we want to be notified when an item is selected so override:
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle action bar item clicks here.  The action bar will
        //automatically handle clicks on the Home/Up button, so long
        //as you specify a parent activity in the AndroidManifest.xml (me: How? )
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            //following Refresh button action is for debug only (as is this whole 'Refresh button' itself!)
            //new FetchWeatherTask().execute();//create a new Async task (FetchWeatherTask() here)
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            //Udacity course asks to code for zip, but OpenWeatherMap apparently no longer supports
            //note: following code really should not block UI but can't be in AsyncTask (weather)?
            //check to see if internet permission is available.  Not sure where it should go
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Internet permission required for refresh", Toast.LENGTH_LONG).show();
                //return true; //we've handled this event (by giving an error message) - see alt below
            } else {
                weatherTask.execute("Mountain, US"); //hard code city name & country abbreviation
                Toast.makeText(getActivity(), "Refresh button selected", Toast.LENGTH_LONG).show();
                return true; //we've handled this event (as intended)
            }

            //return true; //debug temp return value only
            //return super.onOptionsItemSelected(item); //this was in udacity code, not sure why
            return true; //ensures menu selected is always consumed, even if error message above
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
        //Now that we have some dummy forecast data, create an ArrayAdaptor variable
        //& assign it data from a source (like dummy data)
        mForecastAdapter = new ArrayAdapter<String>(
                //parameters
                getActivity(), //current context (current activity)
                R.layout.list_item_forecast, //name of the layout ID of list item layout XML file
                R.id.list_item_forecast_textview, //ID of text view element to populate
                weekForecast); //Array List of data

        //create fragment view (my fragmentView == rootView in Udacity code so refacted to rootView)
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        //get reference to list view & populate it by attaching this adapter to it
        //rootView prefix limits search scope
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        // try to open a network connection (using Github snippet from
        // https://gist.github.com/anonymous/1c04bf2423579e9d2dcd

//CODE FROM https://gist.github.com/udacityandroid/4ee49df1694da9129af9 & Antiterra (same URL)
        // /* The date/time conversion code is going to be moved outside the asynctask later,
        // * so for convenience we're breaking it out into its own method now.
        // */

//         private String getReadableDateString(long time){
//         //private String getReadableDateString(Date time){
//         // Because the API returns a unix timestamp (measured in seconds),
//         // it must be converted to milliseconds in order to be converted to valid date.
//         SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//         return shortenedDateFormat.format(time);
//         }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list"; //'list' is JSON key for list of all day weather results
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";  //'main' is the key for short description

            //create JSON object from the previously returned & input forecastJsonStr String
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            GregorianCalendar gc = new GregorianCalendar();
            Date date = new Date();
            gc.setTime(date); //initialize to current date & time(?)
            //basically we will iterate thru returned results in sync with today's calendar date, inc by 1 each
            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {

                // For now, using the format "Day, description, hi/low"
                String day; // ex:  "Mon 6/23"
                String description; //ex: "Foggy"
                String highAndLow; //ex: "31/17"

                // Get the JSON object representing the ith day in returned weatherArray from OWM_LIST
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                //create a SimpleDateFormat instance with proper formatting for
                //day = "Mon 6/23";
                //SimpleDateFormat sdf = new SimpleDateFormat("EEE M/dd"); //~ dummy data
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d"); // clearer Udacity code
                //create day from gc & sdf (using getTime() to get proper input)
                day = sdf.format(gc.getTime());

                //description is in a child array called "weather" , which is 1 element (0) long
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                //Temperatures are in a child object called "temp".  try not to name variables
                //"temp" when working with temperature.  It confuses everybody!
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low); //our util function above
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
                //Log.v(LOG_TAG, "resultStrs[i].toString(): " + i + " . " + resultStrs[i].toString() );
                //add one to date for next iteration
                gc.add(GregorianCalendar.DATE, 1);
            } //closing bracket for weatherData array iteration

            //verify  resultStrs[] content
            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }

            return resultStrs;
        } // closing bracket for getWeatherDataFromJson

        @Override
        protected String[] doInBackground(String... params) {

            //if no City name, nothing to look up. Verify size of params
            if (params.length == 0) {
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
                    Toast.makeText(getActivity(), "Internet permission required", Toast.LENGTH_LONG).show();
                    return null;
                }
                // Should we show an explanation?
   /*                     if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                Manifest.permission.INTERNET)) {

                            // Show an explanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.
                        */

                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                //Construct the URL from constants above
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";  //apparently this is not the same as units String variable
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";
                //To keep APPID private when uploading project to GitHub,
                //create an ApiKey key/value pair in a file (create if need) called gradle.properties
                //c:/users/<username>/.gradle  with the following (arbitrary name) key = "..." syntax:
                //MyOpenWeatherMapApiKey = " ....yourOWMApiKey#...."  then make sure to tell AS where:
                //Android Studio > settings > Build....> Gradle > Global Gradle settings
                //points to the same folder & tick "offline" (reason for this is fuzzy but works)
                //also be sure project folder includes file build.gradle which must include lines:
                //buildTypes.each {
                //    it.buildConfigField 'String', 'OPEN_WEATHER_MAP_API_KEY', MyOpenWeatherMapApiKey
                //}
                //If it does, you can use OPEN_WEATHER_MAP_API_KEY constant in builtUri below:

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

                Log.v(LOG_TAG, "Forecast string: " + forecastJsonStr);

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

            //call getWeatherDataFraomJson
            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        } //closing bracket for doInBackground()
    } //closing bracket for FetchWeatherTask
} //closing bracket for ForecastFragment
