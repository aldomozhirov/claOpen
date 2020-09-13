package com.mandarin.imageapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Images
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_first.*
import java.io.ByteArrayOutputStream

const val REQUEST_GALLERY_PICK_IMAGE = 10105
const val REQUEST_PHOTO_TAKE_IMAGE = 10104
const val REQUEST_PERMISSIONS_CAMERA_GROUP = 10107
const val REQUEST_PERMISSIONS_STORAGE_GROUP = 10106

class PetEditFragment : Fragment(), View.OnClickListener, PetEditContract {

    private val presenter = PetEditPresenter(this)

    override fun onResume() {
        super.onResume()
        presenter.fetchPetImageAndUpdateView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView?.setOnClickListener(this)
        galleryView?.setOnClickListener(this)
        cameraView?.setOnClickListener(this)
        iconCamera?.setVisible(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == REQUEST_PHOTO_TAKE_IMAGE && resultCode == Activity.RESULT_OK -> {
                val photo: Bitmap = data?.extras?.get("data") as Bitmap
                val tempUri: Uri? = activity?.applicationContext?.let { getImageUri(it, photo) }
                val result = ImageProcessingService.process(getRealPathFromURI(tempUri)).get()
                Glide.with(this).load(result.get("imageUrl")).into(imageView)
                iconCamera?.setVisible(show = false)
                caption.text = result.get("caption")
            }
            requestCode == REQUEST_GALLERY_PICK_IMAGE && resultCode == Activity.RESULT_OK -> {
//                data?.data?.let {
//                    Log.d("some", "uri: ${it}")
//                    Log.d("some", "pat: ${getRealPathFromURI(it)}")
//                }

                data?.data?.let {
                    activity?.contentResolver?.openInputStream(it)
                    Log.d("some", "it: ${activity?.contentResolver?.openInputStream(it)}")
                }
//                    ?.let {
//                    presenter.updatePetPhotoData(BitmapFactory.decodeStream(it))
//                }
            }
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
        return Uri.parse(path)
    }

    fun getRealPathFromURI(uri: Uri?): String? {
        var path = ""
        if (context?.getContentResolver() != null) {
            val cursor: Cursor? =
                uri?.let { context?.contentResolver?.query(it, null, null, null, null) }
            if (cursor != null) {
                cursor.moveToFirst()
                val idx: Int = cursor.getColumnIndex(Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }

    override fun setAvatarPhoto(showEmptyIcon: Boolean) {
        setAvatarViewBitmap(bitmap = presenter.petPhotoBitmap)
    }

    override fun loadPhotoLinkToView() {
        //TODO: load pet photoLink data with Glide
        iconCamera?.setVisible(show = false)
    }

    private fun setAvatarViewBitmap(bitmap: Bitmap?) {
        imageView?.setImageBitmap(bitmap)
    }

    override fun onClick(view: View?) {
        presenter.onViewClick(view?.id ?: return)
    }

    override fun showGalleryPicker() {
        val isAtLeastMarshmallow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        if (isAtLeastMarshmallow
            && activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) } == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSIONS_STORAGE_GROUP)
        } else {
            launchImagePicker()
        }
    }

     override fun showCameraPicker() {
        val isAtLeastMarshmallow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        if (isAtLeastMarshmallow
            && activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSIONS_CAMERA_GROUP)
        } else {
            launchCameraApp()
        }
    }
}

fun Fragment.launchImagePicker() {
    startActivityForResult(
        Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" },
        REQUEST_GALLERY_PICK_IMAGE
    )
}

fun Fragment.launchCameraApp() {
    startActivityForResult(Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_PHOTO_TAKE_IMAGE)
}

fun View?.setVisible(show: Boolean = true) {
    this?.visibility = if (show) View.VISIBLE else View.GONE
}