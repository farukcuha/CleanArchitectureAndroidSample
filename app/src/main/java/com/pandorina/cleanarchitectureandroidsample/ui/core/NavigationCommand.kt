package com.pandorina.cleanarchitectureandroidsample.ui.core

import androidx.navigation.NamedNavArgument

interface NavigationCommand {

    val arguments: List<NamedNavArgument>

    val destination: String
}