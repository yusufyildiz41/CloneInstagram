package com.yusufyildiz.kotlinfirebaseinsta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
{
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()  // Created object of FirebaseAuth

        val currentUser = auth.currentUser

        if (currentUser != null)
        {
            val intent = Intent(this,FeedActivity::class.java)
            startActivity(intent)
            finish() // It is Destroy the MainActivity
        }


    }
    fun signInClicked(view:View)
    {
        auth.signInWithEmailAndPassword(userEmailText.text.toString(),passwordText.text.toString()).addOnCompleteListener { task ->

            if(task.isSuccessful)
            {
                // Signed in
                Toast.makeText(this,"Welcome : ${auth.currentUser!!.email.toString()}",Toast.LENGTH_LONG).show()
                val intent = Intent(this,FeedActivity::class.java)
                startActivity(intent)
                finish() //Destroy Activity
            }

        }.addOnFailureListener { exception ->
            Toast.makeText(this,exception.localizedMessage.toString(),Toast.LENGTH_LONG).show()
        }

    }
    fun signUpClicked(view: View)
    {
        val email = userEmailText.text.toString()
        val password = passwordText.text.toString()

        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if(task.isSuccessful)
            {
                val intent = Intent(this,FeedActivity::class.java)
                startActivity(intent)
                finish() //Destroy activity
            }
        }.addOnFailureListener { exception ->
            if(exception!=null)
            {
                Toast.makeText(applicationContext,exception.localizedMessage.toString(),Toast.LENGTH_LONG).show()
            }
        }


    }
}