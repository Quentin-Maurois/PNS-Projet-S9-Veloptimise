package com.example.wearosapp1.presentation

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wearosapp1.R
import java.nio.ByteBuffer
import java.util.*

class MainActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private lateinit var heartRateTextView: TextView
    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothGattServer: BluetoothGattServer? = null
    private var heartRateCharacteristic: BluetoothGattCharacteristic? = null
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private val PERMISSION_REQUEST_CODE = 1001

    private val connectedDevices = mutableListOf<BluetoothDevice>()

    companion object {
        val HEART_RATE_SERVICE_UUID: UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        heartRateTextView = findViewById(R.id.heart_rate_value)

        // init sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        if (checkPermissions()) {
            initializeBluetoothComponents()
        } else {
            requestBluetoothPermissions()
        }

        // Check and request permissions for BODY_SENSORS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BODY_SENSORS),
                1
            )
        } else {
            startHeartRateService()
        }

        // Start a foreground service to keep the app running in the background
        val serviceIntent = Intent(this, HeartRateService::class.java)
        startForegroundService(serviceIntent)
    }

    private fun checkPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Start the background service to listen for heart rate data
    private fun startHeartRateService() {
        val serviceIntent = Intent(this, HeartRateService::class.java)
        startForegroundService(serviceIntent)
    }


    private fun requestBluetoothPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeBluetoothComponents()
            } else {
                Log.e("MainActivity", "Bluetooth permissions denied")
            }
        }
    }

    private fun initializeBluetoothComponents() {
        bluetoothGattServer = bluetoothManager.openGattServer(this, gattServerCallback)?.apply {
            val service = BluetoothGattService(HEART_RATE_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
            heartRateCharacteristic = BluetoothGattCharacteristic(
                HEART_RATE_MEASUREMENT_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
            )
            service.addCharacteristic(heartRateCharacteristic)
            addService(service)
            Log.i("MainActivity", "GATT server setup successfully")
        }
        bluetoothLeAdvertiser = bluetoothManager.adapter.bluetoothLeAdvertiser
        startAdvertising()
    }

    private fun startAdvertising() {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(HEART_RATE_SERVICE_UUID))
            .build()

        bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.i("MainActivity", "BLE advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("MainActivity", "BLE advertising failed with error code $errorCode")
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectedDevices.add(device)
                Log.d("GATT", "Device connected: ${device.address}")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevices.remove(device)
                Log.d("GATT", "Device disconnected: ${device.address}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        heartRateSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
            val heartRate = event.values[0].toInt()
            //update the UI
            heartRateTextView.text = "Heart Rate : " + heartRate.toString() + " bpm"
            Log.d("HeartRateService", "Heart Rate Sensor Update: $heartRate bpm")

            // Update the Bluetooth characteristic only if there are connected devices
            if (connectedDevices.isNotEmpty()) {
                updateHeartRateCharacteristic(heartRate)
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

//    private fun updateHeartRateCharacteristic(heartRate: Int) {
//        heartRateCharacteristic?.value = heartRate.toString().toByteArray()
//
//        for (device in connectedDevices) {
//            bluetoothGattServer?.notifyCharacteristicChanged(device, heartRateCharacteristic, false)
//            Log.d("GATT", "Notifying device ${device.address} with heart rate: $heartRate")
//        }
//    }
private fun updateHeartRateCharacteristic(heartRate: Int) {
    // Create a ByteBuffer to hold the integer value
    val buffer = ByteBuffer.allocate(4) // 4 bytes for an integer
    buffer.putInt(heartRate) // Put the heart rate as an integer into the buffer

    heartRateCharacteristic?.value = buffer.array() // Set the characteristic value to the byte array

    for (device in connectedDevices) {
        bluetoothGattServer?.notifyCharacteristicChanged(device, heartRateCharacteristic, false)
        Log.d("GATT", "Notifying device ${device.address} with heart rate: $heartRate")
    }
}

}
