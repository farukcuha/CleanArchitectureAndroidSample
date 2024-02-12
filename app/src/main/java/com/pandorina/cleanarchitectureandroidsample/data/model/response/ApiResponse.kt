package com.pandorina.cleanarchitectureandroidsample.data.model.response

interface ApiResponse<T> {
    fun toDomainModel(): T?
}