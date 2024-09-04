package com.example.instamate.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instamate.Models.User
import com.example.instamate.R
import com.example.instamate.utils.Utils
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class SearchAdapter(private val context: Context) : RecyclerView.Adapter<SearchAdapter.UserHolder>() {
    private var userList = mutableListOf<User>()
    private val firestore = FirebaseFirestore.getInstance()
    val curUserId = Utils.getUiLoggedIn()


    inner class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: CircleImageView = itemView.findViewById(R.id.profile_icon)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val followButton: Button = itemView.findViewById(R.id.followBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_rv_item, parent, false)
        return UserHolder(view)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val user = userList[position]
        holder.userName.text = user.name
        Glide.with(context)
            .load(user.profileImage)
            .placeholder(R.drawable.user_icon)
            .into(holder.userImage)


        updateFollowButton(holder.followButton, user)

        holder.followButton.setOnClickListener {
            toggleFollow(position,user)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun setUserList(users: List<User>) {
        userList.clear()
        userList.addAll( users)
        notifyDataSetChanged()
    }

    private fun toggleFollow(position: Int, user: User) {

        val userToFollowId = user.userid

        val userDocRef = firestore.collection("Users").document(curUserId)
        val userToFollowDocRef = firestore.collection("Users").document(userToFollowId)

        firestore.runTransaction { transaction ->
            val userSnapshot = transaction.get(userDocRef)
            val userToFollowSnapshot = transaction.get(userToFollowDocRef)

            val followingIds = userSnapshot.get("followingIds") as? MutableList<String>
            val followersCount = userToFollowSnapshot.getLong("followers") ?: 0
            val followingCount = userSnapshot.getLong("following") ?: 0

            if (followingIds != null) {
                if (followingIds.contains(userToFollowId)) {
                    // Already following, unfollow
                    followingIds.remove(userToFollowId)
                    transaction.update(userDocRef, "followingIds", followingIds)
                    transaction.update(userToFollowDocRef, "followers", followersCount - 1)
                    transaction.update(userDocRef, "following", followingCount - 1)
                } else {
                    // Not following, follow
                    followingIds.add(userToFollowId)
                    transaction.update(userDocRef, "followingIds", followingIds)
                    transaction.update(userToFollowDocRef, "followers", followersCount + 1)
                    transaction.update(userDocRef, "following", followingCount + 1)
                }
            }

        }.addOnSuccessListener {
            notifyDataSetChanged() // Refresh
//            notifyItemChanged(position)
        }.addOnFailureListener { exception ->
            Log.d(
                "My-Tag- SearchAdapter toggleFollow",
                "Failed to follow/unfollow! cur user: ${curUserId}  UserToFollow/unfollow: $userToFollowId  "
            )

        }
    }

    private fun updateFollowButton(button: Button, user: User) {
        val userToFollowId = user.userid

        firestore.collection("Users").document(curUserId).get()
            .addOnSuccessListener { document ->
                val followingIds = document.get("followingIds") as? List<String>

                if (followingIds != null && followingIds.contains(userToFollowId)) {
                    button.text = context.getString(R.string.unfollow)
                    button.setBackgroundColor(context.getColor(R.color.grey))
                } else {
                    button.text = context.getString(R.string.follow)
                    button.setBackgroundColor(context.getColor(R.color.register))
                }
            }
            .addOnFailureListener { exception ->
                Log.d(
                    "My-Tag- SearchAdapter updateFollowButton",
                    "Failed to update button cur user: ${curUserId}  UserToFollow/unfollow: $userToFollowId  "
                )
            }
    }
}
