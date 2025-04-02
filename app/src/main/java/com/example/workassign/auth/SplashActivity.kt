package com.example.workassign.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.workassign.MainActivity
import com.example.workassign.R
import kotlin.jvm.java

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
       Handler(Looper.getMainLooper()).postDelayed({
          startActivity(Intent(this, SignUpActivity::class.java))
           finish()
       }, 3000)
       }
    }