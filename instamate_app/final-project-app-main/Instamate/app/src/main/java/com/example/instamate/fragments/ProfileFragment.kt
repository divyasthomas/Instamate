package com.example.instamate.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.instamate.MainViewModel
import com.example.instamate.R
import com.example.instamate.SignUpActivity
import com.example.instamate.adapters.myPostsAdapter
import com.example.instamate.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth


class ProfileFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentProfileBinding
    lateinit var auth : FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("my-Tag-ProfileFragment", "ProfileFragment onViewCreated is called. ")
        super.onViewCreated(view, savedInstanceState)

        // Ensure ProfileFragment is topmost in the back stack
        findNavController().popBackStack(R.id.profileFragment, false)
        auth = FirebaseAuth.getInstance()

        //logout user
        binding.logoutBut.setOnClickListener{
            Log.d("My-Tag ProfileFragment ", "Logout button clicked. user is ${auth.currentUser}")
            auth.signOut()
            Log.d("My-Tag ProfileFragment ", "Logged out. Now user is ${auth.currentUser}")
            // Start the login or sign-in activity
            val intent = Intent(requireContext(), SignUpActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()

        }

        viewModel.name.observe(viewLifecycleOwner){newName->
            Log.d("myTag-ProfileFragment onViewCreated", "inside name observe -new name to set $newName")
            binding.name.text =newName
        }

        viewModel.image.observe(viewLifecycleOwner){newImage ->
            Log.d("myTag-ProfileFragment onViewCreated", "inside image observe -new name $newImage")
            Glide.with(requireContext()).load(newImage)
                .placeholder(R.drawable.user_icon)
                .into(binding.profileImage)
        }

        viewModel.followers.observe(viewLifecycleOwner){newFollowCount->
            Log.d("myTag-ProfileFragment onViewCreated", "inside followers observe -new name $newFollowCount")
            binding.followersCount.text = newFollowCount
        }

        viewModel.following.observe(viewLifecycleOwner){newFollowingCount ->
            Log.d("myTag-ProfileFragment onViewCreated", "inside following observe -new name $newFollowingCount")
            binding.followingCount.text = newFollowingCount
        }


        viewModel.curUser.observe(viewLifecycleOwner){newData ->
            Log.d("myTag-ProfileFragment onViewCreated", "inside user observe -user data $newData")
            if (newData != null) {
                binding.email.text = newData.email
                binding.bio.text = newData.bio
            }
            else{
                Log.d("myTag-ProfileFragment onViewCreated","Error! inside user observe -new user data is null! $newData ")
            }
        }

        binding.editBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editUserFragment)
        }

        val adapter = myPostsAdapter()
        binding.imagesRv.adapter = adapter
        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        binding.imagesRv.layoutManager = layoutManager

        viewModel.getMyPosts().observe(viewLifecycleOwner) { myPosts ->
            binding.postsCount.text = myPosts.size.toString()
            adapter.setPostList(myPosts)
        }
    }
}
