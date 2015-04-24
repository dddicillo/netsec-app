package com.derekdicillo.fileserver.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.derekdicillo.fileserver.FileAPI;
import com.derekdicillo.fileserver.R;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * Created by dddicillo on 4/23/15.
 */
public class DownloadAsyncTask extends AsyncTask<String, Void, JSONObject> {

    private static final String TAG = "DownloadAsyncTask";

    private Context mCtx;
    private SharedPreferences mPrefs;

    public DownloadAsyncTask(Context context) {
        mCtx = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mCtx);

    }

    @Override
    protected JSONObject doInBackground(String... params) {

        try {
            String fileName = params[0];
            URL url = new URL(FileAPI.BASE_URL + "Containers/" + mPrefs.getInt(FileAPI.USER_ID, 0) + "/download/" + fileName + "?access_token=" + mPrefs.getString(FileAPI.ACCESS_TOKEN, ""));

            SSLContext sc = SSLContext.getInstance("SSL");
            TrustManager[] tm = new TrustManager[]{new CustomTrustManager(mCtx)};
            sc.init(null, tm, new SecureRandom());

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sc.getSocketFactory());

            InputStream in = urlConnection.getInputStream();
            FileOutputStream out = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName));

            byte[] buffer = new byte[1024];
            int read;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            out.flush();

            out.close();
            in.close();

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            Log.e(TAG, "Error creating HttpsURLConnection");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "Invalid download URL");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO: Return response data from server
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        // TODO: Check to make sure download was successful (make error toast otherwise)
        Toast.makeText(mCtx, R.string.download_success, Toast.LENGTH_LONG).show();
    }
}
