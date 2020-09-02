package com.example.zhang.a3c_car;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.widget.ListView;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;


//蓝牙模块
public class BlueTooth {
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    private ListView lv;
    private BluetoothDevice mmDevice;
    private UUID uuid;
    public BluetoothSocket mmSocket;
    private ConnectedThread connectedThread = null;

    public void Bluetooth() {
        BA = BluetoothAdapter.getDefaultAdapter();

        pairedDevices = BA.getBondedDevices();

        if (pairedDevices.size() != 0)
            mmDevice = pairedDevices.iterator().next();
        uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        } catch (Exception e) {
        }
        try {
            mmSocket.connect();
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();
        } catch (IOException connectException) {
        }
    }

    public void zend()
    {
        try {
            connectedThread.interrupt();
            mmSocket.close();

        } catch (IOException connectException) {
        }
    }

    public void on() {
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        }
    }

    public void zsend(String s)
    {
        if(connectedThread==null)
            Bluetooth();
        connectedThread.write(s.getBytes());
    }

    public void off() {
        BA.disable();
    }

    public void visible() {
        Intent getVisible = new Intent(BluetoothAdapter.
                ACTION_REQUEST_DISCOVERABLE);
    }
}
