package jp.ac.it_college.std.reachable_client;

import android.app.ListFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends ListFragment {

    private List<Bitmap> items = new ArrayList<>();
    private static final String DIRECTORY_PATH = Environment.getExternalStorageDirectory()
            + "/ReachaBLE/";
    private String[] list = new File(DIRECTORY_PATH).list();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setListAdapter(new S3DownloadsListAdapter(getActivity(), R.layout.row_s3_downloads, items));
        setDownLoads();
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    private void setDownLoads() {
        items.clear();

        for (String name : list) {
            Bitmap bitmap = BitmapFactory.decodeFile(DIRECTORY_PATH + name);
            items.add(bitmap);
        }

        ((S3DownloadsListAdapter) getListAdapter()).notifyDataSetChanged();
    }
/*
    private TextView mLabelAccessKey;
    private TextView mLabelSecretKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);
        findViews(view);
        return view;
    }

    private void findViews(View view) {
        mLabelAccessKey = (TextView) view.findViewById(R.id.lbl_access_key);
        mLabelSecretKey = (TextView) view.findViewById(R.id.lbl_secret_key);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.showCredentials:
                showCredentials();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCredentials() {
        AWSCredentials credentials = ((MainActivity) getActivity()).getClientManager().getCredentials();
        mLabelAccessKey.setText(String.format("%s\n%s", getString(R.string.lbl_access_key), credentials.getAWSAccessKeyId()));
        mLabelSecretKey.setText(String.format("%s\n%s", getString(R.string.lbl_secret_key), credentials.getAWSSecretKey()));
    }
*/
}
