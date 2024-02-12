package com.pandorina.cleanarchitectureandroidsample.domain.core

interface BaseUseCase<P, T> {

    suspend operator fun invoke(params: P): Result<T>
}