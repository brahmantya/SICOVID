package com.fevziomurtekin.chatbot

import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


object Util{
    fun hideKeyboard(context: AppCompatActivity, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun hideKeyboard(context: AppCompatActivity) {
        try {
            hideKeyboard(context, context.currentFocus!!)
        } catch (e: Exception) { }
    }
}