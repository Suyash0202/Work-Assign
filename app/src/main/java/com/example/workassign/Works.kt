package com.example.workassign

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Works(
    val Id : String?= null,
   val workTitle : String?= null,
    val workDescription : String?= null,
    val workPriority : String = "1",
    val workLastDate : String = "1",
    var workStatus : String = "1",
    var expanded : Boolean = false,
    var firebaseKey: String = ""
) : Parcelable
