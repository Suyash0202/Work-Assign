package com.example.workassign.Data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID
@Parcelize
data class Users(
                 var newId: String?=UUID.randomUUID().toString(),
                 var Id: String?=null,
                 var name : String?= null,
                 var email: String?=null,
                 var password : String?=null,
                 var image : String?=null,
                 var userType : String?=null) : Parcelable
