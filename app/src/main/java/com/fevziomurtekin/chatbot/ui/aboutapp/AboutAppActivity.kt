package com.fevziomurtekin.chatbot.ui.aboutapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import com.fevziomurtekin.chatbot.R

class AboutAppActivity : AppCompatActivity() {
    private lateinit var actionBar : ActionBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)
        setTitle("About App")
        actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                this.finish()
                return true
            }
            else -> {
                return false
            }
        }
        return super.onOptionsItemSelected(item)
    }
}