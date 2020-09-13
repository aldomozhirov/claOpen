package com.mandarin.imageapp

interface PetEditContract {
    fun setAvatarPhoto(showEmptyIcon: Boolean)
    fun loadPhotoLinkToView()
    fun showGalleryPicker()
    fun showCameraPicker()
    fun randomEffects()
}