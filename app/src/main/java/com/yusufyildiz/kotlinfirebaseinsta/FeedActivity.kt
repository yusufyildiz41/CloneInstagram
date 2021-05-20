package com.yusufyildiz.kotlinfirebaseinsta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.type.Date
import kotlinx.android.synthetic.main.activity_feed.*
import java.sql.Timestamp

class FeedActivity : AppCompatActivity()
{
    private lateinit var auth : FirebaseAuth
    private lateinit var db:FirebaseFirestore
    var userEmailFromFB : ArrayList<String> = ArrayList()
    var userCommentFromFB : ArrayList<String> = ArrayList()
    var userImageFromFB: ArrayList<String> = ArrayList()

    var adapter : FeedRecyclerAdapter ?=null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        getDataFromFirestore()


        //Recyclerview

        var layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        adapter = FeedRecyclerAdapter(userEmailFromFB,userCommentFromFB,userImageFromFB)
        recyclerView.adapter = adapter

    }
    fun getDataFromFirestore()
    {
        //Order according to date , Direction.DESCENDING = desc like sql,  ASCENDING = asc like sql,
        db.collection("Posts").orderBy("date",Query.Direction.DESCENDING).addSnapshotListener { value, error ->

            if(error!=null)
            {
                Toast.makeText(this,error.localizedMessage.toString(),Toast.LENGTH_LONG).show()

            }
            else
            {
                if(value!=null)
                {
                    if(!value.isEmpty)
                    {
                        var documents = value.documents
                        userEmailFromFB.clear()
                        userCommentFromFB.clear()
                        userImageFromFB.clear()
                        for(document in documents)
                        {

                            val comment = document.get("comment") as String // Because we made 'any'
                            val userEmail = document.get("userEmail") as String
                            val downloadUrl = document.get("downloadUrl") as String
                            val timeStamp = document.get("date") as com.google.firebase.Timestamp
                            val date = timeStamp.toDate()
                            println(date)

                            println(userEmail)
                            println(comment)
                            userEmailFromFB.add(userEmail)
                            userCommentFromFB.add(comment)
                            userImageFromFB.add(downloadUrl)
                            adapter!!.notifyDataSetChanged()

                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        //Inflater
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.options_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        if(item.itemId==R.id.add_post)
        {
            //Upload Activity
            val intent = Intent(this,UploadActivity::class.java)
            startActivity(intent)
        }
        else if(item.itemId == R.id.logout)
        {
            //Logout
            auth.signOut()
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()

        }
        return super.onOptionsItemSelected(item)


    }
}