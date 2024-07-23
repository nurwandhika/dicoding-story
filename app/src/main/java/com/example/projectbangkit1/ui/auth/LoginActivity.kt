package com.example.projectbangkit1.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.projectbangkit1.BuildConfig
import com.example.projectbangkit1.R
import com.example.projectbangkit1.SettingPreferences
import com.example.projectbangkit1.databinding.ActivityLoginBinding
import com.example.projectbangkit1.ui.viewmodel.SettingFactory
import com.example.projectbangkit1.ui.viewmodel.MainViewModel
import com.example.projectbangkit1.ui.viewmodel.SettingViewModel
import com.example.projectbangkit1.data.Result
import com.example.projectbangkit1.ui.screen.MainActivity
import com.example.projectbangkit1.ui.viewmodel.ViewModelFactory
import kotlin.system.exitProcess

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(this)
    }
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        playAnimation()

        if(BuildConfig.DEBUG){
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        binding.signup.setOnClickListener{
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i, ActivityOptionsCompat.makeSceneTransitionAnimation(this@LoginActivity).toBundle())
        }
        binding.apply {
            btnLogin.setOnClickListener{
                val email = emailInput.text.toString()
                val pass = passInput.text.toString()
                when {
                    email.isEmpty() -> {
                        emailInput.error = resources.getString(R.string.emptymail)
                    }
                    pass.isEmpty() -> {
                        passInput.error = resources.getString(R.string.emptypass)
                    }
                    else -> { login(email, pass)}
                }
            }
        }
    }

    private fun login(email: String, pass: String) {
        val i = Intent(this, MainActivity::class.java)
        val pref = this.dataStore
        val data = SettingPreferences.getInstance(pref)
        val viewModel = ViewModelProvider(this, SettingFactory(data))[SettingViewModel::class.java]

        dialog = Dialog(this)
        dialog.setContentView(R.layout.loading)
        dialog.setCancelable(false)
        if (dialog.window != null) {
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        }
        mainViewModel.login(email, pass).observe(this) { result ->
            if (result != null) {
                dialog.apply {
                    when (result) {
                        is Result.Loading -> {
                            show()
                        }
                        is Result.Success -> {
                            cancel()
                            if(result.data?.error == false) {
                                if(binding.passInput.text?.length!! > 7) {
                                    viewModel.saveThemeSetting(result.data)
                                    Toast.makeText(this@LoginActivity, resources.getString(R.string.login), Toast.LENGTH_SHORT).show()
                                    startActivity(i)
                                } else {
                                    cancel()
                                    Toast.makeText(this@LoginActivity, resources.getString(R.string.errorpass), Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        is Result.Error -> {
                            cancel()
                            Toast.makeText(this@LoginActivity, result.error, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun playAnimation() {
        binding.apply {
            val title = ObjectAnimator.ofFloat(sign, View.ALPHA, 1f).setDuration(DURATION)
            val image = ObjectAnimator.ofFloat(imageView, View.ALPHA, 1f).setDuration(DURATION)
            val subtitle = ObjectAnimator.ofFloat(welcome, View.ALPHA, 1f).setDuration(DURATION)
            val email = ObjectAnimator.ofFloat(email, View.ALPHA, 1f).setDuration(DURATION)
            val pass = ObjectAnimator.ofFloat(password, View.ALPHA, 1f).setDuration(DURATION)
            val button = ObjectAnimator.ofFloat(btnLogin, View.ALPHA, 1f).setDuration(DURATION)
            val sub1 = ObjectAnimator.ofFloat(signup2, View.ALPHA, 1f).setDuration(DURATION)
            val sub2 = ObjectAnimator.ofFloat(signup, View.ALPHA, 1f).setDuration(DURATION)

            val together = AnimatorSet().apply {
                playTogether(sub1, sub2)
            }

            AnimatorSet().apply {
                playSequentially(title, image, subtitle, email, pass, button, together)
                start()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
        exitProcess(0)
    }

    companion object {
        const val DURATION: Long = 333
    }
}