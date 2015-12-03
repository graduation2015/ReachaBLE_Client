package jp.ac.it_college.std.reachable_client;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class BLEScannerKitkat {

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private DownloadService downloadService;
    private boolean isScanning;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private Context context;

    private BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!isAdded(device)) {
                saveDevice(device);
                Log.v("test", device.toString());
                //TODO:Download処理
                Log.i("test", "context: " + context);
                downloadService.getS3Key(context, device);
            }
        }
    };

    public BLEScannerKitkat(Context context) {
        Log.i("test", "" + context);
        this.context = context;
        //初期化
        bluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        // mBluetoothAdapterの取得
        mBluetoothAdapter = bluetoothManager.getAdapter();
        downloadService = new DownloadService();
    }

    // スキャン実施
    public void startScan() {
        // スキャンフィルタを設定するならこちら
        mBluetoothAdapter.startLeScan(callback);

        isScanning = true;
    }

    //スキャン停止
    public void stopScan() {
        if (mBluetoothLeScanner != null) {
            mBluetoothAdapter.stopLeScan(callback);
            isScanning = false;
        }
    }

    // スキャンしたデバイスがリストに追加済みかどうかの確認
    public boolean isAdded(BluetoothDevice device) {
        return deviceList.contains(device);
    }

    // スキャンしたデバイスのリスト保存
    public void saveDevice(BluetoothDevice device) {
        if (deviceList == null) {
            deviceList = new ArrayList<>();
        }

        deviceList.add(device);
    }
}