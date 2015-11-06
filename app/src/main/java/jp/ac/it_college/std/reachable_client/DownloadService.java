package jp.ac.it_college.std.reachable_client;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;



public class DownloadService extends Service {
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private View contentView;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scanStart();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scanStop();
        Toast.makeText(this, "MyService　onDestroy", Toast.LENGTH_SHORT).show();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void scanStart() {
        context = getApplicationContext();
        List<ScanFilter> fillter = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BLEScanner_LOLIPOP blescanner = new BLEScanner_LOLIPOP(context);

            ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
            settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);

            ScanSettings settings = settingsBuilder.build();

            blescanner.startScan(fillter, settings);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            BLEScanner_KITKAT blescanner = new BLEScanner_KITKAT(context);
            blescanner.startScan();
        }
    }

    private void scanStop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BLEScanner_LOLIPOP bleScanner = new BLEScanner_LOLIPOP(context);
            bleScanner.stopScan();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            BLEScanner_KITKAT bleScanner = new BLEScanner_KITKAT(context);
            bleScanner.stopScan();
        }
    }


}
