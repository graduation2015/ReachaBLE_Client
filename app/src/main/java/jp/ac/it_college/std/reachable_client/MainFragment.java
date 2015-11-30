package jp.ac.it_college.std.reachable_client;

import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends ListFragment implements Serializable{

    private List<Bitmap> items = new ArrayList<>();
    public static String IMAGE_PATH;
    public static String JSON_PATH;
//    public static final String TAGS_PATH;
    private String[] list;

    private final int REQUEST_ENABLE_BT = 0x01;

    private Button startButton;
    private Button stopButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mkdir();
        list = new File(IMAGE_PATH).list();
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
            Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_PATH + "/" + name);
            items.add(bitmap);
        }
        ((S3DownloadsListAdapter) getListAdapter()).notifyDataSetChanged();
    }
    class ServiceOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //Service開始、終了
            if (v == startButton) {
                bluetoothSetUp();
                Intent intent = new Intent(getActivity(), DownloadService.class);
                getActivity().startService(intent);
            } else if (v == stopButton) {
                bluetoothDisable();
                getActivity().stopService(new Intent(getActivity(), DownloadService.class));
            }
        }
    }

    private void bluetoothSetUp() {
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();

        if (bt == null) {
            return;
        }

        if (!bt.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void bluetoothDisable() {
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();

        if (bt == null) {
            return;
        }

        if (bt.isEnabled()) {
            bt.disable();
        }
    }

    private void mkdir() {

   /*     File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath());
        file.mkdirs();*/
        IMAGE_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
        JSON_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
        new File(IMAGE_PATH).mkdirs();
        new File(JSON_PATH).mkdirs();

    }
}