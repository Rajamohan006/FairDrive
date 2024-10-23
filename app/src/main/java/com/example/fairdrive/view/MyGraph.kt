package com.example.fairdrive.view

import androidx.compose.material.icons.Icons
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fairdrive.R
import java.util.Locale

@Composable
fun NavGraph(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination =Screens.LoginScreen.route){
        composable(Screens.LoginScreen.route) {
            LoginScreen(navController)
        }
        composable(Screens.HomeScreen.route) {
            HomeScreen(navController)
        }
        composable(Screens.RideHistory.route) {
            RideHistory(navController)
        }
        composable(Screens.RideScreen.route) {
            RideScreen(navController)
        }
        composable(Screens.RegisterScreen.route){
            RegisterScreen(navController)
        }

    }
}
@Composable
fun BottomNavigationBar(navController:NavController) {
    val items = listOf(
        Screens.HomeScreen,
        Screens.RideHistory
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (screen == Screens.HomeScreen)
                                R.drawable.baseline_home_24
                            else
                                R.drawable.baseline_history_24
                        ),
                        contentDescription = screen.route,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = screen.route.replace("_", " ").capitalize(Locale.getDefault()),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}


sealed class Screens(val route: String){
    object LoginScreen : Screens("login_screen")
    object HomeScreen : Screens("home_screen")
    object RideHistory: Screens("ride_history")
    object RideScreen: Screens("ride")
    object RegisterScreen: Screens("register")
}