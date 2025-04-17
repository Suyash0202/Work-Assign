package com.example.workassign

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workassign.Data.Users
import com.example.workassign.auth.SigninActivity
import com.example.workassign.databinding.ActivityEmployeeMainBinding
import com.example.workassign.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EmployeeMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmployeeMainBinding
    private lateinit var employeeWorkAdapter: EmployeeAdapter
    private lateinit var workRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEmployeeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        employeeWorkAdapter = EmployeeAdapter()
        workRef = FirebaseDatabase.getInstance().getReference("Work")

        prepareRv()
        showWork()

        binding.employeelog.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.logout -> {
                    showLogoutDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showWork() {
        Utils.showProgressDialog(this)
        val workId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        workRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (workRoom in snapshot.children) {
                    if (workRoom.key?.contains(workId) == true) {
                        val empWorkRef = workRef.child(workRoom.key!!)
                        empWorkRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val workList = arrayListOf<Users>()
                                for (work in snapshot.children) {
                                    val workItem = work.getValue(Works::class.java)
                                    workItem?.firebaseKey = work.key ?: ""
                                    if (workItem != null) {
                                       // workList.add(workItem)
                                    }
                                }
                                employeeWorkAdapter.differ.submitList(workList)
                                Utils.hideProgressDialog()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@EmployeeMainActivity,
                                    "Failed to load work data",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Utils.hideProgressDialog()
                            }
                        })
                        break // optional: stop after finding the correct room
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@EmployeeMainActivity,
                    "Database error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Utils.hideProgressDialog()
            }
        })
    }

    private fun prepareRv() {
        binding.rvemployeactivity.apply {
            layoutManager = LinearLayoutManager(
                this@EmployeeMainActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = employeeWorkAdapter
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, SigninActivity::class.java))
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
