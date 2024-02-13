package com.pandorina.cleanarchitectureandroidsample.extensions

import retrofit2.Response

suspend inline fun <reified K, T> performHttpRequest(
    crossinline process: suspend () -> Response<T>,
    transform: (T?) -> K
): Result<K> {
    return try {
        val response = process()
        when {
            response.isSuccessful -> {
                val body = response.body()
                val domainData = transform(body)
                Result.success(domainData)
            }
            else -> {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("${response.code()} - $errorBody"))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
