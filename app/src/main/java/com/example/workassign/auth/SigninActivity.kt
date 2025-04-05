package com.example.workassign.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log.e
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.example.workassign.Data.Users
import com.example.workassign.EmployeeMainActivity
import com.example.workassign.MainActivity
import com.example.workassign.R
import com.example.workassign.databinding.ActivitySigninBinding
import com.example.workassign.databinding.ForgetPasswordBinding
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
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        binding.forgetpassword.setOnClickListener {
            showforgetpassword()
        }

        binding.btnlogin.setOnClickListener {

            val email = binding.loginname.text.toString()
            val password = binding.loginpassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (Utils.isInternetAvailable(this)) {
                    loginsucces(email, password)
                } else {
                    Utils.showToast(this, "No internet connection")
                }

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
                firebase.currentUser?.reload()?.await()
                val verifyUser = firebase.currentUser?.isEmailVerified
                if (verifyUser == true) {
                    Utils.hideProgressDialog()
                    if (currentUser != null) {
                        FirebaseDatabase.getInstance().getReference("Users").child(currentUser)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val currentUserData = snapshot.getValue(Users::class.java)
                                    when (currentUserData?.userType) {
                                        "Boss" -> {
                                            startActivity(Intent(this@SigninActivity,MainActivity::class.java))
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            finish()
                                        }

                                        "Employee" -> {
                                            startActivity(
                                                Intent(
                                                    this@SigninActivity,
                                                    EmployeeMainActivity::class.java
                                                )
                                            )
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            finish()
                                        }

                                        else -> {
                                            Utils.showToast(
                                                this@SigninActivity,
                                                "Please Check Your Email & Password"
                                            )
                                            FirebaseAuth.getInstance().signOut()
                                            startActivity(Intent(this@SigninActivity,SigninActivity::class.java))
                                            finish()
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
                        Utils.showToast(this@SigninActivity, "User not found")
                    }

                } else {
                    Utils.hideProgressDialog()
                    Utils.showToast(this@SigninActivity, "Please verify your email")
                    FirebaseAuth.getInstance().signOut()
                }
            } catch (e: Exception) {
                Utils.hideProgressDialog()
                val errorMessage = when (e) {
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid password. Please try again."
                    is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "No account found with this email."
                    is com.google.firebase.auth.FirebaseAuthException -> "Authentication failed: ${e.message}"
                    else -> "Error: ${e.message}"
                }
                Utils.showToast(this@SigninActivity, errorMessage)

            }
        }
    }

    fun showforgetpassword() {
        val dialog = ForgetPasswordBinding.inflate(layoutInflater)
        val alertDialog = android.app.AlertDialog.Builder(this)
            .setView(dialog.root)
            .create()
        alertDialog.show()
        dialog.etEmail.requestFocus()
        dialog.btnResetPassword.setOnClickListener {
            val etemail = dialog.etEmail.text.toString()
          alertDialog.hide()
            resetpassword(etemail)

        }

    }
}

private fun SigninActivity.resetpassword(etemail: String) {
    lifecycleScope.launch {
        try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(etemail).await()
            Utils.showToast(this@resetpassword, "Password reset email sent")
        } catch (e: Exception) {
            Utils.showToast(this@resetpassword, "Error: ${e.message}")
        }


    }
}

