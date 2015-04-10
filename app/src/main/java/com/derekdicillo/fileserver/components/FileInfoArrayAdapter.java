package com.derekdicillo.fileserver.components;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.derekdicillo.fileserver.R;

import java.util.List;

/**
 * Array adapter for file info list
 * <p/>
 * Created by dddicillo on 4/8/15.
 */
public class FileInfoArrayAdapter extends ArrayAdapter<FileInfo> {

    private final LayoutInflater mInflater;

    public FileInfoArrayAdapter(Context context) {
        super(context, R.layout.single_item);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<FileInfo> data) {
        clear();
        if (data != null) {
            for (FileInfo appEntry : data) {
                add(appEntry);
            }
        }
    }

    /**
     * Populate new items in the list.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.single_item, parent, false);
        } else {
            view = convertView;
        }

        FileInfo item = getItem(position);
        ((TextView) view.findViewById(R.id.file_id)).setText("Placeholder");//item.getId().toString());
        ((TextView) view.findViewById(R.id.file_name)).setText(item.getFileName());

        return view;
    }
}
