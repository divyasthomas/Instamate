package com.example.instamate.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instamate.Models.Posts
import com.example.instamate.R
import com.example.instamate.databinding.FeedRvBinding
import com.example.instamate.utils.Utils.Companion.formatTimestamp
import com.example.instamate.utils.Utils.Companion.getUiLoggedIn
import com.google.firebase.firestore.FirebaseFirestore


class FeedAdapter(var context: Context): RecyclerView.Adapter<FeedAdapter.MyHolder>(){
    private var postList = mutableListOf<Posts>()
    val firestore = FirebaseFirestore.getInstance()
    val curUserId = getUiLoggedIn()

    inner class MyHolder(var binding:FeedRvBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        var binding = FeedRvBinding.inflate(LayoutInflater.from(context),parent,false)
        return MyHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

     fun setFeedList(list: List<Posts>){
         postList.clear()
         postList.addAll(list)
         notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val curPost = postList[position]
        //load data
        Glide.with(context).load(curPost.profileImage)
            .placeholder(R.drawable.user_icon)
            .into(holder.binding.feedProfileImage)
        holder.binding.feedUsername.text = curPost.username
        holder.binding.feedCaption.text = curPost.caption
        Glide.with(context).load(curPost.image)
            .placeholder(R.drawable.image)
            .into(holder.binding.feedImage)
        //make time in instagram format
        val timestamp = curPost.timeStamp
        val formattedTime = timestamp?.let { formatTimestamp(it) }
        holder.binding.feedTime.text = formattedTime

        //send functionality
        holder.binding.feedSend.setImageResource(R.drawable.send)
        holder.binding.feedSend.setOnClickListener {
            var send_intent = Intent(Intent.ACTION_SEND)
            send_intent.type = "text/plain"
            send_intent.putExtra(Intent.EXTRA_TEXT,curPost.image)
            context.startActivity(send_intent)
        }
        //like functionality
        holder.binding.feedLikeCount.text = curPost.likes.toString()
        // Determine which like icon to display based on whether the current user has liked the post
       if (curUserId != null && curPost.likers.contains(curUserId)) {
            holder.binding.feedLike.setImageResource(R.drawable.love)
        } else {
            holder.binding.feedLike.setImageResource(R.drawable.heart)
        }
        // toggle like when clicked
        holder.binding.feedLike.setOnClickListener {
            toggleLike(position, curPost, curUserId)
        }
    }

    private fun toggleLike(position: Int, post: Posts, userId: String?) {
        val postId = post.firestoreID
        if (userId != null) {
            if (post.likers.contains(userId)) {
                // User has already liked the post, so unlike it
                post.likers.remove(userId)
                post.likes =  post.likers.size
                Log.d("My-Tag- FeedAdapter ","Like removed ${post.likes} ${post.likers}")

            } else {
                // User hasn't liked the post, so like it
                post.likers.add(userId)
                post.likes = post.likers.size
                Log.d("My-Tag- FeedAdapter ","Like added ${post.likes}  ${post.likers}")

            }
            val updatedLikesCount = post.likers.size

            // Update the likers list and likes count in Firestore
            val updates = hashMapOf<String, Any>(
                "likers" to post.likers,
                "likes" to updatedLikesCount
            )
            firestore.collection("Posts").document(postId)
                .update(updates)
                .addOnSuccessListener {
                    // Update successful
                    notifyItemChanged(position) // Refresh RecyclerView after updating like status
                }
                .addOnFailureListener { e ->
                    // Handle error
                    Log.d(
                        "My-Tag- FeedAdapter Like",
                        "Failed to like post I am user: ${userId} post is ${post} $e "
                    )
                }
        }
    }

}
