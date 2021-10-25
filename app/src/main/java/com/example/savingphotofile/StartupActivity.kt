package com.example.savingphotofile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.savingphotofile.databinding.ActivityStartupBinding

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding : ActivityStartupBinding = DataBindingUtil.setContentView(this,R.layout.activity_startup)
        binding.viewModel = StartupActivityViewModel()
    }
}