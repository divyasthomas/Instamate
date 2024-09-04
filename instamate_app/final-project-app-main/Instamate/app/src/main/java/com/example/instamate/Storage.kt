package com.example.instamate

import android.net.Uri
import android.util.Log
import com.example.instamate.utils.PictureWrapper
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import java.io.File

// Store files in firebase storage

class Storage {

    // Create a storage reference from our app
    private val photoStorage: StorageReference =
        FirebaseStorage.getInstance().reference.child("Images")

    private fun deleteFile(localFile: File, uuid: String) {
        if(localFile.delete()) {
            Log.d("My-Tag Storage ", "Upload FAILED $uuid, file deleted")
        } else {
            Log.d("My-Tag Storage ", "Upload FAILED $uuid, file delete FAILED")
        }
    }

    fun uploadImage( cameraFlag:Boolean,uuid: String, uri: Uri, uploadSuccess:(String?) ->Unit) {
        //SSS
        val uuidRef = photoStorage.child(uuid)
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpg")
            .build()
        val uploadTask = uuidRef.putFile(uri, metadata)

        // Register observers to listen for when the upload is done or if it fails
        uploadTask
            .addOnFailureListener {exception ->
                // Handle unsuccessful uploads
                Log.d("My-Tag Storage uploadGalleryImage", "Failed to uploaded file: $uuid error message:${exception.message}")
                if (cameraFlag){ //only removes if picture taken using camera
                    val localFile = PictureWrapper.fileNameToFile(uuid)
                    deleteFile(localFile, uuid)
                }
                uploadSuccess(null)
            }
            .addOnSuccessListener { task ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                Log.d("My-Tag Storage uploadGalleryImage", "Success! uploaded file: $uuid ")
                // Image upload successful, get the download URL
                uuidRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    Log.d(
                        "My-Tag Storage uploadImage",
                        "Success! retrieved storage URL for image: $imageUrl"
                    )
                    // Pass the imageUrl to the callback function
                    uploadSuccess(imageUrl)
                    }
                    .addOnFailureListener { exception ->
                        // Handle any errors that occurred during fetching the download URL
                        Log.d(
                            "My-Tag Storage uploadGalleryImage",
                            "Failed to retrieve download URL: ${exception.message}"
                        )
                        uploadSuccess(null) // Pass null to indicate failure
                    }
            }
    }

    fun uploadGalleryImage(uri: Uri, uuid: String, uploadSuccess: (String?) -> Unit) {
        val uuidRef = photoStorage.child(uuid)
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpg")
            .build()
        val uploadTask = uuidRef.putFile(uri, metadata)

        // Register observers to listen for when the upload is done or if it fails
        uploadTask
            .addOnSuccessListener { task ->
                // Image upload successful, get the download URL
                uuidRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    Log.d("My-Tag Storage uploadGalleryImage", "Success! Uploaded and retrieved download URL: $imageUrl")

                    // Pass the imageUrl to the callback function
                    uploadSuccess(imageUrl)
                    }
                    .addOnFailureListener { exception ->
                        // Handle any errors that occurred during fetching the download URL
                        Log.d("My-Tag Storage uploadGalleryImage", "Uploaded Image successfully but Failed to retrieve download URL: ${exception.message}")
                        uploadSuccess(null) // Pass null to indicate failure
                    }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred during the upload process
                Log.d("My-Tag Storage uploadGalleryImage", "Upload failed: ${exception.message}")
                uploadSuccess(null) // Pass null to indicate failure
            }
    }

}