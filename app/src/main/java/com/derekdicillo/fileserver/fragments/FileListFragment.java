package com.derekdicillo.fileserver.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.derekdicillo.fileserver.FileAPI;
import com.derekdicillo.fileserver.R;
import com.derekdicillo.fileserver.components.FileInfo;
import com.derekdicillo.fileserver.components.FileInfoArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 */
public class FileListFragment extends Fragment {

    private static final String TAG = "FileListFragment";

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private FileInfoArrayAdapter mAdapter;

    /**
     * The list of files used to populate the adapter
     */
    private List<FileInfo> mFiles;

    /**
     * Functions for interacting with the web server
     */
    private FileAPI mAPI;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FileListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the API
        mAPI = FileAPI.getInstance(getActivity().getApplicationContext());

        // Initialize empty array adapter
        mAdapter = new FileInfoArrayAdapter(getActivity());

    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFileList();
    }

    public void refreshFileList() {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();

        mAPI.fileIndex(
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Set placeholder if no files exist
                            if (response.length() == 0) {
                                mAdapter.add(FileInfo.emptyFileList(getString(R.string.empty_file_list)));
                            }

                            // Add files to list
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject rawFile = response.getJSONObject(i);
                                Log.d(TAG, rawFile.toString());
                                mAdapter.add(new FileInfo(rawFile));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Invalid JSON");
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        FileAPI.handleNetworkError(getActivity(), error);
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filelist, container, false);

        // Set the adapter (initially empty)
        mFiles = new ArrayList<>();
        mAdapter.setData(mFiles);
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        setEmptyText(getString(R.string.loading_data));

        // Add context menu so we can be notified on item clicks
        registerForContextMenu(mListView);

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_file_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String fileName = ((TextView) info.targetView.findViewById(R.id.file_name)).getText().toString();

        switch (item.getItemId()) {
            case R.id.action_download:
                mAPI.fileDownload(fileName);
                return true;
            case R.id.action_delete:
                mAPI.fileDelete(fileName, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getActivity(), R.string.delete_success, Toast.LENGTH_LONG).show();
                        refreshFileList();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        FileAPI.handleNetworkError(getActivity(), error);
                    }
                });
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }
}
