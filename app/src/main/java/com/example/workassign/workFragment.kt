package com.example.workassign

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.workassign.databinding.FragmentWorkBinding

class workFragment : Fragment() {
   private lateinit var binding: FragmentWorkBinding
   val employeeDetails by navArgs<workFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentWorkBinding.inflate(layoutInflater)


        binding.floatingActionButton.setOnClickListener {
           findNavController().navigate(workFragmentDirections.actionWorkFragmentToAssignWorkFragment(employeeDetails.employeeData))
        }

      val empData = employeeDetails.employeeData.name
       binding.worktool.apply {
           title = empData
           setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed()
           }
       }

        return binding.root
    }
}