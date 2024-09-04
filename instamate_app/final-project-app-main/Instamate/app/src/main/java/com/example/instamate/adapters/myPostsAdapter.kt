package com.example.instamate.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instamate.Models.Posts
import com.example.instamate.R

class myPostsAdapter: RecyclerView.Adapter<PostHolder>()  {
    var mypostlist = listOf<Posts>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.photo_list_row, parent, false)
        return PostHolder(view)
    }


    override fun getItemCount(): Int {
        return mypostlist.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        val post = mypostlist[position]
        Glide.with(holder.itemView.context).load(post.image)
            .placeholder(R.drawable.image)
            .into(holder.image)

    }

    fun setPostList(list: List<Posts>){
        val diffResult = DiffUtil.calculateDiff(MyDiffCallback(mypostlist, list))
        mypostlist = list
        diffResult.dispatchUpdatesTo(this)
    }


}

class PostHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val image: ImageView = itemView.findViewById(R.id.photoIB)

}

class MyDiffCallback(
    private val oldList: List<Posts>,
    private val newList: List<Posts>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }


}