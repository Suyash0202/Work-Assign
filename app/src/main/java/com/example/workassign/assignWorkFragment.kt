package com.example.workassign


import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.workassign.Api.ApiUtilities
import com.example.workassign.Data.AndroidConfig
import com.example.workassign.Data.FCMMessage
import com.example.workassign.Data.MessageBody
import com.example.workassign.Data.NotificationData
import com.example.workassign.Data.Works
import com.example.workassign.databinding.FragmentAssignWorkBinding
import com.example.workassign.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import getAccessToken
import okhttp3.ResponseBody
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response


class assignWorkFragment : Fragment() {
    val employeeDetails by navArgs<assignWorkFragmentArgs>()
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
                    requireActivity().finish()

                }
                binding.rootLayout.setOnClickListener {
                    hideKeyboard()
                }

                setPriority()
                setDate()
                binding.btnDone.setOnClickListener {
                    assignWork()
                } }
            return binding.root
        }

    }

    fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireContext())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun assignWork() {
        val workTitle = binding.etTitle.text.toString().trim()
        val workDescription = binding.etWorkDescription.text.toString().trim()
        val lastDate = binding.tvLastDate.text.toString().trim()

        if (workTitle.isEmpty() || workDescription.isEmpty() || lastDate.isEmpty()) {
            Utils.showToast(requireContext(), "Please fill all the fields")
            return
        }

        if (priorityList.isNullOrEmpty()) {
            Utils.showToast(requireContext(), "Please select at least one priority")
            return
        }

        val bossId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("AssignWorkDebug", "bossId: $bossId, employeeId: ${employeeDetails.employeeData.Id}")
        val employeeId = employeeDetails.employeeData.Id
        val workRoom = "${bossId}_${employeeDetails.employeeData.Id}"
        val workRef = FirebaseDatabase.getInstance().getReference("Work").child(workRoom)
        val pushId = workRef.push().key!!

        val work = Works(
            id = pushId,
            bossId = bossId,
            workTitle = workTitle,
            workDescription = workDescription,
            workPriority = priorityList,
            workLastDate = lastDate,
            workStatus = "1"
        )
        Utils.showProgressDialog(requireContext())
        workRef.child(pushId).setValue(work)
            .addOnSuccessListener {
                sendNotificationToEmployee(employeeId,workTitle, lastDate)
                Utils.hideProgressDialog()
                Utils.showToast(requireContext(), "Work assigned successfully")
                val employeeEmail = employeeDetails.employeeData.email
                val employeename = employeeDetails.employeeData.name
                sendEmailToEmployee(employeename.toString(),employeeEmail.toString(), workTitle, workDescription, lastDate)

                val action = assignWorkFragmentDirections.actionAssignWorkFragmentToWorkFragment(employeeDetails.employeeData)
                findNavController().navigate(action)
                findNavController().popBackStack(R.id.assignWorkFragment, true)

            }
            .addOnFailureListener {
                Utils.hideProgressDialog()
                Utils.showToast(requireContext(), "Failed to assign work: ${it.message}")
            }
    }


    private fun setDate() {
        val myCalendar = Calendar.getInstance()
        updateLable(myCalendar)
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
        val myFormat = "dd-MM-yyyy"
        val sdf = java.text.SimpleDateFormat(myFormat)
        binding.tvLastDate.text = sdf.format(calendar.time)

    }


    private fun setPriority() {
        binding.apply {
            tvGreen.setOnClickListener {
                Utils.showToast(requireContext(), "Priority : Low")
                priorityList = "1"
                binding.tvGreen.setImageResource(R.drawable.select)
                binding.tvYellow.setImageResource(R.drawable.yellow_oval)
                binding.tvRed.setImageResource(R.drawable.red_oval)
            }
            tvYellow.setOnClickListener {
                Utils.showToast(requireContext(), "Priority : Medium")
                priorityList = "2"
                binding.tvYellow.setImageResource(R.drawable.select)
                binding.tvGreen.setImageResource(R.drawable.green_oval)
                binding.tvRed.setImageResource(R.drawable.red_oval)
            }
            tvRed.setOnClickListener {
                Utils.showToast(requireContext(), "Priority : High")
                priorityList = "3"
                binding.tvRed.setImageResource(R.drawable.select)
                binding.tvYellow.setImageResource(R.drawable.yellow_oval)
                binding.tvGreen.setImageResource(R.drawable.green_oval)
            }
        }


    }
    private fun sendEmailToEmployee(employeename: String,employeeEmail: String, workTitle: String, workDescription: String, lastDate: String) {
        val subject = "New Work Assigned"
        val message = """
      
      
      Subject: New Task Assignment: $workTitle

       Dear $employeename,

       You have been assigned a new task. Please find the details below:

       • Task Title: $workTitle
       • Description: $workDescription
       • Deadline: $lastDate

       We kindly request you to complete this task within the given timeline. If you have any questions or need further clarification, feel free to reach out.

       Thank you for your dedication and commitment.

       Best regards,
       Work Assign Team
    """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(employeeEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
        }

        try {
            startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (ex: android.content.ActivityNotFoundException) {
            Utils.showToast(requireContext(), "No email apps installed")
        }
    }
    private fun sendNotificationToEmployee(employeeId: String?, workTitle: String, lastDate: String) {
        val empRef = FirebaseDatabase.getInstance().getReference("Users").child(employeeId ?: "")

        empRef.get().addOnSuccessListener {
            val empToken = it.child("usertoken").value?.toString()

            if (empToken.isNullOrBlank() || empToken == "null") {
                Log.e("Suyash", "Invalid or missing FCM token for employee: $employeeId")
                return@addOnSuccessListener
            }

            val notification = NotificationData(
                title = "New Work Assigned",
                body = "Task: $workTitle | Deadline: $lastDate"
            )
            val messageBody = MessageBody(
                token = empToken,
                android = AndroidConfig(notification),
                data = mapOf("lastDate" to lastDate)
            )


            val fcmMessage = FCMMessage(message = messageBody)

            Log.d("Suyash", "Sending FCM to token: $empToken")
            Log.d("Suyash", "Notification: ${notification.title} - ${notification.body}")
            Log.d("Suyash", "Payload: ${Gson().toJson(fcmMessage)}")

            getAccessToken { accessToken ->
                val authHeader = "Bearer $accessToken"

                ApiUtilities.api.sendNotification(fcmMessage, authHeader)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                Log.d("Success", "✅ FCM v1 Notification sent successfully")
                            } else {
                                Log.e("fail", "❌ FCM v1 Failed: ${response.code()} - ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Log.e("fail", "🔥 Error sending notification", t)
                        }
                    })
            }

        }.addOnFailureListener {
            Log.e("Suyash", "❌ Failed to get employee token: ${it.message}")
        }
    }



}





