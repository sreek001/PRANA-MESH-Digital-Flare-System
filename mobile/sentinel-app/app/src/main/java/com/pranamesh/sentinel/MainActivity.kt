package com.pranamesh.sentinel

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pranamesh.sentinel.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private var detectedSignals = mutableListOf<SignalInfo>()
        private var statusCallback: ((List<SignalInfo>) -> Unit)? = null

        fun updateDetectedSignal(deviceId: String, lat: Float, lon: Float, status: Int, battery: Int) {
            val existing = detectedSignals.find { it.deviceId == deviceId }
            if (existing != null) {
                existing.lat = lat
                existing.lon = lon
                existing.status = status
                existing.battery = battery
                existing.timestamp = System.currentTimeMillis()
            } else {
                detectedSignals.add(SignalInfo(deviceId, lat, lon, status, battery, System.currentTimeMillis()))
            }
            statusCallback?.invoke(detectedSignals.toList())
        }

        fun registerCallback(callback: (List<SignalInfo>) -> Unit) {
            statusCallback = callback
            callback(detectedSignals.toList())
        }

        fun unregisterCallback() {
            statusCallback = null
        }
    }

    data class SignalInfo(
        val deviceId: String,
        var lat: Float,
        var lon: Float,
        var status: Int,
        var battery: Int,
        var timestamp: Long
    )

    private lateinit var binding: ActivityMainBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startScanning()
        } else {
            Toast.makeText(this, "Permissions required for BLE scanning", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep screen on during scanning
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        binding.startButton.setOnClickListener {
            if (checkPermissions()) {
                startScanning()
            } else {
                requestPermissions()
            }
        }

        binding.stopButton.setOnClickListener {
            stopScanning()
        }

        MainActivity.registerCallback { signals ->
            runOnUiThread {
                binding.signalCount.text = "📡 Signals Detected: ${signals.size}"
                val statusText = signals.joinToString("\n\n") { s ->
                    val statusLabel = when (s.status) {
                        0 -> "✅ SAFE"
                        1 -> "⚠️ HELP"
                        2 -> "🏥 MEDICAL"
                        3 -> "🔴 CRITICAL"
                        else -> "❓ UNKNOWN"
                    }
                    buildString {
                        append("📱 ${s.deviceId}\n")
                        append("$statusLabel | 🔋 ${s.battery}%\n")
                        append("📍 [${"%.6f".format(s.lat)}, ${"%.6f".format(s.lon)}]")
                    }
                }
                binding.signalList.text = statusText.ifEmpty { "📍 No signals detected yet\nScanning for BLE distress signals..." }
            }
        }

        binding.statusIndicator.text = "Ready - Press START to Begin Scanning"
    }

    override fun onDestroy() {
        super.onDestroy()
        MainActivity.unregisterCallback()
    }

    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }

        return permissions.isEmpty()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissions.add(Manifest.permission.FOREGROUND_SERVICE)

        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startScanning() {
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_LONG).show()
            return
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection - signals will queue", Toast.LENGTH_LONG).show()
        }

        val serviceIntent = Intent(this, BLEScanService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        binding.statusIndicator.text = "SCANNING ACTIVE\nListening for BLE distress signals..."
        binding.statusIndicator.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))

        Toast.makeText(this, "Scanning started", Toast.LENGTH_SHORT).show()
    }

    private fun stopScanning() {
        val serviceIntent = Intent(this, BLEScanService::class.java)
        stopService(serviceIntent)

        binding.statusIndicator.text = "Scanning stopped"
        binding.statusIndicator.setTextColor(ContextCompat.getColor(this, android.R.color.white))

        Toast.makeText(this, "Scanning stopped", Toast.LENGTH_SHORT).show()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
