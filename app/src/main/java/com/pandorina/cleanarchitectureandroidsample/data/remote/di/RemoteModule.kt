package com.pandorina.cleanarchitectureandroidsample.data.remote.di

import com.google.gson.GsonBuilder
import com.pandorina.cleanarchitectureandroidsample.data.remote.NotesService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RemoteModule {

    companion object {

        const val API_BASE_URL = "https://notes-api-cz2h.onrender.com/"
    }

    @Provides
    @Singleton
    fun provideNotesService(): NotesService {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotesService::class.java)
    }
}