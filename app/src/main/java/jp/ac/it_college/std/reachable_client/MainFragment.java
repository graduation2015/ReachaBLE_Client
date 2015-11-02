package jp.ac.it_college.std.reachable_client;

import android.app.ListFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends ListFragment {

    private List<Bitmap> items = new ArrayList<>();
    private static final String DIRECTORY_PATH = Environment.getExternalStorageDirectory()
            + "/ReachaBLE/";
    private static final String IMAGE_PATH = DIRECTORY_PATH + "images/";
    private static final String JSON_PATH = DIRECTORY_PATH + "jsons/";
    private static final String TAGS_PATH  = DIRECTORY_PATH + "tags/";
    private String[] list = new File(IMAGE_PATH).list();

    private Button startButton;
    private Button stopButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setListAdapter(new S3DownloadsListAdapter(getActivity(), R.layout.row_s3_downloads, items));
        setDownLoads();

        View contentView = inflater.inflate(R.layout.fragment_main, container, false);
        startButton = (Button) contentView.findViewById(R.id.start_btn);
        stopButton = (Button) contentView.findViewById(R.id.stop_btn);
        startButton.setOnClickListener(new ServiceOnClickListener());
        stopButton.setOnClickListener(new ServiceOnClickListener());

        return contentView;
    }

    private void setDownLoads() {
        items.clear();

        for (String name : list) {
            Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_PATH + name);
            items.add(bitmap);
        }

        ((S3DownloadsListAdapter) getListAdapter()).notifyDataSetChanged();
    }
    class ServiceOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //Service開始、終了
            if (v == startButton) {
                getActivity().startService(new Intent(getActivity(), DownloadService.class));
            } else if (v == stopButton) {
                getActivity().stopService(new Intent(getActivity(), DownloadService.class));
            }
        }
    }
}