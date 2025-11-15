package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    // DELIVERABLE: Displays splash screen for 2-3 seconds
    private val SPLASH_TIME_OUT: Long = 2500 // 2.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the new splash layout
        setContentView(R.layout.activity_splash)

        // IMPLEMENTATION HINT: Use Handler with postDelayed() for timing
        Handler(Looper.getMainLooper()).postDelayed({
            // Code to execute after the delay

            // Navigate to MainActivity automatically
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Finish the current activity so the user cannot navigate back to the splash screen
            finish()
        }, SPLASH_TIME_OUT)
    }
}