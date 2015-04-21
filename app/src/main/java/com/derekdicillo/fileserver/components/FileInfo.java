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

    private static final String CONTAINER = "container";
    private static final String FILE_NAME = "name";
    private static final String SIZE = "size";

    private Long _container;
    private String _fileName;
    private Long _size;


    public FileInfo(JSONObject fileDetails) {
        try {
            _container = fileDetails.getLong(CONTAINER);
            _fileName = fileDetails.getString(FILE_NAME);
            _size = fileDetails.getLong(SIZE);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Invalid JSON");
        }
    }

    private FileInfo(String fileName) {
        _fileName = fileName;
    }

    // Used to display placeholder if no files exist on server
    public static FileInfo emptyFileList(String fileName) {
        return new FileInfo(fileName);
    }

    public Long getContainer() {
        return _container;
    }

    public String getFileName() {
        return _fileName;
    }

    public Long getSize() {
        return _size;
    }
}
