package jp.ac.it_college.std.reachable_client.aws;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.List;

import jp.ac.it_college.std.reachable_client.R;

public class S3ObjectsListAdapter extends ArrayAdapter<S3ObjectSummary> {

    private List<S3ObjectSummary> mObjects;
    private int mResource;
    private Context mContext;

    public S3ObjectsListAdapter(Context context, int resource, List<S3ObjectSummary> objects) {
        super(context, resource, objects);
        this.mObjects = objects;
        this.mResource = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(mResource, null);
        }

        S3ObjectSummary summary = mObjects.get(position);

        if (summary != null) {
            TextView key = (TextView) view.findViewById(R.id.lbl_key);
            TextView size = (TextView) view.findViewById(R.id.lbl_size);

            if (key != null) {
                key.setText(mContext.getString(R.string.lbl_key) + summary.getKey());
            }

            if (size != null) {
                String sizeLine = String.format("%s %d %s",
                        mContext.getString(R.string.lbl_size), summary.getSize(), mContext.getString(R.string.lbl_byte));
                size.setText(sizeLine);
            }
        }
        return view;
    }


}