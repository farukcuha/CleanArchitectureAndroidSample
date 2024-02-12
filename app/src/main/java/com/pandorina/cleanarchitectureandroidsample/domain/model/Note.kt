package com.pandorina.cleanarchitectureandroidsample.domain.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Note(
    val id: String?,
    val title: String?,
    val time: String?,
) {

    
}
