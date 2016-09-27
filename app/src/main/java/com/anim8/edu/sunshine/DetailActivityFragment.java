package com.anim8.edu.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//import android.widget.ShareActionProvider;

/**
 * A placeholder fragment containing a simple view.
 * Udacity put this class definition at bottom DetailActivity.java
 */
public class DetailActivityFragment extends Fragment {
    //TODO oddly, units don't refresh properly from detail view (not sure about location)

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private String mForecastStr; //member variable (so can reuse?)

    public DetailActivityFragment() {
        //set flag that tells Android fragment that a menu option available
        //otherwise won't call the onCreateOptionsMenu() function
        setHasOptionsMenu(true); //menu option available

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
            //String forecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView) detailView.findViewById(R.id.detail_text)).setText(mForecastStr);
        }
        return detailView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Inflate the menu, this adds items to the action bar if it is present
        //had to create this menu item
        inflater.inflate(R.menu.detailfragment, menu);

        //Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        //Get the provider and hold onto it to set/change the share intent
        //NOTE:  Had to manually import android.support.v7.widget.ShareActionProvider for this line
        //will update this whenever the data we wish to share changes
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        //Attach an intent to this ShareActionProvider.  You will update this at any time
        //the data you want to share changes,
        // like when the user selects a new piece of data they might like to share
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else{
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    private Intent createShareForecastIntent() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        //shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); //deprecated vs. NEW DOCUMENT

        //Following line prevents Android from adding the activity we are sharing with to the
        //activity stack.
        // Allows you to return to current activity, not the shared activity
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");  //letting Android know we're sharing text
        shareIntent.putExtra(Intent.EXTRA_TEXT,mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }
}
