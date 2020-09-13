package com.mandarin.imageapp

import android.graphics.Bitmap
import android.util.Base64
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
        }
    }

    fun updatePetPhotoData(value: Bitmap?) {
//        pet.updatePhotoData(value) TODO: add when create model Pet
        fetchPetImageAndUpdateView()
    }

//    fun Pet.updatePhotoData(bitmap: Bitmap?) {
//        photoData = bitmap?.toBase64Jpeg() ?: ""
//    }
}

const val PREFIX_IMAGE_TYPE = "data:image/jpeg;base64,"


fun Bitmap?.toBase64Jpeg(): String {
    try {
        val stream = ByteArrayOutputStream()
        if (this == null || this.width == 0 || this.height == 0) return ""
        this.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        return PREFIX_IMAGE_TYPE + Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) { }
    return ""
}