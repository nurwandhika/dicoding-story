package com.example.projectbangkit1.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.example.projectbangkit1.R
import com.example.projectbangkit1.data.Result
import com.example.projectbangkit1.databinding.ActivityRegisterBinding
import com.example.projectbangkit1.ui.viewmodel.MainViewModel
import com.example.projectbangkit1.ui.viewmodel.ViewModelFactory

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRegisterBinding
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(this)
    }
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        playAnimation()

        binding.login.setOnClickListener{
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i, ActivityOptionsCompat.makeSceneTransitionAnimation(this@RegisterActivity).toBundle())
        }

        binding.apply{
            btnRegist.setOnClickListener{
                val name = textInput.text.toString()
                val email = emailInput.text.toString()
                val pass = passInput.text.toString()
                when {
                    name.isEmpty() -> {
                        textInput.error = resources.getString(R.string.emptyname)
                    }
                    email.isEmpty() -> {
                        emailInput.error = resources.getString(R.string.emptymail)
                    }
                    pass.isEmpty() -> {
                        passInput.error = resources.getString(R.string.emptypass)
                    }
                    else -> { register(name, email, pass) }
                }
            }
        }
    }

    private fun register(name: String, email: String, pass: String) {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.loading)
        dialog.setCancelable(false)
        if (dialog.window != null) {
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        }
        val i = Intent(this, LoginActivity::class.java)

        mainViewModel.register(name, email, pass).observe(this) { result ->
            if (result != null) {
                dialog.apply {
                    when (result) {
                        is Result.Loading -> {
                            show()
                        }
                        is Result.Success -> {
                            cancel()
                            if(result.data?.error == false ) {
                                if(binding.passInput.text?.length!! > 7) {
                                    Toast.makeText(this@RegisterActivity, resources.getString(R.string.regist), Toast.LENGTH_SHORT).show()
                                    startActivity(i)
                                } else {
                                    cancel()
                                    Toast.makeText(this@RegisterActivity, resources.getString(R.string.errorpass), Toast.LENGTH_LONG).show()
                                }
                            } else {
                                cancel()
                                Toast.makeText(this@RegisterActivity, result.data?.message.toString(), Toast.LENGTH_LONG).show()
                            }
                        }
                        is Result.Error -> {
                            cancel()
                            Toast.makeText(this@RegisterActivity, result.error, Toast.LENGTH_LONG).show()
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
            val name = ObjectAnimator.ofFloat(name, View.ALPHA, 1f).setDuration(DURATION)
            val pass = ObjectAnimator.ofFloat(password, View.ALPHA, 1f).setDuration(DURATION)
            val button = ObjectAnimator.ofFloat(btnRegist, View.ALPHA, 1f).setDuration(DURATION)
            val sub1 = ObjectAnimator.ofFloat(login2, View.ALPHA, 1f).setDuration(DURATION)
            val sub2 = ObjectAnimator.ofFloat(login, View.ALPHA, 1f).setDuration(DURATION)

            val together = AnimatorSet().apply {
                playTogether(sub1, sub2)
            }

            AnimatorSet().apply {
                playSequentially(title, image, subtitle, name, email, pass, button, together)
                start()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val DURATION: Long = 333
    }
}