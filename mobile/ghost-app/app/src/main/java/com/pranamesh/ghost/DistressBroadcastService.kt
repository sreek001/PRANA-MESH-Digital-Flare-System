package com.pranamesh.ghost

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.nio.ByteBuffer

class DistressBroadcastService : Service() {

    companion object {
        const val TAG = "DistressBroadcast"
        const val MANUFACTURER_ID = 0x9999
        const val CHANNEL_ID = "distress_channel"
        const val NOTIFICATION_ID = 1
        const val BROADCAST_INTERVAL_MS = 60000L
    }

    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var cachedLat: Float = 0.0f
    private var cachedLon: Float = 0.0f
    private var deviceId: String = ""
    private var handler = Handler(Looper.getMainLooper())
    private var batteryReceiver: BatteryReceiver? = null
    private var currentBatteryLevel = 100
    private var distressStatus: Int = 2  // Default to MEDICAL distress
    private var advertiseRunnable: Runnable? = null

    private val broadcastRunnable = object : Runnable {
        override fun run() {
            broadcastDistressPacket()
            handler.postDelayed(this, BROADCAST_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "=== DistressBroadcastService CREATED ===")

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        Log.d(TAG, "Foreground service started")

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

        if (bluetoothLeAdvertiser == null) {
            Log.e(TAG, "BLE Advertiser is NULL - device may not support BLE advertising")
        } else {
            Log.d(TAG, "BLE Advertiser initialized")
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        deviceId = Build.SERIAL.takeIf { it.isNotBlank() } ?: "GHOST-${System.currentTimeMillis()}"
        Log.d(TAG, "Device ID: $deviceId")

        cacheLocation()
        startAdvertisingLoop()
        registerBatteryReceiver()

        Log.d(TAG, "=== Service setup complete ===")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Get distress status from intent (default to MEDICAL if not provided)
        distressStatus = intent?.getIntExtra("DISTRESS_STATUS", 2) ?: 2
        Log.d(TAG, "Distress status received: $distressStatus")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        stopBLEAdvertising()
        batteryReceiver?.let {
            unregisterReceiver(it)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Distress Broadcast",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PRANA-MESH Active")
            .setContentText("Broadcasting distress signal")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun cacheLocation() {
        try {
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                location?.let {
                    cachedLat = it.latitude.toFloat()
                    cachedLon = it.longitude.toFloat()
                    Log.d(TAG, "Location cached: $cachedLat, $cachedLon")
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted", e)
        }
    }

    private fun startAdvertisingLoop() {
        advertiseRunnable = broadcastRunnable
        handler.post(broadcastRunnable!!)
    }

    private fun broadcastDistressPacket() {
        try {
            if (bluetoothLeAdvertiser == null) {
                Log.e(TAG, "BLE Advertiser not available - service may have been killed")
                return
            }

            val packet = buildDistressPacket()
            Log.d(TAG, "Broadcasting packet: size=${packet.size}, battery=$currentBatteryLevel, status=$distressStatus")

            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .setTimeout(0)
                .build()

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addManufacturerData(MANUFACTURER_ID, packet)
                .build()

            bluetoothLeAdvertiser?.startAdvertising(settings, data, object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                    Log.d(TAG, "✓ BLE advertising started - Ghost is broadcasting")
                }

                override fun onStartFailure(errorCode: Int) {
                    val errorMsg = when(errorCode) {
                        1 -> "DATA_TOO_LARGE"
                        2 -> "TOO_MANY_ADVERTISERS"
                        3 -> "ADVERTISE_FAILED_ALREADY_STARTED"
                        4 -> "INTERNAL_ERROR"
                        else -> "UNKNOWN_ERROR_$errorCode"
                    }
                    Log.e(TAG, "✗ BLE advertising failed: $errorMsg")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "✗ Crash in broadcastDistressPacket: ${e.message}", e)
        }
    }

    private fun buildDistressPacket(): ByteArray {
        // BLE manufacturer data max is 21 bytes. Format: 8+4+4+1+1 = 18 bytes
        val buffer = ByteBuffer.allocate(18)
        val deviceIdBytes = deviceId.toByteArray(Charsets.UTF_8).copyOf(8)
        buffer.put(deviceIdBytes)      // 8 bytes - device ID
        buffer.putFloat(cachedLat)     // 4 bytes - latitude
        buffer.putFloat(cachedLon)     // 4 bytes - longitude
        buffer.put(currentBatteryLevel.toByte())  // 1 byte - battery
        buffer.put(distressStatus.toByte())       // 1 byte - status (0=SAFE, 1=HELP, 2=MEDICAL, 3=CRITICAL)
        return buffer.array()
    }

    private fun stopBLEAdvertising() {
        bluetoothLeAdvertiser?.stopAdvertising(object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {}
            override fun onStartFailure(errorCode: Int) {}
        })
    }

    private fun registerBatteryReceiver() {
        batteryReceiver = BatteryReceiver()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(batteryReceiver, filter)
        }
    }

    inner class BatteryReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level != -1 && scale != -1) {
                    currentBatteryLevel = ((level.toFloat() / scale.toFloat()) * 100).toInt()
                }
            }
        }
    }
}
