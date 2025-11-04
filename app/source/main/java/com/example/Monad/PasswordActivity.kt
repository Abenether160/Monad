package com.example.burglaralert

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.burglaralert.databinding.ActivityPasswordBinding

class PasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordBinding

    // The shared password required before using the app
    private val SHARED_PASSWORD = "Slewrate@147"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If already activated, go straight to main
        if (AuthManager.isActivated(this)) {
            startMainAndFinish()
            return
        }

        binding.btnSubmitPassword.setOnClickListener {
            val pw = binding.etPassword.text.toString()
            if (pw == SHARED_PASSWORD) {
                AuthManager.setActivated(this, true)
                Toast.makeText(this, "Activated", Toast.LENGTH_SHORT).show()
                startMainAndFinish()
            } else {
                Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startMainAndFinish() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }
}