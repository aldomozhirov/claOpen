package com.mandarin.imageapp

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

class PetEditPresenter(val contract: PetEditContract) {

    val petPhotoBitmap: Bitmap? = null
//        get() = pet.photoData //TODO: get photo data from model

    val petPhotoLink: String = ""
//        get() = pet.photoLink //TODO: get photo link from model

    fun fetchPetImageAndUpdateView() {
        try {
            when {
                petPhotoBitmap != null -> contract.setAvatarPhoto(showEmptyIcon = false)
                petPhotoLink.isNotEmpty() -> contract.loadPhotoLinkToView()
                else -> contract.setAvatarPhoto(showEmptyIcon = true)
            }
        } catch (e: Exception) {
        }
    }

    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.galleryView -> contract.showGalleryPicker()
            R.id.cameraView -> contract.showCameraPicker()
            R.id.randomView -> contract.randomEffects()
        }
    }

    fun updatePetPhotoData(value: Bitmap?) {
       //pet.updatePhotoData(value)
        fetchPetImageAndUpdateView()
    }
}