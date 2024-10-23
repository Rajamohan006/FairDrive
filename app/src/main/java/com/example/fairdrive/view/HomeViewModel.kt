package com.example.fairdrive.view

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class HomeLoginScreen():ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var _loginStatus = MutableStateFlow<Status>(Status.Login)
    val loginStatus = _loginStatus.asStateFlow()

    private val _pickupLocation = MutableStateFlow("")
    val pickupLocation: StateFlow<String> = _pickupLocation.asStateFlow()

    private val _dropoffLocation = MutableStateFlow("")
    val dropoffLocation: StateFlow<String> = _dropoffLocation.asStateFlow()

    private val _distance = MutableStateFlow("")
    val distance: StateFlow<String> = _distance.asStateFlow()

    private val _estimatedTime = MutableStateFlow("")
    val estimatedTime: StateFlow<String> = _estimatedTime.asStateFlow()

    private val _rideHistory = MutableStateFlow<List<Ride>>(emptyList())
    val rideHistory: StateFlow<List<Ride>> = _rideHistory.asStateFlow()

    init {
        fetchRideHistory()
    }


     fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _loginStatus.value = Status.Error
            return
        }

        try {
            Log.d("AuthDebug", "Trying to log in with email: $email and password: $password")
            auth.signInWithEmailAndPassword(email, password)
            _loginStatus.value = Status.Success
        } catch (e: FirebaseAuthInvalidUserException) {
        } catch (e: Exception) {
            Log.e("LoginError", "Login failed: ${e.message}")
            _loginStatus.value = Status.Error
        }
    }
     fun createAccount(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _loginStatus.value = Status.Error
            return
        }
        try {
            auth.createUserWithEmailAndPassword(email, password)
            _loginStatus.value = Status.Success
        } catch (e: Exception) {
            _loginStatus.value = Status.Error
        }
    }
    fun saveRideDetails(
        pickup: String,
        drop: String,
        distance: String,
        estimatedTime: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val rideDetails = mapOf(
            "pickup" to pickup,
            "drop" to drop,
            "distance" to distance,
            "estimatedtime" to estimatedTime,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("rides").add(rideDetails)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError() }
    }
    fun setDistanceAndTime(calculatedDistance: String, calculatedTime: String) {
        _distance.value = calculatedDistance
        _estimatedTime.value = calculatedTime
    }


    private fun fetchRideHistory() {
        firestore.collection("rides").get()
            .addOnSuccessListener { snapshot ->
                val rides = snapshot.map { doc ->
                    Ride(
                        pickup = doc.getString("pickup") ?: "",
                        dropoff = doc.getString("drop") ?: "",
                        estimatedTime = doc.getString("estimatedtime") ?: ""
                    )
                }
                _rideHistory.value = rides
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error fetching rides: ${e.message}")
            }
    }



}
sealed class Status(){
    object Login : Status()
    object Success: Status()
    object Error: Status()
}
data class Ride(
    val pickup: String,
    val dropoff: String,
    val  estimatedTime: String
)