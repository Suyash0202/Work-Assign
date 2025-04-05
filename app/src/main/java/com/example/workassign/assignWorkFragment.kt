package com.example.workassign

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.workassign.databinding.FragmentAssignWorkBinding
import com.example.workassign.utils.Utils


class assignWorkFragment : Fragment() {
    private lateinit var binding: FragmentAssignWorkBinding
    private var priorityList: String = "1"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAssignWorkBinding.inflate(layoutInflater)
        binding.apply {
            toolbar2.apply {
                setNavigationOnClickListener {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

           setPriority()
                setDate()
            }

            return binding.root
        }

    }

    private fun setDate() {
        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar.apply {
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateLable(myCalendar)
            }
        }
        binding.tvDatePicker.setOnClickListener {
            DatePickerDialog(requireContext(), datePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show()
            
        }


    }

    private fun updateLable(calendar: Calendar) {



    }

    private fun setPriority() {
        binding.apply {
            tvGreen.setOnClickListener {
                Utils.showToast(requireContext(), "Priority : Low")
                priorityList = "1"
                binding.tvGreen.setImageResource(R.drawable.ok)
                binding.tvYellow.setImageResource(0)
                binding.tvRed.setImageResource(0)
            }
            tvYellow.setOnClickListener {
                Utils.showToast(requireContext(), "Priority : Medium")
                priorityList = "2"
                binding.tvYellow.setImageResource(R.drawable.ok)
                binding.tvGreen.setImageResource(0)
                binding.tvRed.setImageResource(0)
            }
            tvRed.setOnClickListener {
                Utils.showToast(requireContext(), "Priority : High")
                priorityList = "3"
                binding.tvRed.setImageResource(R.drawable.ok)
                binding.tvYellow.setImageResource(0)
                binding.tvGreen.setImageResource(0)
            }
        }


    }
}
