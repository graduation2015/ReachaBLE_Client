package jp.ac.it_college.std.reachable_client;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

//TODO:改良
public class BleDeviceListManager {
    private static ArrayList<BluetoothDevice> deviceList = new ArrayList<>();

    public ArrayList getDeviceList() {
        return deviceList;
    }
    // スキャンしたデバイスのリスト保存
    public void saveDevice(BluetoothDevice device) {
        if (deviceList == null) {
            deviceList = new ArrayList<>();
        }
        deviceList.add(device);
    }

    // スキャンしたデバイスがリストに追加済みかどうかの確認
    public boolean isAdded(BluetoothDevice device) {
        return deviceList.contains(device);
    }

    // 登録したデバイスからnullが返ってきたとき、デバイスリストから削除する
    public void undoDeviceList(BluetoothDevice device) {
        deviceList.remove(device);
    }

    public void resetList() {
        deviceList = new ArrayList<>();
    }
}
