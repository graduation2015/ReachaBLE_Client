package jp.ac.it_college.std.reachable_client;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

//TODO:改良
public class BleDeviceListManager {
    public static ArrayList<BluetoothDevice> deviceList = new ArrayList<>();

    // スキャンしたデバイスのリスト保存
    public static void saveDevice(BluetoothDevice device) {
        if (deviceList == null) {
            deviceList = new ArrayList<>();
        }
        deviceList.add(device);
    }

    // スキャンしたデバイスがリストに追加済みかどうかの確認
    public static boolean isAdded(BluetoothDevice device) {
        return deviceList.contains(device);
    }

    // 登録したデバイスからnullが返ってきたとき、デバイスリストから削除する
    public static void undoDeviceList(BluetoothDevice device) {
        deviceList.remove(device);
    }
}
