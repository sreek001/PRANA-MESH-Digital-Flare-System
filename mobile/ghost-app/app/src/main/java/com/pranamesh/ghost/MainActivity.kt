package com.pranamesh.ghost

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pranamesh.ghost.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var selectedDistressStatus: Int = 0  // Default to SAFE

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startDistressMode()
        } else {
            Toast.makeText(this, "Permissions required for BLE broadcasting", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep screen on during distress mode
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Setup emergency type selector
        setupEmergencyTypeSelector()

        binding.panicButton.setOnClickListener {
            if (checkPermissions()) {
                startDistressMode()
            } else {
                requestPermissions()
            }
        }

        binding.statusIndicator.text = "Ready - Select emergency type and press PANIC"
    }

    private fun setupEmergencyTypeSelector() {
        binding.emergencyTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedDistressStatus = when (checkedId) {
                R.id.safeOption -> 0
                R.id.helpOption -> 1
                R.id.medicalOption -> 2
                R.id.criticalOption -> 3
                else -> 0
            }
            updateStatusIndicator()
        }
    }

    private fun updateStatusIndicator() {
        val statusText = when (selectedDistressStatus) {
            0 -> "✅ SAFE"
            1 -> "⚠️ HELP"
            2 -> "🏥 MEDICAL"
            3 -> "🔴 CRITICAL"
            else -> "Unknown"
        }
        binding.statusIndicator.text = "Selected: $statusText\nPress PANIC to start broadcasting"
    }

    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
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
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissions.add(Manifest.permission.FOREGROUND_SERVICE)

        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startDistressMode() {
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_LONG).show()
            return
        }

        val serviceIntent = Intent(this, DistressBroadcastService::class.java).apply {
            putExtra("DISTRESS_STATUS", selectedDistressStatus)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        val statusLabel = when (selectedDistressStatus) {
            0 -> "✅ SAFE"
            1 -> "⚠️ HELP"
            2 -> "🏥 MEDICAL"
            3 -> "🔴 CRITICAL"
            else -> "UNKNOWN"
        }

        binding.statusIndicator.text = "DISTRESS MODE ACTIVE\nBroadcasting $statusLabel\nDo NOT close this app"
        binding.statusIndicator.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        binding.panicButton.isEnabled = false
        binding.panicButton.alpha = 0.5f

        Toast.makeText(this, "Distress mode activated - broadcasting $statusLabel", Toast.LENGTH_LONG).show()
    }
}
