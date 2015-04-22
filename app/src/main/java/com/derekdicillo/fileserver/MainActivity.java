package com.derekdicillo.fileserver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.derekdicillo.fileserver.fragments.FileListFragment;

import org.json.JSONObject;


public class MainActivity extends ActionBarActivity implements FileListFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";

    private SharedPreferences mPrefs;
    private FileAPI mAPI;

    /*
     * Reference to FileListFragment for reloading results
     */
    private FileListFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launch LoginActivity if not authenticated
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!mPrefs.contains(FileAPI.USER_ID)) {
            launchLoginActivity();
        }

        // Run Diffie Hellman Key Exchange if no secret key exists
        mAPI = FileAPI.getInstance(getApplicationContext());
        if (!mPrefs.contains(FileAPI.SECRET_KEY)) {
            mAPI.dhKeyExchange(this);
        }

        setContentView(R.layout.activity_main);
        mFragment = new FileListFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mFragment)
                    .commit();
        }

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG, intent.getData().toString());
            }
        }, IntentFilter.create(DownloadManager.ACTION_DOWNLOAD_COMPLETE, "*/*"));
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
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_reload:
                // TODO: Return this to normal when done testing
                //mFragment.refreshFileList();
                mAPI.fileDownload("activities_controller.rb");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        mAPI.logout(new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            launchLoginActivity();
                        }
                    },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, new String(error.networkResponse.data));
                        if (error.networkResponse.statusCode == 500) {
                            Log.e(TAG, "Error in response. Seems to work anyway. Bug?");
                        }
                        launchLoginActivity();
                    }
                });
    }

    private void launchLoginActivity() {
        Intent login = new Intent(this, LoginActivity.class);
        startActivity(login);
        finish();
    }

    @Override
    public void onFragmentInteraction(String fileName) {

    }
}
