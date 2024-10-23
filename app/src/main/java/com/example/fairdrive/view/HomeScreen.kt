package com.example.fairdrive.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fairdrive.MY_API_KEY
import com.example.fairdrive.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.tasks.await
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var groundOverlay: GroundOverlay? by remember { mutableStateOf(null) }
    var searchQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(emptyList<AutocompletePrediction>()) }


    DisposableEffect(Unit) {
        val newMapView = MapView(context).apply {
            onCreate(Bundle())
            getMapAsync { map ->
                googleMap = map
                setupGoogleMap(map)
            }
            onResume()
        }
        mapView = newMapView

        onDispose {
            newMapView.onPause()
            newMapView.onDestroy()
        }
    }
    Scaffold (
        topBar = {
            TopAppBar(
                title ={ Text(text = "FairDrive",
                    fontSize = 24.sp,
                    color = Color.Green)
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screens.RideScreen.route)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Navigate to Ride Screen"
                )
            }
        }
    ){ paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            mapView?.let { view ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(8.dp)
                ) {
                    Column {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search locations...") },
                                    trailingIcon = {
                                IconButton(onClick = {

                                    if (searchQuery.isNotEmpty()) {
                                        val prediction = suggestions.find { it.getPrimaryText(null).toString().contains(searchQuery, true) }
                                        if (prediction != null) {
                                            navigateToLocation(prediction, googleMap, context)
                                            searchQuery = ""
                                            suggestions = emptyList()
                                        } else {
                                            Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            }
                        )



                        AndroidView(factory = { view })
                        googleMap?.let { map ->
                            val newLocation = LatLng(37.7749, -122.4194)
                            map.addMarker(
                                MarkerOptions().position(newLocation).title("New Marker")
                            )
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 12f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }) {
                Text(text = "Satellite Map")
            }

            Button(onClick = {
                googleMap?.uiSettings?.isCompassEnabled = true
            }) {
                Text(text = "Enable Compass")
            }

            Button(onClick = {
                googleMap?.uiSettings?.isRotateGesturesEnabled = true
            }) {
                Text(text = "Enable Rotation Gestures")
            }

            Button(onClick = {
                googleMap?.uiSettings?.isTiltGesturesEnabled = true
            }) {
                Text(text = "Enable Tilt Gestures")
            }
            Button(onClick = {
                val overlayOptions = GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher_foreground))
                    .position(LatLng(40.714086, -74.228697), 8600f, 6500f)

                groundOverlay = googleMap?.addGroundOverlay(overlayOptions)
            }) {
                Text(text = "Add Ground Overlay")
            }

            Button(onClick = {
                groundOverlay?.remove()
                groundOverlay = null
            }) {
                Text(text = "Remove Ground Overlay")
            }





        }
    }
}
fun calculateDistanceAndTime(context: Context, pickup: String, drop: String, onResult: (String, String) -> Unit) {
    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        val pickupAddresses = geocoder.getFromLocationName(pickup, 1)
        val dropAddresses = geocoder.getFromLocationName(drop, 1)

        if (pickupAddresses != null) {
            if (dropAddresses != null) {
                if (pickupAddresses.isNotEmpty() && dropAddresses.isNotEmpty()) {
                    val pickupLocation = pickupAddresses[0]
                    val dropLocation = dropAddresses?.get(0)

                    val startLocation = Location("start").apply {
                        latitude = pickupLocation.latitude
                        longitude = pickupLocation.longitude
                    }
                    val endLocation = Location("end").apply {
                        if (dropLocation != null) {
                            latitude = dropLocation.latitude
                        }
                        if (dropLocation != null) {
                            longitude = dropLocation.longitude
                        }
                    }

                    val distanceInMeters = startLocation.distanceTo(endLocation)
                    val estimatedTimeInMinutes = (distanceInMeters / 1000 * 1.5).toInt()


                    val formattedDistance = "${distanceInMeters / 1000} km"
                    val formattedTime = "${estimatedTimeInMinutes} min"

                    onResult(formattedDistance, formattedTime)
                } else {
                    Toast.makeText(context, "Unable to find one of the locations", Toast.LENGTH_SHORT).show()
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
private fun navigateToLocation(location: AutocompletePrediction, googleMap: GoogleMap?, context: Context) {
    val geocoder = Geocoder(context)
    try {
        val addresses = geocoder.getFromLocationName(location.getFullText(null).toString(), 1)
        if (addresses != null) {
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            } else {
                Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error occurred while searching for location", Toast.LENGTH_SHORT).show()
    }
}
@Composable
fun LocationSearchSuggestions(
    suggestions: List<AutocompletePrediction>,
    googleMap: GoogleMap?,
    navigateToLocation: (AutocompletePrediction) -> Unit
) {
    LazyColumn {
        items(suggestions) { prediction ->

            val primaryText = prediction.getPrimaryText(null).toString()

            Text(
                text = primaryText,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navigateToLocation(prediction)
                    }
                    .padding(8.dp)
            )
        }
    }
}
private fun setupGoogleMap(googleMap: GoogleMap) {
    googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
    googleMap.uiSettings.isCompassEnabled = true
    googleMap.uiSettings.isRotateGesturesEnabled = true
    googleMap.uiSettings.isTiltGesturesEnabled = true
    val initialLocation = LatLng(0.0, 0.0)
    googleMap.addMarker(
        MarkerOptions().position(initialLocation).title("Initial Marker")
    )
    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))
}
const val LOCATION_PERMISSION_REQUEST_CODE = 1

@SuppressLint("MissingPermission")
fun enableMyLocation(googleMap: GoogleMap, context: Context, onPermissionDenied: () -> Unit) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        googleMap.isMyLocationEnabled = true
    } else {
        ActivityCompat.requestPermissions(
            (context as androidx.activity.ComponentActivity),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
        onPermissionDenied()
    }
}