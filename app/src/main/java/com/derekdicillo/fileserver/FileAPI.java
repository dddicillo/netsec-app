package com.derekdicillo.fileserver;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dddicillo on 4/8/15.
 */
public class FileAPI {

    public static final String API_PREFS = "api_prefs";
    public static final String USER_ID = "user_id";
    public static final String ACCESS_TOKEN = "access_token";
    private static final String TAG = "FileAPI";
    // TODO Replace with correct base url
    private static final String BASE_URL = "http://192.168.1.25:3000/api/";
    private static FileAPI mInstance;
    private static Context mCtx;
    private RequestQueue mRequestQueue;
    private DownloadManager mDownloadManager;
    private SharedPreferences mPrefs;

    private FileAPI(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
        mDownloadManager = (DownloadManager) mCtx.getSystemService(Context.DOWNLOAD_SERVICE);
        mPrefs = mCtx.getSharedPreferences(API_PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized FileAPI getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FileAPI(context);
        }
        return mInstance;
    }

    public static boolean handleNetworkError(Activity context, VolleyError error) {
        Log.e(TAG, "Volley error in: " + context.getClass().getName(), error);
        if (error.networkResponse == null) {
            Log.e(TAG, "No network response, network is not available.");
            Toast.makeText(context, "Unable to reach network.", Toast.LENGTH_LONG).show();
            return true;
        } else if (error.networkResponse.statusCode == 401) {
            // User is not authorized, we should clear the tokens and force a refresh
            Log.e(TAG, "User is not authorized, clearing session and finishing activity.");
            context.finish();
            return true;
        } else {
            Log.e(TAG, String.format(
                            "Other network error, code: %d response: %s",
                            error.networkResponse.statusCode,
                            new String(error.networkResponse.data))
            );
            return false;
        }
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    private <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    /**
     * Retrieve userId and accessToken for user with specified email and password
     *
     * @param email         the user's email address
     * @param password      the user's password
     * @param listener      callback on success
     * @param errorListener callback on error
     */
    public void login(String email, String password, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        execute("mUsers/login", Request.Method.POST, params, listener, errorListener);
    }

    /**
     * Clears memory of current user locally and on the server-side
     *
     * @param listener      callback on success
     * @param errorListener callback on error
     */
    public void logout(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        execute("mUsers/logout", Request.Method.POST, null, listener, errorListener);
        // TODO Remove userId and accessToken from SharedPreferences
        mPrefs.edit().remove(USER_ID).remove(ACCESS_TOKEN);
    }

    /**
     * List of names and ids of files available on server
     *
     * @param listener      callback on success
     * @param errorListener callback on error
     */
    public void fileIndex(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        execute("files", Request.Method.GET, null, listener, errorListener);
    }

    /**
     * Download file with specified id
     *
     * @param fileName name of file to be downloaded
     */
    public void fileDownload(String fileName) {
        String url = String.format("Containers/%s/download-file/%s", mPrefs.getString(USER_ID, "empty"), fileName);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        mDownloadManager.enqueue(request);
        // TODO Register BroadcastReceiver
    }

    public void fileUpload(String fileName, File file, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {

    }

    private void execute(
            String endpoint,
            int method,
            Map<String, String> params,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        JSONObject parameters = null;
        if (params != null) {
            parameters = new JSONObject(params);
        }

        JsonObjectRequest request = new JsonObjectRequest(method, BASE_URL + endpoint, parameters, listener, errorListener);

        mRequestQueue.add(request);
    }
}
