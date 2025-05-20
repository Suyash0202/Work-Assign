package com.example.workassign.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.workassign.Data.Users
import com.example.workassign.EmployeeMainActivity
import com.example.workassign.MainActivity
import com.example.workassign.NetworkMonitor
import com.example.workassign.R
import com.example.workassign.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var networkMonitor: NetworkMonitor
    private val REQUEST_CODE_POST_NOTIFICATIONS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val btn = findViewById<Button>(R.id.btnGetStarted)

        btn.setOnClickListener {
            checkAndRequestNotificationPermission { granted ->
                if (granted) {
                    proceedAfterPermission()
                } else {
                    Toast.makeText(
                        this,
                        "Cannot proceed without notification permission",
                        Toast.LENGTH_SHORT
                    ).show()
                    // You can choose to proceed anyway or not:
                    proceedAfterPermission()  // Proceed even if denied (recommended)
                }
            }
        }
    }

    private fun checkAndRequestNotificationPermission(onPermissionResult: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATIONS
                )
                // DO NOT call onPermissionResult here — wait for onRequestPermissionsResult callback
            } else {
                onPermissionResult(true)
            }
        } else {
            onPermissionResult(true) // No permission needed on older versions
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (granted) {
                proceedAfterPermission()
            } else {
                Toast.makeText(
                    this,
                    "Notification permission denied. You may miss important alerts.",
                    Toast.LENGTH_SHORT
                ).show()
                proceedAfterPermission()  // Proceed anyway even if permission denied
            }
        }
    }

    private fun proceedAfterPermission() {
        networkMonitor = NetworkMonitor(this)
        networkMonitor.startListening { isConnected ->
            if (!isConnected) {
                showNoInternetToast()
            }
        }

        val currentUserid = FirebaseAuth.getInstance().currentUser?.uid
        val isLoggedOut = getSharedPreferences("work assign_prefs", MODE_PRIVATE)
            .getBoolean("isLoggedOut", false)

        if (currentUserid == null || isLoggedOut) {
            navigateTo(SigninActivity::class.java)
        } else {
            FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentUserData = snapshot.getValue(Users::class.java)
                        when (currentUserData?.userType) {
                            "Boss" -> {
                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                finish()
                            }
                            "Employee" -> {
                                startActivity(Intent(this@SplashActivity, EmployeeMainActivity::class.java))
                                finish()
                            }
                            else -> {
                                startActivity(Intent(this@SplashActivity, EmployeeMainActivity::class.java))
                                finish()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Utils.hideProgressDialog()
                        Utils.showToast(this@SplashActivity, error.message)
                    }
                })
        }
    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        finish()
    }

    private fun showNoInternetToast() {
        Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show()
    }
}
