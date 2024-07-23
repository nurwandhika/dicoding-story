package com.example.projectbangkit1.ui.screen

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.projectbangkit1.Helper.Companion.withDateFormat
import com.example.projectbangkit1.Helper.Companion.withTimeFormat
import com.example.projectbangkit1.SettingPreferences
import com.example.projectbangkit1.data.Result
import com.example.projectbangkit1.databinding.ActivityDetailBinding
import com.example.projectbangkit1.ui.auth.dataStore
import com.example.projectbangkit1.ui.viewmodel.SettingFactory
import com.example.projectbangkit1.ui.viewmodel.MainViewModel
import com.example.projectbangkit1.ui.viewmodel.SettingViewModel
import com.example.projectbangkit1.ui.viewmodel.ViewModelFactory


class DetailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetailBinding
    private lateinit var viewModel: SettingViewModel
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f

        val pref = SettingPreferences.getInstance(dataStore)
        viewModel = ViewModelProvider(this, SettingFactory(pref))[SettingViewModel::class.java]
        viewModel.getThemeSettings().observe(this) { user ->
            val dataMain = intent.getStringExtra(STORYKEY)
            if (dataMain != null) {
                detail(user.token, dataMain.toString())
            }
        }
    }

    private fun detail(token: String, id: String) {
        mainViewModel.getDetail(token, id).observe(this) { result ->
            binding.progressBar.apply {
                if (result != null) {
                    when (result) {
                        is Result.Loading-> {
                            visibility = View.VISIBLE
                        }
                        is Result.Success -> {
                            visibility = View.GONE
                            val separate = result.data.story?.createdAt?.split("T".toRegex())?.map { it.trim() }

                            binding.apply {
                                nama.text = result.data.story?.name
                                date.text = separate?.get(0)?.withDateFormat()
                                time.text = separate?.get(1)?.withTimeFormat()
                                desc.text = result.data.story?.description
                                val img = image
                                img.loadImage(result.data.story?.photoUrl)
                            }
                        }
                        is Result.Error -> {
                            visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun ImageView.loadImage(url: String?) {
        Glide.with(this.context)
            .load(url)
            .into(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val STORYKEY = "storyid"
    }
}