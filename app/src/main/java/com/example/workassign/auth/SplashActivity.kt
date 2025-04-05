package com.example.workassign.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log.e
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.workassign.Data.Users
import com.example.workassign.EmployeeMainActivity
import com.example.workassign.MainActivity
import com.example.workassign.R
import com.example.workassign.auth.SigninActivity
import com.example.workassign.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlin.jvm.java

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
       Handler(Looper.getMainLooper()).postDelayed({
           val currentUserid = FirebaseAuth.getInstance().currentUser?.uid
           val isLoggedOut = getSharedPreferences("workassign_prefs", MODE_PRIVATE)
               .getBoolean("isLoggedOut", false)
           if (currentUserid == null || isLoggedOut) {
               navigateTo(SigninActivity::class.java)
           }
           else {
               lifecycleScope.launch {
                   try {
                       FirebaseDatabase.getInstance().getReference("Users")
                           .child(currentUserid.toString())
                           .addListenerForSingleValueEvent(object : ValueEventListener {
                               override fun onDataChange(snapshot: DataSnapshot) {
                                   val currentUserData = snapshot.getValue(Users::class.java)
                                   when (currentUserData?.userType) {
                                       "Boss" -> {
                                           startActivity(
                                               Intent(
                                                   this@SplashActivity,
                                                   MainActivity::class.java
                                               )
                                           )
                                           finish()
                                       }

                                       "Employee" -> {
                                           startActivity(
                                               Intent(
                                                   this@SplashActivity,
                                                   EmployeeMainActivity::class.java
                                               )
                                           )
                                           finish()
                                       }

                                       else -> {
                                           startActivity(
                                               Intent(
                                                   this@SplashActivity,
                                                   EmployeeMainActivity::class.java
                                               )
                                           )
                                           finish()
                                       }
                                   }
                               }

                               override fun onCancelled(error: DatabaseError) {
                                   Utils.hideProgressDialog()
                                   Utils.showToast(this@SplashActivity, error.message)
                               }
                           })
                   } catch (e: Exception) {
                       Utils.showToast(this@SplashActivity, e.message.toString())
                   }

               }
           } }, 3000)
    }

    private fun navigateTo(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
        finish()
    }
    }