package com.example.login.home.home

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.login.R
import com.example.login.signIn.SignInSignUpUtils
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale


class OthersProfile : AppCompatActivity() {

    private lateinit var userImage : CircleImageView
    private lateinit var userName : TextView
    private lateinit var aboutUser : TextView
    private lateinit var joinedOn : TextView
    private lateinit var detailsLinearLayout: LinearLayout
    private lateinit var articlesLinearLayout: LinearLayout

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    private lateinit var authorId : String
    private lateinit var db : FirebaseFirestore


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_others_profile)

        if(!SignInSignUpUtils.isInternetAvailable(this))
            SignInSignUpUtils.noInternetToast(this)

        userImage = findViewById(R.id.userImage)
        userName = findViewById(R.id.userName)
        aboutUser = findViewById(R.id.aboutUser)
        joinedOn = findViewById(R.id.joinedOn)
        detailsLinearLayout = findViewById(R.id.detailsLinearLayout)
        articlesLinearLayout = findViewById(R.id.articlesLinearLayout)


        authorId = intent.extras!!.getString("authorId").toString()
        val recentArticlesFragment = RecentArticlesFragment()
        recentArticlesFragment.arguments = Bundle().apply {
            putString("authorId", authorId)
        }
        val topArticlesFragment = TopArticlesFragment()
        topArticlesFragment.arguments = Bundle().apply {
            putString("authorId", authorId)
        }
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(recentArticlesFragment, "Recent")
        viewPagerAdapter.addFragment(topArticlesFragment, "Top")
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)

        detailsLinearLayout.visibility = View.GONE
        articlesLinearLayout.visibility = View.GONE

        db = FirebaseFirestore.getInstance()
        db.collection("users").document(authorId).get()
            .addOnSuccessListener {documentSnapShot->
                if(documentSnapShot.exists()){
                    val name = documentSnapShot.get("name").toString()
                    val about = documentSnapShot.get("about").toString()
                    val imageUrl = documentSnapShot.get("imageUrl").toString()
                    val accountCreatedOn = documentSnapShot.get("accountCreatedOn") as Timestamp
                    val dateFormat = SimpleDateFormat("d MMM, yyyy", Locale.getDefault()).format(accountCreatedOn.toDate())
                    userName.text = name
                    aboutUser.text = about
                    Glide.with(this).load(imageUrl).into(userImage)
                    joinedOn.text = "Joined on $dateFormat"
                    detailsLinearLayout.visibility = View.VISIBLE
                    articlesLinearLayout.visibility = View.VISIBLE
                }
            }
    }

}