package com.example.workassign


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workassign.auth.SigninActivity
import com.example.workassign.databinding.ActivityEmployeeMainBinding
import com.example.workassign.utils.Utils
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EmployeeMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmployeeMainBinding
    private lateinit var employeeWorkAdapter: EmployeeActivityAdapter
    private lateinit var workRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEmployeeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        val workId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            Utils.hideProgressDialog()
            return
        }

        Log.d("FirebaseDebug", "Current User ID: $workId")

        workRef = FirebaseDatabase.getInstance().getReference("Work")
        workRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseDebug", "Work snapshot children count: ${snapshot.childrenCount}")

                var found = false
                for (workRoom in snapshot.children) {
                    Log.d("FirebaseDebug", "Checking workRoom key: ${workRoom.key}")

                    if (workRoom.key == workId) {  // Use exact match here
                        found = true
                        Log.d("FirebaseDebug", "Match found for userId: ${workRoom.key}")

                        val empWorkRef = workRef.child(workRoom.key!!)
                        empWorkRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                Log.d("FirebaseDebug", "Inner listener triggered, children: ${snapshot.childrenCount}")
                                val workList = arrayListOf<Works>()
                                for (work in snapshot.children) {
                                    val workItem = work.getValue(Works::class.java)
                                    workItem?.firebaseKey = work.key ?: ""
                                    if (workItem != null) {
                                        workList.add(workItem)
                                    }
                                }
                                Log.d("FirebaseDebug", "Work list size: ${workList.size}")
                                employeeWorkAdapter.differ.submitList(workList)
                                Utils.hideProgressDialog()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FirebaseDebug", "Inner listener cancelled: ${error.message}")
                                Toast.makeText(
                                    this@EmployeeMainActivity,
                                    "Failed to load work data",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Utils.hideProgressDialog()
                            }
                        })
                        break
                    }
                }
                if (!found) {
                    Log.d("FirebaseDebug", "No matching workRoom key for userId: $workId")
                    Toast.makeText(this@EmployeeMainActivity, "No work found for user", Toast.LENGTH_SHORT).show()
                    Utils.hideProgressDialog()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug", "Outer listener cancelled: ${error.message}")
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
        employeeWorkAdapter = EmployeeActivityAdapter(::onProgressClick, ::onCompleteClick)
        binding.rvemployeactivity.apply {
            layoutManager = LinearLayoutManager(this@EmployeeMainActivity, LinearLayoutManager.VERTICAL, false)
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

    private fun onProgressClick(works: Works, progressButton: MaterialButton) {
        AlertDialog.Builder(this)
            .setTitle("Starting Work")
            .setMessage("Are you starting work?")
            .setPositiveButton("Yes") { _, _ ->
                progressButton.apply {
                    text = "In Progress"
                    setTextColor(ContextCompat.getColor(this@EmployeeMainActivity, R.color.Light4))
                }
                updateStatus(works, "2")
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun onCompleteClick(works: Works, completeButton: MaterialButton) {
        AlertDialog.Builder(this)
            .setTitle("Completing Work")
            .setMessage("Mark work as complete?")
            .setPositiveButton("Yes") { _, _ ->
                completeButton.apply {
                    text = "Completed"
                    setTextColor(ContextCompat.getColor(this@EmployeeMainActivity, R.color.Light4))
                }
                updateStatus(works, "3")
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun updateStatus(works: Works, status: String) {
        val workId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        workRef = FirebaseDatabase.getInstance().getReference("Work")

        workRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (workRoom in snapshot.children) {
                    if (workRoom.key == workId) {
                        val empWorkRef = workRef.child(workRoom.key!!)
                        empWorkRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (allWork in snapshot.children) {
                                    val workItem = allWork.getValue(Works::class.java)
                                    if (workItem?.id == works.id) {
                                        empWorkRef.child(allWork.key!!).child("workStatus").setValue(status)
                                            .addOnSuccessListener {
                                                Toast.makeText(this@EmployeeMainActivity, "Status Updated", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this@EmployeeMainActivity, "Failed to update", Toast.LENGTH_SHORT).show()
                                            }
                                        return
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@EmployeeMainActivity, "Failed to load work data", Toast.LENGTH_SHORT).show()
                                Utils.hideProgressDialog()
                            }
                        })
                        break
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
}
