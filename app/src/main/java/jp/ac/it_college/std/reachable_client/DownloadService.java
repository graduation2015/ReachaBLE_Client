package jp.ac.it_college.std.reachable_client;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import jp.ac.it_college.std.reachable_client.aws.AwsUtil;
import jp.ac.it_college.std.reachable_client.json.CouponController;


public class DownloadService extends Service implements Serializable{
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private Context context;
    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter bt;
    private int mStatus;
    private AmazonS3Client s3Client;
    private Timer timer;
    private String key;
    private boolean isConnecting = true;




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
        Log.v("test", "start");
//        beginDownload("company01");
        timer = new Timer();
        timer.schedule(new CheckBluetoothEnable(), 500, 3000);

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

    /**
     * androidのバージョンごとにBLEを起動させる記述が違うので
     * androidのバージョンがLOLIPOP(5.x系)かKITKAT(4.x系)を判別し、バージョンにあった記述でBLEを起動させる
     */
    private BLEScannerLolipop bleScannerLolipop;
    private BLEScannerKitkat bleScannerKitkat;
    private void scanStart() {

        timer.cancel();

        List<ScanFilter> fillter = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleScannerLolipop = new BLEScannerLolipop(context);

            ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
            settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);

            ScanSettings settings = settingsBuilder.build();

            bleScannerLolipop.startScan(fillter, settings);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bleScannerKitkat = new BLEScannerKitkat(context);
            bleScannerKitkat.startScan();
        }
    }

    /**
     * BLEを停止させる
     */
    private void scanStop() {
        if (bt.getState() == BluetoothAdapter.STATE_ON) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bleScannerLolipop.stopScan();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                bleScannerKitkat.stopScan();
            }
        }
    }

    /**
     * 指定されたUUIDを使ってBluetoothGattServiceを作成
     * @return
     */
    private BluetoothGattCharacteristic getCharacteristic() {
        BluetoothGattService service = bluetoothGatt
                .getService(UUID.fromString(SERVICE_UUID_YOU_CAN_CHANGE));

        Log.i("test", "gatt service: " + service);
        BluetoothGattCharacteristic characteristic = null;
        try {
            characteristic = service.getCharacteristic(UUID.fromString(CHAR_UUID_YOU_CAN_CHANGE));
        } catch (NullPointerException e) {
            Log.e("test", e.toString());
        }
        return characteristic;
    }

    /**
     * BLEを飛ばしている機器に接続する
     * @param context
     * @param device
     */
    public void connect(Context context, BluetoothDevice device) {
        Log.v("test", "connect to " + device);
        bluetoothGatt = device.connectGatt(context, false, mGattCallback);
        bluetoothGatt.connect();
    }

    /**
     * 接続を切る
     */
    public void disconnect() {
        Log.v("test", "disconnect");
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

    /**
     * 接続した機器から文字列を取得
     * @param device
     */
    private void readCharacteristic(BluetoothDevice device) {
        Log.v("test","read " + bluetoothGatt);
        BleDeviceListManager deviceManager = new BleDeviceListManager();

        if (mStatus == BluetoothGatt.GATT_SUCCESS & bluetoothGatt != null) {
            BluetoothGattCharacteristic characteristic = getCharacteristic();
            if (characteristic != null) {
                try {
                    bluetoothGatt.readCharacteristic(characteristic);
                } catch (Exception e) {
                    Log.e("test", "readCharacteristic : " + e.toString(), e);
                    deviceManager.undoDeviceList(device);
                    disconnect();
                }
            } else {
                disconnect();
            }
        } else {
            Log.v("test", "gatt is null");
            deviceManager.undoDeviceList(device);
            disconnect();
        }
    }

    /**
     * 取得した文字列を使ってAWSのS3から画像とjsonファイルを取得するメソッドへ飛ばす
     * @param characteristic
     */
    private void handleCharacteristic(BluetoothGattCharacteristic characteristic) {
        byte[] bytes = characteristic.getValue();
        String msg = new String(bytes);
        Log.v("test", msg);
        beginDownload(msg);
        disconnect();
//        ((ImageView) contentView.findViewById(R.id.img_response)).setImageBitmap(decodeBytes(bytes));
    }

    /**
     * BLEを飛ばしている機器が見つかったら[接続]→[文字列取得]→[切断]
     * @param context
     * @param device
     */
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

    /**
     * 取得した文字列を使ってS3から画像とjsonファイルをダウンロードする
     * ２つのファイルが取得できたらプッシュ通知をする
     */
    int count;
    private void beginDownload(String msg) {
        key = msg;
        String imagePath = MainFragment.IMAGE_PATH;
        String jsonPath =  MainFragment.JSON_PATH;
        count = 0;
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

        SharedPreferences pref = context.getSharedPreferences("new Coupon pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("new Coupon", pref.getString("new Coupon", "") + msg + " ").apply();
    }

    /**
     * S3からダウンロードしている間の状態を取得する
     */
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
                    if ( ++count >= 2 ) {
                        CouponController controller = new CouponController();
                        try {
                            controller.addCouponDownloadDate(context, key);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //プッシュ通知
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                        builder.setSmallIcon(R.drawable.icon_reachable);

                        builder.setContentTitle("クーポンを取得しました"); // 1行目
                        builder.setContentText("タップしてクーポンの確認"); // 2行目

                        Intent intent = new Intent(context, MainActivity.class);
                        PendingIntent contentIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_ONE_SHOT);
                        builder.setContentIntent(contentIntent);

                        builder.setAutoCancel(true);

                        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
                        builder.build().defaults |= Notification.DEFAULT_VIBRATE;
                        manager.notify(1, builder.build());

                    }
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

    /**
     * デバイスのBluetoothをONにして、使える状態になるまで待機、その後BLEのserviceを起動する
     */
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
