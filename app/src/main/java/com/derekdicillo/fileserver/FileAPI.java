package com.derekdicillo.fileserver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.derekdicillo.fileserver.components.CustomTrustManager;
import com.derekdicillo.fileserver.components.DownloadAsyncTask;
import com.derekdicillo.fileserver.components.UploadAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * Created by dddicillo on 4/8/15.
 */
public class FileAPI {

    // SharedPreferences Keys
    public static final String USER_ID = "user_id";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String SECRET_KEY = "secret_key";

    // JSON API Keys
    public static final String ACCESS_TOKEN_JSON = "id";
    public static final String USER_ID_JSON = "userId";

    public static final String BASE_URL = "https://netsec.techexplored.io:4000/api/";
    private static final String TAG = "FileAPI";
    private static FileAPI mInstance;
    private RequestQueue mRequestQueue;
    private SharedPreferences mPrefs;

    private FileAPI(Context context) {

        if (mRequestQueue == null) {
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                TrustManager[] tm = new TrustManager[]{new CustomTrustManager(context)};
                sc.init(null, tm, new SecureRandom());

                HurlStack stack = new HurlStack(null, sc.getSocketFactory());
                mRequestQueue = Volley.newRequestQueue(context.getApplicationContext(), stack);

            } catch (GeneralSecurityException e) {
                Log.e(TAG, "Error creating Volley RequestQueue", e);
            }

        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
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
            Intent login = new Intent(context, LoginActivity.class);
            context.startActivity(login);
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

    /**
     * Create new user with the given credentials
     *
     * @param email         the user's email address
     * @param password      the user's password
     * @param listener      callback on success
     * @param errorListener callback on error
     */
    public void signup(String email, String password, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        executeObject("mUsers", Request.Method.POST, params, listener, errorListener);
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
        executeObject("mUsers/login", Request.Method.POST, params, listener, errorListener);
    }

    /**
     * Clears memory of current user locally and on the server-side
     *
     * @param listener      callback on success
     * @param errorListener callback on error
     */
    public void logout(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        executeObject("mUsers/logout", Request.Method.POST, null, listener, errorListener);
        //Remove userId and accessToken from SharedPreferences
        mPrefs.edit().clear().apply();
    }

    /**
     * List of names and ids of files available on server
     *
     * @param listener      callback on success
     * @param errorListener callback on error
     */
    public void fileIndex(Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String endpoint = "Containers/" + mPrefs.getInt(USER_ID, 0) + "/files";
        executeArray(endpoint, Request.Method.GET, null, listener, errorListener);
    }

    /**
     * Download file with specified id
     *
     * @param fileName name of file to be downloaded
     */
    public void fileDownload(String fileName, Activity context) {
        new DownloadAsyncTask(context).execute(fileName);
    }

    public void fileUpload(String filePath, Activity context) {
        new UploadAsyncTask(context).execute(filePath);
    }

    public void fileDelete(String fileName, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String endpoint = String.format("Containers/%d/files/%s", mPrefs.getInt(USER_ID, 0), fileName);
        executeObject(endpoint, Request.Method.DELETE, null, listener, errorListener);
    }

    private void executeObject(
            String endpoint,
            int method,
            Map<String, String> params,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        JSONObject parameters = null;
        if (params != null) {
            parameters = new JSONObject(params);
        }

        String url = BASE_URL + endpoint + "?access_token=" + mPrefs.getString(ACCESS_TOKEN, "");
        JsonObjectRequest request = new JsonObjectRequest(method, url, parameters, listener, errorListener);
        mRequestQueue.add(request);
        Log.d(TAG, "Request to: " + url);
        if (parameters != null) {
            Log.d(TAG, "Params: " + parameters.toString());
        }
    }

    private void executeArray(
            String endpoint,
            int method,
            Map<String, String> params,
            Response.Listener<JSONArray> listener,
            Response.ErrorListener errorListener) {

        JSONObject parameters = null;
        if (params != null) {
            parameters = new JSONObject(params);
        }

        String url = BASE_URL + endpoint + "?access_token=" + mPrefs.getString(ACCESS_TOKEN, "");
        JsonArrayRequest request = new JsonArrayRequest(method, url, parameters, listener, errorListener);
        mRequestQueue.add(request);
        Log.d(TAG, "Request to: " + url);
        if (parameters != null) {
            Log.d(TAG, "Params: " + parameters.toString());
        }
    }
}
