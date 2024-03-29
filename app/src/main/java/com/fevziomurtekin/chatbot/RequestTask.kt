package com.fevziomurtekin.chatbot

import androidx.appcompat.app.AppCompatActivity
import android.os.AsyncTask
import com.google.cloud.dialogflow.v2.*

class RequestTask : AsyncTask<Void, Void, DetectIntentResponse> {

    var activity: AppCompatActivity? = null
    private var session: SessionName? = null
    private var sessionsClient: SessionsClient? = null
    private var queryInput: QueryInput? = null


    constructor(activity: AppCompatActivity, session:SessionName, sessionsClient: SessionsClient, queryInput: QueryInput){
        this.activity=activity
        this.session=session
        this.queryInput=queryInput
        this.sessionsClient=sessionsClient
    }

    override fun doInBackground(vararg params: Void?): DetectIntentResponse? {
        try {
            
            val detectIntentRequest = DetectIntentRequest.newBuilder()
                .setSession(session.toString())
                .setQueryInput(queryInput)
                .build()
           return sessionsClient?.detectIntent(detectIntentRequest)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: DetectIntentResponse?) {
        (activity as MainActivity).onResult(result)
    }

}