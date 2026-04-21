package com.pranamesh.sentinel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.ByteBuffer
import java.nio.charset.Charset

class BLEScanService : Service() {

    companion object {
        const val TAG = "BLEScanService"
        const val MANUFACTURER_ID = 0x0099  // Try standard format
        const val MANUFACTURER_ID_ALT = 0x9999  // Try reversed format
        const val CHANNEL_ID = "sentinel_channel"
        const val NOTIFICATION_ID = 2
    }

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var sentinelId: String = ""
    private var handler = Handler(Looper.getMainLooper())
    private var isScanning = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "📡 Scan result: ${result.device.address} RSSI: ${result.rssi}")
            processScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            Log.d(TAG, "📡 Batch scan: ${results.size} results")
            results.forEach { processScanResult(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "✗ Scan failed: $errorCode")
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        sentinelId = Build.SERIAL.takeIf { it.isNotBlank() } ?: "SENTINEL-${System.currentTimeMillis()}"

        startScanning()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopScanning()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sentinel Scanner",
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
            .setContentTitle("PRANA-MESH Sentinel")
            .setContentText("Scanning for distress signals...")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun startScanning() {
        if (isScanning) return

        // Use LOW_LATENCY for faster detection (important for distress signals)
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)  // Report immediately
            .build()

        // Don't use manufacturer data filter - scan for all BLE devices
        // We'll filter by manufacturer ID in processScanResult
        val filter = ScanFilter.Builder().build()

        try {
            bluetoothLeScanner?.startScan(listOf(filter), settings, scanCallback)
            isScanning = true
            Log.d(TAG, "BLE scanning started (LOW_LATENCY mode)")
        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth scan permission not granted", e)
        }
    }

    private fun stopScanning() {
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
            isScanning = false
            Log.d(TAG, "BLE scanning stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping scan", e)
        }
    }

    private fun processScanResult(result: ScanResult) {
        val scanRecord = result.scanRecord ?: return

        // Log all manufacturer data for debugging
        val allManufacturerData = scanRecord.manufacturerSpecificData
        if (allManufacturerData != null && allManufacturerData.size() > 0) {
            for (i in 0 until allManufacturerData.size()) {
                val manId = allManufacturerData.keyAt(i)
                val data = allManufacturerData.valueAt(i)
                Log.d(TAG, "🔍 Manufacturer ID: 0x${manId.toString(16).uppercase()} Data size: ${data.size} bytes")

                // Check for both possible manufacturer IDs
                if (manId == MANUFACTURER_ID || manId == MANUFACTURER_ID_ALT || manId == 0x9999 || manId == 0x0099) {
                    Log.d(TAG, "✓ MATCH found for manufacturer ID: 0x${manId.toString(16).uppercase()}")
                    parseAndReport(data, result)
                }
            }
        }
    }

    private fun parseAndReport(data: ByteArray, result: ScanResult) {
        if (data.size < 18) {
            Log.e(TAG, "✗ Packet too small: ${data.size} bytes (need 18)")
            return
        }

        val packet = ByteBuffer.wrap(data)

        val deviceIdBytes = ByteArray(8)
        packet.get(deviceIdBytes)
        val deviceId = String(deviceIdBytes, Charset.forName("UTF-8")).trim()

        val lat = packet.float
        val lon = packet.float
        val battery = packet.get().toInt()
        val status = packet.get().toInt()

        Log.d(TAG, "✓✓✓ DISTRESS SIGNAL DETECTED ✓✓✓")
        Log.d(TAG, "  Device: $deviceId | Status: $status | Battery: $battery%")
        Log.d(TAG, "  Location: $lat, $lon | RSSI: ${result.rssi}")

        handler.post {
            MainActivity.updateDetectedSignal(deviceId, lat, lon, status, battery)
        }

        sendToBackend(deviceId, lat, lon, status, battery)
    }

    private fun sendToBackend(deviceId: String, lat: Float, lon: Float, status: Int, battery: Int) {
        val report = DistressReport(
            device_id = deviceId,
            lat = lat,
            lon = lon,
            status = status,
            battery = battery,
            timestamp = System.currentTimeMillis() / 1000,
            sentinel_id = sentinelId
        )

        Log.d(TAG, "🌐 Sending to backend: $deviceId (status=$status)")

        RetrofitClient.api.reportSignal(report).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Signal reported to backend: $deviceId")
                } else {
                    Log.e(TAG, "❌ Backend returned: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "❌ Network error: ${t.message}")
                Log.e(TAG, "Check if backend is reachable at RetrofitClient.BASE_URL")
            }
        })
    }
}
