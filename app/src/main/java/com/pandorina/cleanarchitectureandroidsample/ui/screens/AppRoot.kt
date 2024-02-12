package com.pandorina.cleanarchitectureandroidsample.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pandorina.cleanarchitectureandroidsample.ui.screens.notes.NotesNavigation
import com.pandorina.cleanarchitectureandroidsample.ui.screens.notes.NotesScreen

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    NavHost(
        navController,
        startDestination = NotesNavigation.destination
    ) {
        composable(NotesNavigation.destination) {
            NotesScreen(navController)
        }
    }
}