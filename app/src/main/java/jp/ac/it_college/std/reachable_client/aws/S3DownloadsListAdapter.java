package jp.ac.it_college.std.reachable_client.aws;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.ac.it_college.std.reachable_client.R;

public class S3DownloadsListAdapter extends ArrayAdapter<Bitmap> {
    private List<Bitmap> items = new ArrayList<>();
    private int resource;
    private Context context;

    public S3DownloadsListAdapter(Context context, int resource, List<Bitmap> objects) {
        super(context, resource, objects);
        this.items = objects;
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resource, null);
        }

        Bitmap bitmap = items.get(position);

        if (bitmap != null) {
            ImageView imageView = (ImageView) view.findViewById(R.id.image_view_downloads);
            TextView fileName = (TextView) view.findViewById(R.id.lbl_file_name);

            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }

            if (fileName != null) {
                fileName.setText(bitmap.getConfig().name());
            }
        }

        return view;
    }
}