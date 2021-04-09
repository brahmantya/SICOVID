package com.fevziomurtekin.chatbot.ui.reference

import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fevziomurtekin.chatbot.R


class ReferenceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reference)
        setupHyperlink()
    }
    fun setupHyperlink(){
        val array = arrayOf(R.id.txt_reference1, R.id.txt_reference2, R.id.txt_reference3, R.id.txt_reference4, R.id.txt_reference5)
        for (i in array.indices){
            val linkTextView = findViewById<TextView>(array[i])
            linkTextView.movementMethod = LinkMovementMethod.getInstance()
            linkTextView.setLinkTextColor(Color.BLUE)
        }

    }
}