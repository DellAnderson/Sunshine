package com.anim8.edu.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.action_settings:
                //launch settings page
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                //or startActivity(new Intent(this, SettingsActivity.class);
                return true;
            case R.id.action_map:
                openPreferredLocationInMap(); //helper method
                return true;
        }
        return super.onOptionsItemSelected(item); //important line
    }
    //helper method for Location Map
    private void openPreferredLocationInMap() {
        //create implicit intent to view preferred location on map
        //obtain location from Shared Preferences
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        String geoLocation = sharedPrefs.getString(
                getString(R.string.pref_location_key), //shared pref unit key's value if present
                getString(R.string.pref_location_default)); //default value
        //Uri geoLocationUri = Uri.parse("geo:0,0?q=1600+Amphitheatre+Parkway,+Mountain+View,+California");
        Uri geoLocationUri = Uri.parse("geo:0,0?q="+geoLocation+",CA");
        // http://developer.android.com/guide/components/intents-common.html#Maps
            /*Uri geoLocationUri = Uri.parse("geo:0,0?").buildUpon()
                    .appendQueryParameter("q", geoLocation)
                    .build();*/
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        //debugging use only
        makeText(this, geoLocationUri.toString(), LENGTH_LONG).show();

        mapIntent.setData(geoLocationUri);
        //Be sure there is at least one activity available to handle intent
           /* PackageManager packageManager = getActivity().getPackageManager();
            List activities = packageManager.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            boolean isIntentSafe = activities.size() > 0; */
        if (mapIntent.resolveActivity(this.getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            makeText(this, "Must download a map activity to show maps", LENGTH_LONG).show();
        }
    }
}
