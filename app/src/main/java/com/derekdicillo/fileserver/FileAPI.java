package com.derekdicillo.fileserver;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.util.Map;

/**
 * Created by dddicillo on 4/8/15.
 */
public class FileAPI {

    private static final String TAG = "FileAPI";

    // TODO Replace with correct base url
    private static final String BASE_URL = "http://192.168.1.25:3000/explorer/resources/";

    private static FileAPI mInstance;
    private static Context mCtx;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private FileAPI(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized FileAPI getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FileAPI(context);
        }
        return mInstance;
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

    public ImageLoader getImageLoader() {
        return mImageLoader;
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
     * @param fileId        id of file to be downloaded
     * @param listener      callback on success
     * @param errorListener callback on error
     */
    public void fileDownload(int fileId, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = String.format("file/%s", fileId);
        execute(url, Request.Method.GET, null, listener, errorListener);
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
}
