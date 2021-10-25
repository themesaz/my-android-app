package com.example.savingphotofile

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View

class StartupActivityViewModel {

    fun onSimpleFileSaveClickHandler(context: Context){
        val intent = Intent(context,SimpleSaveImageActivity::class.java)
        context.startActivity(intent)
    }
    fun onPhilipsFileSaveClickHandler(context: Context){
        val intent = Intent(context,MainActivity::class.java)
        context.startActivity(intent)
    }
}