package com.example.fairdrive.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView

@Composable
fun RideHistory(navController: NavController,
                viewModel:  HomeLoginScreen = viewModel()
) {
    val rideHistory by viewModel.rideHistory.collectAsState()
    Scaffold (
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ){ paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {  }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(rideHistory) { ride ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "Pickup: ${ride.pickup}")
                    Text(text = "Dropoff: ${ride.dropoff}")
                    Text(text = "Estimated Time: ${ride.estimatedTime}")
                }
            }
        }
    }
}
@Composable
fun RideScreen(
    navController: NavController,
    viewModel: HomeLoginScreen = viewModel()
) {
    val context = LocalContext.current


    val distance by viewModel.distance.collectAsState()
    val estimatedTime by viewModel.estimatedTime.collectAsState()

    // MutableState variables for UI input
    var inputPickupLocation by remember { mutableStateOf("") }
    var inputDropLocation by remember { mutableStateOf("") }

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back Icon for Navigation
        Icon(
            Icons.Default.ArrowBack,
            contentDescription = null,
            modifier = Modifier.clickable {
                navController.popBackStack()
            }
        )

        // Pickup Location TextField
        OutlinedTextField(
            value = inputPickupLocation,
            onValueChange = { inputPickupLocation = it },
            leadingIcon = {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        googleMap?.let {
                            enableMyLocation(it, context) {
                                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                            }
                        } ?: run {
                            Toast.makeText(context, "Map is not ready", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Pickup Location") }
        )

        // Drop Location TextField
        OutlinedTextField(
            value = inputDropLocation,
            onValueChange = { inputDropLocation = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Drop Location") }
        )

        // Button to Calculate Distance and Time
        Button(
            onClick = {
                calculateDistanceAndTime(context, inputPickupLocation, inputDropLocation) { calculatedDistance, calculatedTime ->
                    viewModel.setDistanceAndTime(calculatedDistance, calculatedTime)
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = "Find Distance")
        }

        // Displaying Calculated Distance and Time
        Text(text = "Distance: $distance", modifier = Modifier.padding(top = 8.dp))
        Text(text = "Estimated Time: $estimatedTime", modifier = Modifier.padding(top = 8.dp))

        // Submit Button to Save Ride Details to Firestore
        Button(
            onClick = {
                viewModel.saveRideDetails(
                    pickup = inputPickupLocation,
                    drop = inputDropLocation,
                    distance = distance,
                    estimatedTime = estimatedTime,
                    onSuccess = {
                        Toast.makeText(context, "Ride details saved successfully", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(context, "Error saving ride details", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = "Submit")
        }
    }
}
