package jp.ac.it_college.std.reachable_client;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class DownloadService extends Service {
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private View contentView;
    private Context context;
    private BluetoothGatt bluetoothGatt;
    private int mStatus;

    public static final String SERVICE_UUID_YOU_CAN_CHANGE = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_UUID_YOU_CAN_CHANGE = "00002a29-0000-1000-8000-00805f9b34fb";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("test", "create");
        scanStart();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("test","start");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("test","destroy");
        scanStop();
        Toast.makeText(this, "MyService　onDestroy", Toast.LENGTH_SHORT).show();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    bluetoothGatt = gatt;
                    discoverService();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    disconnect();
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            bluetoothGatt = gatt;
            mStatus = status;
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.v("test", "onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleCharacteristic(characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
    };

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

    private BluetoothGattCharacteristic getCharacteristic() {
        return bluetoothGatt
                .getService(UUID.fromString(SERVICE_UUID_YOU_CAN_CHANGE))
                .getCharacteristic(UUID.fromString(CHAR_UUID_YOU_CAN_CHANGE));
    }

    public void connect(Context context, BluetoothDevice device) {
        Log.v("test","connect");
        bluetoothGatt = device.connectGatt(context, false, mGattCallback);
        bluetoothGatt.connect();
    }

    public void disconnect() {
        Log.v("test","disconnect");
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    // サービス取得要求
    public void discoverService() {
        if (bluetoothGatt != null) {
            bluetoothGatt.discoverServices();
        }
    }

    private void readCharacteristic() {
        Log.v("test","read");
        if (mStatus == BluetoothGatt.GATT_SUCCESS && bluetoothGatt != null) {
            try {
                bluetoothGatt.readCharacteristic(getCharacteristic());
            } catch (Exception e) {
                Log.v("test", "nullpo");
                e.printStackTrace();
            }
        }
    }

    private void handleCharacteristic(BluetoothGattCharacteristic characteristic) {
        byte[] bytes = characteristic.getValue();
        String msg = new String(bytes);
        Log.v("test",msg);
        //TODO:Key取得後の処理
//        ((ImageView) contentView.findViewById(R.id.img_response)).setImageBitmap(decodeBytes(bytes));
    }
    public void getS3Key(BluetoothDevice device) {
        Log.v("test","gets3key");
        connect(context, device);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                readCharacteristic();
            }
        }, 3000);
    }
}
