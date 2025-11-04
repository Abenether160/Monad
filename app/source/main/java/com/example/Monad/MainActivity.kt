package com.example.burglaralert

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.burglaralert.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val overlayRequestCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If not activated, launch PasswordActivity and finish
        if (!AuthManager.isActivated(this)) {
            val pi = Intent(this, PasswordActivity::class.java)
            startActivity(pi)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        }

        // Start foreground service
        val serviceIntent = Intent(this, AlertService::class.java)
        startForegroundService(serviceIntent)

        // Send alert button
        binding.btnSendAlert.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Grant overlay permission first", Toast.LENGTH_SHORT).show()
                requestOverlayPermission()
            } else {
                val intent = Intent(this, AlertService::class.java)
                intent.putExtra("SEND_ALERT", true)
                startService(intent)
            }
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(this, "Please grant overlay permission", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, overlayRequestCode)
        }
    }
}