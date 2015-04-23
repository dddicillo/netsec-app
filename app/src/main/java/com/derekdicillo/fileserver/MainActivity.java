package com.derekdicillo.fileserver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.derekdicillo.fileserver.fragments.FileListFragment;
import com.ipaulpro.afilechooser.utils.FileUtils;

import org.json.JSONObject;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CHOOSER = 1324;


    private Context mCtx;
    private SharedPreferences mPrefs;
    private FileAPI mAPI;
    private BroadcastReceiver mReceiver;

    /*
     * Reference to FileListFragment for reloading results
     */
    private FileListFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCtx = this;

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

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Add decryption and move file to 'Downloads' directory
                Toast.makeText(mCtx, R.string.download_success, Toast.LENGTH_LONG).show();
            }
        };
    }

    @Override
    protected void onResume() {
        registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
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
                mFragment.refreshFileList();
                return true;
            case R.id.action_upload:
                Intent chooserIntent = FileUtils.createGetContentIntent();
                Intent intent = Intent.createChooser(chooserIntent, "Select a file");
                startActivityForResult(intent, REQUEST_CHOOSER);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSER:
                if (resultCode == RESULT_OK) {

                    final Uri uri = data.getData();

                    // Get the File path from the Uri
                    String path = FileUtils.getPath(this, uri);

                    // Alternatively, use FileUtils.getFile(Context, Uri)
                    if (path != null && FileUtils.isLocal(path)) {
                        mAPI.fileUpload(path, this, mFragment);
                    }
                }
                break;
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
}
