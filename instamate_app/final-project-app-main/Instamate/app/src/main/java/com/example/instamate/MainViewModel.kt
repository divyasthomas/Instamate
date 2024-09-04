package com.example.instamate

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instamate.Models.Posts
import com.example.instamate.Models.User
import com.example.instamate.utils.Utils
import com.example.instamate.utils.Utils.Companion.USER_FOLDER
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel :ViewModel(){

    private val storage = Storage()
    private val firestore = FirebaseFirestore.getInstance()
    val curUser = MutableLiveData<User?>()
    val name = MutableLiveData<String>()
    val image = MutableLiveData<String?>()
    val followers = MutableLiveData<String>()
    val following = MutableLiveData<String>()
    // Remember the uuid, and hence file name of file camera will create
    private var pictureUUID = ""
    private var pictureUri: Uri? = null
    val pictureURL = MutableLiveData<String>()
    var cameraFlag = false

    init {
        Log.d("My-Tag MainViewModel-init","initialized")
        getCurrentUser()
    }

    fun getCurrentUser() = viewModelScope.launch(Dispatchers.IO) {
        firestore.collection("Users").document(Utils.getUiLoggedIn()).addSnapshotListener { value, error ->
            if (error!=null){
                return@addSnapshotListener
            }
            if ((value != null) && value.exists()){
                val users = value.toObject(User::class.java)
                name.value  = users!!.name
                image.value = users.profileImage
                followers.value = users.followers.toString()
                following.value = users.following.toString()
                curUser.value= users
                Log.d("My-Tag MainViewModel- getCurrentUser","cur user viewmodel$curUser")

            }
        }
    }

    // Only call this from PictureWrapper
    fun takePictureUUID(uuid: String) {
        pictureUUID = uuid
        Log.d("My-Tag MainViewModel- takePictureUUID"," pictureuuid assigned to photo $pictureUUID")
    }

    fun getPictureUUID():String{
        Log.d("My-Tag MainViewModel- getPictureUUID"," pictureuuid returned is $pictureUUID")
        return pictureUUID
    }

    fun setUri(uri: Uri){
        pictureUri =uri
        Log.d("My-Tag MainViewModel- setUri"," setUri for photo $uri")
    }

    fun getUri(): Uri? {
        Log.d("My-Tag MainViewModel- getpictureUri"," pictureUri returned is $pictureUri")
       return pictureUri
    }


    fun pictureUpload( cameraFlag:Boolean, finished:(String?) ->Unit){
        if (pictureUUID.isNullOrBlank() && pictureUri==null){
            Log.d("My-Tag MainViewModel- pictureUpload"," failed to call storageupload $pictureUri $pictureUUID")

        }
        else{
            pictureUri?.let {
                storage.uploadImage(cameraFlag, pictureUUID, it) {imageUrl->
                    finished(imageUrl)
                    pictureUUID = ""
                    pictureUri = null
                }
            }
        }
    }

    fun pictureFailure() {
        // Note, the camera intent will only create the file if the user hits accept
        // so I've never seen this called
        Log.d("My-Tag MainViewModel- pictureFailure"," called pictureFailure resetting pictureUUID and  pictureUri ")
        pictureUUID = ""
        pictureUri = null
    }

    fun getMyPosts(): LiveData<List<Posts>> {
        val posts = MutableLiveData<List<Posts>>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("Posts")
                    .whereEqualTo("userid", Utils.getUiLoggedIn())
                    .addSnapshotListener { snapshot, exception ->
                        if (exception != null) {
                            // Handle the exception here
                            return@addSnapshotListener
                        }
                        val postList = snapshot?.documents?.mapNotNull {
                            it.toObject(Posts::class.java)
                        }
                            ?.sortedByDescending { it.timeStamp
                            }
                        Log.d("My-Tag MainViewModel- getMyPosts","returning all my posts..${postList}")
                        posts.postValue(postList!!) // Switch back to the main thread
                    }
            } catch (e: Exception) {
                Log.d("My-Tag MainViewModel-getMyPosts","Error${e.message}")
            }
        }
        return posts
    }

    fun getAllUsers(): LiveData<List<User>> {
        val users = MutableLiveData<List<User>>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection(USER_FOLDER).addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        // Handle the exception here
                        return@addSnapshotListener
                    }
                    val usersList = mutableListOf<User>()
                    snapshot?.documents?.forEach { document ->
                        val user = document.toObject(User::class.java)
                        if (user != null && user.userid != Utils.getUiLoggedIn()) {
                            usersList.add(user)
                        }
                    }
                    Log.d("My-Tag MainViewModel-getAllUsers","returning all other users..${usersList}")

                    users.postValue(usersList) // Switch back to the main thread
                }
            } catch (e: Exception) {
                Log.d("My-Tag MainViewModel-getAllUsers","Error${e.message}")
            }
        }
        return users
    }


    fun loadMyFeed(): LiveData<List<Posts>> {
        val posts = MutableLiveData<List<Posts>>()
        val curUserId = Utils.getUiLoggedIn()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Get the current user's followingIds including their own ID
                val followingIds = getUserFollowingIds(curUserId)

                // Append the current user's ID to followingIds if it's not already present
                if (curUserId != null ) {
                    val updatedFollowingIds = followingIds.toMutableList().apply {
                        add(curUserId)
                    }

                    // Query posts from users in updatedFollowingIds
                    if (updatedFollowingIds.isNotEmpty()) {
                        firestore.collection("Posts")
                            .whereIn("userid", updatedFollowingIds)
                            .addSnapshotListener { value, error ->
                                if (error != null) {
                                    return@addSnapshotListener
                                }
                                val postList = mutableListOf<Posts>()
                                value?.documents?.forEach { documentSnapshot ->
                                    val post = documentSnapshot.toObject(Posts::class.java)
                                    post?.let { postList.add(it) }
                                }

                                val sortedFeed = postList.sortedByDescending { it.timeStamp }
                                posts.postValue(sortedFeed)
                                Log.d("My-Tag- loadMyFeed", "sorted feed is ${sortedFeed} ")

                            }
                    } else {
                        // If no users are being followed, return empty list of posts
                    Log.d("My-Tag- loadMyFeed", "no users followed ..returning empty list ")
                        posts.postValue(emptyList())
                    }
                }
            } catch (e: Exception) {
                posts.postValue(emptyList())
            }
        }

        return posts
    }

    private suspend fun getUserFollowingIds(userId: String): List<String> {
        return try {
            val userDoc = firestore.collection("Users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
            val followingIds = user?.followingIds ?: emptyList<String>()
            // Return the list of followingIds
            followingIds
        } catch (e: Exception) {
            emptyList()
        }
    }

}