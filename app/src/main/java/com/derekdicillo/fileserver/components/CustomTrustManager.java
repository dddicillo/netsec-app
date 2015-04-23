package com.derekdicillo.fileserver.components;

import android.content.Context;
import android.util.Log;

import com.derekdicillo.fileserver.R;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Created by dddicillo on 4/22/15.
 */
public class CustomTrustManager implements X509TrustManager {

    private static final String TAG = "CustomTrustManager";

    private Context mCtx;

    public CustomTrustManager(Context ctx) {
        mCtx = ctx;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        try {
            InputStream certStream = mCtx.getResources().openRawResource(R.raw.netsec_cert);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)cf.generateCertificate(certStream);
            certStream.close();

            return new X509Certificate[] { cert };
        } catch (CertificateException e) {
            Log.e(TAG, "Error on CertificateFactory", e);
        } catch (IOException e) {
            Log.e(TAG, "Error on InputStream", e);
        }

        return new X509Certificate[] {};
    }
}
