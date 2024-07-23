package com.example.projectbangkit1.ui.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.projectbangkit1.R
import com.example.projectbangkit1.SettingPreferences
import com.example.projectbangkit1.databinding.ActivitySplashBinding
import com.example.projectbangkit1.ui.auth.LoginActivity
import com.example.projectbangkit1.ui.auth.dataStore
import com.example.projectbangkit1.ui.viewmodel.SettingFactory
import com.example.projectbangkit1.ui.viewmodel.SettingViewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private var isLogin = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = SettingPreferences.getInstance(dataStore)
        val mainViewModel = ViewModelProvider(this, SettingFactory(pref))[SettingViewModel::class.java]
        mainViewModel.getThemeSettings().observe(this) {
            isLogin = it.token.isEmpty()
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Handler(Looper.getMainLooper()).postDelayed({
                if (!isLogin) {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            finish()
        }, DURATION)

        Glide.with(this)
            .asGif()
            .load("https://media.giphy.com/avatars/dicoding/GXrNtkjLX1sR.gif")
            .apply(RequestOptions.placeholderOf(R.drawable.logo_icon))
            .into(binding.logo)
    }

    companion object {
        const val DURATION: Long = 3500
    }
}