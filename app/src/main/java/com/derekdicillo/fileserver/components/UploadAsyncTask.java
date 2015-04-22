package com.derekdicillo.fileserver.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.derekdicillo.fileserver.FileAPI;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


/**
 * Created by dddicillo on 4/22/15.
 */
public class UploadAsyncTask extends AsyncTask<String, Void, JSONObject> {

    private static final String TAG = "UploadAsyncTask";
    private static final String UPLOADED_FILES = "uploadedFiles";

    Context mCtx;
    SharedPreferences mPrefs;
    MultipartEntityBuilder mBuilder;

    public UploadAsyncTask(Context context) {
        mCtx = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
        mBuilder = MultipartEntityBuilder.create();
        mBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        String uri = FileAPI.BASE_URL + "/Containers/" + mPrefs.getInt(FileAPI.USER_ID, 0) + "/upload?access_token=" + mPrefs.getString(FileAPI.ACCESS_TOKEN, "");
        HttpPost httpPost = new HttpPost(uri);

        for (String fileURI : params) {
            File file = new File(fileURI);
            mBuilder.addBinaryBody(UPLOADED_FILES, file, ContentType.MULTIPART_FORM_DATA, file.getName());
        }

        httpPost.setEntity(mBuilder.build());
        JSONObject responseObj = null;
        try {
            String responseBody = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), "UTF-8");
            responseObj = new JSONObject(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Invalid JSON");
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return responseObj;
    }

}
