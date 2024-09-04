package com.example.instamate.utils

import android.net.Uri
import android.text.format.DateUtils
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.UUID
import javax.security.auth.callback.Callback


class Utils {


    companion object {

        private val auth = FirebaseAuth.getInstance()
        private var userid: String = ""


        const val USER_FOLDER = "Users"
        const val POSTS_FOLDER = "Posts"

        fun getUiLoggedIn(): String {
            if (auth.currentUser != null) {
                userid = auth.currentUser!!.uid
            }
            return userid
        }

        fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
            val now = Calendar.getInstance().timeInMillis
            val postTime = timestamp.toDate().time

            // Calculate the difference in milliseconds between the current time and the post's timestamp
            val diffMillis = now - postTime

            // Use DateUtils class to convert the time difference into a user-friendly format
            return when {
                DateUtils.isToday(postTime) -> {
                    DateUtils.getRelativeTimeSpanString(postTime, now, DateUtils.MINUTE_IN_MILLIS).toString()
                }
                diffMillis < DateUtils.WEEK_IN_MILLIS -> {
                    DateUtils.getRelativeTimeSpanString(postTime, now, DateUtils.DAY_IN_MILLIS).toString()
                }
                else -> {
                    DateUtils.getRelativeTimeSpanString(postTime, now, DateUtils.WEEK_IN_MILLIS).toString()
                }
            }
        }

    }
}
