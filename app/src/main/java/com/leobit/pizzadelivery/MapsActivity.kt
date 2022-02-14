package com.leobit.pizzadelivery

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.location.LocationRequest.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.maps.android.clustering.ClusterManager
import com.leobit.pizzadelivery.databinding.ActivityMapsBinding
import com.leobit.pizzadelivery.services.ForegroungService

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    enum class Coordinats(val coordinats: LatLng) {
        PIZZA_POINT_1(LatLng(48.2057302385081, 22.647282499728526)),
        PIZZA_POINT_2(LatLng(48.20426796195503, 22.630714205342485)),
        PIZZA_POINT_3(LatLng(48.19367053096812, 22.657667919576543)),
        PIZZA_POINT_4(LatLng(48.19578336980909, 22.658833412017113)),
        PIZZA_POINT_5(LatLng(48.196652713848984, 22.64919468984506)),
        PIZZA_POINT_6(LatLng(48.198262942409706, 22.647364749535516)),
        PIZZA_POINT_7(LatLng(48.20661889136624, 22.634046550988796)),
        PIZZA_POINT_8(LatLng(48.206925281890356, 22.633451135212596)),
        PIZZA_POINT_9(LatLng(48.208096377052215, 22.635045772034122)),
        PIZZA_POINT_10(LatLng(48.208803090868805, 22.637775962537084)),
        PIZZA_POINT_11(LatLng(48.209594090371496, 22.636829919013042)),
        PIZZA_POINT_12(LatLng(48.21325483073108, 22.657876204495587)),
        PIZZA_POINT_13(LatLng(48.21598610262083, 22.658137401626405)),
        PIZZA_POINT_14(LatLng(48.1915271817124, 22.645923747801902)),
        PIZZA_POINT_15(LatLng(48.20614859852161, 22.645352950642017)),
        PIZZA_POINT_16(LatLng(48.210716173205846, 22.65049394940813)),
        PIZZA_POINT_17(LatLng(48.20535536834951, 22.642823547611126)),
        PIZZA_POINT_18(LatLng(48.20853172606778, 22.63892890196782)),
        PIZZA_POINT_19(LatLng(48.210983334859556, 22.65353495281016)),
        PIZZA_POINT_20(LatLng(48.20440362700222, 22.636515256833885)),


    }

    private var circle: Circle? = null
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mtask: Task<LocationSettingsResponse>
    private val locationRequest_H = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY)
    private lateinit var mLocationCallback: LocationCallback
    private var mRequestingLocationUpdates  = true
    private  var mMe: LatLng = LatLng(0.0,0.0)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations) {
                     mMe = LatLng(location.latitude,location.longitude)
                }
            }

        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                }
        } catch (e: SecurityException) {
            Log.e("SecurityException", "there is no permission for getting postion of User")
        }




        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setInfoWindowAdapter(MarkerInfoAdapter(this))
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        addClusteredMarkers(googleMap)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(Coordinats.PIZZA_POINT_20.coordinats))
    }

    private fun addClusteredMarkers(googleMap: GoogleMap) {
        // Create the ClusterManager class and set the custom renderer.
        val clusterManager = ClusterManager<Place>(this, googleMap)
        clusterManager.renderer =
            PlaceRender(
                this,
                googleMap,
                clusterManager
            )

        // Set custom info window adapter
        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoAdapter(this))

        // Add the places to the ClusterManager.
        var list = mutableListOf<Place>()
        Coordinats.values().forEach {
            list.add(Place(it.name, it.coordinats, "", 0.0f))
        }
        clusterManager.addItems(list)
        clusterManager.cluster()

        clusterManager.setOnClusterItemClickListener { item ->
            addCircle(googleMap, item)
            return@setOnClusterItemClickListener false
        }


        // Set ClusterManager as the OnCameraIdleListener so that it
        // can re-cluster when zooming in and out.
        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
        }

        val intent: Intent = Intent(this, ForegroungService::class.java)
        startForegroundService(intent)


    }

    private fun addCircle(googleMap: GoogleMap, item: Place) {
        circle?.remove()
        circle = googleMap.addCircle(
            CircleOptions()
                .center(item.position)
                .radius(1000.0)
                .fillColor(ContextCompat.getColor(this, R.color.purple_200))
                .strokeColor(ContextCompat.getColor(this, R.color.black))
        )

    }


    private val bicycleIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(this, R.color.black)
        BitmapHelper.vectorToBitMap(this, R.drawable.ic_pizza_slice_solid, color)
    }

    private fun setSetting() {

        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest_H)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        mtask = client.checkLocationSettings(builder.build())

    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("REQUESTING_LOCATIONS_UPDATES_KEY",mRequestingLocationUpdates)

        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        if(mRequestingLocationUpdates) startLocationUpdate()

    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate(){
        fusedLocationClient.requestLocationUpdates(
            locationRequest_H,
            mLocationCallback,
            Looper.getMainLooper()
        )

    }


    override fun onPause(){
        super.onPause()
        stopUpdatingLocation()
    }

    private fun stopUpdatingLocation(){
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
    }
    private fun updateValueFrom(savedInstanceState: Bundle?){
        savedInstanceState ?: return

        if(savedInstanceState.keySet().contains("REQUESTING_LOCATIONS_UPDATES_KEY")){
            mRequestingLocationUpdates = savedInstanceState.getBoolean("REQUESTING_LOCATIONS_UPDATES_KEY")

        }

    }

}
