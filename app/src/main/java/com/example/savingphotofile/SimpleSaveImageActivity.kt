package com.example.savingphotofile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception

class SimpleSaveImageActivity : AppCompatActivity() {

    private lateinit var imgPicture : ImageView
    private lateinit var btnAddImage : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_save_image)

        imgPicture = findViewById(R.id.imgPicture)
        btnAddImage = findViewById(R.id.btnAddImage)

        val bitmap = loadBitmap("pic.jpg")

        if (bitmap != null)
            imgPicture.setImageBitmap(bitmap)
        else
            Toast.makeText(this,"No image found",Toast.LENGTH_SHORT).show()

        val myFile = File(filesDir,"image.jpg")
        Log.e("LOGS",myFile.path)



        btnAddImage.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val intentChooser = Intent.createChooser(intent,"Pick an image")
            resultLauncher.launch(intentChooser)

        }
    }

    private fun loadBitmap(name : String) : Bitmap?{

        var bitmap : Bitmap? = null
        try {
            val fileInputStream : FileInputStream = openFileInput(name)
            bitmap = BitmapFactory.decodeStream(fileInputStream)
            fileInputStream.close()
        }
        catch (ex : Exception){
        }
        return bitmap
    }
    private fun saveBitmap(name : String,bitmap: Bitmap){

        try{
            val fileOutputStream : FileOutputStream = openFileOutput(name, MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream)
            fileOutputStream.close()
            Log.e("LOGS","File saved")
        }catch (ex : Exception){
            Log.e("LOGS","error is saving file ${ex.message}")

        }
    }
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == RESULT_OK) {

                val imageUri = result?.data?.data

                if (Build.VERSION.SDK_INT > 29) {
                    val source = ImageDecoder.createSource(this.contentResolver, imageUri!!)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    imgPicture.setImageBitmap(bitmap)
                    saveBitmap("pic.jpg",bitmap)
                } else {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageUri!!)
                    imgPicture.setImageBitmap(bitmap)
                    saveBitmap("pic.jpg",bitmap)
                }
            }
        }
    }
}