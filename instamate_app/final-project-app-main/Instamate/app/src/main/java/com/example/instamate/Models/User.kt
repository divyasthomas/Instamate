package com.example.instamate.Models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.ArrayList


data class User(
    var userid: String = "",
    var name: String = "",
    var normalisedName: String = "",
    var profileImage: String? = null,
    var email: String="",
    var password: String="",
    var bio:String ?=null,
    var followers: Int = 0 ,// Default value for followers
    var following: Int = 0, // Default value for following
    val followingIds: ArrayList<String> = ArrayList(),//mutable list for user following,
    // Written on the server
    @ServerTimestamp val timeStamp: Timestamp? = null,

)
