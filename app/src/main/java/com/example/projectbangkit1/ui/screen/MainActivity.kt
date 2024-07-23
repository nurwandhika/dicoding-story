package com.example.projectbangkit1.ui.screen

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projectbangkit1.R
import com.example.projectbangkit1.SettingPreferences
import com.example.projectbangkit1.databinding.ActivityMainBinding
import com.example.projectbangkit1.ui.adapter.ListItemAdapter
import com.example.projectbangkit1.ui.adapter.LoadingStateAdapter
import com.example.projectbangkit1.ui.auth.LoginActivity
import com.example.projectbangkit1.ui.auth.dataStore
import com.example.projectbangkit1.ui.viewmodel.MainViewModel
import com.example.projectbangkit1.ui.viewmodel.SettingFactory
import com.example.projectbangkit1.ui.viewmodel.SettingViewModel
import com.example.projectbangkit1.ui.viewmodel.ViewModelFactory
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SettingViewModel
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val pref = SettingPreferences.getInstance(dataStore)
        viewModel = ViewModelProvider(this, SettingFactory(pref))[SettingViewModel::class.java]
        viewModel.getThemeSettings().observe(this) { user ->
            story(user.token)
        }

        binding.fabAdd.setOnClickListener{
            val i = Intent(this@MainActivity, AddStoryActivity::class.java)
            startActivity(i, ActivityOptionsCompat.makeSceneTransitionAnimation(this@MainActivity).toBundle())
        }
    }

    private fun story(token: String) {
        val adapter = ListItemAdapter()
        mainViewModel.getStory(token).observe(this@MainActivity) { result ->
            adapter.submitData(lifecycle, result)
        }
        mainViewModel.rotate.observe(this@MainActivity) { result ->
            binding.apply {
                rvUser.adapter = adapter.withLoadStateFooter(
                    footer = LoadingStateAdapter {
                        adapter.retry()
                    }
                )
                if(result) {
                    rvUser.layoutManager = GridLayoutManager(this@MainActivity, 2)
                } else {
                    rvUser.layoutManager = GridLayoutManager(this@MainActivity, 4)
                }
                rvUser.setHasFixedSize(true)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mainViewModel.setRotate(false)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mainViewModel.setRotate(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                AlertDialog.Builder(this@MainActivity).apply {
                    setMessage(getString(R.string.logout))
                    setPositiveButton(getString(R.string.yes)) { _, _ ->
                        viewModel.deleteData()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    }
                    setNegativeButton(getString(R.string.no), null)
                }.show()
                true
            }
            R.id.action_setting -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            R.id.action_map -> {
                startActivity(Intent(this, MapsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setMessage(getString(R.string.exit))
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                finish()
                exitProcess(0)
            }
            setNegativeButton(getString(R.string.no), null)
        }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

}