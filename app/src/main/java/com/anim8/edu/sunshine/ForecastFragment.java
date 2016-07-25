package com.anim8.edu.sunshine;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import java.util.Date;
import java.util.GregorianCalendar;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

/**
 * A placeholder fragment containing a simple view.
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout
 */
public class ForecastFragment extends Fragment {

    //Define string ArrayAdapter variable globally so can access in FetchWeatherTask AsyncTask
    private ArrayAdapter<String> mForecastAdapter;
    //constructor
    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Must override onCreate to tell fragment it has OptionsMenu
        //Add this line in order for this fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();

    }

    @Override //Then we override onCreateOptions menu to inflate our forecastfragment XML
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater); //not sure this is required
        inflater.inflate(R.menu.forecastfragment, menu);  //we created forecastfragment
    }

    @Override //we want to be notified when an item is selected so override:
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle action bar item clicks here.  The action bar will
        //automatically handle clicks on the Home/Up button, so long
        //as you specify a parent activity in the AndroidManifest.xml (me: How? )
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        //following Refresh button action is for debug only (as is this whole 'Refresh button' itself!)
        //new FetchWeatherTask().execute();//create a new Async task (FetchWeatherTask() here)
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        //Udacity course asks to code for zip, but OpenWeatherMap apparently no longer supports
        //note: following code really should not block UI but can't be in AsyncTask (weather)?
        //check to see if internet permission is available.  Not sure where it should go
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            makeText(getActivity(), "Internet permission required for refresh", LENGTH_LONG).show();
            //return true; //we've handled this event (by giving an error message) - see alt below

        } else { //Here's where the FetchWeatherTask AsyncTask is actually called if has internet permission
            //create a handle to defaultSharedPreference of our choice
            //from: https://developer.android.com/training/basics/data-storage/shared-preferences.html
//                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
//                String defaultValue = getResources().getString(R.string.pref_location_default);
//                String location = sharedPref.getInt(getString(R.string.pref_location_key), defaultValue);
//From: http://stackoverflow.com/questions/9587810/why-does-preferences-getstringkey-default-always-return-default
            //& Udacity https://classroom.udacity.com/courses/ud853/lessons/1474559101/concepts/15761486050923
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            //this line is particularly opaque!!
            String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
            //weatherTask.execute("Mountain, US"); //old hard code city name & country abbreviation
            weatherTask.execute(location+", US"); //new prefs dependent code city name & hard code country abbreviation

            makeText(getActivity(), "Refresh button selected", LENGTH_LONG).show();
            //return true; //we've handled this event (as intended)
        }
        //return true; //ensures menu selected is always consumed, even if error message above

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mForecastAdapter = new ArrayAdapter<String>(
                //parameters
                getActivity(), //current context (current activity)
                R.layout.list_item_forecast, //name of the layout ID of list item layout XML file
                R.id.list_item_forecast_textview, //ID of text view element to populate
                //weekForecast); //Array List of data
                new ArrayList<String>());//no date needed here because will get
                // at onStart with updateWeather() refactored code

        //create fragment view (my fragmentView == rootView in Udacity code so refactored to rootView)
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        //get reference to list view & populate it by attaching this adapter to it
        //rootView prefix limits search scope
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, //AdapterView where the click happened (here parent of ListView)
                                    View v, //view within the AdapterView that was clicked (provided by adapter)
                                    int position, //position of the view in the adapter
                                    long l) { //row id of the itme that was clicked
                String forecast = mForecastAdapter.getItem(position); //get text from ArrayList item clicked
                //create a Toast with forecast string
                //also works
                //String forecast = adapterView.getItemAtPosition(position).toString();
                //Toast.makeText(getActivity(), forecast, LENGTH_LONG).show();


                //replace Toast with Explicit Intent to DetailActivity
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra(Intent.EXTRA_TEXT, forecast);  //EXTRA_TEXT can be anything
                startActivity(detailIntent);
            }
        });



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

        //ToDo: Refactor SimpleDateFormat code here

        /**
         * Prepare the weather high/lows for presentation.
         */
        //basically a String formatter for hi/lo temperatures
        private String formatHighLows(double high, double low) {
            //Data is fetched in Celsius by default
            //If user prefers to see in Fahrenheit, convert the values here
            //we do this rather than fetchin in Fahrenhit so that the user can
            //change this option without us having to re-fetch the data once
            //we start storing the values in a database

            SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharedPrefs.getString(
                    getString(R.string.pref_units_key), //shared pref unit key's value if present
                    getString(R.string.pref_units_metric)); //default value

            if (unitType.equals(getString(R.string.pref_units_imperial))){
                high = (high * 1.8) + 32; //C = (F-32)/1.8
                low = (low * 1.8) + 32;
            }else if (!unitType.equals(getString(R.string.pref_units_metric))){
                Log.d(LOG_TAG, "Unit type not found: " + unitType);//if neither imperial nor metric
            }


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
        //basically a JSON formatting utility
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

                //TODO correct units:


                highAndLow = formatHighLows(high, low); //our util function above
                resultStrs[i] = day + " - " + description + " - " + highAndLow;

                //add one to date for next iteration
                gc.add(GregorianCalendar.DATE, 1);
            } //closing bracket for weatherData array iteration

            //this is the result we are really looking for from getWeatherDataFromJson
            return resultStrs;  //getWeatherDataFromJson returns string[] (array)
        } // closing bracket for getWeatherDataFromJson utility method

        //this is the actual FetchWeatherTask AsyncTask code(?)
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
                    makeText(getActivity(), "Internet permission required", LENGTH_LONG).show();
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
                //this is the result string from internet
                forecastJsonStr = buffer.toString();

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

            //call getWeatherDataFromJson
            try { //this is where we actually return the 'result' which are
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;

        } //closing bracket for doInBackground()

        @Override
        protected void onPostExecute(String[] result) {  //result is from
            //string[] array from getWeatherDataFromJson utility method (??)
            if (result != null) {
                mForecastAdapter.clear();
                mForecastAdapter.addAll(result); //for API 11 (Honeycomb) or higher
                //following is alternative to addAll() but less efficient redraw
                /*for (String dayForecastStr : result) {
                    mForecastAdapter.add(dayForecastStr);
                }//end for loop
*/
                //New data is back from the surver. Hooray!
            }//end if
        }
    } //closing bracket for FetchWeatherTask
} //closing bracket for ForecastFragment
