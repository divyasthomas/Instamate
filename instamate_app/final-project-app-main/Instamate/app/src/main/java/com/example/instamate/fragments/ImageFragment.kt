package com.example.instamate.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.instamate.MainViewModel
import com.example.instamate.databinding.FragmentImageBinding
import com.example.instamate.utils.PictureWrapper


class ImageFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentImageBinding
    lateinit var  sourceFragment: String

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Log.d("my-Tag-ImageFragment", "cameraLauncher succeeded! going to set imageview to image! ")
            updateImageView()
            viewModel.cameraFlag = true
        }
        else{
            Log.d("my-Tag-ImageFragment", "cameraLauncher failed! ")
            Toast.makeText(requireContext(),"Error! Sorry! Try again",Toast.LENGTH_SHORT)
            viewModel.pictureFailure()
        }
    }

    private val galleryLauncher =registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            // Content retrieval failed or was canceled
            Log.d("Launcher", "Content retrieval failed or canceled")
            Toast.makeText(requireContext(),"Error! Sorry! Try again",Toast.LENGTH_SHORT)
            viewModel.pictureFailure()
        }
        else{
            viewModel.setUri(uri)
            updateImageView()
            viewModel.cameraFlag = true

        }
    }

    private fun updateImageView(){
        //decide depending on source fragment which imageView to make visible or invisible
        val photoFile = viewModel.getUri()
        if (sourceFragment == "editUserFragment") {
            binding.image.visibility = View.INVISIBLE
            //glide can handle invalid or empty string
            binding.profileImage.visibility = View.VISIBLE
            Glide.with(requireContext()).load(photoFile).into(binding.profileImage)
            Log.d("my-Tag-ImageFragment", "Profile Image loaded! ")
        }
        else if (sourceFragment == "postFragment"){
            //glide can handle invalid or empty string
            binding.profileImage.visibility = View.INVISIBLE
            Glide.with(requireContext()).load(photoFile).into(binding.image)
            Log.d("my-Tag-ImageFragment", "Post Image loaded! ")

        }
        else{
            Log.d("my-Tag-ImageFragment", "None of the fragments arguments matched! ")
        }

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.pictureURL.value =""
        // Inflate the layout for this fragment
        binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("my-Tag-ImageFragment", "ImageFragment onViewCreated is called. ")
        super.onViewCreated(view, savedInstanceState)

        //who called - to decide whether the circular imageview for profile  or square imageView for Post should display
        sourceFragment = arguments?.getString("sourceFragment") ?: ""


        //take picture using camera
        binding.cameraBtn.setOnClickListener{
            viewModel.pictureURL.value =""
            PictureWrapper.takePicture(requireContext(), viewModel, cameraLauncher)

        }

        //choose picture from gallery
        binding.galleryBtn.setOnClickListener{
            viewModel.pictureURL.value =""
            PictureWrapper.pickFromGallery( viewModel, galleryLauncher)
        }

        //upload picture to storage
        binding.uploadBtn.setOnClickListener{
            viewModel.pictureUpload (viewModel.cameraFlag){ imageUrl ->
                //successful upload
                if (imageUrl!=null){
                    viewModel.pictureURL.value = imageUrl
                    Log.d("my-Tag-ImageFragment", "uploadBtn- uploaded picture! Returning to previous fragment.. ${viewModel.pictureURL.value}")
                    findNavController().popBackStack()
                }

                else{
                    Log.d("my-Tag-ImageFragment", "uploadBtn- failed to upload or retrieve picture ")
                }
                viewModel.cameraFlag = false
            }
        }

        //cancel loading image
        binding.cancelBtn.setOnClickListener{
            findNavController().popBackStack()
        }



    }

}