package jp.ac.it_college.std.reachable_client;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import jp.ac.it_college.std.reachable_client.aws.AwsUtil;


public class DownloadService extends Service {
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private Context context;
    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter bt;
    private int mStatus;
    private AmazonS3Client s3Client;
    private Timer timer = new Timer();


    public static final String SERVICE_UUID_YOU_CAN_CHANGE = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_UUID_YOU_CAN_CHANGE = "00002a29-0000-1000-8000-00805f9b34fb";

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothSetUp();
        context = getApplicationContext();

        Log.v("test", "create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("test","start");
        timer.schedule(new CheckBluetoothEnable(), 500, 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("test","destroy");
        scanStop();
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

    private void bluetoothSetUp() {
        bt = BluetoothAdapter.getDefaultAdapter();

        if (bt == null) {
            return;
        }
        if (!bt.isEnabled()) {
            bt.enable();
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void scanStart() {

        timer.cancel();
        List<ScanFilter> fillter = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BLEScannerLolipop blescanner = new BLEScannerLolipop(context);

            ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
            settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);

            ScanSettings settings = settingsBuilder.build();

            blescanner.startScan(fillter, settings);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            BLEScannerKitkat blescanner = new BLEScannerKitkat(context);
            blescanner.startScan();
        }
    }

    private void scanStop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BLEScannerLolipop bleScanner = new BLEScannerLolipop(context);
            bleScanner.stopScan();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            BLEScannerKitkat bleScanner = new BLEScannerKitkat(context);
            bleScanner.stopScan();
        }
    }

    private BluetoothGattCharacteristic getCharacteristic() {
        BluetoothGattService service = bluetoothGatt
                .getService(UUID.fromString(SERVICE_UUID_YOU_CAN_CHANGE));
        while (service == null) {
            service = bluetoothGatt
                    .getService(UUID.fromString(SERVICE_UUID_YOU_CAN_CHANGE));
        }
        Log.i("test", "gatt service: " + service);

        return service
                .getCharacteristic(UUID.fromString(CHAR_UUID_YOU_CAN_CHANGE));
    }

    public void connect(Context context, BluetoothDevice device) {
        Log.v("test", "connect");
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

    private void readCharacteristic(BluetoothDevice device) {
        Log.v("test","read");
        //dviceListにdeviceを追加
        BleDeviceListManager.saveDevice(device);
        if (mStatus == BluetoothGatt.GATT_SUCCESS && bluetoothGatt != null) {
            try {
                bluetoothGatt.readCharacteristic(getCharacteristic());
            } catch (Exception e) {
                Log.e("test", "readCharacteristic : " + e.toString(), e);
                BleDeviceListManager.undoDeviceList(device);
                disconnect();
            }
        } else {
            Log.v("test","gatt is null");
            disconnect();
        }
    }

    private void handleCharacteristic(BluetoothGattCharacteristic characteristic) {
        byte[] bytes = characteristic.getValue();
        String msg = new String(bytes);
        Log.v("test", msg);
        beginDownload(msg);

//        ((ImageView) contentView.findViewById(R.id.img_response)).setImageBitmap(decodeBytes(bytes));
    }

    public void getS3Key(Context context, final BluetoothDevice device) {
        Log.v("test", "gets3key");
        this.context = context;
        connect(context, device);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                readCharacteristic(device);
            }
        }, 3000);
    }

    private void beginDownload(String msg) {
        String imagePath = MainFragment.IMAGE_PATH;
        String jsonPath =  MainFragment.JSON_PATH;

//        TransferUtility utility = new TransferUtility(s3Client, context);

        TransferUtility utility = AwsUtil.getTransferUtility(context);

        File file = new File(imagePath, msg);

        //Image Download
        TransferObserver observer = utility.download(
                Constants.BUCKET_NAME + msg,
                msg,
                file);
        observer.setTransferListener(new S3DownloadListener());

        file = new File(jsonPath, msg + ".json");
        //Json Download
        observer = utility.download(
                Constants.BUCKET_NAME + msg,
                msg + ".json",
                file);

        observer.setTransferListener(new S3DownloadListener());
    }

    private class S3DownloadListener implements TransferListener {
        private static final String DIALOG_TITLE = "Download";
        private static final String DIALOG_MESSAGE = "Downloading...";
        private static final String TAG = "S3DownloadListener";

        @Override
        public void onStateChanged(int i, TransferState transferState) {
            Log.v("test",transferState.toString());
            switch (transferState) {
                case IN_PROGRESS:
//                    mDialogFragment.show(getFragmentManager(), "tag_downloading");
                    break;
                case COMPLETED:
//                    mDialogFragment.dismiss();
//                    Toast.makeText(getActivity(), "Download completed.", Toast.LENGTH_SHORT).show();
                    break;
                case FAILED:
//                    mDialogFragment.dismiss();
//                    Toast.makeText(getActivity(), "Download failed.", Toast.LENGTH_SHORT).show();
                default:
                    Log.d(TAG, transferState.name());
                    break;
            }
        }

        @Override
        public void onProgressChanged(int i, long l, long l1) {

        }

        @Override
        public void onError(int i, Exception e) {
            Log.e("test", String.valueOf(i), e);
        }
    }

    private class CheckBluetoothEnable extends TimerTask {

        @Override
        public void run() {
            switch (bt.getState()) {
                case BluetoothAdapter.STATE_ON:
                    scanStart();
                    break;
                default:
                    break;
            }
            Log.v("test", String.valueOf(bt.getState()));
        }
    }
}
