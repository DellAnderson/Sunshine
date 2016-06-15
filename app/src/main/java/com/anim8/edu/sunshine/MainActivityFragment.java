package com.anim8.edu.sunshine;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

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
//        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));/*
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;

        //NOTE:  This was original code
//        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}
