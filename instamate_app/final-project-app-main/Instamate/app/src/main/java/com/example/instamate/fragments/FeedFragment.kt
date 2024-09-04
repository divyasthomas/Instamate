package com.example.instamate.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instamate.MainViewModel
import com.example.instamate.adapters.FeedAdapter
import com.example.instamate.databinding.FragmentFeedBinding
import com.google.firebase.firestore.FirebaseFirestore


class FeedFragment : Fragment(){
        private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentFeedBinding
    private lateinit var adapter : FeedAdapter
    val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("my-Tag-FeedFragment", "FeedFragment onViewCreated is called. ")
        super.onViewCreated(view, savedInstanceState)

        // Inflate the layout for this fragment
        adapter = FeedAdapter(requireContext())
        binding.feedRv.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRv.adapter = adapter

        viewModel.loadMyFeed().observe(viewLifecycleOwner){newList ->
            adapter.setFeedList(newList)

        }
    }

}