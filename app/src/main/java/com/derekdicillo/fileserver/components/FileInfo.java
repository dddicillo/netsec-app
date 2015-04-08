package com.derekdicillo.fileserver.components;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dddicillo on 4/8/15.
 */
public class FileInfo {

    private static final String TAG = "FileInfo";

    private static final String FILE_NAME = "name";

    private String _fileName;

    public FileInfo(JSONObject fileDetails) {
        try {
            _fileName = fileDetails.getString(FILE_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Invalid JSON: expected field '" + FILE_NAME + "'");
        }
    }

    public String getFileName() {
        return _fileName;
    }
}
