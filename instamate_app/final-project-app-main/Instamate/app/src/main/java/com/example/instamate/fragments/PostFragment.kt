package com.example.instamate.fragments

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
import com.example.instamate.databinding.FragmentPostBinding
import com.example.instamate.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.instamate.Models.Posts


class PostFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentPostBinding
    val firestore = FirebaseFirestore.getInstance()
    var posterName:String = ""
    var posterImage:String = ""
    var posterId:String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("My-Tag PostFragment ", "onCreate called")
        viewModel.pictureURL.value ="" //reset in case of any residual link

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d("My-Tag PostFragment ", "onCreateView called")
        binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("my-Tag-PostFragment", "PostFragment onViewCreated is called. ")
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()


        Log.d("My-Tag PostFragment ", "current user is ${auth.currentUser}")
        viewModel.curUser.observe(viewLifecycleOwner){newData ->
            Log.d("myTag-PostFragment onViewCreated", "inside user observe -user data $newData")
            if (newData != null) {
                posterName = newData.name
                posterImage = newData.profileImage.toString()
                posterId = newData.userid
            }
            else{
                Log.d("myTag-ProfileFragment onViewCreated","Error! inside user observe -new user data is null! $newData ")
            }
        }

        binding.postImage.setOnClickListener {
            Log.d("myTag-ProfileFragment onViewCreated","Clicked image! going to image fragment.. ")
            findNavController().navigate(
                R.id.action_postFragment_to_imageFragment,
                bundleOf("sourceFragment" to "postFragment")
            )
        }

        binding.postBtn.setOnClickListener{
            createPost()
        }

        binding.cancelBtn.setOnClickListener{
            Log.d("myTag-ProfileFragment onViewCreated","Clicked cancel! resetting pictureURL ")
            viewModel.pictureURL.value =""
            findNavController().popBackStack()

        }

        Glide.with(requireContext()).load(viewModel.pictureURL.value)
            .placeholder(R.drawable.postpicture)
            .into(binding.postImage)

    }

    private  fun createPost(){
        var postCaption = binding.caption.editText?.text.toString()
        var postImage = viewModel.pictureURL.value

        if (postImage.isNullOrBlank()){
            Toast.makeText(requireContext(),  "Sorry! You need to have an image!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        val newPost = Posts(
            username = posterName,
            image = postImage,
            caption = postCaption,
            likes = 0,
            userid = posterId,
            profileImage =posterImage,
            likers = ArrayList()

        )

        firestore.collection(Utils.POSTS_FOLDER).add(newPost)
            .addOnSuccessListener {postId ->
                Toast.makeText(requireContext(),"Created new Post successfully",
                    Toast.LENGTH_SHORT).show()
                Log.d("My-Tag PostFragment", " Created new Post successfully ${auth.currentUser} postId is $postId ")
                viewModel.pictureURL.value =""
                // Navigate back
                findNavController().popBackStack()
            }
            .addOnFailureListener { e->
                Toast.makeText(
                    requireContext(),
                    "Failed to create new Post. Try Again! Error: ${e?.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("My-Tag PostFragment", " Failed to create new Post. current useris ${auth.currentUser} error is ${e?.message}")
                viewModel.pictureURL.value =""

            }


    }

}