package com.example.workassign
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workassign.Data.Users
import com.example.workassign.auth.SigninActivity
import com.example.workassign.databinding.FragmentEmployeeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class employeeFragment : Fragment() {
    private lateinit var binding: FragmentEmployeeBinding
    private lateinit var employeeAdapter: EmployeeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentEmployeeBinding.inflate(layoutInflater)

        binding.apply {
            employeelog.setOnMenuItemClickListener {
                it
                when (it.itemId) {
                    R.id.logout -> {
                        showLogoutDialog()
                        true
                    }
                    else -> false
                }
            }
        }
        prepareRvForEmployeeAdapter()
        showallEmployee()
        return binding.root
    }

    private fun showallEmployee() {
        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val empList =   arrayListOf<Users>()
                for (emp in snapshot.children) {
                    val currentUser = emp.getValue(Users::class.java)
                    if (currentUser!!.userType == "Employee") {
                        empList.add(currentUser)
                    }
                    }
                    employeeAdapter.differ.submitList(empList)
                    binding.rvemployes.adapter = employeeAdapter

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    private fun prepareRvForEmployeeAdapter() {
        employeeAdapter = EmployeeAdapter()
        binding.rvemployes.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), SigninActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
