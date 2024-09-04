package com.example.instamate.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instamate.MainViewModel
import com.example.instamate.Models.User
import com.example.instamate.adapters.SearchAdapter
import com.example.instamate.databinding.FragmentSearchBinding
import com.example.instamate.utils.Utils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import java.util.Locale


class SearchFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter : SearchAdapter
    var searchList = listOf<User>()
    val firestore = FirebaseFirestore.getInstance()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("my-Tag-SearchFragment", "SearchFragment onViewCreated is called. ")
        super.onViewCreated(view, savedInstanceState)
        adapter = SearchAdapter(requireContext())
        binding.searchRv.layoutManager = LinearLayoutManager(requireContext())
        binding.searchRv.adapter = adapter
        //sending new list
        viewModel.getAllUsers().observe(viewLifecycleOwner){newUserList ->
            searchList = newUserList
            adapter.setUserList(newUserList)
        }
        //search functionality
        binding.searchBtn.setOnClickListener{
            var searchtext = binding.searchView.text.toString().trim()

            if (!searchtext.isNullOrBlank()) {
                //get list that match
                firestore.collection("Users").whereEqualTo("normalisedName",
                    searchtext.lowercase(Locale.ROOT)
                ).get()
                    .addOnSuccessListener {
                        var tempList = ArrayList<User>()
                        for (i in it.documents) {
                            if (i.id.toString().equals(Utils.getUiLoggedIn())) {
                            } else {
                                var user: User = i.toObject<User>()!!
                                tempList.add(user)
                            }
                        }
                        adapter.setUserList(tempList)
                        adapter.notifyDataSetChanged()
                    }
            }
            else{ //get entire list
                adapter.setUserList(searchList)
                adapter.notifyDataSetChanged()
            }
        }
    }

}
