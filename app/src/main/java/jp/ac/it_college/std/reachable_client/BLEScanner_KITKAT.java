package jp.ac.it_college.std.reachable_client;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class BLEScanner_KITKAT {

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private boolean isScanning;

    private BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!isAdded(device)) {
                deviceList.add(device);
                //TODO:Download処理
            }
        }
    };

    public BLEScanner_KITKAT(Context context) {
        //初期化
        bluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        // mBluetoothAdapterの取得
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    // スキャン実施
    public void startScan(List<ScanFilter> filters, ScanSettings settings) {
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