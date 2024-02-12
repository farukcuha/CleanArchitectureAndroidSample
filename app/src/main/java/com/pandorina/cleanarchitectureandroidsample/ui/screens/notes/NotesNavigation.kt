package com.pandorina.cleanarchitectureandroidsample.ui.screens.notes

import androidx.navigation.NamedNavArgument
import com.pandorina.cleanarchitectureandroidsample.ui.core.NavigationCommand

object NotesNavigation: NavigationCommand {
    override val arguments: List<NamedNavArgument>
        get() = emptyList()
    override val destination: String
        get() = "notes_screen"
}