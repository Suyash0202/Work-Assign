package com.example.workassign

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workassign.databinding.FragmentWorkBinding
import com.example.workassign.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.getValue

class workFragment : Fragment() {
    private lateinit var binding: FragmentWorkBinding
    private lateinit var workAdapter: WorkAdapter
    private lateinit var workRoom: String
    val employeeDetails by navArgs<workFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentWorkBinding.inflate(layoutInflater)


        binding.floatingActionButton.setOnClickListener {
            val action =
                workFragmentDirections.actionWorkFragmentToAssignWorkFragment(employeeDetails.employeeData)
            findNavController().navigate(action)
        }

        val empData = employeeDetails.employeeData.name
        binding.worktool.apply {
            title = empData
            setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        prepareRecyclerView()
        showWorks()
        return binding.root
    }

    private fun showWorks() {
        Utils.showProgressDialog(requireContext())
        val bossId = FirebaseAuth.getInstance().currentUser?.uid

         workRoom = "${bossId}_${employeeDetails.employeeData.Id}"
        FirebaseDatabase.getInstance().getReference("Work").child(workRoom)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("WorkFragment", "onDataChange: ${snapshot.childrenCount} items found")
                    val workList =mutableListOf<Works>()
                    for (allworks in snapshot.children) {
                        val work = allworks.getValue(Works::class.java)
                       work?.firebaseKey = allworks.key ?: ""
                        workList.add(work!!)
                        Log.d("AdapterDebug", "Title: ${work?.workTitle}, Date: ${work?.workLastDate}, Desc: ${work?.workDescription}")

                    }
                    Log.d("FirebaseData", "Fetched ${workList.size} works")
                    workAdapter.differ.submitList(workList)
                    Utils.hideProgressDialog()
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.hideProgressDialog()
                    Log.e("FirebaseData", "Error fetching data: ${error.message}")
                }

            })
    }

    private fun prepareRecyclerView() {
        workAdapter = WorkAdapter(::onUnassignedClick)
        binding.workRecycle.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = workAdapter
        }
    }

    fun onUnassignedClick(works: Works) {
              val builder= AlertDialog.Builder(context)
              val alerDialoge = builder.create()
              builder.setTitle("Unassigned Work")
                  .setMessage("Are you Sue You want to Unassigned this Work")
                  .setPositiveButton("Yes") {_,_->
                      unassignWork(works)


                  }
                  .setNegativeButton("No"){_,_->
                      alerDialoge.dismiss()

                  }.show()


    }
    private fun workFragment.unassignWork(works: Works) {
        val workKey = works.firebaseKey
        if(workKey.isNotEmpty()) {
            FirebaseDatabase.getInstance().getReference("Work").child(workRoom).child(workKey)
                .removeValue().addOnSuccessListener {

                    Utils.showToast(requireContext(), "Work Unassigned Successfully")
                }
                .addOnFailureListener {
                    Utils.showToast(requireContext(), "Error: ${it.message}")
                }
        }
        else{
            Utils.showToast(requireContext(), "Error: Work key is empty")
        }

    }
}


