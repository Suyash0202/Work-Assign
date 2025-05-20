package com.example.workassign.Data

import com.google.firebase.database.PropertyName

data class Works(
    @get:PropertyName("expanded")
    @set:PropertyName("expanded")
    var expanded: Boolean = false,

    @get:PropertyName("firebaseKey")
    @set:PropertyName("firebaseKey")
    var firebaseKey: String = "",

    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String? = null,

    var bossId: String? = null,

    @get:PropertyName("workDescription")
    @set:PropertyName("workDescription")
    var workDescription: String? = null,

    @get:PropertyName("workLastDate")
    @set:PropertyName("workLastDate")
    var workLastDate: String? = null,

    @get:PropertyName("workPriority")
    @set:PropertyName("workPriority")
    var workPriority: String? = null,

    @get:PropertyName("workStatus")
    @set:PropertyName("workStatus")
    var workStatus: String? = null,

    @get:PropertyName("workTitle")
    @set:PropertyName("workTitle")
    var workTitle: String? = null
)