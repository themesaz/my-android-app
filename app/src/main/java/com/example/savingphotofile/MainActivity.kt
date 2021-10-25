package com.example.savingphotofile

import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.savingphotofile.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var internalStoragePhotoAdapter: InternalStoragePhotoAdapter
    private lateinit var externalStoragePhotoAdapter: SharedPhotoAdapter

    private var readPermissionGranted = false
    private var writePermissionGranted = false

    private lateinit var permissionLauncher : ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        internalStoragePhotoAdapter = InternalStoragePhotoAdapter {
            val isDeletedSuccessfully = deletePhotoFromInternalStorage(it.name)
            if (isDeletedSuccessfully){
                loadPhotosFromInternalStorageToRecyclerView()
                Toast.makeText(this,"Photo deleted successfully",Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(this,"Failed to delete photo",Toast.LENGTH_SHORT).show()
            }
        }
        externalStoragePhotoAdapter = SharedPhotoAdapter {

        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permissions -> readPermissionGranted = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
            writePermissionGranted = permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted
        }
        updateOrRequestPermissions()

        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){
            val isPrivate = binding.switchPrivate.isChecked

            val isSavedSuccessfully = when{
                isPrivate -> savePhotoToInternalStorage(UUID.randomUUID().toString(),it)
                writePermissionGranted -> savePhotoToExternalStorage(UUID.randomUUID().toString(),it)
                else -> false
            }
            if (isSavedSuccessfully){
                loadPhotosFromInternalStorageToRecyclerView()
                Toast.makeText(this,"Photo saved successfully",Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(this,"Failed to save photo",Toast.LENGTH_SHORT).show()
            }

        }
        binding.btnTakePhoto.setOnClickListener{
            takePhoto.launch()
        }

        setupInternalStorageRecyclerView()
        loadPhotosFromInternalStorageToRecyclerView()
    }
    //some changes
    private fun updateOrRequestPermissions(){
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) ==  PackageManager.PERMISSION_GRANTED
        val minSdk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk

        Log.e("LOGS",hasWritePermission.toString())

        val permissionsToRequest = mutableListOf<String>()
        if (!readPermissionGranted)
            permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!writePermissionGranted)
            permissionsToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionsToRequest.isNotEmpty()){
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun setupInternalStorageRecyclerView() = binding.rvPrivatePhotos.apply {
        layoutManager = StaggeredGridLayoutManager(3,RecyclerView.VERTICAL)
        adapter = internalStoragePhotoAdapter
    }

    private fun loadPhotosFromInternalStorageToRecyclerView(){
        lifecycleScope.launch {
            val photos = loadPhotosFromInternalStorage()
            internalStoragePhotoAdapter.submitList(photos)
        }
    }

    private fun deletePhotoFromInternalStorage(fileName : String) : Boolean{
        return try{
            deleteFile(fileName)
        }catch (ex : Exception){
            ex.printStackTrace()
            false
        }
    }

    private suspend fun loadPhotosFromInternalStorage() : List<InternalStoragePhoto>{

        return withContext(Dispatchers.IO){

            val files = filesDir.listFiles()

            files?.filter { it.isFile && it.canRead() && it.name.endsWith(".jpg") }?.map {
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
                InternalStoragePhoto(it.name,bmp)
            } ?: listOf()
        }
    }

    private fun savePhotoToInternalStorage(name : String , bmp : Bitmap) : Boolean{

        return try{
            openFileOutput("$name.jpg", MODE_PRIVATE).use{ stream ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG,100,stream)){
                    throw IOException("Can not save bitmap.")
                }
                true
            }
        }catch (ex : IOException){
            ex.printStackTrace()
            Log.d("LOGS",ex.message!!)
            return false
        }
    }
    private fun savePhotoToExternalStorage(displayName : String , bmp : Bitmap) : Boolean{

        val imageCollection = sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,"$displayName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
            put(MediaStore.Images.Media.WIDTH,bmp.width)
            put(MediaStore.Images.Media.HEIGHT,bmp.height)
        }

        return try {
            contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                        throw IOException("Couldn't save bitmap")
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry")
            true
        }catch (ex : IOException){
            ex.printStackTrace()
            false
        }
    }
}