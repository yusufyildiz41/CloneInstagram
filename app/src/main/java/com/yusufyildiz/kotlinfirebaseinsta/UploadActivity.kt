package com.yusufyildiz.kotlinfirebaseinsta

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.os.IResultReceiver
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_upload.*
import java.net.URI
import java.util.*
import java.util.jar.Manifest
import kotlin.coroutines.Continuation

class UploadActivity : AppCompatActivity()
{
    var selectedPicture : Uri? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


    }
    fun imageViewClicked(view:View)
    {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)

        }
        else
        {
            val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI) // Go to gallery..
            startActivityForResult(intent,2)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(requestCode == 1)
        {
            if(grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode==2 && resultCode == RESULT_OK && data != null)
        {

            selectedPicture = data.data
            try
            {
                if(selectedPicture != null)
                {
                    if(Build.VERSION.SDK_INT >= 20)
                    {
                        val source = ImageDecoder.createSource(contentResolver,selectedPicture!!) // First, we must create the source
                        val bitmap = ImageDecoder.decodeBitmap(source) // Second, we must decode the source to the bitmap
                        uploadImageView.setImageBitmap(bitmap)
                    }
                    else
                    {
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,selectedPicture) //This code makes convert to a bitmap of the selected image
                        uploadImageView.setImageBitmap(bitmap)
                    }

                }
            }catch (e:Exception)
            {
                e.printStackTrace()
            }


        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    fun uploadClicked(view: View)
    {
        //UUID (Universal Unique Id) -> image name

        val uuid = UUID.randomUUID()  // This code generate the Random name
        val imageName = "$uuid.jpg"


        val storage = FirebaseStorage.getInstance()
        val reference = storage.reference //(Create generally references(all of them(docs,folders,images,...)))
        val imagesReferences = storage.reference.child("images").child(imageName)
        if(selectedPicture!=null)
        {
            imagesReferences.putFile(selectedPicture!!).addOnSuccessListener { taskSnapshot ->

                val uploadPictureReference = FirebaseStorage.getInstance().reference.child("images").child(imageName)
                uploadPictureReference.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    println(downloadUrl)
                    //Database and Hashmap

                    val postMap = hashMapOf<String,Any>() //Key and Value
                    postMap.put("downloadUrl",downloadUrl)
                    postMap.put("userEmail",auth.currentUser!!.email.toString())
                    postMap.put("comment",uploadCommentText.text.toString())
                    postMap.put("date",Timestamp.now())


                    db.collection("Posts").add(postMap).addOnCompleteListener { task ->
                        if(task.isComplete && task.isSuccessful)
                        {
                            //back
                            finish()
                        }

                    }.addOnFailureListener {exception ->
                        Toast.makeText(this,exception.localizedMessage.toString(),Toast.LENGTH_LONG).show()
                    }

                }


            }
        }

    }
}