package com.pandorina.cleanarchitectureandroidsample.data.repository.di

import com.pandorina.cleanarchitectureandroidsample.data.remote.NotesService
import com.pandorina.cleanarchitectureandroidsample.data.repository.NotesRepositoryImpl
import com.pandorina.cleanarchitectureandroidsample.domain.repository.NotesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideNotesRepository(
        notesService: NotesService
    ): NotesRepository {
        return NotesRepositoryImpl(notesService)
    }
}