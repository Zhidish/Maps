package com.leobit.pizzadelivery.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.maps.android.clustering.ClusterManager
import com.leobit.pizzadelivery.R
import com.leobit.pizzadelivery.databinding.ActivityMapsBinding
import com.leobit.pizzadelivery.db.Points
import com.leobit.pizzadelivery.db.PointsDao
import com.leobit.pizzadelivery.db.PointsDataBase
import com.leobit.pizzadelivery.services.ForegroungService
import com.leobit.pizzadelivery.services.ForegroungService.Companion.lastLiveLocation
import com.leobit.pizzadelivery.services.ForegroungService.Companion.isRunning
import com.leobit.pizzadelivery.pp.PermissionUtility
import com.leobit.pizzadelivery.ui.dialog.DeliveryDialong
import com.leobit.pizzadelivery.ui.viewmodel.PointsViewModel
import com.leobit.pizzadelivery.ui.viewmodel.PointsViewModelFactory
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions



const val REQUEST_CODE_LOCATION_PERMISSION = 0
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, DeliveryDialong.NoticeDialogListener,EasyPermissions.PermissionCallbacks {


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
    private var mMyPosition: Marker? = null
    private lateinit var mMap: GoogleMap
    private lateinit var mbinding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mtask: Task<LocationSettingsResponse>
    private val locationRequest_H = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY)
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mDataBaseInstance: PointsDao
    private var mRequestingLocationUpdates = true
    private lateinit var viewModel: PointsViewModel
    private lateinit var clusterManager: ClusterManager<Points>



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setInfoWindowAdapter(MarkerInfoAdapter(this))
        addClusteredMarkers(googleMap)

        lastLiveLocation.observe(this) {
            viewModel.checkAllPositions(it)
            addMarkerOfMyPosition(googleMap, it)
            if (viewModel.isPizzaPoint) {
                showDialog()
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Coordinats.PIZZA_POINT_20.coordinats))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mbinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mbinding.root)
        mDataBaseInstance = PointsDataBase.getInstance(this).pointsDao
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment


        mbinding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.run_app -> isRunning.value = true
                R.id.stop_run -> {
                    isRunning.value = false
                    mMyPosition?.remove()
                }
            }
            true
        }

        requestPermissions()

        mapFragment.getMapAsync(this)

    }



    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("REQUESTING_LOCATIONS_UPDATES_KEY", mRequestingLocationUpdates)

        super.onSaveInstanceState(outState)
    }

    override fun onDialogPositiveClick(dialong: DialogFragment) {
        clusterManager.removeItem(viewModel.point)
        clusterManager.cluster()
        viewModel.updatePoint()
        viewModel.isPizzaPoint= false
        viewModel.getPoints()

    }


    override fun onNegativeDialogListener(dialong: DialogFragment) {

    }


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        isRunning.value = true

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    private fun stopUpdatingLocation() {
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    private fun addClusteredMarkers(googleMap: GoogleMap) {
        clusterManager = ClusterManager<Points>(this, googleMap)
        val factory = PointsViewModelFactory(mDataBaseInstance, clusterManager)
        viewModel = ViewModelProvider(this, factory).get(PointsViewModel::class.java)
        clusterManager.renderer =
            PlaceRender(
                this,
                googleMap,
                clusterManager
            )
        viewModel.clearTable()
        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoAdapter(this))


        var listPoints = mutableListOf<Points>()
        listPoints.add(
            Points(
                latitude = 48.20964606107151,
                longitude = 22.649658610938268,
                pizza = "Гавайська",
                price = 300L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude = 48.2057302385081,
                longitude = 22.647282499728526,
                pizza = "Мексиканська",
                price = 300L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude = 48.20426796195503,
                longitude = 22.630714205342485,
                pizza = "Кальцоне",
                price = 300L,
                isDelivered = false,
            )
        )

        listPoints.add(
            Points(
                latitude = 48.19367053096812,
                longitude = 22.657667919576543,
                pizza = "Папая",
                price = 300L,
                isDelivered = false,
            )
        )


        listPoints.add(
            Points(
                latitude = 48.19578336980909,
                longitude = 22.658833412017113,
                pizza = "Прошуто",
                price = 400L,
                isDelivered = false,
            )
        )


        listPoints.add(
            Points(
                latitude = 48.196652713848984,
                longitude = 22.64919468984506,
                pizza = "Фонтана",
                price = 300L,
                isDelivered = false,
            )
        )


        listPoints.add(
            Points(
                latitude = 48.198262942409706,
                longitude = 22.647364749535516,
                pizza = "Падана",
                price = 500L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude =48.20661889136624,
                longitude =22.634046550988796,
                pizza = "Гори і Море",
                price = 500L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude =48.208096377052215,
                longitude =22.635045772034122,
                pizza = "Мимоза",
                price = 500L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude = 48.208803090868805,
                longitude = 22.637775962537084,
                pizza = "Мимоза",
                price = 500L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude = 48.209594090371496,
                longitude = 22.636829919013042,
                pizza = "Прошуто",
                price = 400L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude = 48.21325483073108,
                longitude = 22.657876204495587,
                pizza = "Американа",
                price = 600L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude = 48.21598610262083,
                longitude = 22.658137401626405,
                pizza = "Римська",
                price = 600L,
                isDelivered = false,
            )
        )

        listPoints.add(
            Points(
                latitude = 48.1915271817124,
                longitude = 22.645923747801902,
                pizza = "Крудо",
                price = 300L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude = 48.20614859852161,
                longitude =22.645352950642017,
                pizza = "Ортолана",
                price = 500L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude = 48.210716173205846,
                longitude =22.65049394940813,
                pizza = "Крудо",
                price = 500L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude = 48.20535536834951,
                longitude =22.642823547611126,
                pizza = "Мексиканська",
                price = 500L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude = 48.20535536834951,
                longitude =22.642823547611126,
                pizza = "З Песто",
                price = 400L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude = 48.20853172606778,
                longitude = 22.63892890196782,
                pizza = "Ортолана",
                price = 900L,
                isDelivered = false,
            )
        )
        listPoints.add(
            Points(
                latitude = 48.210983334859556,
                longitude = 22.65353495281016,
                pizza = "З грибами",
                price = 900L,
                isDelivered = false,
            )
        )

        listPoints.forEach {
              viewModel.addPoints(it)
          }


        viewModel.getPoints()
        viewModel.allPositions.observe(this, Observer {
            clusterManager.addItems(it)

        })


        clusterManager.cluster()

        clusterManager.setOnClusterItemClickListener { item ->
            addCircle(googleMap, item)
            return@setOnClusterItemClickListener false
        }

        googleMap.setOnMapClickListener {
            circle?.remove()
        }


        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
        }

        val intent: Intent = Intent(this, ForegroungService::class.java)
        startForegroundService(intent)

    }

    private fun addCircle(googleMap: GoogleMap, item: Points) {
        mMyPosition?.remove()
        mMyPosition = googleMap.addMarker(
            MarkerOptions()
                .position(item.position)
        )

        circle?.remove()
        circle = googleMap.addCircle(
            CircleOptions()
                .center(item.position)
                .radius(50.0)
                .fillColor(ContextCompat.getColor(this, R.color.purple_200))
                .strokeColor(ContextCompat.getColor(this, R.color.black))
        )

    }

    private fun addMarkerOfMyPosition(googleMap: GoogleMap, latLng: LatLng) {
        mMyPosition?.remove()
        mMyPosition = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
        )

    }

    private fun setSetting() {
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest_H)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        mtask = client.checkLocationSettings(builder.build())

    }

    private fun updateValueFrom(savedInstanceState: Bundle?) {
        savedInstanceState ?: return
        if (savedInstanceState.keySet().contains("REQUESTING_LOCATIONS_UPDATES_KEY")) {
            mRequestingLocationUpdates =
                savedInstanceState.getBoolean("REQUESTING_LOCATIONS_UPDATES_KEY")

        }
    }

    private fun showDialog() {
        val dialogFragment = DeliveryDialong()
        dialogFragment.show(supportFragmentManager, "Pizza")
        onPause()
    }

    private fun requestPermissions(){
        if(PermissionUtility.hasLocationPermission(this )){
            return
        }

        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permission to use this app",
                    REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )
        }else{
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        }

    }

}
