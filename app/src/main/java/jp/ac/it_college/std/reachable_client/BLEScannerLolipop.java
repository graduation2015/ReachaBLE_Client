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

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BLEScannerLolipop extends ScanCallback {

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private DownloadService downloadService;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private boolean isScanning;
    private Context context;

    public BLEScannerLolipop(Context context) {
        this.context = context;
        //初期化
        bluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        // mBluetoothAdapterの取得
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // mBluetoothLeScannerの初期化
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        downloadService = new DownloadService();
    }

    // スキャン実施
    public void startScan(List<ScanFilter> filters, ScanSettings settings) {
        // スキャンフィルタを設定するならこちら
        mBluetoothLeScanner.startScan(filters, settings, this);

        isScanning = true;
    }

    //スキャン停止
    public void stopScan() {
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(this);
            isScanning = false;
        }
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        BleDeviceListManager deviceManager = new BleDeviceListManager();
        if (result != null && result.getDevice() != null) {
            if (!deviceManager.isAdded(result.getDevice())) {
                deviceManager.saveDevice(result.getDevice());
                downloadService.getS3Key(context, result.getDevice());
            }
        }
    }
}