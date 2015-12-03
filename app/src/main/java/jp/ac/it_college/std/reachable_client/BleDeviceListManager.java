package jp.ac.it_college.std.reachable_client;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

/**
 * Created by s13006 on 15/12/03.
 */
public class BleDeviceListManager {
    private static ArrayList<BluetoothDevice> deviceList = new ArrayList<>();

    public static ArrayList<BluetoothDevice> getDeviceList() {
        return deviceList;
    }
}
