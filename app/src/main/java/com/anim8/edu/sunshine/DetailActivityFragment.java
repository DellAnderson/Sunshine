package com.anim8.edu.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 * Udacity put this class definition at bottom DetailActivity.java
 */
public class DetailActivityFragment extends Fragment {
//TODO oddly, units don't refresh properly from detail view (not sure about location)

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Detail activity was called via an intent.  Inspect the intent for forecast data
        //NOTE:  THIS IS VERY IMPORTANT when passing data
        Intent intent = getActivity().getIntent();
        //create a view based on fragment_detail
        View detailView = inflater.inflate(R.layout.fragment_detail, container, false);
        //create textview to display forecast detail //TODO see why breakpoint next line crashes app
        TextView forecast_textView_detail = (TextView) detailView.findViewById(R.id.detail_text);
        //read the intent's forecast data back to display in the text view
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String forecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView) detailView.findViewById(R.id.detail_text)).setText(forecastStr);
        }
        return detailView;
    }

    @Override
    public void onStart() {
        super.onStart();  //TODO make units update
    }
}
