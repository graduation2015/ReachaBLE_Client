package jp.ac.it_college.std.reachable_client;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class DownloadService extends Service {
    private BLEScanner_LOLIPOP bleScanner;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private View contentView;

    public DownloadService(Context context) {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "MyService　onDestroy", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void getS3Key() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //TODO:BLEScan
//            BLEScanner_LOLIPOP blescanner = new BLEScanner_LOLIPOP();
        }
    }
}
