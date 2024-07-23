package com.example.projectbangkit1.ui.screen


import android.Manifest
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.example.projectbangkit1.R
import com.example.projectbangkit1.SettingPreferences
import com.example.projectbangkit1.data.Result
import com.example.projectbangkit1.databinding.ActivityMapsBinding
import com.example.projectbangkit1.ui.auth.dataStore
import com.example.projectbangkit1.ui.viewmodel.MainViewModel
import com.example.projectbangkit1.ui.viewmodel.SettingFactory
import com.example.projectbangkit1.ui.viewmodel.SettingViewModel
import com.example.projectbangkit1.ui.viewmodel.ViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var viewModel: SettingViewModel
    private lateinit var dialog: Dialog
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        dialog = Dialog(this)
        dialog.setContentView(R.layout.loading)
        dialog.setCancelable(false)
        if (dialog.window != null) {
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        }

        val dicodingSpace = LatLng(LAT,LON)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dicodingSpace, 5f))

        val boundsBuilder = LatLngBounds.builder()
        val pref = SettingPreferences.getInstance(dataStore)
        viewModel = ViewModelProvider(this, SettingFactory(pref))[SettingViewModel::class.java]
        viewModel.getThemeSettings().observe(this) { user ->
            mainViewModel.getMapStory(user.token).observe(this) { result ->
                dialog.apply {
                    when (result) {
                        is Result.Loading-> {
                            show()
                        }
                        is Result.Success -> {
                            cancel()
                            result.data?.map {
                                if (it != null && it.lat < MAXLAT) {
                                    val latlng = LatLng(it.lat, it.lon)
                                    val address = getAddressName(it.lat, it.lon)
                                    mMap.addMarker(
                                        MarkerOptions()
                                            .position(latlng)
                                            .title(it.name)
                                            .icon(vectorToBitmap(R.drawable.ic_baseline_person_pin_circle_24))
                                            .snippet(address?.take(MAXSTRING))
                                    )
                                    boundsBuilder.include(latlng)
                                }
                            }
                        }
                        is Result.Error -> {
                            cancel()
                            Toast.makeText(this@MapsActivity, result.error, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
        mMap.setOnMarkerClickListener {
            val marker = it.position
            Toast.makeText(this@MapsActivity, resources.getString(R.string.map, marker), Toast.LENGTH_SHORT).show()
            false
        }
        setMapStyle()
        getMyLocation()
    }

    @Suppress("DEPRECATION")
    private fun getAddressName(lat: Double, lon: Double): String? {
        var addressName: String? = null
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val list = geocoder.getFromLocation(lat, lon, 1)
            if(list != null && list.size != 0) {
                addressName = list[0].getAddressLine(0)
                Log.d(TAG, "$addressName")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressName
    }

    private val requestLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_styles))
            if (!success) {
                Log.e(TAG, resources.getString(R.string.error))
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, exception.toString())
        }
    }

    private fun vectorToBitmap(@DrawableRes id: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
        if (vectorDrawable == null) {
            Log.e("BitmapHelper", resources.getString(R.string.error))
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    companion object {
        const val MAXLAT = 90.000000
        const val MAXSTRING = 50
        const val LAT = -6.8957643
        const val LON = 107.6338462
    }
}