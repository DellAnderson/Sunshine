package com.anim8.edu.sunshine;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    //Define string ArrayAdapter variable
    ArrayAdapter<String> mForecastAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //NOTE:  Udacity placed this in "MainActivity extends ActionBarActivity" - I guessed it goes here

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

        //create an adapter and assign to ArrayAdapter variable
        mForecastAdapter = new ArrayAdapter<String>(
                //parameters
                getActivity(), //context (current activity)
                R.layout.list_item_forecast, //ID of list item layout XML file
                R.id.list_item_forecast_textview, //ID of text view element
                weekForecast); //Array List of data

        //create fragment view
        View fragmentView = inflater.inflate(R.layout.fragment_main,container, false);
        //find list view & populate it:  fragmentView prefix limits search scope
        ListView listView = (ListView) fragmentView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        return fragmentView;
    }
}
