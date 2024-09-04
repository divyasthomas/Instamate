package com.example.instamate.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import com.example.instamate.MainViewModel
import java.io.File
import java.util.UUID

class PictureWrapper {

        companion object {

            private fun generateFileName(): String {
                Log.d("My-Tag PictureWrapper","generateFileName() called. Random uuid generated")
                return UUID.randomUUID().toString()
            }

            fun fileNameToFile(uuid: String): File {
                // Create the File where the photo should go
                val storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES)
                // Annoyingly, file name must end with .jpg extension
                val localPhotoFile = File(storageDir, "${uuid}.jpg")
                Log.d("My-Tag TakePictureWrapper", "fileNameToFile() - local photo file path ${localPhotoFile.absolutePath}")
                return localPhotoFile
            }

            fun takePicture(context: Context,
                            viewModel: MainViewModel,
                            takePictureLauncher : ActivityResultLauncher<Uri>
            ) {
                Log.d("My-Tag PictureWrapper","takePicture() called")
                val uuid = generateFileName()
                // We need to remember the picture's file name for the callback,
                // so put it in the view model
                viewModel.takePictureUUID(uuid)
                val localPhotoFile = fileNameToFile(uuid)
                val uri = FileProvider.getUriForFile(
                    context, context.applicationInfo.packageName, localPhotoFile)
                viewModel.setUri(uri)
                takePictureLauncher.launch(uri)
            }

            fun pickFromGallery( viewModel: MainViewModel,
                                pickGalleryLauncher: ActivityResultLauncher<String>) {
                val uuid = generateFileName()
                viewModel.takePictureUUID(uuid)
                // Launch gallery picker using ActivityResultLauncher for GetContent
                pickGalleryLauncher.launch("image/*")
            }


        }


}