package com.example.projectbangkit1.ui.screen

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.projectbangkit1.R
import com.example.projectbangkit1.SettingPreferences
import com.example.projectbangkit1.data.Result
import com.example.projectbangkit1.databinding.ActivityAddStoryBinding
import com.example.projectbangkit1.ui.auth.dataStore
import com.example.projectbangkit1.ui.createCustomTempFile
import com.example.projectbangkit1.ui.reduceFileImage
import com.example.projectbangkit1.ui.uriToFile
import com.example.projectbangkit1.ui.viewmodel.MainViewModel
import com.example.projectbangkit1.ui.viewmodel.SettingFactory
import com.example.projectbangkit1.ui.viewmodel.SettingViewModel
import com.example.projectbangkit1.ui.viewmodel.ViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var currentPhotoPath: String
    private lateinit var viewModel: SettingViewModel
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(this)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dialog: Dialog

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            val imageBitmap =  BitmapFactory.decodeFile(myFile.path)
            mainViewModel.setFile(myFile)
            binding.previewImageView.setImageBitmap(imageBitmap)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val selectedImage = it.data?.data as Uri
            selectedImage.let { result ->
                val myFile = uriToFile(result, this@AddStoryActivity)
                mainViewModel.setFile(myFile)
                binding.previewImageView.setImageURI(result)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f

        if(!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, REQUEST_CODE)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val pref = SettingPreferences.getInstance(dataStore)
        viewModel = ViewModelProvider(this, SettingFactory(pref))[SettingViewModel::class.java]
        binding.apply {
            progressBar.visibility = View.GONE
            camera.setOnClickListener { startTakePhoto() }
            gallery.setOnClickListener { startGallery() }
            mainViewModel.tempFile.observe(this@AddStoryActivity) { file ->
                val bitmap = BitmapFactory.decodeFile(file.path)
                previewImageView.setImageBitmap(bitmap)
            }

            var clicked = false
            btnLoc.setOnClickListener {
                getMyLastLocation()
                if(clicked) {
                    clicked = false
                    viewModel.setLatLng(null, null)
                } else {
                    clicked = true
                }
            }
            viewModel.latlng.observe(this@AddStoryActivity) { loc ->
                mainViewModel.tempFile.observe(this@AddStoryActivity) { file ->
                    btnUpload.setOnClickListener {
                        val story = textInput.text.toString()
                        when {
                            story.isEmpty() -> {
                                Toast.makeText(
                                    this@AddStoryActivity,
                                    resources.getString(R.string.emptydesc),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            file == null -> {
                                Toast.makeText(
                                    this@AddStoryActivity,
                                    resources.getString(R.string.emptyfoto),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            else -> {
                                val image = reduceFileImage(file)
                                addStory(story, image, loc[0], loc[1])
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addStory(story: String, image: File, lat: Float?, lon: Float?) {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.loading)
        dialog.setCancelable(false)
        if (dialog.window != null) {
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        }
        viewModel.getThemeSettings().observe(this@AddStoryActivity) { user ->
            mainViewModel.postStory(user.token, story, image, lat, lon).observe(this) { result ->
                dialog.apply {
                    if (result != null) {
                        when (result) {
                            is Result.Loading -> {
                                show()
                            }
                            is Result.Success -> {
                                cancel()
                                Toast.makeText(
                                    this@AddStoryActivity,
                                    resources.getString(R.string.upsuccess),
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                finish()
                            }
                            is Result.Error -> {
                                cancel()
                                Toast.makeText(
                                    this@AddStoryActivity,
                                    result.error,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private val requestLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLastLocation()
            }
        }

    private fun getMyLastLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.setLatLng(location.latitude.toFloat(), location.longitude.toFloat())
                }
            }
        } else {
            requestLauncher.launch(LOCATION_SERVICE)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, resources.getString(R.string.camera))
        launcherIntentGallery.launch(chooser)
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)
        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddStoryActivity,
                "submissionstory",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    resources.getString(R.string.permit),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSION.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        private val REQUIRED_PERMISSION = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE = 20
    }
}