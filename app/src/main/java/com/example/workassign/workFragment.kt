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
import com.google.firebase.database.*

class workFragment : Fragment() {

    private var _binding: FragmentWorkBinding? = null
    private val binding get() = _binding!!

    private lateinit var workAdapter: WorkAdapter
    private lateinit var workRoom: String

    private val employeeDetails by navArgs<workFragmentArgs>()

    private val databaseReference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("Work")
    }

    private var workListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentWorkBinding.inflate(inflater, container, false)

        setupToolbar()
        prepareRecyclerView()
        showWorks()

        binding.floatingActionButton.setOnClickListener {
            val action = workFragmentDirections.actionWorkFragmentToAssignWorkFragment(employeeDetails.employeeData)
            findNavController().navigate(action)
        }

        return binding.root
    }

    private fun setupToolbar() {
        val empName = employeeDetails.employeeData.name
        binding.worktool.apply {
            title = empName
            setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun prepareRecyclerView() {
        workAdapter = WorkAdapter(::onUnassignedClick)
        binding.workRecycle.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workAdapter
        }
    }

    private fun showWorks() {
        Utils.showProgressDialog(requireContext())

        val bossId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Utils.hideProgressDialog()
            Log.e("workFragment", "Current user is null")
            return
        }
        workRoom = "${bossId}_${employeeDetails.employeeData.Id}"

        // Remove previous listener if exists
        workListener?.let { databaseReference.child(workRoom).removeEventListener(it) }

        workListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("workFragment", "Work items found: ${snapshot.childrenCount}")

                val workList = mutableListOf<Works>()

                for (workSnapshot in snapshot.children) {
                    val work = workSnapshot.getValue(Works::class.java)
                    work?.firebaseKey = workSnapshot.key ?: ""
                    work?.let { workList.add(it.copy()) }
                    Log.d("workFragment", "Title: ${work?.workTitle}, Date: ${work?.workLastDate}")
                }
                workAdapter.differ.submitList(null)
                workAdapter.differ.submitList(workList)
                Utils.hideProgressDialog()
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.hideProgressDialog()
                Log.e("workFragment", "Error fetching data: ${error.message}")
                Utils.showToast(requireContext(), "Failed to load works: ${error.message}")
            }
        }

        databaseReference.child(workRoom).addValueEventListener(workListener!!)
    }

    private fun onUnassignedClick(work: Works) {
        AlertDialog.Builder(requireContext())
            .setTitle("Unassign Work")
            .setMessage("Are you sure you want to unassign this work?")
            .setPositiveButton("Yes") { dialog, _ ->
                unassignWork(work)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun unassignWork(work: Works) {
        val workKey = work.firebaseKey
        if (workKey.isNotEmpty()) {
            databaseReference.child(workRoom).child(workKey)
                .removeValue()
                .addOnSuccessListener {
                    Utils.showToast(requireContext(), "Work unassigned successfully")
                }
                .addOnFailureListener { e ->
                    Utils.showToast(requireContext(), "Error: ${e.message}")
                }
        } else {
            Utils.showToast(requireContext(), "Work key is empty, cannot unassign")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove listener to avoid memory leaks
        workListener?.let { databaseReference.child(workRoom).removeEventListener(it) }
        _binding = null
    }
}
