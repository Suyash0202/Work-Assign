package com.example.workassign.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.workassign.Data.Users
import com.example.workassign.databinding.AccountDialogeBinding
import com.example.workassign.databinding.ActivitySignUpBinding
import com.example.workassign.utils.Utils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

        binding.ivProfile.setOnClickListener {
            selectImage.launch("image/*")
        }

        binding.rgUserType.setOnCheckedChangeListener { _, checkedId ->
            userType = findViewById<RadioButton>(checkedId).text.toString()
            Log.d("userType", userType)
        }

        binding.btnRegister.setOnClickListener {
            if (userType.isEmpty()) {
                Utils.showToast(this, "Please select a user type")
            } else {
                registerUser()
            }
        }

        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
            finish()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }


    private fun registerUser() {

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (userImageUri == null) {
                Utils.showToast(this, "Please Upload Image")
            } else if (password == confirmPassword) {
                Utils.showProgressDialog(this)
                Log.d("AuthUser", "Current user UID:")
                createUserInFirebase(name, email, password)
            } else {
                Utils.showToast(this, "Passwords do not match")
            }
        } else {
            Utils.showToast(this, "Empty fields are not allowed")
        }
    }

    private fun createUserInFirebase(name: String, email: String, password: String) {


        lifecycleScope.launch {
            try {
                val user = firebase.createUserWithEmailAndPassword(email, password).await().user
                Log.d("UserUID", "Current user UID: ${FirebaseAuth.getInstance().currentUser?.uid}")

                if (user != null) {
                    uploadUserProfile(user.uid, name, email, password)
                }

            } catch (e: Exception) {
                Utils.hideProgressDialog()
                Utils.showToast(this@SignUpActivity, e.message.toString())
            }
        }
    }

    private fun uploadUserProfile(userId: String, name: String, email: String, password: String) {
        lifecycleScope.launch {
            val storageRef = FirebaseStorage.getInstance().getReference("Profile")
                .child(userId)
                .child("Profile.jpg")

            try {
                val uploadTask = storageRef.putFile(userImageUri!!).await()
                if (uploadTask.task.isSuccessful) {
                    val downloadUrl = storageRef.downloadUrl.await()
                    saveUserDataToDatabase(userId, name, email, password, downloadUrl)
                } else {
                    Utils.hideProgressDialog()
                    Utils.showToast(this@SignUpActivity, "Image upload failed")
                }
            } catch (e: Exception) {
                Utils.hideProgressDialog()
                Utils.showToast(this@SignUpActivity, e.message.toString())
            }
        }
    }

    private fun saveUserDataToDatabase(userId: String, name: String, email: String, password: String, downloadUrl: Uri) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener{ task->
            if(!task.isSuccessful) return@OnCompleteListener
            var token = task.result
            lifecycleScope.launch {
                try {
                    val userData = Users(Id=userId,name= name, email =  email, password =  password,image = downloadUrl.toString(), userType =  userType, usertoken = token)
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(userId)
                        .setValue(userData)
                        .await()
                    FirebaseAuth.getInstance().currentUser?.sendEmailVerification()?.addOnSuccessListener {

                        val dialog = AccountDialogeBinding.inflate(layoutInflater)
                        val alertDialog = AlertDialog.Builder(this@SignUpActivity)
                            .setView(dialog.root)
                            .setCancelable(false)
                            .create()
                        Utils.hideProgressDialog()
                        alertDialog.show()
                        dialog.Okid.setOnClickListener {
                            alertDialog.dismiss()
                            FirebaseAuth.getInstance().signOut()
                            startActivity(Intent(this@SignUpActivity, SigninActivity::class.java))
                            finish()
                        }
                    }
                        ?.addOnFailureListener {exception->
                            Utils.hideProgressDialog()
                            Utils.showToast(this@SignUpActivity, "Email verification failed: ${exception.message}")
                        }
                } catch (e: Exception) {
                    Utils.hideProgressDialog()
                    Utils.showToast(this@SignUpActivity, e.message.toString())
                }
            }
        })


    }
}
