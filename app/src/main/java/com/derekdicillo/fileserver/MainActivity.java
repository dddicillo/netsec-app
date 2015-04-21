package com.derekdicillo.fileserver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.derekdicillo.fileserver.fragments.FileListFragment;


public class MainActivity extends ActionBarActivity implements FileListFragment.OnFragmentInteractionListener{

    private static final String TAG = "MainActivity";

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launch LoginActivity if not authenticated
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Log.e(TAG, Integer.toString(mPrefs.getInt(FileAPI.USER_ID, 0)));

        if (!mPrefs.contains(FileAPI.USER_ID)) {
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
            finish();
        }

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new FileListFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(int id) {

    }
}
