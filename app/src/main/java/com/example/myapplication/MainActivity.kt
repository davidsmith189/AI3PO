package com.example.myapplication


import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar= findViewById<androidx.appcompat.widget.Toolbar>(R.id.custom_toolbar)
        setSupportActionBar(toolbar)

        val username=intent.getStringExtra("USERNAME")?: "User Name"
        val descriptionText = findViewById<TextView>(R.id.descriptionText)
        descriptionText.text = username  // Set the retrieved username

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val saveIcon: ImageView = findViewById(R.id.save)
        saveIcon.setOnClickListener {
            Toast.makeText(this, "Chat Saved Successfully", Toast.LENGTH_SHORT).show()
        }


        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter



        supportActionBar?.setDisplayShowTitleEnabled(false)



        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Discussion"
                1 -> tab.text = "Doodle"
                2 -> tab.text = "Saved"
            }
        }.attach()
    }
}
