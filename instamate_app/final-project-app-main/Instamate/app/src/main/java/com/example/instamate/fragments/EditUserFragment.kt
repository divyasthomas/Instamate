package com.example.instamate.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.instamate.MainViewModel
import com.example.instamate.R
import com.example.instamate.databinding.FragmentEditUserBinding
import com.example.instamate.utils.Utils
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale


class EditUserFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentEditUserBinding
    val firestore = FirebaseFirestore.getInstance()
    val uid = Utils.getUiLoggedIn()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("My-Tag PostFragment ", "onCreate called")
        viewModel.pictureURL.value =""

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d("My-Tag EditUserFragment", "onCreateView called")
        binding = FragmentEditUserBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("my-Tag-EditUserFragment", "EditUserFragment onViewCreated is called. ")
        super.onViewCreated(view, savedInstanceState)

        loadExistingUserData()
        binding.plus.setOnClickListener {
            findNavController().navigate(
                R.id.action_editUserFragment_to_imageFragment,
                bundleOf("sourceFragment" to "editUserFragment")
            )
        }

        binding.updateBtn.setOnClickListener{
            updateUserProfile()
        }

        binding.cancelBtn.setOnClickListener{
            findNavController().popBackStack()
            viewModel.pictureURL.value =""
        }



    }

    private fun loadExistingUserData() {
        if (uid != null) {
            Log.d(
                "My-Tag EditUserFrag loadExistingUserData() ",
                "non null uid is $uid uid of viewModel user is ${viewModel.curUser.value!!.userid}"
            )
            binding.name.editText?.setText(viewModel.curUser.value!!.name)
            binding.bio.editText?.setText(viewModel.curUser.value!!.bio)

            //if picture is not empty then set it to profile pic
            if (!viewModel.curUser.value!!.profileImage.isNullOrBlank()) {
                Log.d("My-Tag loadExistingUserData- updateUserProfile"," user profile image is not empty: ${viewModel.curUser.value!!.profileImage}!!")

                //if image has not been updated
                if (viewModel.pictureURL.value.isNullOrBlank()) {
                    viewModel.pictureURL.value = viewModel.curUser.value!!.profileImage!!
                    Log.d("My-Tag loadExistingUserData- updateUserProfile"," set  viewModel.pictureURL value to ${viewModel.pictureURL.value} ")

                }
            }
            //if profile pic is empty
            else {
                Log.d("My-Tag loadExistingUserData- updateUserProfile"," user profile image is empty: ${viewModel.curUser.value!!.profileImage}!!")

            }

           Glide.with(requireContext()).load(viewModel.pictureURL.value)
               .placeholder(R.drawable.user_icon)
                    .into(binding.profileImage)
        }
    }

    private fun updateUserProfile() {
        val newName = binding.name.editText?.text.toString()
        val newBio = binding.bio.editText?.text.toString()
        val newProfileImage = viewModel.pictureURL.value
        val newnormalisedName = newName.lowercase(Locale.getDefault())


        if(newName.isNullOrBlank()){
            Toast.makeText(requireContext(),  "Sorry! Name cannot be blank!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        if ( uid != null) {
            val userRef = firestore.collection(Utils.USER_FOLDER).document(uid)

            var updatedUser = viewModel.curUser.value!!

            // Update user data
            updatedUser.name = newName
            updatedUser.normalisedName = newnormalisedName
            updatedUser.bio = newBio
            updatedUser.profileImage = newProfileImage

            userRef.set( updatedUser).addOnSuccessListener {

                //once user has updated their info new name and image, info must be updated in their posts
                val collectionref = firestore.collection("Posts")
                val query = collectionref.whereEqualTo("userid", Utils.getUiLoggedIn())

                //update
                query.get().addOnSuccessListener { documents ->
                    val batch = firestore.batch()
                    for (document in documents) {
                        batch.update(document.reference, "username", newName)
                        batch.update(document.reference, "profileImage", newProfileImage)
                    }
                    batch.commit().addOnSuccessListener {
                        Log.d("UpdateUserProfile", "Successfully updated posts with new user info")
                    }.addOnFailureListener { e ->
                        Log.e("UpdateUserProfile", "Failed to update posts: ${e.message}")
                    }
                }.addOnFailureListener { e ->
                    Log.e("UpdateUserProfile", "Failed to query posts: ${e.message}")
                }


                viewModel.pictureURL.value =""
                // Navigate to profile fragment
             findNavController().popBackStack()

            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "updateUserProfile Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("my-Tag-EditUserFragment", "Failed to update profile: ${e.message} ")
                viewModel.pictureURL.value =""

            }
        }
        else{
            Log.d("My-Tag EditUserFragment- updateUserProfile"," failed. uid empty $uid")
            viewModel.pictureURL.value =""

        }
    }
}
