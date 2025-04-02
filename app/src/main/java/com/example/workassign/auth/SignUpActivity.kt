package com.example.workassign.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.workassign.Data.Users
import com.example.workassign.MainActivity
import com.example.workassign.R
import com.example.workassign.databinding.ActivitySignUpBinding
import com.example.workassign.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java
import kotlin.toString

class SignUpActivity : AppCompatActivity() {
    private lateinit var firebase: FirebaseAuth
    lateinit var binding: ActivitySignUpBinding
    var userImageUri: Uri? = null
    var userType: String = ""

    private val selectImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            userImageUri = uri

            binding.ivProfile.load(userImageUri) {
                transformations(CircleCropTransformation())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebase = FirebaseAuth.getInstance()

        binding.ivProfile.setOnClickListener { selectImage.launch("image/*") }

        binding.rgUserType.setOnCheckedChangeListener { _, checkedId ->
            userType = findViewById<RadioButton>(checkedId).text.toString()
            Log.d("userType", userType)
        }

        binding.btnRegister.setOnClickListener { setSignup() }

        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
            finish()
            true
        }


    }
    private fun setSignup() {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (userImageUri == null) {
                Utils.showToast(this, "Please Upload Image")
            } else if (password == confirmPassword) {
                Utils.showProgressDialog(this)
                uploadImageProfile(name, email, password)
            } else {
                Utils.showToast(this, "Password not match")
            }
        } else {
            Utils.showToast(this, "Empty fields are not allowed")
        }
    }

    private fun uploadImageProfile(name: String, email: String, password: String) {
        lifecycleScope.launch {
            val currentUserId = firebase.currentUser?.uid ?: return@launch
            val storageRef = FirebaseStorage.getInstance().getReference("Profile")
                .child(currentUserId).child("Profile.jpg")

            try {
                val uploadTask = storageRef.putFile(userImageUri!!).await()
                if (uploadTask.task.isSuccessful) {
                    val downloadUrl = storageRef.downloadUrl.await()
                    saveUserData(name, email, password, downloadUrl)
                } else {
                    Utils.hideProgressDialog()
                    //showToast("Upload failed: ${uploadTask.task.exception?.message}")
                }
            } catch (e: Exception) {
                Utils.hideProgressDialog()
                Utils.showToast(this@SignUpActivity, e.message.toString())
            }
        }
    }

    private fun saveUserData(name: String, email: String, password: String, downloadUrl: Uri) {
        lifecycleScope.launch {
            try {
                val firebaseAuth = FirebaseAuth.getInstance()
                val user = firebase.createUserWithEmailAndPassword(email, password).await().user ?: return@launch
                val database = FirebaseDatabase.getInstance().getReference("Users")
                val userUid = user.uid
                val userType = if (binding.rbBoss.isChecked) "Boss" else "Employee"
                val userData = Users(user.uid, name, email,password, downloadUrl.toString(), userType)

                database.child(userUid).setValue(userData).await()

                Utils.apply {
                    hideProgressDialog()
                    showToast(this@SignUpActivity, "Registration Successful")
                }

                startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                Utils.apply {
                    hideProgressDialog()
                    showToast(this@SignUpActivity, e.message.toString())
                }
            }
        }
    }

}

