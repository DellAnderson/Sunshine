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

    //ListView list;

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

        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));

        // This code was missing. Withotu it, listview blank - found online
        // at http://stackoverflow.com/questions/30045343/listview-not-showing-up-in-my-layout-android-studios
        ArrayAdapter<String> mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);
        ListView listView = (ListView) fragmentView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        return fragmentView;

        //NOTE:  This was original code
//        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}
