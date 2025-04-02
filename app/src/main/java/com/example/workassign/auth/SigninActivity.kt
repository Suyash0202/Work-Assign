package com.example.workassign.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.example.workassign.Data.Users
import com.example.workassign.R
import com.example.workassign.databinding.ActivitySigninBinding
import com.example.workassign.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

class SigninActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvSignUp.setOnLongClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
            true
        }
        login()

    }

    private fun login() {
        binding.btnlogin.setOnClickListener {
            val email = binding.loginname.text.toString()
            val password = binding.loginpassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginsucces(email, password)
            } else {
                Utils.showToast(this, "Empty fields are not allowed")
            }
        }
    }

    private fun loginsucces(email: String, password: String) {
        Utils.showProgressDialog(this)
        val firebase = FirebaseAuth.getInstance()
        lifecycleScope.launch {

            try {
                val authresult = firebase.signInWithEmailAndPassword(email, password).await()
                val currentUser = authresult.user?.uid
                Utils.hideProgressDialog()
                if (currentUser != null) {
                    FirebaseDatabase.getInstance().getReference("Users").child(currentUser)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val currentUserData = snapshot.getValue(Users::class.java)
                                when (currentUserData?.userType) {
                                    "Boss" -> {

                                        startActivity(intent)
                                        finish()
                                    }

                                    "Employee" -> {

                                        startActivity(intent)
                                        finish()
                                    }

                                    else -> {
                                        Utils.hideProgressDialog()
                                        Utils.showToast(this@SigninActivity, "Login Failed")
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Utils.hideProgressDialog()
                                Utils.showToast(this@SigninActivity, error.message)
                            }

                        })
                } else {
                    Utils.hideProgressDialog()
                    Utils.showToast(this@SigninActivity, "Login Failed")
                }
            } catch (e: Exception) {
                Utils.showToast(this@SigninActivity, e.message.toString())
            }
        }


    }
}