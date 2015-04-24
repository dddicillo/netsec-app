package com.derekdicillo.fileserver.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.derekdicillo.fileserver.FileAPI;
import com.derekdicillo.fileserver.R;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
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
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;


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
        try {
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", new MySSLSocketFactory(), 443));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
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

    @Override
    protected void onPostExecute(JSONObject response) {
        if (response != null) {
            Log.d(TAG, response.toString());
            Toast.makeText(mCtx, R.string.upload_success, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mCtx, R.string.upload_fail, Toast.LENGTH_LONG).show();
        }
    }

    private class MySSLSocketFactory extends SSLSocketFactory {
        protected SSLContext sslContext = SSLContext.getInstance("SSL");

        public MySSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(null, null, null, null, null, null);
            sslContext.init(null, new TrustManager[]{new CustomTrustManager(mCtx)}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }

    }
}
