package com.example.theperfectionist

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.IOException
import java.util.UUID

class BluetoothManager(private val context: Context) {
    var isConnected: Boolean = false
    var connectedDevice: BluetoothDevice? = null

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    private fun hasBtPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED
        } else true
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): Set<BluetoothDevice>? {
        if (!hasBtPermission()) return null
        return bluetoothAdapter.bondedDevices
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice, onConnected: (BluetoothSocket?) -> Unit) {
        if (!hasBtPermission()) return

        // BLE devices must use GATT
        if (device.type == BluetoothDevice.DEVICE_TYPE_LE ||
            device.type == BluetoothDevice.DEVICE_TYPE_DUAL) {

            Log.d("BT", "Connecting to BLE device: ${device.name}")

            device.connectGatt(context, false, object : android.bluetooth.BluetoothGattCallback() {
                override fun onConnectionStateChange(
                    gatt: android.bluetooth.BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {
                    if (newState == android.bluetooth.BluetoothProfile.STATE_CONNECTED) {
                        Log.d("BT", "BLE Connected to ${device.name}")
                        onConnected(null) // no socket for BLE
                    }
                }
            })

            return
        }

        // Classic RFCOMM devices
        Thread {
            try {
                val uuid = device.uuids?.firstOrNull()?.uuid
                    ?: UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

                val socket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothAdapter.cancelDiscovery()
                socket.connect()

                Log.d("BT", "RFCOMM Connected to ${device.name}")
                onConnected(socket)

            } catch (e: Exception) {
                Log.e("BT", "RFCOMM connection failed", e)
                onConnected(null)
            }
        }.start()
    }



    @SuppressLint("MissingPermission")
    fun listenForMessages(socket: BluetoothSocket) {
        if (!hasBtPermission()) return

        val input = socket.inputStream
        Thread {
            val buffer = ByteArray(1024)
            while (true) {
                val bytes = input.read(buffer)
                val msg = String(buffer, 0, bytes)
                Log.d("BT", "Received: $msg")
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    fun send(socket: BluetoothSocket, msg: String) {
        if (!hasBtPermission()) return
        socket.outputStream.write(msg.toByteArray())
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery(onDeviceFound: (BluetoothDevice) -> Unit) {
        if (!hasBtPermission()) return

        val filter = android.content.IntentFilter(BluetoothDevice.ACTION_FOUND)

        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context, intent: android.content.Intent) {
                if (intent.action == BluetoothDevice.ACTION_FOUND) {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        onDeviceFound(device)
                    }
                }
            }
        }

        context.registerReceiver(receiver, filter)
        bluetoothAdapter.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun startBleScan(onDeviceFound: (BluetoothDevice) -> Unit) {
        if (!hasBtPermission()) return

        val scanner = bluetoothAdapter.bluetoothLeScanner

        val callback = object : android.bluetooth.le.ScanCallback() {
            override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
                onDeviceFound(result.device)
            }
        }

        scanner.startScan(callback)
    }
}
