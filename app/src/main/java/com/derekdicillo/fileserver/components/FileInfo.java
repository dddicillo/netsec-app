package com.derekdicillo.fileserver.components;

import android.util.Log;

import com.derekdicillo.fileserver.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model class for the file info received from the server
 *
 * Created by dddicillo on 4/8/15.
 */
public class FileInfo {

    private static final String TAG = "FileInfo";

    private static final String FILE_ID = "id";
    private static final String FILE_NAME = "path";

    private Long _id;
    private String _fileName;

    public FileInfo(JSONObject fileDetails) {
        try {
//            _id = fileDetails.getLong(FILE_ID);
            _fileName = fileDetails.getString(FILE_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Invalid JSON");
        }
    }

    private FileInfo(String fileName) {
        _id = null;
        _fileName = fileName;
    }

    // Used to display placeholder if no files exist on server
    public static FileInfo emptyFileList(String fileName) {
        return new FileInfo(fileName);
    }

    public Long getId() {
        return _id;
    }

    public String getFileName() {
        return _fileName;
    }
}
