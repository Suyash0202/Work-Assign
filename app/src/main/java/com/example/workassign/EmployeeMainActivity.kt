package com.example.workassign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View

import android.widget.AdapterView
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EmployeeMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmployeeMainBinding
    private lateinit var employeeWorkAdapter: EmployeeActivityAdapter
    private lateinit var workRef: DatabaseReference
    private var masterWorkList: List<Works> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEmployeeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        workRef = FirebaseDatabase.getInstance().getReference("Work")

        prepareRv()
        showWork()
        binding.statusFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedStatus = parent?.getItemAtPosition(position).toString()
                filterListByStatus(selectedStatus)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

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

        workRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseDebug", "Work snapshot children count: ${snapshot.childrenCount}")

                val employeeId = FirebaseAuth.getInstance().currentUser?.uid
                var foundWorkRooms = mutableListOf<DataSnapshot>()

                for (workRoom in snapshot.children) {
                    val key = workRoom.key ?: continue
                    Log.d("FirebaseDebug", "Checking workRoom key: $key")

                    if (key.endsWith("_$employeeId")) {
                        foundWorkRooms.add(workRoom)
                    }
                }

                if (foundWorkRooms.isEmpty()) {
                    Log.d("FirebaseDebug", "No matching workRoom for userId: $employeeId")
                    Toast.makeText(this@EmployeeMainActivity, "No work found for user", Toast.LENGTH_SHORT).show()
                    Utils.hideProgressDialog()
                    return
                }
                val workList = mutableListOf<Works>()
                for (workRoom in foundWorkRooms) {
                    for (workSnapshot in workRoom.children) {
                        val workItem = workSnapshot.getValue(Works::class.java)
                        workItem?.firebaseKey = workSnapshot.key ?: ""
                        if (workItem != null) {
                            workList.add(workItem)
                        }
                    }
                }
                Log.d("DATA_SIZE", "Total work items found: ${workList.size}")
                masterWorkList = workList
                employeeWorkAdapter.differ.submitList(workList)
                Utils.hideProgressDialog()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug", "Database error: ${error.message}")
                Toast.makeText(this@EmployeeMainActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
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
        val employeeId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        workRef = FirebaseDatabase.getInstance().getReference("Work")

        // Instead of looping all, we find the workRoom key ending with _employeeId
        workRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var foundWorkRoomKey: String? = null

                for (workRoom in snapshot.children) {
                    val key = workRoom.key ?: continue
                    if (key.endsWith("_$employeeId")) {
                        foundWorkRoomKey = key
                        break
                    }
                }

                if (foundWorkRoomKey == null) {
                    Toast.makeText(this@EmployeeMainActivity, "Work room not found for user", Toast.LENGTH_SHORT).show()
                    return
                }

                val empWorkRef = workRef.child(foundWorkRoomKey)
                empWorkRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var workKeyToUpdate: String? = null

                        for (allWork in snapshot.children) {
                            val workItem = allWork.getValue(Works::class.java)
                            if (workItem?.id == works.id) {
                                workKeyToUpdate = allWork.key
                                break
                            }
                        }

                        if (workKeyToUpdate == null) {
                            Toast.makeText(this@EmployeeMainActivity, "Work item not found", Toast.LENGTH_SHORT).show()
                            return
                        }

                        empWorkRef.child(workKeyToUpdate).child("workStatus").setValue(status)
                            .addOnSuccessListener {
                                Toast.makeText(this@EmployeeMainActivity, "Status Updated", Toast.LENGTH_SHORT).show()

                                // Update local list in adapter here:
                                val updatedList = employeeWorkAdapter.differ.currentList.toMutableList()
                                val index = updatedList.indexOfFirst { it.id == works.id }
                                if (index != -1) {
                                    val updatedWork = works.copy(workStatus = status)
                                    updatedList[index] = updatedWork
                                    employeeWorkAdapter.differ.submitList(updatedList)
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@EmployeeMainActivity, "Failed to update", Toast.LENGTH_SHORT).show()
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EmployeeMainActivity, "Failed to load work data", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EmployeeMainActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun filterListByStatus(status: String) {
        val filteredList = when (status) {
            "All Status" -> {
                Log.d("FilterDebug", "Filtering: All items")
                masterWorkList
            }
            else -> {
                val statusCode = when (status) {
                    "Pending" -> "1"
                    "In Progress" -> "2"
                    "Completed" -> "3"
                    else -> ""
                }

                Log.d("FilterDebug", "Filtering for status name: $status | statusCode: $statusCode")
                val list = masterWorkList.filter {
                    it.workStatus == statusCode
                }
                Log.d("FilterDebug", "Filtered list size: ${list.size}")
                list
            }
        }
        if (filteredList.isEmpty()) {
            binding.noDataText.visibility = View.VISIBLE
        } else {
            binding.noDataText.visibility = View.GONE
        }

        Log.d("FilterDebug", "All statuses in list: ${masterWorkList.map { it.workStatus }}")
        employeeWorkAdapter.differ.submitList(filteredList)
    }


}
